package com.viatom.bloodoxygendemo.data.entity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lepu.blepro.ble.cmd.OxyBleResponse
import com.lepu.blepro.ble.data.LepuDevice
import java.text.SimpleDateFormat

/**
 * author: wujuan
 * created on: 2021/4/6 12:37
 * description:
 */
@Entity(
    tableName = "devices",
    indices = [Index(value = ["deviceName"], unique = true), Index("productTypeName")]
)
data class DeviceEntity(
        @PrimaryKey(autoGenerate = true)
        var id : Long = 0,

        val deviceName: String?,
        val deviceMacAddress: String,
        val productTypeName: String,
        val currentTime: Long?,
        val serialNum: String?,
        val data: ByteArray
)
{

    companion object {
        @ExperimentalUnsignedTypes
        @SuppressLint("SimpleDateFormat")
        fun convert2DeviceEntity(b: BluetoothDevice, lepuDevice: OxyBleResponse.OxyInfo): DeviceEntity {
            return lepuDevice.run {
                val sdf = SimpleDateFormat("yyyy-MM-dd,HH:mm:ss")

                DeviceEntity(
                    deviceName = b.name,
                    deviceMacAddress = b.address,
                    productTypeName = b.name.split(" ")[0],
                    currentTime = sdf.parse(lepuDevice.curTime).time,
                            serialNum = lepuDevice.sn,
                            data = lepuDevice.bytes
                    )
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeviceEntity

        if (deviceName != other.deviceName) return false
        if (deviceMacAddress != other.deviceMacAddress) return false
        if (productTypeName != other.productTypeName) return false
        if (currentTime != other.currentTime) return false
        if (serialNum != other.serialNum) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = deviceName.hashCode()
        result = 31 * result + deviceMacAddress.hashCode()
        result = 31 * result + productTypeName.hashCode()
        result = 31 * result + (currentTime?.hashCode() ?: 0)
        result = 31 * result + (serialNum?.hashCode() ?: 0)
        result = 31 * result + data.contentHashCode()
        return result
    }


}