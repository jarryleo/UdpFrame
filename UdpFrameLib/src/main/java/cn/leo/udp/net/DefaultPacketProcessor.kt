package cn.leo.udp.net

import android.support.annotation.IntRange

/**
 * @author : Jarry Leo
 * @date : 2018/9/18 10:19
 */
open class DefaultPacketProcessor(@IntRange(from = 1, to = 64 * 1024) override var subPacketSize: Int = Config.PACK_SIZE) : PacketProcessor(subPacketSize) {

    override fun subpackage(data: ByteArray): Array<ByteArray> {
        val list = mutableListOf<ByteArray>()
        val dataSize = data.size
        var packetSize = 0
        while (packetSize < dataSize) {
            var toIndex = packetSize + subPacketSize
            toIndex = if (toIndex > dataSize) {
                dataSize
            } else {
                toIndex
            }
            val packet = data.copyOfRange(packetSize, toIndex)
            packetSize += subPacketSize
            list.add(packet)
        }
        return list.toTypedArray()
    }

    override fun merge(data: ByteArray, host: String) {
        mergeProcessResultListener?.onMergeSuccess(data, host)
    }

}