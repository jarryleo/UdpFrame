package cn.leo.udp.net

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by Leo on 2018/2/26.
 * 安卓UDP底层传输模块，拆分数据包传输
 * 无丢包处理。
 */

internal class UdpListenCore : Thread {
    private var mPort = Config.DEF_PORT
    private lateinit var mReceiveSocket: DatagramSocket
    private val mMainThreadHandler = Handler(Looper.getMainLooper())
    private val mDataArrivedObservers = HashMap<OnDataArrivedListener, Boolean>()
    //缓存(host,data)
    private val mCaches = ConcurrentHashMap<String, ArrayList<ByteArray>>()

    constructor(port: Int) {
        mPort = port
        initSocket()
    }

    constructor(onDataArrivedListener: OnDataArrivedListener) {
        mDataArrivedObservers[onDataArrivedListener] = checkThread(onDataArrivedListener)
        initSocket()
    }

    constructor(onDataArrivedListener: OnDataArrivedListener, port: Int = Config.DEF_PORT) {
        mDataArrivedObservers[onDataArrivedListener] = checkThread(onDataArrivedListener)
        mPort = port
        initSocket()
    }


    private fun initSocket() {
        mReceiveSocket = DatagramSocket(mPort)
        start() //启动监听
    }

    override fun run() {
        listen()
    }

    /**
     *  订阅数据回调
     */
    internal fun subscribe(onDataArrivedListener: OnDataArrivedListener) {
        mDataArrivedObservers[onDataArrivedListener] = checkThread(onDataArrivedListener)
    }

    /**
     * 取消数据回调订阅
     */
    internal fun unSubscribe(onDataArrivedListener: OnDataArrivedListener) {
        mDataArrivedObservers.remove(onDataArrivedListener)
    }

    /**
     * 检查注解
     */
    private fun checkThread(onDataArrivedListener: OnDataArrivedListener): Boolean {
        val kClass = onDataArrivedListener::class.java
        val declaredMethod = kClass.getDeclaredMethod("onDataArrived",
                ByteArray::class.java, String::class.java)
        return declaredMethod.isAnnotationPresent(UdpDataArrivedOnMainThread::class.java)
    }

    /**
     * 监听UDP信息,接受数据
     */
    private fun listen() {
        val data = ByteArray(Config.PACK_SIZE)
        val dp = DatagramPacket(data, data.size)
        //缓存数据
        while (true) {
            try {
                mReceiveSocket.receive(dp)
                //发送发地址
                val remoteAddress = dp.address.hostAddress
                //检查数据包头部
                val head = ByteArray(2)
                val body = ByteArray(dp.length - 2)
                //取出头部
                System.arraycopy(data, 0, head, 0, head.size)
                //取出数据体
                System.arraycopy(data, 2, body, 0, body.size)
                //安全退出，不再监听
                if (head[0] == (-0xEE).toByte() && head[1] == (-0xDD).toByte()) {
                    if ("127.0.0.1" == remoteAddress) {
                        break
                    }
                }
                //不符合规范的数据包直接抛弃
                if (head[0] < head[1]) {
                    continue
                }
                //数据只有1个包
                if (head[0] == 1.toByte()) {
                    //数据回调给上层协议层
                    onReceiveData(body, remoteAddress)
                } else {
                    //分包接收处理
                    var cache = mCaches[remoteAddress]

                    //新的数据包组到来
                    if (head[1] == 1.toByte()) {
                        //没有缓存创建新缓存
                        if (cache == null) {
                            cache = ArrayList()
                            mCaches[remoteAddress] = cache
                        } else {
                            //有的话清空这个地址的缓存
                            cache.clear()
                        }
                    } else {
                        //不是新的数据包，但是没有缓存。则抛弃这个包
                        if (cache == null) {
                            continue
                        }
                    }
                    //缓存数据包(漏数据包则不缓存)
                    if (cache.size + 1 == head[1].toInt()) {
                        cache.add(body)
                    }
                    //所有数据包都抵达完成则拼接
                    if (head[0] == head[1]) {
                        //数据包完整的话
                        if (cache.size == head[0].toInt()) {
                            //开始组装数据
                            //获取数据总长度
                            val dataLength = cache.sumBy { it.size }
                            val sumData = ByteArray(dataLength)
                            //已经拼接长度
                            var length = 0
                            for (bytes in cache) {
                                System.arraycopy(bytes, 0, sumData, length, bytes.size)
                                length += bytes.size
                            }
                            //数据回调给上层协议层
                            onReceiveData(sumData, remoteAddress)
                            //清空缓存
                            cache.clear()
                        } else {
                            //数据包不完整
                            Log.e("udp", " -- data is incomplete")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        //关闭端口
        mReceiveSocket.disconnect()
        mReceiveSocket.close()
        //清空缓存
        mCaches.clear()
        mDataArrivedObservers.clear()
    }

    /**
     * 接受数据线程处理
     */
    private fun onReceiveData(body: ByteArray, host: String) {
        for (mDataArrivedObserver in mDataArrivedObservers) {
            val onMainThread = mDataArrivedObserver.value
            val onDataArrivedListener = mDataArrivedObserver.key
            if (onMainThread) {
                mMainThreadHandler.post {
                    onDataArrivedListener.onDataArrived(body, host)
                }
            } else {
                onDataArrivedListener.onDataArrived(body, host)
            }
        }
    }

}