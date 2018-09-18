package cn.leo.udp.net

import android.content.Context

/**
 * @author : Jarry Leo
 * @date : 2018/9/18 16:39
 */
interface UdpSender {
    fun send(data: ByteArray)
    fun setRemoteHost(host: String)
    fun setPort(port: Int)
    fun setPacketProcessor(packetProcessor: PacketProcessor)
    fun sendBroadcast(context: Context, data: ByteArray)
}