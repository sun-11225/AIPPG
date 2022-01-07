package com.viatom.bloodoxygendemo.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.Er1BleResponse
import com.lepu.blepro.ble.cmd.OxyBleResponse
import com.lepu.blepro.ble.data.OxyDataController
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.viatom.bloodoxygendemo.R
import com.viatom.bloodoxygendemo.ble.BatteryInfo
import com.viatom.bloodoxygendemo.ble.CollectUtil
import com.viatom.bloodoxygendemo.ble.LpBleUtil
import com.viatom.bloodoxygendemo.constants.Constant
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.age
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.collectSwitch
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.gender
import com.viatom.bloodoxygendemo.databinding.FragmentDashboardBinding
import com.viatom.bloodoxygendemo.ui.ConnectDialog
import com.viatom.bloodoxygendemo.viewmodel.DashboardViewModel
import com.viatom.bloodoxygendemo.viewmodel.MainViewModel
import com.viatom.bloodoxygendemo.views.OxyView
import com.viatom.lib.vihealth.update.dialog.DialogHelper

import kotlinx.coroutines.launch
import org.w3c.dom.Text
import kotlin.math.floor

/**
 * @author：created by SunHao
 * 创建时间：2021/11/18 10:47
 * 邮箱：sunhao@viatomtech.com
 * 类说明: 实时页面
 */
