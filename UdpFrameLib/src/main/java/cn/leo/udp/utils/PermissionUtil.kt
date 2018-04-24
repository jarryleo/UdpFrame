package cn.leo.udp.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat

/**
 * Created by Leo on 2018/2/27.
 */
class PermissionUtil private constructor(activity: FragmentActivity) {
    private val tag = "fragmentRequestPermissionCallBack"
    private var mFragmentCallback: FragmentCallback? = null
    private val REQUEST_CODE = 110

    /**
     * 权限中文翻译，需要特殊权限自己添加
     */
    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    enum class Permission(val permissionCh: String, val permission: String) {
        READ_CONTACTS("读取联系人", Manifest.permission.READ_CONTACTS),
        READ_PHONE_STATE("读取电话信息", Manifest.permission.READ_PHONE_STATE),
        READ_CALENDAR("读取日历", Manifest.permission.READ_CALENDAR),
        CAMERA("相机", Manifest.permission.CAMERA),
        CALL_PHONE("拨打电话", Manifest.permission.CALL_PHONE),
        BODY_SENSORS("传感器", Manifest.permission.BODY_SENSORS),
        ACCESS_FINE_LOCATION("精确定位", Manifest.permission.ACCESS_FINE_LOCATION),
        ACCESS_COARSE_LOCATION("粗略定位", Manifest.permission.ACCESS_COARSE_LOCATION),
        READ_EXTERNAL_STORAGE("读取存储卡", Manifest.permission.READ_EXTERNAL_STORAGE),
        WRITE_EXTERNAL_STORAGE("写入存储卡", Manifest.permission.WRITE_EXTERNAL_STORAGE),
        RECORD_AUDIO("录音", Manifest.permission.RECORD_AUDIO),
        READ_SMS("读取短信", Manifest.permission.READ_SMS)
    }

    interface Result {
        fun onSuccess()

        fun onFailed()
    }

    /**
     * fragment，作为权限回调监听，和从设置界面返回监听
     */
    class FragmentCallback : Fragment() {
        private val REQUEST_CODE = 110
        private var mResult: Result? = null
        private var mPermissions: Array<Permission>? = null
        private var mRequestTime: Long = 0

        fun setRequestTime() {
            mRequestTime = SystemClock.elapsedRealtime()
        }

        fun setResult(result: Result?) {
            mResult = result
        }

        fun setPermissions(permissions: Array<Permission>) {
            mPermissions = permissions
        }

        override fun onRequestPermissionsResult(requestCode: Int,
                                                permissions: Array<String>,
                                                grantResults: IntArray) {
            var result = true
            when (requestCode) {
                REQUEST_CODE -> for (i in permissions.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        result = false
                        break
                    }
                }
            }
            if (mResult != null) {
                if (result) {
                    detach()
                    mResult!!.onSuccess()
                } else {
                    if (SystemClock.elapsedRealtime() - mRequestTime < 300) {
                        val sb = StringBuilder()
                        mPermissions!!
                                .filterNot { checkPermission(activity, it) }
                                .forEach {
                                    sb.append(" [")
                                            .append(it.permissionCh)
                                            .append("] ")
                                }
                        openSettingActivity("需要" + sb.toString() + "权限,前往开启?")
                    } else {
                        mResult!!.onFailed()
                    }
                }
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == REQUEST_CODE) {
                if (mResult != null && mPermissions != null) {
                    var result = true
                    for (mPermission in mPermissions!!) {
                        if (!checkPermission(activity, mPermission)) {
                            result = false
                            break
                        }
                    }
                    if (result) {
                        detach()
                        mResult!!.onSuccess()
                    } else {
                        mResult!!.onFailed()
                    }
                }
            }
        }

        //解绑fragment
        private fun detach() {
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.detach(this)
            fragmentTransaction.remove(this)
            fragmentTransaction.commitAllowingStateLoss()
        }

        /**
         * 打开应用权限设置界面
         */
        fun openSettingActivity(message: String) {
            showMessageOKCancel(message, DialogInterface.OnClickListener { dialog, which ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
                startActivityForResult(intent, REQUEST_CODE)
            }, DialogInterface.OnClickListener { _, _ -> mResult!!.onFailed() })
        }

