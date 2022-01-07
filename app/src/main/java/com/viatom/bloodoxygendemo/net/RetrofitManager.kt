package com.viatom.bloodoxygendemo.net

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * @author：created by SunHao
 * 创建时间：2021/11/18 16:24
 * 邮箱：sunhao@viatomtech.com
 * 类说明: 网络请求管理类
 */
object RetrofitManager {

    private const val BASE_URL = "http://ppgtoaf.pub.kanebay.com"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    inline fun <reified T> create(): T = create(T::class.java)

//    val commonService =  create<CommonService>()

}