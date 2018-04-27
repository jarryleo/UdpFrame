package cn.leo.udp.net

/**
 * Created by Leo on 2018/4/27.
 */
internal object Config {
    //拆分单个包大小(包的个数为byte最大值)这个值不能超过UDP包最大值64K。
    const val PACK_SIZE = 1024
    //默认端口
    const val DEF_PORT = 37320
    const val HOST_FLAG = "host"
    const val PORT_FLAG = "port"
    const val DATA_FLAG = "data"
}