        /**
         * 弹出对话框
         *
         * @param message    消息内容
         * @param okListener 点击回调
         */
        private fun showMessageOKCancel(message: String,
                                        okListener: DialogInterface.OnClickListener,
                                        cancelListener: DialogInterface.OnClickListener) {
            AlertDialog.Builder(activity)
                    .setMessage(message)
                    .setPositiveButton("确定", okListener)
                    .setNegativeButton("取消", cancelListener)
                    .create()
                    .show()
        }
    }

    private var mActivity: FragmentActivity? = activity
    private var mPermissions: Array<Permission>? = null

    /**
     * 获取请求权限实例
     *
     * @param activity FragmentActivity
     * @return 请求权限工具对象
     */
    fun getInstance(activity: FragmentActivity): PermissionUtil {
        return PermissionUtil(activity)
    }

    /**
     * 需要请求的权限列表
     *
     * @param permissions 权限列表
     * @return 返回自身链式编程
     */
    fun request(permissions: Array<Permission>): PermissionUtil {
        mPermissions = permissions
        return this
    }

    /**
     * 执行权限请求
     *
     * @param result 请求结果回调
     */
    fun execute(result: Result?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkPermissions()) {
            result?.onSuccess()
            return
        }
        //创建fragment回调
        val fragmentManager = mActivity?.supportFragmentManager
        val fragmentByTag = fragmentManager?.findFragmentByTag(tag)
        if (fragmentByTag != null) {
            mFragmentCallback = fragmentByTag as FragmentCallback
            mFragmentCallback!!.setResult(result)
        } else {
            mFragmentCallback = FragmentCallback()
            mFragmentCallback!!.setResult(result)
            fragmentManager!!
                    .beginTransaction()
                    .add(mFragmentCallback, tag)
                    .commit()
            fragmentManager.executePendingTransactions()
        }
        //开始请求
        requestPermission()
    }

    /**
     * 检查权限列表是否全部通过
     *
     * @return 权限列表是否全部通过
     */
    private fun checkPermissions(): Boolean {
        for (mPermission in mPermissions!!) {
            if (!checkPermission(mPermission)) {
                return false
            }
        }
        return true
    }

    /**
     * 检查权限
     *
     * @param permission 权限列表
     * @return 权限是否通过
     */
    private fun checkPermission(permission: Permission): Boolean {
        //检查权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        val checkSelfPermission = ContextCompat
                .checkSelfPermission(this!!.mActivity!!, permission.permission)
        return checkSelfPermission == PackageManager.PERMISSION_GRANTED
    }


    companion object {
        /**
         * 静态检查权限
         *
         * @param context    上下文
         * @param permission 权限列表
         * @return 权限是否通过
         */
        fun checkPermission(context: Context, permission: Permission): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return true
            }
            val checkSelfPermission = ContextCompat
                    .checkSelfPermission(context, permission.permission)
            return checkSelfPermission == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 申请权限
     */
    private fun requestPermission() {

        if (mActivity!!.supportFragmentManager.findFragmentByTag(tag) == null) {
            throw PermissionRequestException("一个权限申请工具类对象只能申请一次权限")
        }
        if (mFragmentCallback != null && mPermissions != null) {
            mFragmentCallback!!.setPermissions(mPermissions!!)
            //提取权限列表里面没通过的
            val per = arrayOfNulls<String>(mPermissions!!.size)
            val sb = StringBuilder()
            for (i in mPermissions!!.indices) {
                per[i] = mPermissions!![i].permission
                if (!checkPermission(mPermissions!![i])) {
                    sb.append(" [")
                            .append(mPermissions!![i].permissionCh)
                            .append("] ")
                }
            }
            //如果用户点了不提示(或者同时申请多个权限)，我们主动提示用户
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity!!, mPermissions!![0].permission)) {
                mFragmentCallback!!.openSettingActivity("需要" + sb.toString() + "权限,前往开启?")
            } else {
                //申请权限
                try {
                    mFragmentCallback!!.setRequestTime()
                    mFragmentCallback!!.requestPermissions(per, REQUEST_CODE)
                } catch (e: Exception) {
                    mFragmentCallback!!.openSettingActivity("需要" + sb.toString() + "权限,前往开启?")
                }

            }
        }
    }


    internal class PermissionRequestException(message: String) : RuntimeException(message)
}