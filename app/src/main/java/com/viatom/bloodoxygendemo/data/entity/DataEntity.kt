package com.viatom.bloodoxygendemo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author：created by SunHao
 * 创建时间：2021/11/19 17:21
 * 邮箱：sunhao@viatomtech.com
 * 类说明:
 */
@Entity(tableName = "AIData")
data class DataEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val detectCode : Int,
    val message : String,

    var recordId : Long,
    var UserId : Long,
    var time : String,
    var timeEnd : String,
    var deviceName : String
)