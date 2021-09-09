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
	        compile 'com.github.jarryleo:UdpFrame:v2.5'
	}
```
## 使用方法

### 订阅端口
```
     /**
         * 订阅接受消息的端口
         * 参数1:接受消息端口号
         * 参数2:消息回调
         * 参数3:消息处理器,构造为包大小,最大不能超过64K
         */ 
    UdpFrame.subscribe(37320,this, DefaultPacketProcessor(1024 * 30))
```
### 数据回调
```
    /**
    *参数1:data -接收到的数据
    *参数2:host -数据来源的ip地址
    *注解:此方法切换到主线程
    */
    @UdpDataArrivedOnMainThread
    override fun onDataArrived(data: ByteArray, host: String) {
        tvMsg.text = String(data)
    }
```
> 有注解回调在UI线程，没有注解在子线程

### 发送数据
```
	
       /**
         * 获取消息发送器
         * 参数1:目标的ip地址
         * 参数2:目标端口
         * 参数3:消息处理器,构造为包大小,最大不能超过64K
         */
        val sender = UdpFrame.getSender("127.0.0.1",37320, DefaultPacketProcessor(1024 * 30))
	
       	//发送数据,data为字节数组
	sender.send(data)
	
       
```
#### 注意:
> 发送和接收消息需要相同的包处理器,可以省略;如果不相同则会导致消息错乱!
> 默认的是包大小1024的包处理器,超过1024会自动分包,需要自己拼接!
> 另外提供一个大包处理器BigPacketProcessor,默认最大可以一次发送127K的数据包,并且能自动拼接;

### 取消订阅(重要)
```
	override fun onDestroy() {
        	super.onDestroy()
        	//必须取消订阅,否则会导致内存泄漏
        	UdpFrame.unSubscribe(this)
    	}
```

### 注意页面关闭时请取消订阅,否则会导致内存泄漏
