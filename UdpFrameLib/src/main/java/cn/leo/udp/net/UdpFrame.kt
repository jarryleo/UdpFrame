package cn.leo.udp.net

import android.content.Context

/**
 * Created by Leo on 2018/4/27.
 */
class UdpFrame : UdpInterface {

    private var udpCore: UdpCore

    constructor(onDataArrivedListener: OnDataArrivedListener) {
        udpCore = UdpCore(onDataArrivedListener)
    }

    constructor(onDataArrivedListener: OnDataArrivedListener, port: Int) {
        udpCore = UdpCore(onDataArrivedListener, port)
    }

    /**
     *发送局域网广播
     */
    override fun sendBroadcast(context: Context, data: ByteArray) {
        udpCore.sendBroadcast(context, data)
    }

    /**
     * 发送数据到host默认端口
     */
    override fun send(data: ByteArray, host: String) {
        udpCore.send(data, host)
    }

    /**
     * 发送数据到指定端口
     */
    override fun send(data: ByteArray, host: String, port: Int) {
        udpCore.send(data, host, port)
    }

    /**
     * 重设数据接收回调
     */
    override fun setOnDataArrivedListener(onDataArrivedListener: OnDataArrivedListener) {
        udpCore.setOnDataArrivedListener(onDataArrivedListener)
    }

    /**
     * 关闭监听，释放资源
     */
    override fun close() {
        udpCore.close()
    }
}