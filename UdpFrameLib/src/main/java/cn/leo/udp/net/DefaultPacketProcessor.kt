package cn.leo.udp.net

/**
 * @author : Jarry Leo
 * @date : 2018/9/18 10:19
 */
open class DefaultPacketProcessor(private var subPacketSize: Int = Config.PACK_SIZE) : PacketProcessor() {

    override fun subpackage(data: ByteArray): Array<ByteArray> {
        val list = mutableListOf<ByteArray>()
        val size = data.size
        var packetSize = 0
        while (packetSize < size) {
            val packet = data.copyOfRange(packetSize, packetSize + subPacketSize)
            packetSize += subPacketSize
            list.add(packet)
        }
        return list.toTypedArray()
    }

    override fun merge(data: ByteArray, host: String) {
        mergeProcessResultListener?.onMergeSuccess(data, host)
    }

}