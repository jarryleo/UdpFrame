package cn.leo.udp.net

/**
 * Created by Leo on 2018/4/27.
 */
internal interface UdpInterface {
    fun getSender(
        host: String,
        port: Int = Config.DEF_PORT,
        packetProcessor: PacketProcessor
    ): UdpSender

    fun subscribe(
        onDataArrivedListener: OnDataArrivedListener,
        packetProcessor: PacketProcessor
    )

    fun subscribe(
        port: Int,
        onDataArrivedListener: OnDataArrivedListener,
        packetProcessor: PacketProcessor
    )

    fun unSubscribe(onDataArrivedListener: OnDataArrivedListener)
    fun unSubscribe(port: Int)
    fun close()
}