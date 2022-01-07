package com.viatom.bloodoxygendemo.ext

import android.Manifest
import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.content.ContextCompat.getExternalFilesDirs
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
import com.jeremyliao.liveeventbus.LiveEventBus
import com.permissionx.guolindev.PermissionX
import com.viatom.bloodoxygendemo.R
import com.viatom.bloodoxygendemo.constants.Constant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File
import java.io.IOException
import java.util.*

/**
 * author: wujuan
 * created on: 2021/4/1 14:36
 * description:
 */


@ExperimentalCoroutinesApi
fun FragmentActivity.permissionNecessary() {

        PermissionX.init(this@permissionNecessary)
                .permissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                )
                .onExplainRequestReason { scope, deniedList ->
                    // 当请求被拒绝后，说明权限原因
                    scope.showRequestReasonDialog(
                        deniedList, getString(R.string.permission_location_reason), getString(
                            R.string.open
                        ), getString(R.string.ignore)
                    )


                }
                .onForwardToSettings { scope, deniedList ->
                    //选择了拒绝且不再询问的权限，去设置
                    scope.showForwardToSettingsDialog(
                        deniedList, getString(R.string.permission_location_setting), getString(
                            R.string.confirm
                        ), getString(R.string.ignore)
                    )
                }
                .request { allGranted, grantedList, deniedList ->
                    Log.e("权限", "$allGranted, $grantedList, $deniedList")
                    LiveEventBus.get(Constant.Event.permissionNecessary).post(allGranted)
                }

}


/**
 * 蓝牙功能是否已经启用
 * @receiver Context
 * @return Boolean
 */
fun Context.isEnableBluetooth(): Boolean{
    return if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) false else BluetoothAdapter.getDefaultAdapter()?.isEnabled ?: false
}

/**
 * 检查蓝牙是否开启，弹框提示到设置
 * @receiver Activity
 * @param requestCode Int
 * @param finishOnCancel Boolean
 * @return Boolean
 */
@JvmOverloads
fun Activity.checkBluetooth(requestCode: Int, finishOnCancel: Boolean = false): Boolean =
    BluetoothAdapter.getDefaultAdapter()?.let {
        return if(it.isEnabled) true else {
            MaterialDialog.Builder(this)
                .title(R.string.prompt)
                .content(R.string.permission_bluetooth)
                .negativeText(R.string.cancel)
                .onNegative(SingleButtonCallback { dialog: MaterialDialog, which: DialogAction? ->
                    dialog.dismiss()
                    if (finishOnCancel) finish()
                })
                .positiveText(R.string.open)
                .onPositive(SingleButtonCallback { dialog: MaterialDialog, which: DialogAction? ->
                    dialog.dismiss()
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(intent, requestCode)
                })
                .show()
            false
        }
    }?: false



/**
 * 获取主外部存储卷
 * @receiver Application
 * @return File?
 */
fun Application.getExtraDir(): File?{
    return if (Environment().isExternalStorageWritable()){
        val externalStorageVolumes: Array<out File> =
            getExternalFilesDirs(this, null)
        externalStorageVolumes[0]
    }else null

}

/**
 * 本项目使用专属存储空间
 * @receiver Context
 * @param filename String
 * @return File
 */
fun Context.getFile(filename: String): File?{
    return if (filename.isEmpty()) null else
        File(getExternalFilesDir(null), filename)
}


fun Context.createDir(path: String): Boolean{
   getFile(path).let {
       it?.let {
           if(!it.exists()) {
               it.mkdirs()
               Log.d("createDir success", it.absolutePath)
               return true
           }
           return true
       }?: return false

    }
    return false
}

@Throws(IOException::class)
fun Context.createFile(dir: String, filename: String): File?{
    try {
        "$dir/$filename".run {
            getFile(this)?.let {
                if(!it.exists()) {
                    if (createDir(dir)) {
                        it.createNewFile()
                        Log.d("createFile success", it.absolutePath)
                        return it
                    }
                    return null
                }
                return it
            }?: return null
        }
    }catch (e: IOException){
        e.printStackTrace()
        Log.e("createFile", "e")
        return null
    }

}


fun Context.deleteFile(filename: String) {
    // Get path for the file on external storage.  If external
    // storage is not currently mounted this will fail.
    File(getExternalFilesDir(null), filename).let {
        if (it.exists()) it.delete()
    }
}


fun Context.hasFile(filename: String): Boolean  = File(getExternalFilesDir(null), filename).exists()





/**
 * 获取屏幕真实宽高
 * @return
 */
fun Activity.screenSize(): IntArray {
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = windowManager.defaultDisplay
    val dm = DisplayMetrics()
    try {
        display.getRealMetrics(dm)
        return intArrayOf(dm.widthPixels, dm.heightPixels)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return IntArray(2)
}



fun Context.convertDpToPixel(dp: Float): Float {
    return dp * (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Context.convertPixelsToDp(px: Float): Float {
    return px / (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

