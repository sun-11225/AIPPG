package com.viatom.bloodoxygendemo.net

import android.content.Context
import android.widget.Toast
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


/**
 * @author：created by SunHao
 * 创建时间：2021/11/19 11:38
 * 邮箱：sunhao@viatomtech.com
 * 类说明: 数据类
 */
data class RetrofitResponse<D>(val code: Int, val message: String, val reason : String, val data: D)

//@Entity
//data class Data(val detectCode: Int, val message: String){
//    @PrimaryKey(autoGenerate = true)
//    val id: Long = 0
//}

//fun RetrofitResponse<*>.isSuccess(context: Context): Boolean{
//    return when (code) {
//        200 -> {
//            true
//        }
//        else -> {
//            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
//            false
//        }
//    }
//
//}
//
//fun RetrofitResponse<*>.isSuccess(): Boolean{
//    return when (code) {
//        200 -> {
//            true
//        }
//        else -> {
//
//            false
//        }
//    }
//
//}

