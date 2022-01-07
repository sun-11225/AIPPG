package com.viatom.bloodoxygendemo.viewmodel

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.util.Log
import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lepu.blepro.ble.cmd.OxyBleResponse
import com.lepu.blepro.objs.Bluetooth
import com.viatom.bloodoxygendemo.BuildConfig
import com.viatom.bloodoxygendemo.ble.BleSO
import com.viatom.bloodoxygendemo.ble.CollectUtil
import com.viatom.bloodoxygendemo.ble.LpBleUtil
import com.viatom.bloodoxygendemo.constants.Constant
import com.viatom.bloodoxygendemo.constants.Constant.BluetoothConfig.Companion.SUPPORT_MODEL
import com.viatom.bloodoxygendemo.data.entity.DeviceEntity
import com.viatom.bloodoxygendemo.data.entity.UserEntity
import com.viatom.bloodoxygendemo.data.local.DBHelper
import com.viatom.bloodoxygendemo.ext.createDir
import com.viatom.bloodoxygendemo.util.doFailure
import com.viatom.bloodoxygendemo.util.doSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*

/**
 * @author：created by SunHao
 * 创建时间：2021/11/18 15:06
 * 邮箱：sunhao@viatomtech.com
 * 类说明:
 */

class MainViewModel : ViewModel() {

    /**
     * 蓝牙状态
     */
    private val _bleEnable = MutableLiveData<Boolean>().apply {
        value = false
    }

    val bleEnable: LiveData<Boolean>
        get() = _bleEnable

    fun bleEnableChanged(state : Boolean = true) {
        _bleEnable.value = state
    }

    /**
     * 连接过程中的蓝牙对象
     */
    private val _toConnectDevice = MutableLiveData<Bluetooth?>()

    val toConnectDevice: LiveData<Bluetooth?>
        get() = _toConnectDevice

    fun toConnectDeviceChanged(device: Bluetooth?){
        _toConnectDevice.value = device
    }

    /**
     * 当前蓝牙
     */
    private val _curBluetooth = MutableLiveData<DeviceEntity?>()

    val curBluetooth: LiveData<DeviceEntity?>
        get() = _curBluetooth

    fun curBluetoothChanged(device: DeviceEntity?){
        _curBluetooth.value = device
    }

    /**
     * 连接状态
     */
    private val _connectState = MutableLiveData<Int>().apply {
        value = LpBleUtil.State.DISCONNECTED
    }

    val connectState: LiveData<Int>
        get() = _connectState

    fun connectStateChanged(state: Int) {
        _connectState.value = state
    }

    /**
     * 当前用户
     */
    private val _currentUser = MutableLiveData<UserEntity?>()

    val currentUser: LiveData<UserEntity?>
    get() = _currentUser

    fun currentUserChanged(userEntity: UserEntity?){
        _currentUser.value = userEntity
    }


    /**
     * 初始化蓝牙服务
     * @param application Application
     */
    fun initBle(application: Application) {
        Log.d(Companion.TAG,"init ble")
        application.createDir(Constant.Dir.O2RingDir)

        LpBleUtil.getServiceHelper()
            .initLog(BuildConfig.DEBUG)
            .initRawFolder(SparseArray<String>().apply {
                this.put(SUPPORT_MODEL, Constant.Dir.O2RingDir)
            }) // 如需下载主机文件必须配置
            .initModelConfig(SparseArray<Int>().apply {
                this.put(SUPPORT_MODEL, SUPPORT_MODEL)
            }) // 配置要支持的设备
            .initService(
                application,
                BleSO.getInstance(application)
            ) //必须在initModelConfig initRawFolder之后调用
    }

