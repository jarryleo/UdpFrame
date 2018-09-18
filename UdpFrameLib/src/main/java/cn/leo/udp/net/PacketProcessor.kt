package cn.leo.udp.net

/**
 * @author : Jarry Leo
 * @date : 2018/9/18 9:48
 * 分包处理器,把一个大包分成若干小包数组
 */
abstract class PacketProcessor {
    protected var mergeProcessResultListener: PacketProcessor.MergeProcessResultListener? = null
    fun setMergeResultListener(resultListener: MergeProcessResultListener) {
        mergeProcessResultListener = resultListener
    }

    abstract fun subpackage(data: ByteArray): Array<ByteArray>
    abstract fun merge(data: ByteArray, host: String)

    interface MergeProcessResultListener {
        fun onMergeSuccess(data: ByteArray, host: String)
        fun onMergeFailed(data: ByteArray, host: String)
    }
}