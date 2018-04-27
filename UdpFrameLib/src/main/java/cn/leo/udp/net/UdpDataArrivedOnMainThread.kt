package cn.leo.udp.net

/**
 * Created by Leo on 2018/4/27.
 */

//数据回调方法上打上这个注解就是在主线程回调数据
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class UdpDataArrivedOnMainThread