package cn.leo.udp.net

import android.os.Handler
import android.os.Looper
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by Leo on 2018/2/26.
 * 安卓UDP底层传输模块，拆分数据包传输
 * 无丢包处理。
 */

internal class UdpListenCore(port: Int = Config.DEF_PORT) : Thread(), PacketProcessorInterface.MergeProcessResultListener {


    private var mPort = port
    private lateinit var mReceiveSocket: DatagramSocket
    private val mMainThreadHandler = Handler(Looper.getMainLooper())
    private val mDataArrivedObservers = HashMap<OnDataArrivedListener, Boolean>()
    private var packetProcessor: PacketProcessorInterface? = null
    //缓存(host,data)
    private val mCaches = ConcurrentHashMap<String, ArrayList<ByteArray>>()

    init {
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
     *  设置包处理器
     */
    fun setPacketProcessor(packetProcessor: PacketProcessorInterface) {
        if (this.packetProcessor != null && this.packetProcessor != packetProcessor) {
            throw IllegalArgumentException("one port just set one packet processor!")
        }
        this.packetProcessor = packetProcessor
        packetProcessor.setMergeResultListener(this)
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
                val host = dp.address.hostAddress
                val head = data.copyOf(2)
                if (head[0] == (-0xEE).toByte() && head[1] == (-0xDD).toByte()) {
                    if ("127.0.0.1" == host) {
                        break
                    }
                }
                packetProcessor?.merge(data, host)
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

    override fun onMergeSuccess(data: ByteArray, host: String) {
        onReceiveData(data, host)
    }

    override fun onMergeFailed(data: ByteArray, host: String) {
        //合并错误的包处理
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