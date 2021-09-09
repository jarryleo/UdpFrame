package cn.leo.udp.net

import android.content.Context

/**
 * @author : Jarry Leo
 * @date : 2018/9/18 16:39
 */
interface UdpSender {
    fun send(data: ByteArray): UdpSender
    fun setRemoteHost(host: String): UdpSender
    fun setPort(port: Int): UdpSender
    fun setPacketProcessor(packetProcessor: PacketProcessor): UdpSender
    fun sendBroadcast(data: ByteArray): UdpSender
}