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
	        compile 'com.github.jarryleo:UdpFrame:v1.0.2'
	}
```
### 使用方法

```
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
```


### 数据回调
```
    @UdpFrame.MainThread
    override fun onDataArrived(data: ByteArray, length: Int, host: String) {
        tvMsg.text = String(data)
    }
```
有注解回调在UI线程，没有注解在子线程
