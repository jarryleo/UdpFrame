package cn.leo.udpframe;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

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
        });
        String[][] commands = {{"aa","哈哈"},{"bb","呵呵"}};
        HashMap<String, String> commandMap = getCommandMap(commands);
        String haha = commandMap.get("aa");

    }

    public HashMap<String, String> getCommandMap(String[][] commands) {
        HashMap<String, String> commandMap = new HashMap<>();
        for (int i = 0; i < commands.length; i++) {
            commandMap.put(commands[i][0], commands[i][1]);
        }
        return commandMap;
    }
}
