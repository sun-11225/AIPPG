package com.viatom.bloodoxygendemo.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

/**
 * author: wujuan
 * created on: 2021/4/16 11:09
 * description:
 */
@Entity(tableName = "user")
@Parcelize
data class UserEntity(
    @PrimaryKey
    val userId: Long = 0,
    val name: String,
    val height: String,
    val weight: String,
    val birthday: String,
    val gender: String
) : Parcelable
