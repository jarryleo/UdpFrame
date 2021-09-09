package cn.leo.udp.net

/**
 * @author : Jarry Leo
 * @date : 2018/9/18 16:42
 */
internal class UdpSenderImpl : UdpSender {

    private val sendCore = UdpSendCore()
    private var host: String = "127.0.0.1"
    private var port: Int = Config.DEF_PORT

    override fun send(data: ByteArray): UdpSender {
        sendCore.send(data, host, port)
        return this
    }

    override fun setRemoteHost(host: String): UdpSender {
        this.host = host
        return this
    }

    override fun setPort(port: Int): UdpSender {
        this.port = port
        return this
    }

    override fun setPacketProcessor(packetProcessor: PacketProcessor): UdpSender {
        sendCore.setPacketProcessor(packetProcessor)
        return this
    }

    override fun sendBroadcast(data: ByteArray): UdpSender {
        sendCore.sendBroadcast(data, this.port)
        return this
    }

    fun safeClose(port: Int) {
        sendCore.closeListen(port)
    }
}