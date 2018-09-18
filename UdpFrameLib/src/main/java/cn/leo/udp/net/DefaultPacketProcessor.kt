package cn.leo.udp.net

/**
 * @author : Jarry Leo
 * @date : 2018/9/18 10:19
 */
open class DefaultPacketProcessor : PacketProcessor() {

    override fun subpackage(data: ByteArray): Array<ByteArray> {
        return arrayOf(data)
    }

    override fun merge(data: ByteArray, host: String) {
        mergeProcessResultListener?.onMergeSuccess(data, host)
    }

}