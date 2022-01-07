package com.viatom.bloodoxygendemo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author：created by SunHao
 * 创建时间：2021/11/18 15:01
 * 邮箱：sunhao@viatomtech.com
 * 类说明:
 */
class ConnectViewModel : ViewModel() {

    /**
     * 扫描状态
     */
    private val _scanning = MutableLiveData<Boolean>().apply {
        value = false
    }

    val scanning: LiveData<Boolean>
        get() = _scanning


    fun scanChanged(state: Boolean = true) {
        _scanning.value = state
    }

    //已连接蓝牙
    private val _connectedBle = MutableLiveData<String>()

    val connectedBle: LiveData<String>
        get() = _connectedBle


    fun connectedBleChanged(state: String = "") {
        _connectedBle.value = state
    }

}