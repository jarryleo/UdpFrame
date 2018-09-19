package cn.leo.udpframe

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import cn.leo.udp.net.BigPacketProcessor
import cn.leo.udp.net.OnDataArrivedListener
import cn.leo.udp.net.UdpFrame

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("cn.leo.udpframe", appContext.packageName)


    }
    @Test
    fun testUdpPacket(){
        var s1 = "这是第一句测试语句!"
        repeat(10) {s1+=s1}
        //订阅端口监听
        UdpFrame.subscribe(25678,object :OnDataArrivedListener{
            override fun onDataArrived(data: ByteArray, host: String) {
                val result = String(data)
                println("收到消息:")
                println(result)
                assertEquals("字符串相同",result,"111")
            }
        },BigPacketProcessor())
        //创建发送者
        val sender = UdpFrame.getSender("127.0.0.1", 25678, BigPacketProcessor())
        //发送消息
        sender.send(s1.toByteArray())
        //取消订阅
        UdpFrame.unSubscribe(25678)
    }

}
