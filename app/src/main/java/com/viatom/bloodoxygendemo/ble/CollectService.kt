package com.viatom.bloodoxygendemo.ble

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.lepu.blepro.utils.LepuBleLog
import com.viatom.bloodoxygendemo.constants.Constant
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.AUTO_DURATION_MILLS
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.AUTO_EXIT
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.AUTO_INTERVAL
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.AUTO_START
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.AUTO_STOP
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.MANUAL_DURATION_S
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.collectSwitch
import com.viatom.bloodoxygendemo.util.LpResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * author: wujuan
 * created on: 2021/4/12 16:23
 * description:
 */
class CollectService : Service(){

    private val binder = CollectBinder()


   inner class CollectBinder: Binder(){
       fun getService(): CollectService = this@CollectService
   }

    companion object {

        @JvmStatic
        fun startService(context: Context) {
            LepuBleLog.d("startService")
            Intent(context, CollectService::class.java).also { intent -> context.startService(intent)}
        }

        @JvmStatic
        fun stopService(context: Context) {
            val intent = Intent(context, CollectService::class.java)
            context.stopService(intent)
        }

        private const val TAG = "CollectService"

    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    /**
     *
     * 自动采集
     * @return Flow<LpResult<Int>>
     */
    suspend fun autoCollect(): Flow<LpResult<Int>> {
        return flow{
            try {
                while (true){
                    delay(AUTO_INTERVAL) //

                    CollectUtil.getInstance(this@CollectService).tempValueAuto = false

                    for (i in 1..AUTO_DURATION_MILLS){


                        if (LpBleUtil.isDisconnected(Constant.BluetoothConfig.SUPPORT_MODEL)) {
                            Log.e("collectUtil", "自动:蓝牙已断开, 停止本次自动采集")
                            LpResult.Success(AUTO_EXIT)
                            break
                        }

//                        if (Constant.BluetoothConfig.currentRunState != Constant.RunState.RECORDING) {
//                            Log.e("collectUtil","自动:不在测量中, 停止本次自动采集")
//                            LpResult.Success(AUTO_EXIT)
//                            break
//                        }
                        if (CollectUtil.getInstance(this@CollectService).tempValueAuto){
                            Log.e("collectUtil","自动:测量无效值, 停止本次自动采集")
                            LpResult.Success(AUTO_EXIT)
                            break
                        }

                        if (i == 1) {
                            Log.d("collectUtil", "自动 AUTO_START")
                            emit(LpResult.Success(AUTO_START))
                        }

                        delay(1000)
                        Log.d("collectUtil", "自动 读秒 $i")


                        if (i == AUTO_DURATION_MILLS )  {
                            emit(LpResult.Success(AUTO_STOP))
                            Log.d("collectUtil", "自动AUTO_STOP")
                        }

                    }


                }
            } catch (e: Exception) {
                e.printStackTrace()
                emit(LpResult.Failure(e.cause))
            }
        }.flowOn(Dispatchers.IO)

    }


    /**
     * 手动采集
     * @return Flow<LpResult<Int>>
     */
    suspend fun manualCount(): Flow<LpResult<Int>> {
        return flow{
            try {
                Log.d("manualCollect", " start.....")
                CollectUtil.getInstance(this@CollectService).tempValueManual = false

//                for (i in 0..MANUAL_DURATION_S ){
                for (i in MANUAL_DURATION_S downTo 0 ){

                    if (LpBleUtil.isDisconnected(Constant.BluetoothConfig.SUPPORT_MODEL)) {
                        Log.e("collectUtil","手动：蓝牙已断开, 停止采集")

                        emit(LpResult.Failure(Exception("蓝牙已断开, 停止采集")))
                        return@flow
                    }

//                    if (Constant.BluetoothConfig.currentRunState != Constant.RunState.RECORDING) {
//                        Log.e("collectUtil","手动：不在测量中, 停止采集")
//
//                        emit(LpResult.Failure(Exception("不在测量中, 停止采集")))
//                        return@flow
//                    }
                    if (CollectUtil.getInstance(this@CollectService).tempValueManual){
                        Log.e("collectUtil","手动：测量无效值, 停止采集")
                        emit(LpResult.Failure(Exception("测量无效值, 停止采集")))
                        return@flow
                    }

                    if (!collectSwitch) break


                    emit(LpResult.Success(i))
                    delay(1000)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("manualCollect", "$e")
                emit(LpResult.Failure(e.cause))
            }
        }.flowOn(Dispatchers.IO)

    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("collectService", "unbind success")

        return super.onUnbind(intent)
    }

}