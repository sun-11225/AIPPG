package com.viatom.bloodoxygendemo

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.OxyBleResponse
import com.lepu.blepro.ble.data.LepuDevice
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import com.permissionx.guolindev.PermissionX
import com.viatom.bloodoxygendemo.ble.CollectUtil
import com.viatom.bloodoxygendemo.ble.DataController
import com.viatom.bloodoxygendemo.ble.DataController.releaseAll
import com.viatom.bloodoxygendemo.ble.LpBleUtil
import com.viatom.bloodoxygendemo.constants.Constant
import com.viatom.bloodoxygendemo.constants.Constant.BluetoothConfig.Companion.CHECK_BLE_REQUEST_CODE
import com.viatom.bloodoxygendemo.constants.Constant.BluetoothConfig.Companion.SUPPORT_MODEL
import com.viatom.bloodoxygendemo.databinding.ActivityMainBinding
import com.viatom.bloodoxygendemo.ext.checkBluetooth
import com.viatom.bloodoxygendemo.ext.createDir
import com.viatom.bloodoxygendemo.fragment.DashboardFragment
import com.viatom.bloodoxygendemo.fragment.HistoryFragment
import com.viatom.bloodoxygendemo.repository.Repository
import com.viatom.bloodoxygendemo.viewmodel.ConnectViewModel
import com.viatom.bloodoxygendemo.viewmodel.MainViewModel
import java.io.File
import android.os.Environment
import android.widget.Toast
import com.viatom.bloodoxygendemo.data.entity.DeviceEntity
import com.viatom.bloodoxygendemo.data.entity.UserEntity
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.security.AccessController.getContext


class MainActivity : AppCompatActivity(), BleChangeObserver {

    lateinit var binding: ActivityMainBinding
    lateinit var historyFragment: HistoryFragment
    lateinit var dashboardFragment: DashboardFragment
    private var previousMenu: MenuItem? = null
    var navItemClick: Boolean = false
    private val viewModel: MainViewModel by viewModels()
    private lateinit var dialog: ProgressDialog
    private var connectNumber: Int =0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        Log.d(TAG, "onCreate ${intent.extras.toString()}")
        val userEntity = UserEntity(1001, "小明", "188", "78", "1992-9-23", "男")
        viewModel.saveUser(application, userEntity)
        initNavigation()
        subscribeUi()
        initLiveEvent()
        initPermission()
        // 跳转传入UserEntity， userId 应为医汇通平台的id
//        intent.getParcelableExtra<UserEntity>("userEntity")?.let {
//            Log.d(TAG, "$it")
//            Toast.makeText(this,"登陆成功",Toast.LENGTH_SHORT).show()
//            // 将用户更新到db 及viewModel
//            viewModel.saveUser(application, it)
//
//        initNavigation()
//        subscribeUi()
//        initLiveEvent()
//        initPermission()
//    }?: run{
//        Toast.makeText(this, "缺少用户信息", Toast.LENGTH_SHORT).show()
//    }

