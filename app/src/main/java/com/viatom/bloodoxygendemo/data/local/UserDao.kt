package com.viatom.bloodoxygendemo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.viatom.bloodoxygendemo.data.entity.UserEntity

/**
 * author: wujuan
 * created on: 2021/4/16 11:13
 * description:
 */
@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: UserEntity)


    @Query("SELECT * FROM user WHERE userId=:id")
    fun queryUser(id: Long): UserEntity?
}