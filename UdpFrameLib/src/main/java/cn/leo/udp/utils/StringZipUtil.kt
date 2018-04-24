package cn.leo.udp.utils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Created by Leo on 2018/2/26.
 */
object StringZipUtil {
    // 压缩
    @Throws(IOException::class)
    fun compress(str: String?): String? {
        if (str == null || str.isEmpty()) {
            return str
        }
        val out = ByteArrayOutputStream()
        val gzip = GZIPOutputStream(out)
        gzip.write(str.toByteArray())
        gzip.close()
        return out.toString("ISO-8859-1")
    }

    // 解压缩
    @Throws(IOException::class)
    fun uncompress(str: String?): String? {
        if (str == null || str.isEmpty()) {
            return str
        }
        val bos = ByteArrayOutputStream()
        val bis = ByteArrayInputStream(str
                .toByteArray(charset("ISO-8859-1")))
        val gunZip = GZIPInputStream(bis)
        val buffer = ByteArray(256)
        var n: Int = gunZip.read(buffer)
        while (n >= 0) {
            bos.write(buffer, 0, n)
            n = gunZip.read(buffer)
        }
        /*while ((n = gunZip.read(buffer)) >= 0) {
            bos.write(buffer, 0, n)
        }*/
        // toString()使用平台默认编码，也可以显式的指定如toString("GBK")
        return bos.toString()
    }
}