package cn.leo.udp.model

import java.util.*

/**
 * create by : Jarry Leo
 * date : 2018/8/1 13:43
 */
data class PacketModel(var data: ByteArray) {
    var timestamp: Long = System.currentTimeMillis() * 1000 + Random().nextInt(1000)
}