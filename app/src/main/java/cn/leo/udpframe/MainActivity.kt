package cn.leo.udpframe

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.leo.udp.manager.WifiLManager
import cn.leo.udp.net.UdpFrame
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), UdpFrame.OnDataArrivedListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val udpFrame = UdpFrame(this,25678)
        btnSendMsg.setOnClickListener {
            val data = WifiLManager.getLocalIpAddress(this).toByteArray()
            //udpFrame.sendBroadcast(this, data)
            udpFrame.send(data,"127.0.0.1")
        }
    }

    override fun onDataArrived(data: ByteArray, length: Int, host: String) {
        tvMsg.text = String(data)
    }
}
