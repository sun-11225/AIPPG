package com.viatom.bloodoxygendemo.repository

import android.util.Log
import com.viatom.bloodoxygendemo.MyApp
import com.viatom.bloodoxygendemo.ble.CollectUtil
import com.viatom.bloodoxygendemo.data.entity.DataEntity
import com.viatom.bloodoxygendemo.data.local.DBHelper
import com.viatom.bloodoxygendemo.net.CommonService
import com.viatom.bloodoxygendemo.net.RetrofitManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * @author：created by SunHao
 * 创建时间：2021/11/18 10:48
 * 邮箱：sunhao@viatomtech.com
 * 类说明: 仓库类
 */
object Repository {
    private const val TAG = "collectRepository"

    private val commonService = RetrofitManager.create<CommonService>()

    /**
     *  上传脉搏波文件进行AI分析，获取服务器AI分析结果
     *  切换到子线程执行网络请求
     *
     * @param file File txt文件
     * @return LiveData<Result<Data>>
     */
    fun uploadFile(file: File,recordId : Long, type: Int , timeData : String,timeEnd : String,name : String){
        Log.d(TAG, "开始上传并分析文件...... ${file.name}")
        GlobalScope.launch(Dispatchers.IO) {
           try {
//            RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
                file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    .let { requestBody ->
                        MultipartBody.Part.createFormData("pDataFile", file.name, requestBody)
                            .let { body ->
                                Log.d(TAG, "upload param...... ${body.body}")
                                val response = commonService.oxyAnalysis(body).await()
                                Log.d(TAG, "返回码: ${response.code}")
                                if (response.code == 1) {
//                                    Result.success(response.data)
                                    response.data.let {
                                        Log.d(TAG,"检测结果：${response.data.detectCode}, 说明：${response.data.message}")
                                        Log.d(TAG, "分析成功 采集类型$type")
                                        //保存结果到数据库
                                        it.recordId = recordId
                                        it.time = timeData
                                        it.timeEnd = timeEnd
                                        it.deviceName = name
                                        Log.d(TAG, "recordId: ${it.recordId} deviceName: ${it.deviceName}")
                                        CollectUtil.getInstance(MyApp.context).insertData(it,type)
//                                        CollectUtil.getInstance(MyApp.context).insertReport(it,type)
                                    }

                                } else {
                                    Log.d(TAG,"error：${response.message}, 说明：${response.reason}")
                                    CollectUtil.getInstance(MyApp.context).finishCollecting(false, type, "接口响应异常")
//                                    Result.failure(java.lang.RuntimeException("response error is ${response.message} + ${response.reason}"))
                                }
                            }
                    }
            } catch (e: Exception) {
                Log.d(TAG,"Exception: ${e.printStackTrace()} ")
                e.printStackTrace()
//                Result.failure(e)
            }
        }
//        listener.invoke(result)
    }


    private suspend fun <T> retrofit2.Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : retrofit2.Callback<T> {
                override fun onResponse(call: retrofit2.Call<T>, response: Response<T>) {
                    val body = response.body()
                    Log.d(TAG,"分析结果: $body")
                    //返回值
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(RuntimeException("collect response body is null"))
                }

                override fun onFailure(call: retrofit2.Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }
}