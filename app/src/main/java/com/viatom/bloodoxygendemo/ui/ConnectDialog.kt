package com.viatom.bloodoxygendemo.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.objs.BluetoothController
import com.viatom.bloodoxygendemo.adapter.ConnectAdapter
import com.viatom.bloodoxygendemo.R
import com.viatom.bloodoxygendemo.ble.LpBleUtil
import com.viatom.bloodoxygendemo.constants.Constant
import com.viatom.bloodoxygendemo.constants.Constant.BluetoothConfig.Companion.SUPPORT_MODEL
import com.viatom.bloodoxygendemo.databinding.ConnectDialogBinding
import com.viatom.bloodoxygendemo.ext.convertDpToPixel
import com.viatom.bloodoxygendemo.ext.screenSize
import com.viatom.bloodoxygendemo.viewmodel.ConnectViewModel
import com.viatom.bloodoxygendemo.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * @author：created by SunHao
 * 创建时间：2021/11/18 14:47
 * 邮箱：sunhao@viatomtech.com
 * 类说明: 蓝牙连接界面
 */
class ConnectDialog : DialogFragment() {

    private lateinit var binding: ConnectDialogBinding

    private val mainVM: MainViewModel by activityViewModels()

    private val connectVM: ConnectViewModel by activityViewModels()

    private lateinit var adapter: ConnectAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.connect_dialog, container, false)
        binding.lifecycleOwner = this
        binding.ctx = this

        activity?.let { fragmentActivity ->
            val width = fragmentActivity.screenSize()[0]

            dialog?.let { d ->
                d.requestWindowFeature(Window.FEATURE_NO_TITLE)

                d.window?.let {
                    it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    it.decorView.setBackgroundColor(Color.TRANSPARENT)
                    it.setDimAmount(0.6f)

                    // 设置宽度
                    val params: WindowManager.LayoutParams = it.attributes

                    params.width = (width - requireContext().convertDpToPixel(32f)).toInt()
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT
                    params.gravity = Gravity.CENTER_HORIZONTAL
                    it.attributes = params
                }
                d.setCanceledOnTouchOutside(true)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initLiveEvent()
        subscribeUi()

    }

    private fun subscribeUi() {
        //蓝牙状态
        mainVM.bleEnable.observe(this) {
            //ble状态可用即开始扫描蓝牙
            if (Constant.BluetoothConfig.bleSdkEnable && it) {
                connectVM.scanChanged()
            }
        }

        //扫描状态
        connectVM.scanning.observe(this) {
            if (it) {
                startScan()
                binding.deviceList.smoothScrollToPosition(0)
            } else {
                Log.d(TAG, "stop scan")
                LpBleUtil.stopScan()
            }
        }

        //连接状态
        mainVM.connectState.observe(this) {
            binding.connectedState.run {
                this.text = LpBleUtil.convertState(it)
            }
        }

        connectVM.connectedBle.observe(this){
            binding.connectedName.run{
                this.text = connectVM.connectedBle.value
            }
        }
    }

    private fun startScan() {
        Log.d(TAG, "start scan")
        //清空
        BluetoothController.clear()
        adapter.setNewInstance(null)
        adapter.notifyDataSetChanged()

        //重新扫描
        LpBleUtil.startScan(SUPPORT_MODEL)
        // 10s后停止扫描
        lifecycleScope.launch {
            delay(10000)
            Log.d(TAG,"stopScan")
            connectVM.scanChanged(false)
        }
    }

    private fun initView() {
        LinearLayoutManager(context).apply {
            this.orientation = LinearLayoutManager.VERTICAL
            binding.deviceList.layoutManager = this
        }

        adapter = ConnectAdapter(R.layout.connect_device_item, null).apply {
            this.setOnItemClickListener { _, _, position ->
                if (LpBleUtil.isBleConnected(SUPPORT_MODEL)) {
                    //断开蓝牙连接
                    LpBleUtil.disconnect(false)
                }
                this.data[position].let { device ->
                    mainVM.toConnectDeviceChanged(device)
                    connectVM.connectedBleChanged(device.name)
                    //蓝牙连接
                    LpBleUtil.connect(requireContext(), device)
                }
            }
            //设置适配器
            binding.deviceList.adapter = this
        }

        binding.imageBack.setOnClickListener {
            dismiss()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initLiveEvent() {
        //扫描通知
        LiveEventBus.get(EventMsgConst.Discovery.EventDeviceFound)
            .observe(this) {
                //填充数据
                adapter.setNewInstance(BluetoothController.getDevices(SUPPORT_MODEL))
                adapter.notifyDataSetChanged()

            }
    }

    fun reconnect() {

    }

    fun refresh() {
        connectVM.scanChanged()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, " onDestroyView stop scan")
        LpBleUtil.stopScan()
    }

    companion object {
        private const val TAG = "ConnectDialog"
    }


}