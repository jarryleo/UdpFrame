package cn.leo.udpframe;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import cn.leo.udp.net.DefaultPacketProcessor;
import cn.leo.udp.net.OnDataArrivedListener;
import cn.leo.udp.net.UdpDataArrivedOnMainThread;
import cn.leo.udp.net.UdpFrame;

/**
 * Created by Leo on 2018/4/27.
 */

public class Test {
    public void testUdp() {
        UdpFrame.INSTANCE.subscribe(11255, new OnDataArrivedListener() {
            @Override
            @UdpDataArrivedOnMainThread
            public void onDataArrived(@NotNull byte[] data, @NotNull String host) {

            }
        },new DefaultPacketProcessor());
    }
}
