package cn.leo.udpframe;

import org.jetbrains.annotations.NotNull;

import cn.leo.udp.net.DefaultPacketProcessor;
import cn.leo.udp.net.OnDataArrivedListener;
import cn.leo.udp.net.UdpDataArrivedOnMainThread;
import cn.leo.udp.net.UdpFrame;
import cn.leo.udp.net.UdpSender;

/**
 * Created by Leo on 2018/4/27.
 */

public class Test {
    public void testUdp() {
        //订阅消息
        UdpFrame.INSTANCE.subscribe(11255, new OnDataArrivedListener() {
            @Override
            @UdpDataArrivedOnMainThread
            public void onDataArrived(@NotNull byte[] data, @NotNull String host) {
                System.out.println(new String(data));
            }
        }, new DefaultPacketProcessor());
        //创建发送器
        UdpSender sender = UdpFrame.INSTANCE.getSender("127.0.0.1", 11255, new DefaultPacketProcessor());
        //发送消息
        sender.send("测试发送消息".getBytes());
    }
}
