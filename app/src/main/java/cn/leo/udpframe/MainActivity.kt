package cn.leo.udpframe

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.leo.udp.manager.WifiLManager
import cn.leo.udp.net.OnDataArrivedListener
import cn.leo.udp.net.UdpDataArrivedOnMainThread
import cn.leo.udp.net.UdpFrame
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), OnDataArrivedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val udpFrame = UdpFrame(this, 25678)
        btnSendMsg.setOnClickListener {
            val data = WifiLManager.getLocalIpAddress(this).toByteArray()
            //udpFrame.sendBroadcast(this, data)
            udpFrame.send(data, "127.0.0.1")
        }
        btnClose.setOnClickListener {
            udpFrame.close()
            tvMsg.text = "端口已关闭"
        }
    }

    @UdpDataArrivedOnMainThread
    override fun onDataArrived(data: ByteArray, host: String) {
        tvMsg.text = String(data)
        Executors.newFixedThreadPool(10)
    }
}
