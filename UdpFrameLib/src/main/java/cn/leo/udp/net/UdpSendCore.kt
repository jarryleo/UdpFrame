package cn.leo.udp.net

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import cn.leo.udp.manager.WifiLManager
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

/**
 * Created by Leo on 2018/4/27.
 */
internal class UdpSendCore {
    private val mSendSocket = DatagramSocket()
    private var mHandlerThread: HandlerThread = HandlerThread("sendThread")
    private var mSendHandler: Handler

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
     * 最大127K
     */
    private fun sendData(data: ByteArray, host: String, port: Int) {
        if (mSendSocket.isClosed) {
            throw IllegalStateException("send socket is closed.")
        }
        //发送地址
        val ia = InetSocketAddress(host, port)
        //已发送字节数
        var sendLength = 0
        //要发送的长度
        val dataSize = data.size
        //拆分后包的总个数
        val packCount = dataSize / (Config.PACK_SIZE - 2 + 1) + 1
        //最大发送数据不能超过127个拆分的数据包大小
        if (packCount > Byte.MAX_VALUE) {
            throw IllegalArgumentException("The maximum number of data sent is 127K.")
        }
        //循环发送数据包
        while (sendLength < dataSize) {
            //要发送的数据(长度不超过最小包长)
            val length = if (dataSize - sendLength > Config.PACK_SIZE - 2) {
                Config.PACK_SIZE - 2
            } else {
                (dataSize - sendLength)
            } + 2
            //定义新包大小
            //val pack = ByteArray(length)
            //-2 表示去掉头长度，+1表示，长度刚好1个包的时候不会多出来
            //当前包序号，从1开始
            val packIndex = sendLength / (Config.PACK_SIZE - 2) + 1
            val head = byteArrayOf(packCount.toByte(), packIndex.toByte())
            //添加数据头
            //System.arraycopy(head, 0, pack, 0, head.size)
            //添加数据体
            //System.arraycopy(data, sendLength, pack, head.size, pack.size - head.size)
            val pack = head + data
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