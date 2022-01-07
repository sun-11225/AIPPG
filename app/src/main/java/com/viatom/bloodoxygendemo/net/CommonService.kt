package com.viatom.bloodoxygendemo.net

import com.viatom.bloodoxygendemo.data.entity.DataEntity
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * @author：created by SunHao
 * 创建时间：2021/11/18 10:49
 * 邮箱：sunhao@viatomtech.com
 * 类说明:
 */
interface CommonService {

    /**
     * 上传血氧数据分析
     */
    @Multipart
    @POST("/api/af/v1/afDetect/afDetectPPGByFile")
    fun oxyAnalysis(@Part file: MultipartBody.Part) : Call<RetrofitResponse<DataEntity>>
}