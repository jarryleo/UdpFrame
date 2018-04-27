package cn.leo.udp.net

import android.content.Context
import android.os.*
import android.util.Log
import cn.leo.udp.manager.WifiLManager
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by Leo on 2018/2/26.
 * 安卓UDP底层传输模块，拆分数据包传输
 * 无丢包处理。
 */

internal class UdpCore(private var onDataArrivedListener: OnDataArrivedListener,
                       private val listenPort: Int = 37320) : Thread() {
    private val hostFlag = "host"
    private val portFlag = "port"
    private val dataFlag = "data"
    //拆分单个包大小(包的个数为byte最大值)这个值不能超过UDP包最大值64K。
    private val mPackSize = 1024
    private val mSendSocket = DatagramSocket()
    private val mReceiveSocket = DatagramSocket(listenPort)
    private var mHandlerThread: HandlerThread = HandlerThread("sendThread")
    private val mMainThreadHandler = Handler(Looper.getMainLooper())
    private var mSendHandler: Handler
    //UI线程上回调数据
    private var mDataArriveOnMainThread = false
    //缓存(host,data)
    private val mCaches = ConcurrentHashMap<String, ArrayList<ByteArray>>()

    override fun run() {
        listen()
    }

    init {
        //开启发送数据子线程
        mHandlerThread.start()
        //发送数据子线程handler
        mSendHandler = Handler(mHandlerThread.looper) {
            if (it.what == -1) {
                safetyClose()
                return@Handler true
            }
            val data = it.data
            val host = data.getString(hostFlag)
            val port = data.getInt(portFlag)
            val byteArray = data.getByteArray(dataFlag)
            sendData(byteArray, host, port)
            true
        }
        start() //启动监听
        checkThread()
    }

    /**
     * 检查注解
     */
    private fun checkThread() {
        val kClass = onDataArrivedListener::class.java
        val declaredMethod = kClass.getDeclaredMethod("onDataArrived",
                ByteArray::class.java, Int::class.java, String::class.java)
        val annotation = declaredMethod.getAnnotation(UdpDataArrivedOnMainThread::class.java)
        mDataArriveOnMainThread = annotation != null
    }

    /**
     * 替换接受数据监听器
     */
    internal fun setOnDataArrivedListener(onDataArrivedListener: OnDataArrivedListener) {
        this.onDataArrivedListener = onDataArrivedListener
        checkThread()
    }

    /**
     *发送
     */
    internal fun send(data: ByteArray, host: String, port: Int = listenPort) {
        val message = Message.obtain()
        val bundle = Bundle()
        bundle.putString(hostFlag, host)
        bundle.putInt(portFlag, port)
        bundle.putByteArray(dataFlag, data)
        message.data = bundle
        mSendHandler.sendMessage(message)
    }

    /**
     * 发送局域网广播
     */
    internal fun sendBroadcast(context: Context, data: ByteArray) {
        val broadCastAddress = WifiLManager.getBroadcastAddress(context)
        send(data, broadCastAddress)
    }

    /**
     * 监听UDP信息,接受数据
     */
    private fun listen() {
        val data = ByteArray(mPackSize)
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
        mSendSocket.close()
        //停止发送线程
        mHandlerThread.quit()
        //清空缓存
        mCaches.clear()
    }

    /**
     * 接受数据线程处理
     */
    private fun onReceiveData(body: ByteArray, host: String) {
        if (mDataArriveOnMainThread) {
            mMainThreadHandler.post {
                onDataArrivedListener.onDataArrived(body, host)
            }
        } else {
            onDataArrivedListener.onDataArrived(body, host)
        }
    }

    /**
     *发送数据包
     * 最大127K
     */
    private fun sendData(data: ByteArray, host: String, port: Int) {
        //发送地址
        val ia = InetSocketAddress(host, port)
        //已发送字节数
        var sendLength = 0
        //要发送的长度
        val dataSize = data.size
        //拆分后包的总个数
        val packCount = dataSize / (mPackSize - 2 + 1) + 1
        //最大发送数据不能超过127个拆分的数据包大小
        if (packCount > Byte.MAX_VALUE) {
            throw IllegalArgumentException("The maximum number of data sent is 127K.")
        }
        //循环发送数据包
        while (sendLength < dataSize) {
            //要发送的数据(长度不超过最小包长)
            val length = if (dataSize - sendLength > mPackSize - 2) {
                mPackSize - 2
            } else {
                (dataSize - sendLength)
            } + 2
            //定义新包大小
            val pack = ByteArray(length)
            //-2 表示去掉头长度，+1表示，长度刚好1个包的时候不会多出来
            //当前包序号，从1开始
            val packIndex = sendLength / (mPackSize - 2) + 1
            val head = byteArrayOf(packCount.toByte(), packIndex.toByte())
            //添加数据头
            System.arraycopy(head, 0, pack, 0, head.size)
            //添加数据体
            System.arraycopy(data, sendLength, pack, head.size, pack.size - head.size)
            //发送小包
            val dp = DatagramPacket(pack, pack.size, ia)
            try {
                mSendSocket.send(dp)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            //已发长度累计
            sendLength += pack.size - 2
        }
    }

    /**
     * 安全关闭udp并释放端口
     */
    internal fun close() {
        mSendHandler.obtainMessage(-1).sendToTarget()
    }

    private fun safetyClose() {
        val ia = InetSocketAddress("localhost", listenPort)
        val head = byteArrayOf((-0xEE).toByte(), (-0xDD).toByte())
        val dp = DatagramPacket(head, head.size, ia)
        mSendSocket.send(dp)
    }
}