package cn.leo.udp.net

import android.support.annotation.IntRange

/**
 * @author : Jarry Leo
 * @date : 2018/9/18 9:48
 * 包处理器,可以处理自定义包结构或者分包
 */
abstract class PacketProcessor(@IntRange(from = 1, to = 64 * 1024) open var subPacketSize: Int = Config.PACK_SIZE) {
    protected var mergeProcessResultListener: PacketProcessor.MergeProcessResultListener? = null
    fun setMergeResultListener(resultListener: MergeProcessResultListener) {
        mergeProcessResultListener = resultListener
    }

    /**
     * 发送前的分包处理
     */
    abstract fun subpackage(data: ByteArray): Array<ByteArray>

    /**
     * 接受到数据的合包处理
     */
    abstract fun merge(data: ByteArray, host: String)

    interface MergeProcessResultListener {
        fun onMergeSuccess(data: ByteArray, host: String)
        fun onMergeFailed(data: ByteArray, host: String)
    }
}