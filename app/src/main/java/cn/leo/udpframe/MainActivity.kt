package cn.leo.udpframe

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.leo.localnet.utils.toast
import cn.leo.udp.manager.WifiLManager
import cn.leo.udp.net.OnDataArrivedListener
import cn.leo.udp.net.UdpDataArrivedOnMainThread
import cn.leo.udp.net.UdpFrame
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OnDataArrivedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        UdpFrame.subscribe(this)
        UdpFrame.subscribe(
                37321,
                object : OnDataArrivedListener {
                    @UdpDataArrivedOnMainThread
                    override fun onDataArrived(data: ByteArray, host: String) {
                        toast(String(data))
                    }
                }
        )
        btnSendMsg.setOnClickListener {
            val data = WifiLManager.getLocalIpAddress(this).toByteArray()
            //UdpFrame.sendBroadcast(this, data)
            UdpFrame.send(data, "127.0.0.1")
            UdpFrame.send("测试端口2".toByteArray(), "127.0.0.1", 37321)
        }
        btnClose.setOnClickListener {
            UdpFrame.close()
            tvMsg.text = "端口已关闭"
        }

    }

    @UdpDataArrivedOnMainThread
    override fun onDataArrived(data: ByteArray, host: String) {
        tvMsg.text = String(data)
    }
}
