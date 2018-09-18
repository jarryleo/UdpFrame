package cn.leo.udp.net

import android.content.Context

/**
 * Created by Leo on 2018/4/27.
 */
internal interface UdpInterface {
    fun send(data: ByteArray, host: String, port: Int, packetProcessor: PacketProcessorInterface = DefaultPacketProcessor())
    fun send(data: ByteArray, host: String, packetProcessor: PacketProcessorInterface = DefaultPacketProcessor())
    fun sendBroadcast(context: Context, data: ByteArray, packetProcessor: PacketProcessorInterface = DefaultPacketProcessor())
    fun subscribe(onDataArrivedListener: OnDataArrivedListener, packetProcessor: PacketProcessorInterface = DefaultPacketProcessor())
    fun subscribe(port: Int, onDataArrivedListener: OnDataArrivedListener, packetProcessor: PacketProcessorInterface = DefaultPacketProcessor())
    fun unSubscribe(onDataArrivedListener: OnDataArrivedListener)
    fun unSubscribe(port: Int)
    fun close()
}