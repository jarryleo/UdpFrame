# UdpFrame
## 安卓udp通信框架
### Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
### Step 2. Add the dependency
```
	dependencies {
	        compile 'com.github.jarryleo:UdpFrame:v2.0'
	}
```
### 使用方法

```
	UdpFrame.subscribe(this) //订阅接受默认端口37320数据
        UdpFrame.subscribe(
                37321,//订阅接收指定端口数据
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
```


### 数据回调
```
    @UdpDataArrivedOnMainThread
    override fun onDataArrived(data: ByteArray, host: String) {
        tvMsg.text = String(data)
    }
```
有注解回调在UI线程，没有注解在子线程
