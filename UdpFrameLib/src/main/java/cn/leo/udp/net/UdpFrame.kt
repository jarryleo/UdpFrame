package cn.leo.udp.net

import android.support.annotation.IntRange

/**
 * Created by Leo on 2018/4/27.
 */
object UdpFrame : UdpInterface {
    private var listenCoreObservers = HashMap<Int, UdpListenCore>()
    private val closeableSender: UdpSenderImpl = UdpSenderImpl()

    /**
     * 获取消息发送器
     */
    override fun getSender(
        host: String,
        @IntRange(from = 0, to = 65535) port: Int,
        packetProcessor: PacketProcessor
    ): UdpSender {
        val udpSender = UdpSenderImpl()
        udpSender.setRemoteHost(host)
        udpSender.setPort(port)
        udpSender.setPacketProcessor(packetProcessor)
        return udpSender
    }

    /**
     * 订阅默认端口监听数据回调
     */
    override fun subscribe(
        onDataArrivedListener: OnDataArrivedListener,
        packetProcessor: PacketProcessor
    ) {
        subscribe(Config.DEF_PORT, onDataArrivedListener, packetProcessor)
    }

    /**
     * 订阅端口数据回调
     */
    override fun subscribe(
        @IntRange(from = 0, to = 65535) port: Int,
        onDataArrivedListener: OnDataArrivedListener,
        packetProcessor: PacketProcessor
    ) {
        if (listenCoreObservers.containsKey(port)) {
            val listenCore = listenCoreObservers[port]
            listenCore?.subscribe(onDataArrivedListener)
            listenCore?.setPacketProcessor(packetProcessor)
        } else {
            //创建新的端口监听
            val listenCore = UdpListenCore(port)
            listenCore.subscribe(onDataArrivedListener)
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
        closeableSender.safeClose(port)
        listenCoreObservers.remove(port)
    }

    /**
     * 关闭监听，释放资源
     */
    override fun close() {
        listenCoreObservers.keys.forEach {
            closeableSender.safeClose(it)
        }
        listenCoreObservers.clear()
    }
}