package com.viatom.bloodoxygendemo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

/**
 * author: wujuan
 * created on: 2021/4/12 16:11
 * description: 保存录制的数据
 */
@Entity(tableName = "record")
data class RecordEntity(
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0,
        val createTime: Long,
        var isAnalysed: Boolean = false, //是否分析过 不代表是否分析成功
        val fileName: String, //本地源文件
        val collectType: Int = 0, //0 自动  1 手动
        val duration: Int = 0, // s
        val data: ByteArray,
        val deviceName: String,
        val userId: Long,

) {
        companion object {

                fun convert2RecordEntity(
                        createTime: Long,
                        filename: String,
                        type: Int,
                        data: ByteArray,
                        duration: Int,
                        deviceName: String,
                        userId: Long,
                ): RecordEntity {

                        return RecordEntity(
                                createTime = createTime,
                                fileName = filename,
                                collectType = type,
//                                data = intConvertFloat(data),
                                data = data,
                                duration = duration,
                                deviceName = deviceName,
                                userId = userId
                        )
                }

                private fun intConvertFloat(data: IntArray): FloatArray {
                        val floatArray = FloatArray(data.size)
                        for (i in data.indices){
                                floatArray[i] = data[i].toFloat()
                        }
                        return floatArray
                }


        }

        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as RecordEntity

                if (id != other.id) return false
                if (createTime != other.createTime) return false
                if (isAnalysed != other.isAnalysed) return false
                if (fileName != other.fileName) return false
                if (collectType != other.collectType) return false
                if (duration != other.duration) return false
                if (!data.contentEquals(other.data)) return false
                if (deviceName != other.deviceName) return false
                if (userId != other.userId) return false

                return true
        }

        override fun hashCode(): Int {
                var result = id.hashCode()
                result = 31 * result + createTime.hashCode()
                result = 31 * result + isAnalysed.hashCode()
                result = 31 * result + fileName.hashCode()
                result = 31 * result + collectType
                result = 31 * result + duration
                result = 31 * result + data.contentHashCode()
                result = 31 * result + deviceName.hashCode()
                result = 31 * result + userId.hashCode()
                return result
        }


}

//fun RecordEntity.getProcessTime(position: Int, pattern: String): String{
//        val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
//        return simpleDateFormat.format(position * 60 * 1000 + createTime)
//}
//
//
//
//fun RecordEntity.getProcessDuration(position: Int): Int{
//        val totalMin = duration / 60;
//        val mode = duration % 60;
//
//        val start = position * 60;
//        var second = (position + 1) * 60;
//        if (position == totalMin && mode != 0) {
//                second = start + mode;
//        }
//        return (second - start);
//}







