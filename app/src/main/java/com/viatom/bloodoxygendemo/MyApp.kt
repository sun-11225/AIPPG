package com.viatom.bloodoxygendemo

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * @author：created by SunHao
 * 创建时间：2021/11/19 10:04
 * 邮箱：sunhao@viatomtech.com
 * 类说明:
 */
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        initUser()
    }

    /**
     * 初始化全局通用user表
     */
    private fun initUser() {

    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}