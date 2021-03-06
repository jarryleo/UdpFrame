package cn.leo.udp.net

import android.support.annotation.IntRange
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * @author : Jarry Leo
 * @date : 2018/9/18 10:19
 */
open class BigPacketProcessor(@IntRange(from = 1, to = 64 * 1024) override var subPacketSize: Int = Config.PACK_SIZE) : PacketProcessor() {
    //缓存(host,data)
    private val mCaches = ConcurrentHashMap<String, MutableMap<Int, ByteArray>>()

    override fun subpackage(data: ByteArray): Array<ByteArray> {
        val list: MutableList<ByteArray> = mutableListOf()
        //已拆分字节数
        var sendLength = 0
        //要发送的长度
        val dataSize = data.size
        //拆分后包的总个数
        val packCount = dataSize / (subPacketSize - 2 + 1) + 1
        //最大发送数据不能超过127个拆分的数据包大小
        if (packCount > Byte.MAX_VALUE) {
            throw IllegalArgumentException("The maximum size of data sent is 127K.")
        }
        //循环发送数据包
        while (sendLength < dataSize) {
            //当前包序号，从1开始
            val packIndex = sendLength / (subPacketSize - 2) + 1
            val head = byteArrayOf(packCount.toByte(), packIndex.toByte())
            var toIndex = sendLength + subPacketSize - head.size
            toIndex = if (toIndex > dataSize) {
                dataSize
            } else {
                toIndex
            }
            val body = data.copyOfRange(sendLength, toIndex)
            val pack = head + body
            list.add(pack)
            //已发长度累计
            sendLength += pack.size - 2
        }
        return list.toTypedArray()
    }

    override fun merge(data: ByteArray, host: String) {
        val head = data.copyOf(2)
        val body = data.copyOfRange(2, data.size)

        //不符合规范的数据包直接抛弃
        if (head[0] < head[1]) {
            mergeProcessResultListener?.onMergeFailed(data, host)
            return
        }
        //数据只有1个包
        if (head[0] == 1.toByte()) {
            //数据回调给上层协议层
            mergeProcessResultListener?.onMergeSuccess(body, host)
        } else {
            //分包接收处理
            var cache = mCaches[host]

            //新的数据包组到来
            if (head[1] == 1.toByte()) {
                //没有缓存创建新缓存
                if (cache == null) {
                    cache = mutableMapOf()
                    mCaches[host] = cache
                } else {
                    //有的话清空这个地址的缓存
                    cache.clear()
                }
            } else {
                //不是新的数据包，但是没有缓存。则抛弃这个包
                if (cache == null) {
                    mergeProcessResultListener?.onMergeFailed(data, host)
                    return
                }
            }
            //缓存数据包(漏数据包则不缓存)
            if (cache.size + 1 == head[1].toInt()) {
                cache[head[1].toInt()] = body
            }
            //所有数据包都抵达完成则拼接
            if (head[0] == head[1]) {
                //数据包完整的话
                if (cache.size == head[0].toInt()) {
                    //开始组装数据
                    var sumData = ByteArray(0)
                    for (i in 1..cache.size) {
                        sumData += cache[i]!!
                    }
                    //数据回调给上层协议层
                    mergeProcessResultListener?.onMergeSuccess(sumData, host)
                    //清空缓存
                    cache.clear()
                } else {
                    //数据包不完整
                    Log.e("udp", " -- data is incomplete")
                }
            }
        }
    }
}