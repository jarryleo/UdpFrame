package cn.leo.udp.manager

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings


/**
 * Created by Leo on 2018/2/25.
 */
object ApManager {
    /**
     * 判断是否可以代码开启热点
     */
    fun canOpenAp(context: Context): Boolean =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) true
            else Settings.System.canWrite(context)

    /**
     * 判断便携热点是否开启
     *
     * @param context 上下文
     * @return 开关
     */
    fun isApOn(context: Context): Boolean {
        val wifimanager = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        try {
            val method = wifimanager.javaClass.getDeclaredMethod("isWifiApEnabled")
            method.isAccessible = true
            return method.invoke(wifimanager) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    /**
     * 开启便携热点
     *
     * @param context  上下文
     * @param ssid     SSID
     * @param password 密码
     * @return 是否成功
     */
    fun openAp(context: Context, ssid: String, password: String): Boolean {
        val wifimanager = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        if (wifimanager.isWifiEnabled) {
            wifimanager.isWifiEnabled = false
        }
        try {
            var method = wifimanager.javaClass.getMethod("setWifiApEnabled", WifiConfiguration::class.java, Boolean::class.javaPrimitiveType)
            method.invoke(wifimanager, null, false)
            method = wifimanager.javaClass.getMethod("setWifiApEnabled", WifiConfiguration::class.java, Boolean::class.javaPrimitiveType)
            method.invoke(wifimanager, createApConfiguration(ssid, password), true)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    /**
     * 关闭便携热点
     *
     * @param context 上下文
     */
    fun closeAp(context: Context) {
        val wifimanager = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        try {
            val method = wifimanager.javaClass.getMethod("setWifiApEnabled", WifiConfiguration::class.java, Boolean::class.javaPrimitiveType)
            method.invoke(wifimanager, null, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 获取便携热点的SSID
     *
     * @param context 上下文
     * @return SSID
     */
    fun getApSSID(context: Context): String {
        try {
            val wifiManager = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            val method = wifiManager.javaClass.getMethod("getWifiApConfiguration")
            method.isAccessible = true
            val wifiConfiguration = method.invoke(wifiManager) as WifiConfiguration
            return wifiConfiguration.SSID.replace("\"".toRegex(), "")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    /**
     * 获取开启便携热点后设备本身的IP地址
     *
     * @param context 上下文
     * @return IP地址
     */
    fun getHotspotIpAddress(context: Context): String {
        val wifimanager = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val dhcpInfo = wifimanager.dhcpInfo
        if (dhcpInfo != null) {
            val address = dhcpInfo.serverAddress
            return ((address and 0xFF).toString()
                    + "." + (address shr 8 and 0xFF)
                    + "." + (address shr 16 and 0xFF)
                    + "." + (address shr 24 and 0xFF))
        }
        return ""
    }

    /**
     * 配置热点信息
     *
     * @param ssid     便携热点SSID
     * @param password 便携热点密码
     * @return 热点信息
     */
    private fun createApConfiguration(ssid: String, password: String): WifiConfiguration {
        val config = WifiConfiguration()
        config.SSID = ssid
        config.preSharedKey = password
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
        return config
    }
}