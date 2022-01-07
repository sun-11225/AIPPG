package com.viatom.bloodoxygendemo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.viatom.bloodoxygendemo.data.entity.DataEntity

/**
 * @author：created by SunHao
 * 创建时间：2021/11/19 17:23
 * 邮箱：sunhao@viatomtech.com
 * 类说明:
 */
@Dao
interface DataDao {

    @Insert
    fun insertData(dataEntity: DataEntity) : Long

    @Query("SELECT * FROM AIData WHERE deviceName =:name ORDER BY id DESC")
    fun getDataAll(name : String?) : List<DataEntity>

    @Query("SELECT * FROM AIData WHERE deviceName =:name ORDER BY id DESC LIMIT 0,1")
    fun getLastData(name: String) : DataEntity
}