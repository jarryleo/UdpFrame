package cn.leo.udpframe

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import cn.leo.localnet.utils.toast
import cn.leo.udp.net.BigPacketProcessor
import cn.leo.udp.net.OnDataArrivedListener
import cn.leo.udp.net.UdpDataArrivedOnMainThread
import cn.leo.udp.net.UdpFrame
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OnDataArrivedListener {
    private var s1 = "这是一句测试12345"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //订阅消息
        UdpFrame.subscribe(this, BigPacketProcessor())
        //获取发送器
        val sender = UdpFrame.getSender("127.0.0.1", packetProcessor = BigPacketProcessor())
        btnSendMsg.setOnClickListener {
            //发送消息
            val data = s1.toByteArray()
            sender.send(data)
            Log.e("s1-------", "size:" + data.size)
            Log.e("s1-------", s1)
        }
        repeat(10) { s1 += s1 }
    }

    //消息到达监听
    @UdpDataArrivedOnMainThread
    override fun onDataArrived(data: ByteArray, host: String) {
        Log.e("s-------", "size:" + data.size)
        val s = String(data)
        Log.e("s--------", s)
        tvMsg.text = s
        toast("" + (s == s1))
    }

    override fun onDestroy() {
        super.onDestroy()
        //必须取消订阅,否则会导致内存泄漏
        UdpFrame.unSubscribe(this)
    }
}
