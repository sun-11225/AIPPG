package com.viatom.bloodoxygendemo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.viatom.bloodoxygendemo.data.entity.*

@Database(
    entities = [UserEntity::class,DataEntity::class,DeviceEntity::class,RecordEntity::class],
//    views = arrayOf(ReportDetail::class),
    version = 1, exportSchema = false
)
@TypeConverters(value = arrayOf(LocalTypeConverter::class))
abstract class AppDataBase : RoomDatabase() {

    abstract fun deviceDao(): DeviceDao
    abstract fun recordDao(): RecordDao
//    abstract fun reportDao(): ReportDao
    abstract fun userDao(): UserDao
    abstract fun dataDao(): DataDao

}