        //测试
//        Log.d(TAG,"start test")
//        val path = copyAssetGetFilePath("pc60_fs50.txt")
//        Repository.uploadFile(File(path),0,0)
    }

    private fun initPermission() {
        PermissionX.init(this).permissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).onExplainRequestReason { scope, deniedList ->
            // 当请求被拒绝后，说明权限原因
            scope.showRequestReasonDialog(
                deniedList, getString(R.string.permission_location_reason), getString(
                    R.string.open
                ), getString(R.string.ignore)
            )
        }.onForwardToSettings { scope, deniedList ->
            //选择了拒绝且不再询问的权限，去设置
            scope.showForwardToSettingsDialog(
                deniedList, getString(R.string.permission_location_setting), getString(
                    R.string.confirm
                ), getString(R.string.ignore)
            )
        }.request { allGranted, grantedList, deniedList ->
            Log.e("权限", "$allGranted, $grantedList, $deniedList")
//                LiveEventBus.get(Constant.Event.permissionNecessary).post(true)

            //权限OK, 检查蓝牙状态
            if (allGranted) {
                checkBluetooth(CHECK_BLE_REQUEST_CODE).let {
                    viewModel.bleEnableChanged()
                }
            }
        }
    }

    private fun initLiveEvent() {

        // 当BleService onServiceConnected执行后发出通知 蓝牙sdk 初始化完成
        LiveEventBus.get(EventMsgConst.Ble.EventServiceConnectedAndInterfaceInit).observe(
            this, {
                Constant.BluetoothConfig.bleSdkEnable = true
                Log.d(TAG, "afterLpBleInit")
                afterLpBleInit()
            })

        //同步时间
        LiveEventBus.get(InterfaceEvent.Oxy.EventOxySyncDeviceInfo).observe(this) {
            Log.d(TAG,"sync time after get info")
            LpBleUtil.getInfo(SUPPORT_MODEL)
        }

        //获取设备信息
        LiveEventBus.get(InterfaceEvent.Oxy.EventOxyInfo).observe(this) { event ->
                event as InterfaceEvent
                Log.d(TAG, "currentDevice init")
                viewModel.toConnectDevice.let {

                    //根目录下创建设备名文件夹
                    viewModel.toConnectDevice.value?.device?.let { b ->
                        b.name?.let {
                            val createDir = createDir(it)
                            Log.d(TAG, "create dir $createDir")
                        }
                        //保存设备
                        viewModel.getDevice(application, b, event.data as OxyBleResponse.OxyInfo)

                        //加载所有数据
//                        b.name.let { name ->
//                            CollectUtil.getInstance(application.applicationContext).getDataAll(
//                                name
//                            )
//                        }

//                    })
                        viewModel.queryUser(application, 1001)

                        //置空关闭弹框
                        viewModel.toConnectDeviceChanged(null)
                    }
                }
                //开启实时任务
                if (LpBleUtil.isRtStop(SUPPORT_MODEL)) LpBleUtil.startRtTask(SUPPORT_MODEL)
        }

        //采集服务已经初始化成功, 自动采集
        LiveEventBus.get(Constant.Event.collectServiceConnected).observe(this, {
//            Log.d(TAG, "run auto collect")
//            viewModel.runAutoCollect(application)

        })

    }

    private fun copyAssetGetFilePath(fileName: String): String {
        try {
            val cacheDir: File = this.cacheDir
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val outFile = File(cacheDir, fileName)
            if (!outFile.exists()) {
                val res = outFile.createNewFile()
                if (!res) {
                    return ""
                }
            } else {
                if (outFile.length() > 10) { //表示已经写入一次
                    return outFile.path
                }
            }
            val `is`: InputStream = this.assets.open(fileName)
            val fos = FileOutputStream(outFile)
            val buffer = ByteArray(1024)
            var byteCount: Int
            while (`is`.read(buffer).also { byteCount = it } != -1) {
                fos.write(buffer, 0, byteCount)
            }
            fos.flush()
            `is`.close()
            fos.close()
            return outFile.path
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun subscribeUi() {
        //蓝牙状态
        viewModel.bleEnable.observe(this) { enabled ->
            if (enabled) {
                if (Constant.BluetoothConfig.bleSdkEnable) {
                    Log.d(TAG, " ble sdk enable afterLpBleInit")
                    afterLpBleInit()
                } else {
                    viewModel.initBle(application)
                }
            }
        }

////      查询当前设备
//        viewModel.curBluetooth.observe(this, { device ->
//
//            Log.e(TAG, "加载设备已存在的数据")
////                LpBleUtil.reconnect(SUPPORT_MODEL, device.deviceName)
////                LpBleUtil.startRtTask(SUPPORT_MODEL)
//
//            // 读取本地最近保存的设备
////            viewModel.getCurrentDevice(application)
//            //加载所有数据
////            CollectUtil.getInstance(application.applicationContext).getDataAll(device.deviceName)
//
//        })

        //连接中的蓝牙对象
        viewModel.toConnectDevice.observe(this) { device ->
            device?.let {
                //正在连接
                Log.d(TAG, "device:  ${device.name}")

                showConnecting(device)
            } ?: hideConnecting()
        }
    }

    //初始化导航页面
    private fun initNavigation() {
        historyFragment = HistoryFragment()
        dashboardFragment = DashboardFragment()

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                if (previousMenu != null) {
                    previousMenu?.isChecked = false
                } else {
                    binding.navView.menu.getItem(0).isChecked = true
                }
                binding.navView.menu.getItem(position).isChecked = true
                previousMenu = binding.navView.menu.getItem(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        binding.viewPager.adapter = (object :
            FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

            override fun getItem(position: Int): Fragment {
                return when (position) {
                    0 -> dashboardFragment
                    1 -> historyFragment
                    else -> dashboardFragment
                }
            }

            override fun getCount(): Int {
                return 2
            }
        })

        binding.navView.setOnNavigationItemSelectedListener { item ->
            if (previousMenu == item) {
                return@setOnNavigationItemSelectedListener false
            }
            previousMenu = item
            navItemClick = true
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    binding.viewPager.currentItem = 0
//                    historyFragment.updateRecordMenuState()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_history -> {
                    binding.viewPager.currentItem = 1
                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }

        }
        binding.viewPager.currentItem = 0
        binding.viewPager.offscreenPageLimit = 2
    }

    private fun afterLpBleInit() {
        lifecycle.addObserver(
            BIOL(
                this,
                intArrayOf(SUPPORT_MODEL)
            )
        ) // ble service 初始完成后添加订阅才有效

        // 读取本地最近保存的设备
        viewModel.getCurrentDevice(application)

        //初始化自动采集服务
        CollectUtil.getInstance(application).initService()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHECK_BLE_REQUEST_CODE) {
            //重启蓝牙权限后
            LpBleUtil.reInitBle()
            viewModel.bleEnableChanged()
        }
    }

    /**
     * 监听蓝牙状态
     *
     * @param model Int 设备model
     * @param state Int 蓝牙状态
     */
    override fun onBleStateChanged(model: Int, state: Int) {
        viewModel.connectStateChanged(state)

        when (state) {
            LpBleUtil.State.DISCONNECTED -> {
                Log.d(TAG,"蓝牙断开连接")
                LpBleUtil.stopRtTask(SUPPORT_MODEL)
//                viewModel.resetDashboard()

                //app 运行时如果断开 去重连。
//                if (LpBleUtil.isAutoConnect(SUPPORT_MODEL)) { //默认自动重连开启
//                    viewModel.curBluetooth.value?.deviceName?.let {
//                        Log.d(TAG,"重新连接")
//                        LpBleUtil.reconnect(
//                            SUPPORT_MODEL,
//                            it
//                        )
//                    }
//                }

            }
            LpBleUtil.State.CONNECTED -> {
                Log.d(TAG, "ble connected")

                // 读取本地最近保存的设备
//                viewModel.getCurrentDevice(application)

                //关闭弹框
//                hideConnecting()
//                if (connectNumber > 0) {
//                    if (LpBleUtil.isRtStop(SUPPORT_MODEL)) LpBleUtil.startRtTask(SUPPORT_MODEL)
//                    Log.d(Companion.TAG, "start realtime task")
//                }
            }
        }
    }

    private fun showConnecting(b: Bluetooth) {
        if (!this::dialog.isInitialized)
            dialog = ProgressDialog(this)
        dialog.setMessage("正在连接 ${b.name}...")
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun hideConnecting() {
        if (this::dialog.isInitialized) dialog.dismiss()
    }

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onDestroy() {
        super.onDestroy()

        LpBleUtil.stopRtTask(SUPPORT_MODEL)
        LpBleUtil.disconnect(false)

        Constant.releaseAll()
        DataController.releaseAll()
        CollectUtil.getInstance(application).releaseAll()

    }
}