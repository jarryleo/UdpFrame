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
	        compile 'com.github.jarryleo:UdpFrame:v2.1'
	}
```
### 使用方法

```
	UdpFrame.subscribe(this) //订阅接受默认端口37320数据
	//订阅消息附带包处理器,对发出和接受的数据包做处理
        UdpFrame.subscribe(this, BigPacketProcessor())
        UdpFrame.subscribe(
                37321,//订阅接收指定端口数据
                object : OnDataArrivedListener {
                    @UdpDataArrivedOnMainThread
                    override fun onDataArrived(data: ByteArray, host: String) {
                        toast(String(data))
                    }
                }
        )
        //获取发送器
        val sender = UdpFrame.getSender("127.0.0.1", packetProcessor = BigPacketProcessor())
        btnSendMsg.setOnClickListener {
            //发送消息
            sender.send("测试发送消息".toByteArray())
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

### 注意页面关闭时请取消订阅,否则会导致内存泄漏
