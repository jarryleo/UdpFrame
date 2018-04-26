package cn.leo.udp.net

/**
 * Created by Leo on 2018/4/26.
 */
interface OnDataArrivedListener {
    fun onDataArrived(data: ByteArray, length: Int, host: String)
}