    fun getCurrentDevice(application: Application){
        DBHelper.getInstance(application).let {
            viewModelScope.launch {
                it.getCurrentDevice()
                    .onStart {
                        Log.d(TAG, "开始查询当前设备")
                    }
                    .catch {
                        Log.d(TAG, "查询当前设备出错")
                    }
                    .onCompletion {
                        Log.d(TAG, "查询当前设备结束")

                    }
                    .collect { result ->
                        result.doFailure {
                            Log.d(TAG, "查询当前设备失败")
                        }
                        result.doSuccess {
                            Log.d(TAG, "查询当前设备成功${it}")
                            _curBluetooth.postValue(it)
                        }

                    }
            }

        }
    }

    @ExperimentalUnsignedTypes
    fun getDevice(application: Application, bluetooth: BluetoothDevice, info :OxyBleResponse.OxyInfo){
        DBHelper.getInstance(application).let {
            viewModelScope.launch {
                it.getDevice(bluetooth.name)
                    .onStart {
                        Log.d(TAG, "开始查询设备")
                    }
                    .catch {
                        Log.d(TAG, "查询设备出错")
                    }
                    .onCompletion {
                        Log.d(TAG, "查询设备结束")

                    }
                    .collect { result ->
                        result.doFailure {
                            Log.d(TAG, "查询设备失败")
                        }
                        result.doSuccess {
                            Log.d(TAG, "查询设备成功${it}")
                            if (it == null){
                                Log.d(TAG,"数据库中没有此设备,保存设备")
                                saveDevice(application, DeviceEntity.convert2DeviceEntity(bluetooth, info)
                                )
                            }else{
                                Log.d(TAG,"数据库中已存在设备,不重复设备,加载数据")
                                _curBluetooth.postValue(it)
                                CollectUtil.getInstance(application.applicationContext).getDataAll(it.deviceName)
                            }
                        }

                    }
            }

        }
    }



    /**
     * 应该保证自动采集时 deviceName及UserId已经存在
     * @param application Application
     */
    fun runAutoCollect(application: Application){
        GlobalScope.launch {
            CollectUtil.getInstance(application).runAutoCollect(this@MainViewModel)
        }

    }

    /**
     * 保存设备信息
     *
     * @param application Application
     * @param deviceEntity DeviceEntity
     */
    fun saveDevice(application: Application, deviceEntity: DeviceEntity){
        DBHelper.getInstance(application).let {
            viewModelScope.launch(Dispatchers.IO) {
                Log.e(TAG, "saveDevice..$deviceEntity")
                it.insertOrUpdateDevice(deviceEntity).collectLatest {
                    it.doSuccess {
                        Log.d(TAG,"保存设备信息成功..$it")
//                        getCurrentDevice(application)
                        _curBluetooth.postValue(deviceEntity)
                    }
                    it.doFailure {
                        Log.d(TAG,"保存设备信息失败")
                    }
                }
            }

        }
    }

    /**
     * 保存用户信息
     *
     * @param application Application
     * @param userEntity UserEntity
     */
    fun saveUser(application: Application, userEntity: UserEntity){
        DBHelper.getInstance(application).let {
            viewModelScope.launch(Dispatchers.IO) {
                Log.e(TAG, "saveUser..$userEntity")
                it.insertOrUpdateUser(userEntity)
                    .collectLatest {
                        it.doSuccess {
                            Log.d(TAG,"保存用户信息成功")
                            _currentUser.postValue(userEntity)
                        }
                        it.doFailure {

                        }
                    }
            }

        }
    }

    /**
     * 查询用户信息
     *
     * @param application Application
     * @param userId Long
     */
    fun queryUser(application: Application,userId : Long){
        DBHelper.getInstance(application).let {
            viewModelScope.launch(Dispatchers.IO) {
                Log.e(TAG, "queryUser..$userId")
                DBHelper.getInstance(application).queryUser(userId)
                    .collect {
                        it.doFailure { }
                        it.doSuccess { result ->
                            Log.e(TAG,"query userName ${result?.name}")
                        }

                    }
            }

        }
    }


    companion object {
        private const val TAG = "MainActivity"
    }


}