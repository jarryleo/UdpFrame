package cn.leo.udp.net

import android.annotation.TargetApi
import android.content.Context
import android.os.*
import cn.leo.udp.manager.WifiLManager
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

/**
 * Created by Leo on 2018/4/27.
 */
@TargetApi(Build.VERSION_CODES.CUPCAKE)
internal class UdpSendCore {
    private val mSendSocket = DatagramSocket()
    private var mHandlerThread: HandlerThread = HandlerThread("UdpFrameSendThread")
    private var mSendHandler: Handler
    private var packetProcessor: PacketProcessorInterface = DefaultPacketProcessor()

    fun setPacketProcessor(packetProcessor: PacketProcessorInterface) {
        this.packetProcessor = packetProcessor
    }


    init {
        //开启发送数据子线程
        mHandlerThread.start()
        //发送数据子线程handler
        mSendHandler = Handler(mHandlerThread.looper) {
            if (it.what == -1) {
                safetyClose(it.arg1)
                return@Handler true
            }
            val data = it.data
            val host = data.getString(Config.HOST_FLAG)
            val port = data.getInt(Config.PORT_FLAG)
            val byteArray = data.getByteArray(Config.DATA_FLAG)
            sendData(byteArray, host, port)
            true
        }
    }


    /**
     *发送
     */
    internal fun send(data: ByteArray, host: String, port: Int = Config.DEF_PORT) {
        val message = Message.obtain()
        val bundle = Bundle()
        bundle.putString(Config.HOST_FLAG, host)
        bundle.putInt(Config.PORT_FLAG, port)
        bundle.putByteArray(Config.DATA_FLAG, data)
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
     *发送数据包
     */
    private fun sendData(data: ByteArray, host: String, port: Int) {
        if (mSendSocket.isClosed) {
            throw IllegalStateException("send socket is closed.")
        }
        //发送地址
        val ia = InetSocketAddress(host, port)
        val subpackage = packetProcessor.subpackage(data)
        subpackage.forEach {
            //发送小包
            val dp = DatagramPacket(it, it.size, ia)
            try {
                mSendSocket.send(dp)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 安全关闭udp并释放端口
     */
    internal fun closeListen(port: Int) {
        mSendHandler.obtainMessage(-1, port, port).sendToTarget()
    }

    internal fun closeSend() {
        mSendSocket.close()
    }

    private fun safetyClose(port: Int) {
        val ia = InetSocketAddress("localhost", port)
        val head = byteArrayOf((-0xEE).toByte(), (-0xDD).toByte())
        val dp = DatagramPacket(head, head.size, ia)
        mSendSocket.send(dp)
    }

}