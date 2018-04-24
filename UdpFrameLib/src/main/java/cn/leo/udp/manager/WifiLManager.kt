package cn.leo.udp.manager

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.text.TextUtils
import java.net.InetAddress


/**
 * Created by Leo on 2018/2/25.
 */
object WifiLManager {

    /**
     * 判断wifi是否可用
     */
    fun isWifiConnected(context: Context): Boolean {
        val mConnectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mWiFiNetworkInfo = mConnectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        return mWiFiNetworkInfo.isAvailable
    }

    /**
     * 开启Wifi
     *
     * @param context 上下文
     * @return 是否成功
     */
    fun openWifi(context: Context): Boolean {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager != null && (wifiManager.isWifiEnabled || wifiManager.setWifiEnabled(true))
    }

    /**
     * 开启Wifi扫描
     *
     * @param context 上下文
     */
    fun startWifiScan(context: Context) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager ?: return
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }
        wifiManager.startScan()
    }

    /**
     * 关闭Wifi
     *
     * @param context 上下文
     */
    fun closeWifi(context: Context) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiManager != null && wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = false
        }
    }

    /**
     * 连接指定Wifi
     *
     * @param context  上下文
     * @param ssid     SSID
     * @param password 密码
     * @return 是否连接成功
     */
    fun connectWifi(context: Context, ssid: String, password: String): Boolean {
        val connectedSsid = getConnectedSSID(context)
        if (!TextUtils.isEmpty(connectedSsid) && connectedSsid == ssid) {
            return true
        }
        openWifi(context)
        var wifiConfiguration = isWifiExist(context, ssid)
        if (wifiConfiguration == null) {
            wifiConfiguration = createWifiConfiguration(ssid, password)
        }
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager ?: return false
        val networkId = wifiManager.addNetwork(wifiConfiguration)
        return wifiManager.enableNetwork(networkId, true)
    }

    /**
     * 断开Wifi连接
     *
     * @param context 上下文
     */
    fun disconnectWifi(context: Context) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiManager != null && wifiManager.isWifiEnabled) {
            wifiManager.disconnect()
        }
    }

    /**
     * 获取当前连接的Wifi的SSID
     *
     * @param context 上下文
     * @return SSID
     */
    fun getConnectedSSID(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager?.connectionInfo
        return wifiInfo?.ssid?.replace("\"".toRegex(), "") ?: ""
    }

    /**
     * 获取连接的Wifi热点的IP地址
     *
     * @param context 上下文
     * @return IP地址
     */
    fun getHotspotIpAddress(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiinfo = wifiManager.connectionInfo
        if (wifiinfo != null) {
            val dhcpInfo = wifiManager.dhcpInfo
            if (dhcpInfo != null) {
                val address = dhcpInfo.gateway
                return ((address and 0xFF).toString()
                        + "." + (address shr 8 and 0xFF)
                        + "." + (address shr 16 and 0xFF)
                        + "." + (address shr 24 and 0xFF))
            }
        }
        return ""
    }

    /**
     * 获取连接Wifi后设备本身的IP地址
     *
     * @param context 上下文
     * @return IP地址
     */
    fun getLocalIpAddress(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiinfo = wifiManager.connectionInfo
        if (wifiinfo != null) {
            val ipAddress = wifiinfo.ipAddress
            if (ipAddress == 0 && ApManager.isApOn(context)) {
                return "192.168.43.1"
            }
            return ((ipAddress and 0xFF).toString()
                    + "." + (ipAddress shr 8 and 0xFF)
                    + "." + (ipAddress shr 16 and 0xFF)
                    + "." + (ipAddress shr 24 and 0xFF))
        }
        return ""
    }

    /**
     * 获取广播地址
     */
    fun getBroadcastAddress(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcp = wifiManager.dhcpInfo ?: return if (ApManager.isApOn(context)) {
            "192.168.43.255"
        } else {
            "255.255.255.255"
        }
        val broadcast = dhcp.ipAddress and dhcp.netmask or dhcp.netmask.inv()
        val quads = ByteArray(4)
        for (k in 0..3)
            quads[k] = (broadcast shr k * 8 and 0xFF).toByte()
        return InetAddress.getByAddress(quads).hostAddress
    }

    /**
     * 判断本地是否有保存指定Wifi的配置信息（之前是否曾成功连接过该Wifi）
     *
     * @param context 上下文
     * @param ssid    SSID
     * @return Wifi的配置信息
     */
    private fun isWifiExist(context: Context, ssid: String): WifiConfiguration? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiConfigurationList = wifiManager?.configuredNetworks
        if (wifiConfigurationList != null && wifiConfigurationList.size > 0) {
            for (wifiConfiguration in wifiConfigurationList) {
                if (wifiConfiguration.SSID == "\"" + ssid + "\"") {
                    return wifiConfiguration
                }
            }
        }
        return null
    }

    /**
     * 清除指定Wifi的配置信息
     *
     * @param ssid SSID
     */
    fun cleanWifiInfo(context: Context, ssid: String) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiConfiguration = isWifiExist(context, ssid)
        if (wifiManager != null && wifiConfiguration != null) {
            wifiManager.removeNetwork(wifiConfiguration.networkId)
        }
    }

    /**
     * 创建Wifi网络配置
     *
     * @param ssid     SSID
     * @param password 密码
     * @return Wifi网络配置
     */
    private fun createWifiConfiguration(ssid: String, password: String): WifiConfiguration {
        val wifiConfiguration = WifiConfiguration()
        wifiConfiguration.SSID = "\"" + ssid + "\""
        wifiConfiguration.preSharedKey = "\"" + password + "\""
        wifiConfiguration.hiddenSSID = true
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
        return wifiConfiguration
    }

}