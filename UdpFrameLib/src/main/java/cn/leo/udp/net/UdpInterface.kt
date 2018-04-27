package cn.leo.udp.net

import android.content.Context

/**
 * Created by Leo on 2018/4/27.
 */
internal interface UdpInterface {
    fun send(data: ByteArray, host: String, port: Int)
    fun send(data: ByteArray, host: String)
    fun sendBroadcast(context: Context, data: ByteArray)
    fun subscribe(onDataArrivedListener: OnDataArrivedListener)
    fun subscribe(port: Int, onDataArrivedListener: OnDataArrivedListener)
    fun unSubscribe(onDataArrivedListener: OnDataArrivedListener)
    fun unSubscribe(port: Int)
    fun close()
}