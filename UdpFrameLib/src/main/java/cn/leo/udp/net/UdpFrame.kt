package cn.leo.udp.net

import android.content.Context

/**
 * Created by Leo on 2018/4/27.
 */
object UdpFrame : UdpInterface {
    private val sendCore = UdpSendCore()
    private var listenCoreObservers = HashMap<Int, UdpListenCore>()

    /**
     *发送局域网广播
     */
    override fun sendBroadcast(context: Context, data: ByteArray, packetProcessor: PacketProcessorInterface) {
        sendCore.setPacketProcessor(packetProcessor)
        sendCore.sendBroadcast(context, data)
    }

    /**
     * 发送数据到host默认端口
     */
    override fun send(data: ByteArray, host: String, packetProcessor: PacketProcessorInterface) {
        sendCore.setPacketProcessor(packetProcessor)
        sendCore.send(data, host)
    }

    /**
     * 发送数据到指定端口
     */
    override fun send(data: ByteArray, host: String, port: Int, packetProcessor: PacketProcessorInterface) {
        sendCore.setPacketProcessor(packetProcessor)
        sendCore.send(data, host, port)
    }

    /**
     * 订阅默认端口监听数据回调
     */
    override fun subscribe(onDataArrivedListener: OnDataArrivedListener, packetProcessor: PacketProcessorInterface) {
        subscribe(Config.DEF_PORT, onDataArrivedListener)
    }

    /**
     * 订阅端口数据回调
     */
    override fun subscribe(port: Int, onDataArrivedListener: OnDataArrivedListener, packetProcessor: PacketProcessorInterface) {
        if (listenCoreObservers.containsKey(port)) {
            val listenCore = listenCoreObservers[port]
            listenCore?.subscribe(onDataArrivedListener)
        } else {
            //创建新的端口监听
            val listenCore = UdpListenCore(onDataArrivedListener, port)
            listenCore.setPacketProcessor(packetProcessor)
            listenCoreObservers[port] = listenCore
        }
    }

    /**
     * 取消数据回调订阅
     */
    override fun unSubscribe(onDataArrivedListener: OnDataArrivedListener) {
        listenCoreObservers.values.forEach { it.unSubscribe(onDataArrivedListener) }
    }

    /**
     * 取消端口订阅
     */
    override fun unSubscribe(port: Int) {
        sendCore.closeListen(port)
        listenCoreObservers.remove(port)
    }

    /**
     * 关闭监听，释放资源
     */
    override fun close() {
        listenCoreObservers.keys.forEach { sendCore.closeListen(it) }
        listenCoreObservers.clear()
    }
}