class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
//    private val viewModel : DashboardViewModel by lazy { ViewModelProvider(this).get(DashboardViewModel::class.java) }
    private val viewModel : DashboardViewModel by activityViewModels()

    private val mainVM: MainViewModel by activityViewModels()


    lateinit var collectUtil: CollectUtil

    private lateinit var oxyView: OxyView



    /**
     * rt wave
     */
    private val waveHandler = Handler()

    inner class WaveTask : Runnable {
        override fun run() {
            if (!runWave) {
                return
            }

            val interval: Int = if (OxyDataController.dataRec.size > 250) {
                30
            } else if (OxyDataController.dataRec.size > 150) {
                35
            } else if (OxyDataController.dataRec.size > 75) {
                40
            } else {
                45
            }

            waveHandler.postDelayed(this, interval.toLong())
//            Log.d(TAG,"DataRec: ${OxyDataController.dataRec.size}, delayed $interval")

            val temp = OxyDataController.draw(5)
//            model.dataSrc.value = OxyDataController.feed(model.dataSrc.value, temp)
            viewModel.dataSrc.value = OxyDataController.feed(viewModel.dataSrc.value, temp)
        }
    }

    private var runWave = false
    private fun startWave() {
        if (runWave) {
            return
        }
        runWave = true
        waveHandler.post(WaveTask())
    }

    private fun stopWave() {
        runWave = false
        OxyDataController.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        collectUtil = CollectUtil.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false)
        binding.lifecycleOwner = this
        binding.ctx = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLiveEvent()
        initView()
        subscribeUi()

    }


    @SuppressLint("SetTextI18n")
    private fun subscribeUi() {
        mainVM.connectState.observe(viewLifecycleOwner, {
//            if (mainVM.connectState.value == Ble.State.CONNECTED){
//                Log.d(TAG,"start wave")
//                oxyView.visibility = View.VISIBLE
//                startWave()
//            }
        })

        //实时状态切换 更新UI
//        viewModel.runState.observe(viewLifecycleOwner, {
//            binding.battery.visibility = if (it in RunState.NONE..RunState.OFFLINE) View.INVISIBLE else View.VISIBLE
//            binding.hr.visibility = if (it in RunState.NONE..RunState.PREPARING_TEST || it in RunState.SAVE_FAILED..RunState.LEAD_OFF) View.INVISIBLE else View.VISIBLE
//            binding.bpmText.visibility = if (it in RunState.NONE..RunState.PREPARING_TEST || it in RunState.SAVE_FAILED..RunState.LEAD_OFF) View.INVISIBLE else View.VISIBLE
//            binding.bpmImg.visibility = if (it in RunState.NONE..RunState.PREPARING_TEST || it in RunState.SAVE_FAILED..RunState.LEAD_OFF) View.INVISIBLE else View.VISIBLE
//
//
//        })

        // 是否超过最大测量时长
        viewModel.overTime.observe(viewLifecycleOwner, {

            Log.d("dashboard overtime", "$it")
            if (it)
                MaterialDialog.Builder(requireContext())
                    .content(R.string.measure_overtime)
                    .positiveText(R.string.i_know)
                    .show()
        })

          //电池UI
        viewModel.battery.observe(viewLifecycleOwner, {

            it?.let {
                binding.battery.run {
                    Log.d(TAG," state: ${it.state}  percent: ${it.percent}")
                    this.visibility = View.VISIBLE
                    this.setState(it.state)
                    this.power = it.percent
                }

            }
        })

        viewModel.spo2.observe(viewLifecycleOwner, { h ->
            (h < 30 || h > 250).let {
                binding.spo2.text = if (it) "--" else h.toString()
            }

        })

        viewModel.pr.observe(viewLifecycleOwner){ h->
            (h < 30 || h > 250).let {
                binding.tvPr.text = if (it) "--" else h.toString()
            }
        }


        viewModel.collectBtnText.observe(viewLifecycleOwner, {
            //更新采集按钮UI
            binding.collection.run {
                text = it
                background = if (it == getString(R.string.collection)) resources.getDrawable(R.drawable.public_shape_white_corner_28) else resources.getDrawable(R.drawable.public_shape_black_corner_28)
                setTextColor(if (it == getString(R.string.collection)) resources.getColor(R.color.color_363636) else resources.getColor(R.color.white))
            }

        })

//        //导联状态
//        viewModel.fingerState.observe(viewLifecycleOwner){
//            Log.d("demo","导联状态")
//            if (it != "1"){
//                LpBleUtil.stopRtTask(Constant.BluetoothConfig.SUPPORT_MODEL)
//            }
//        }

    }

    @ExperimentalUnsignedTypes
    private fun initLiveEvent() {
        LiveEventBus.get(InterfaceEvent.Oxy.EventOxyRtData).observe(this, { event ->
            Log.d(TAG, "get(InterfaceEvent.Oxy.EventOxyRtData)")
            (event as InterfaceEvent).let {
                (it.data as OxyBleResponse.RtWave).let { data ->

                    data.let { param ->

                        //运行状态
//                        param.getRunState().run {
//                            viewModel._runState.value = this

                            //判断状态是否切换了

                            Constant.BluetoothConfig.currentRunState.let {

//                                Log.d(TAG, "currentRunState = $this,lastState = $it")
//                                if (this != it) {
//                                    Log.d(TAG, "currentRunState = $this,lastState = $it--------------切换")

//                                    ecgView.clear()
//                                    ecgView.invalidate()

//                                    viewModel._fingerState.value = this in Constant.RunState.PREPARING_TEST..Constant.RunState.RECORDING
//                                }
                            }

                            //更新记录为最新的状态
//                            Constant.BluetoothConfig.currentRunState = this

            //                            if((this !in RunState.PREPARING_TEST..RunState.RECORDING) && (watchTimer != null || waveTimer != null)){
            //                                ecgView.clear()
            //                                ecgView.invalidate()
            //                                stopTimer()
            //                            }

//                            Log.d(TAG, " runState $this")
//                        }


                        //电池
                        BatteryInfo(param.batteryState.toInt(), param.battery).run {
//                            viewModel._battery.value = this
                            viewModel.batteryChanged(this)
                            Log.d(TAG, "battery $this")
                        }

                        //最大测量时长
//                        viewModel._overTime.value = param.recordTime >= TimeUnit.DAYS.toSeconds(1)
//                        viewModel.overTimeChanged(param.)
                        //pr
                        viewModel.hrChanged(param.pr)
//                        viewModel._hr.value = param.pr
//                        Log.d(TAG, "hr  ${param.pr}")
                        viewModel.spo2Changed(param.spo2)
                        //导联状态
                        viewModel.fingerStateChanged(param.state)
//                        viewModel._fingerState.value = param.state
                        Log.d(TAG,"load state" + param.state)
//                        Log.d(TAG, "spo2  ${param.spo2}")

                        //心电信号
//                        viewModel._isSignalPoor.value = param.isSignalPoor()

                        //wave data
                        viewModel.feedWaveData(data, collectUtil)

                    }

                }

            }

        })

        //当开始实时任务
        LiveEventBus.get(EventMsgConst.RealTime.EventRealTimeStart).observe(viewLifecycleOwner){
            Log.d(TAG,"start wave")
            oxyView.visibility = View.VISIBLE
            startWave()
            binding.tvWarmPrompt.visibility = View.VISIBLE
        }

        // 当停止实时任务
        LiveEventBus.get(EventMsgConst.RealTime.EventRealTimeStop).observe(viewLifecycleOwner, {

            Log.d(TAG,"stop wave")
            oxyView.visibility = View.GONE
            stopWave()

        })

        //保存及分析流程成功
        LiveEventBus.get(Constant.Event.analysisProcessSuccess).observe(viewLifecycleOwner, {
            it?.let {
                if ((it as String).isNotEmpty())
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }


//            viewModel._collectBtnText.value = getString(R.string.collection)
            viewModel.collectBtnTextChanged(getString(R.string.collection))
//            binding.report.isVisible = true

        })

        //保存及分析流程失败
        LiveEventBus.get(Constant.Event.analysisProcessFailed).observe(viewLifecycleOwner, {
            it?.let {
                if ((it as String).isNotEmpty())
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }

//            viewModel._collectBtnText.value = getString(R.string.collection)
            viewModel.collectBtnTextChanged(getString(R.string.collection))
//            binding.report.isVisible = true


        })

        viewModel.dataSrc.observe(viewLifecycleOwner) {
            if (this::oxyView.isInitialized) {
//                Log.d(TAG,"draw view ${it.joinToString()}")
                oxyView.setDataSrc(it)
                oxyView.invalidate()
            }
        }

    }

    private fun initView() {
        binding.oxiView.post {
            initOxyView()
        }

        binding.battery.visibility = View.GONE
    }

    private fun initOxyView() {
        Log.d(TAG,"initOxyView")
        // cal screen
        val dm =resources.displayMetrics
        val index = floor(binding.oxiView.width / dm.xdpi * 25.4 / 25 * 125).toInt()
        OxyDataController.maxIndex = index

        val mm2px = 25.4f / dm.xdpi
        OxyDataController.mm2px = mm2px

//        Log.d(TAG,"max index: $index" + "mm2px: $mm2px")

        binding.oxiView.measure(0, 0)
        oxyView = OxyView(context)
        binding.oxiView.addView(oxyView)

        viewModel.dataSrc.value = OxyDataController.iniDataSrc(index)

        oxyView.visibility = View.GONE
    }

    //点击事件
    fun showDialog(){
        if (!Constant.BluetoothConfig.bleSdkEnable) {
            Toast.makeText(requireContext(), "初始化中，请稍候再试！", Toast.LENGTH_SHORT).show()
            return
        }
        activity?.supportFragmentManager?.let { ConnectDialog().show(it, "show") }
    }

    fun manualCollect() {
//        collectSwitch = !collectSwitch

        if (mainVM.connectState.value != LpBleUtil.State.CONNECTED) {
            Toast.makeText(requireContext(), "蓝牙未连接，无法采集", Toast.LENGTH_SHORT).show()
            return
        }
        if (viewModel.fingerState.value != "1") {
            Toast.makeText(requireContext(), "导联断开， 无法采集", Toast.LENGTH_SHORT).show()
            return
        }
//        if (Constant.BluetoothConfig.currentRunState != Constant.RunState.RECORDING) {
//            Toast.makeText(requireContext(), "不在测量中， 无法采集", Toast.LENGTH_SHORT).show()
//            return
//        }
//        if (activity?.let { collectUtil.manualCounting } == true) {
//            Toast.makeText(requireContext(), "正在采集/分析中", Toast.LENGTH_SHORT).show()
//            return
//        }
        if (!collectUtil.checkService()) {
            Toast.makeText(requireContext(), "正在初始化采集服务", Toast.LENGTH_SHORT).show()
            return
        }

        if (viewModel._collectBtnText.value != "采集"){
            if ( viewModel._collectBtnText.value?.toInt()!! > 1){
//                collectSwitch = false
//                lifecycleScope.launch {
//                    context?.let { CollectUtil.getInstance(it).saveResult(viewModel, mainVM) }
//                }
                if (activity?.let { collectUtil.manualCounting } == true) {
                    Toast.makeText(requireContext(), "采集要求为五分钟", Toast.LENGTH_SHORT).show()
                    return
                }
            }
//            else {
//                if (activity?.let { collectUtil.manualCounting } == true) {
//                    Toast.makeText(requireContext(), "采集要求为五分钟", Toast.LENGTH_SHORT).show()
//                    return
//                }
//            }
        }else{
            collectSwitch = !collectSwitch
        }
//        collectSwitch = !collectSwitch
        if (collectSwitch) {
            Log.d("demo","dialog")
            val dialog = Dialog(requireActivity(),R.style.CustomDialogTheme)
            dialog.setContentView(R.layout.dialog_info)
            dialog.setCanceledOnTouchOutside(false)//点击外部不关闭
            dialog.setCancelable(false)//点击返回键不关闭
//            val dialog = DialogHelper.newInstance<View>(requireContext(), R.layout.dialog_info)
            val edAge = dialog.findViewById(R.id.info_age) as TextView
            val edMale = dialog.findViewById(R.id.info_gender) as TextView
            val btnOk = dialog.findViewById(R.id.info_btn_ok) as Button
            val btnClose = dialog.findViewById(R.id.info_btn_close) as Button
            dialog.show()
            edAge.setOnClickListener {
                activity?.let {
                    viewModel.showAgeAndGenderDialog(it, dialog,DashboardViewModel.AgType.AGE){ age ->
                        edAge.text = age
                        edAge.setTextColor(it.resources.getColor(R.color.colorBlack))
                        Constant.Collection.age = edAge.text.toString().trim()
                    }
                }
            }
            edMale.setOnClickListener {
                activity?.let {
                    viewModel.showAgeAndGenderDialog(it,dialog, DashboardViewModel.AgType.GENDER){ gender ->
                        edMale.text = gender
                        edMale.setTextColor(it.resources.getColor(R.color.colorBlack))
                        Constant.Collection.gender = if (edMale.text.toString().trim() == "女") {
                            "woman"
                        } else {
                            "man"
                        }
                    }
                }
            }
            btnClose.setOnClickListener{
                collectSwitch = false
                dialog.dismiss()
            }
            btnOk.setOnClickListener {
                    if (age.isNullOrEmpty() || gender.isNullOrEmpty()) {
                        Toast.makeText(context, "请输入年龄和性别后开始测量", Toast.LENGTH_SHORT).show()
                    } else {
                        lifecycleScope.launch {
                            activity?.let {
                                collectUtil.manualCollect(viewModel, mainVM)
                            }
                        }
                        dialog.dismiss()
                    }

            }
//            activity?.supportFragmentManager?.let { dialog.show(it, "showInfoDialog") }
//            lifecycleScope.launch {
//                activity?.let {
//                    collectUtil.manualCollect(viewModel, mainVM)
//                }
//            }
        }
    }

    companion object {
        private const val TAG = "DashboardFragment"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
    }


}
