package cn.leo.udp.net

/**
 * Created by Leo on 2018/4/27.
 * 数据回调接口
 */
interface OnDataArrivedListener {
    fun onDataArrived(data: ByteArray, host: String)
}