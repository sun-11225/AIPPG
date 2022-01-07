package com.viatom.bloodoxygendemo.data.local

import androidx.room.*
import com.viatom.bloodoxygendemo.data.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

/**
 * author: wujuan
 * created on: 2021/4/6 16:01
 * description:
 */
@Dao
interface DeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDevice(deviceEntity: DeviceEntity)



    @Query("SELECT * FROM devices WHERE deviceName=:name")
    fun getDevice(name: String?): Flow<DeviceEntity>?

    @Query("SELECT * FROM devices ORDER BY currentTime DESC LIMIT 1")
    fun getCurrentDevices(): Flow<DeviceEntity>?



}