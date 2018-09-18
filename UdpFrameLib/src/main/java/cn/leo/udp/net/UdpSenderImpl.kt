package cn.leo.udp.net

import android.content.Context

/**
 * @author : Jarry Leo
 * @date : 2018/9/18 16:42
 */
internal class UdpSenderImpl : UdpSender {

    private val sendCore = UdpSendCore()
    private var host: String = "127.0.0.1"
    private var port: Int = Config.DEF_PORT

    override fun send(data: ByteArray) {
        sendCore.send(data, host, port)
    }

    override fun setRemoteHost(host: String) {
        this.host = host
    }

    override fun setPort(port: Int) {
        this.port = port
    }

    override fun setPacketProcessor(packetProcessor: PacketProcessorInterface) {
        sendCore.setPacketProcessor(packetProcessor)
    }

    override fun sendBroadcast(context: Context, data: ByteArray) {
        sendCore.sendBroadcast(context, data)
    }
}