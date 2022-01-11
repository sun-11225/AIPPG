package com.viatom.bloodoxygendemo.viewmodel

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.lepu.blepro.ble.cmd.OxyBleResponse
import com.viatom.bloodoxygendemo.MyApp
import com.viatom.bloodoxygendemo.ble.BatteryInfo
import com.viatom.bloodoxygendemo.ble.CollectUtil
import com.viatom.bloodoxygendemo.ble.LpBleUtil
import com.viatom.bloodoxygendemo.constants.Constant
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.collectSwitch
import com.viatom.lib.vihealth.update.dialog.DialogHelper
import kotlin.coroutines.coroutineContext

/**
 * @author：created by SunHao
 * 创建时间：2021/11/18 14:43
 * 邮箱：sunhao@viatomtech.com
 * 类说明:
 */
class DashboardViewModel : ViewModel() {

    private val _runState = MutableLiveData<Int>().apply {
        value = Constant.RunState.NONE
    }
    val runState : LiveData<Int>
    get() = _runState


    private val _battery = MutableLiveData<BatteryInfo>().apply {
        value = null
    }
    val battery : LiveData<BatteryInfo>
    get() = _battery

    fun batteryChanged(info: BatteryInfo){
        _battery.value = info
    }

    private val _overTime = MutableLiveData<Boolean>().apply {
        value = false
    }
    val overTime : LiveData<Boolean>
    get() = _overTime

    fun overTimeChanged(b : Boolean){
        _overTime.value = b
    }

    private val _spo2 = MutableLiveData<Int>().apply {
        value = 0
    }
    val spo2 : LiveData<Int>
    get() = _spo2

    fun spo2Changed(c : Int){
        _spo2.value = c
    }

    private val _pr = MutableLiveData<Int>().apply {
        value = 0
    }
    val pr : LiveData<Int>
    get() = _pr

    fun hrChanged(c : Int){
        _pr.value = c
    }

    private val _isSignalPoor = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isSignalPoor : LiveData<Boolean>
    get() = _isSignalPoor



     val _collectBtnText = MutableLiveData<String>().apply {
        value = "采集"
    }
    var collectBtnText : LiveData<String> = _collectBtnText


    fun collectBtnTextChanged(s : String){
        _collectBtnText.value = s
    }

    // draw
    val dataSrc: MutableLiveData<IntArray> by lazy {
        MutableLiveData<IntArray>()
    }


    /**
     *  实时任务时导联是否正常
     */
     private val _fingerState = MutableLiveData<String>().apply {
        value = ""
    }
    val fingerState : LiveData<String>
    get() = _fingerState

    fun fingerStateChanged(state : String){
        _fingerState.value = state
    }

    /**
     * 实时数据池添加数据
     * @param data RtData
     */
    @ExperimentalUnsignedTypes
    fun feedWaveData(data: OxyBleResponse.RtWave, collectUtil: CollectUtil){
//         data.wFs?.let {
//        }
        data.wByte?.let {
//            Log.d("collect", "去添加实时数据")
            collectData(collectUtil, it) //分析的数据不滤波
        }
    }

    private fun collectData(collectUtil: CollectUtil, data: ByteArray){
        Log.e("collect", "temp == ${data.joinToString() }")//${Constant.BluetoothConfig.currentRunState}

        if (data.isEmpty()){
            Log.d("demo","实时数据为空，停止实时任务")
            LpBleUtil.stopRtTask(Constant.BluetoothConfig.SUPPORT_MODEL)
            collectUtil.manualCounting = false
            collectSwitch = false
            Constant.Collection.age = ""
            Constant.Collection.gender = ""
            _collectBtnText.postValue("采集")
            Toast.makeText(MyApp.context,"采集数据异常，请重新采集！",Toast.LENGTH_LONG).show()
        }

        if (collectUtil.manualCounting) {
            if (data.isEmpty()) {
                collectUtil.tempValueManual = true
            }

        }

        if (collectUtil.autoCounting){
            if (data.isEmpty()) {
                collectUtil.tempValueAuto = true
            }

        }

        collectUtil.run {
            if (manualCounting) {

                this.actionCollectManual(data)
            }
            if (this.autoCounting ) {
                this.actionCollectAuto(data)

            }

        }
    }


    enum class AgType(value: Int){
        AGE(0),
        GENDER(1)
    }
    /**
     * 设置年龄性别弹框
     *
     * @param activity Activity
     * @param listener Function1<[@kotlin.ParameterName] String, Unit>
     */
    fun showAgeAndGenderDialog(activity: Activity,dialog: Dialog,type: AgType, listener: (String) -> Unit){
        closeHintKeyBoard(activity)
        val list = ArrayList<String>()
        var defaultSelectIndex = 0//默认选中项
        //kg
        when (type) {
            AgType.AGE -> {
                for (i in 18 until 99) {
                    list.add(i.toString())
                }
            }
            AgType.GENDER -> {
                list.add("男")
                list.add("女")
            }

        }

        val pvCustomOptions = OptionsPickerBuilder(activity) { options1, options2, options3, v ->
//            textView.text = list[options1]
            listener.invoke(list[options1].toString())
        }
            .setCancelText("取消")//取消按钮文字
            .setSubmitText("确定")//确认按钮文字
            .setSubmitColor(Color.parseColor("#37a5db"))//确定按钮文字颜色
            .setCancelColor(Color.parseColor("#37a5db"))//取消按钮文字颜色
            .setContentTextSize(16)
            .setLineSpacingMultiplier(2f)
            .setSelectOptions(defaultSelectIndex)
            .isDialog(true)
            .setDecorView(dialog.window?.decorView as ViewGroup)
            .build<String>()
            pvCustomOptions.dialog.window?.setGravity(Gravity.BOTTOM)
            pvCustomOptions.dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT)
        pvCustomOptions.setPicker(list)//添加数据
        pvCustomOptions.show()

    }

    /**
     * 关闭软键盘
     * getCurrentFocus()是拿到已经获得焦点的view
     * getCurrentFocus().getWindowToken()是拿到window上的token
     */
    fun closeHintKeyBoard(activity: Activity) {
        //拿到InputMethodManager
        val manager: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        //如果window上view获取焦点 && view不为空
        if (manager.isActive && null != activity.currentFocus) {
            //拿到view的token 不为空
            if (null != activity.currentFocus!!.windowToken) {
                //表示软键盘窗口总是隐藏，除非开始时以SHOW_FORCED显示。
                manager.hideSoftInputFromWindow(
                    activity.currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        }
    }
}