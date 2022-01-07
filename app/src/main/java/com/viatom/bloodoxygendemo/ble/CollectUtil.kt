package com.viatom.bloodoxygendemo.ble

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.jeremyliao.liveeventbus.LiveEventBus
import com.viatom.bloodoxygendemo.constants.Constant
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.AUTO_DURATION_MILLS
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.AUTO_EXIT
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.AUTO_START
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.AUTO_STOP
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.MANUAL_DURATION_S
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.TYPE_AUTO
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.TYPE_MANUAL
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.age
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.collectSwitch
import com.viatom.bloodoxygendemo.constants.Constant.Collection.Companion.gender
import com.viatom.bloodoxygendemo.data.entity.DataEntity
import com.viatom.bloodoxygendemo.data.entity.RecordEntity
import com.viatom.bloodoxygendemo.data.local.DBHelper
import com.viatom.bloodoxygendemo.util.LpResult
import com.viatom.bloodoxygendemo.util.SingletonHolder
import com.viatom.bloodoxygendemo.util.doFailure
import com.viatom.bloodoxygendemo.util.doSuccess
import com.viatom.bloodoxygendemo.ext.createFile

import com.viatom.bloodoxygendemo.repository.Repository.uploadFile
import com.viatom.bloodoxygendemo.viewmodel.DashboardViewModel
import com.viatom.bloodoxygendemo.viewmodel.MainViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min
import kotlin.math.roundToInt


/**
 * author: wujuan
 * created on: 2021/4/12 16:30
 * description:
 */
class CollectUtil private constructor(val context: Context) {
    val C_TAG: String = "collectUtil"

    companion object : SingletonHolder<CollectUtil, Context>(::CollectUtil)


    var autoData: ByteArray = ByteArray(0)


    var autoCreateTime: Long = 0L
    var autoCounting: Boolean = false


    var manualData: ByteArray = ByteArray(0)
    var manualCreateTime: Long = 0L
    var manualCounting: Boolean = false
    var tempValueManual: Boolean = false
    var tempValueAuto: Boolean = false

    var startTime : String = ""
    var timeEnd : String = ""
    private val sDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())


    lateinit var collectService: CollectService


    private val con = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(C_TAG, "onServiceConnected")
            service?.let {
                if (service is CollectService.CollectBinder) {
                    collectService = service.getService()
                    Log.d(C_TAG, "collectService inited")
                    // 应该在service 连接成功后调用 actionCollect
                    LiveEventBus.get(Constant.Event.collectServiceConnected).post(true)

                }
            }

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(C_TAG, "ServiceDisconnected")
        }

    }

    fun checkService(): Boolean{
        return this::collectService.isInitialized
    }

    fun unbindService(){
        if (checkService())
            context.unbindService(con)
    }

    fun initService(): CollectUtil {
        Log.d(C_TAG, "into initService")

        CollectService.startService(context)

        Intent(context, CollectService::class.java).also { intent ->
            context.bindService(intent, con, Context.BIND_AUTO_CREATE)
        }

        return this

    }


    suspend fun runAutoCollect(mainViewModel: MainViewModel) {

        collectService.autoCollect()
            .onStart {
                Log.d(C_TAG, "自动采集服务已经运行")

            }
            .onCompletion {
                finishCollecting(false, TYPE_AUTO)
            }
            .catch {
                finishCollecting(false, TYPE_AUTO)
            }
            .collect { result ->
                result.doFailure {
                    finishCollecting(false, TYPE_AUTO)
                }
                result.doSuccess {

                    Log.d(
                        C_TAG,
                        "自动采集 doSuccess $it"
                    )
                    when (it) {
                        AUTO_START -> {
                            //检查当前设备实时状态， 只要这一刻是测量准备和测量中 就开始采集， 否则这30分钟不进行采集
                            Log.d(
                                C_TAG,
                                "自动采集 autoCreateTime$autoCreateTime  autoCounting $autoCounting"
                            )

                            cleanAutoData()
                            autoCreateTime = System.currentTimeMillis()
                            autoCounting = true



                        }
                        AUTO_STOP -> {
                            //停止采集 去分析
                            Log.d(C_TAG, "自动采集倒计时结束，去分析  autoCreateTime$autoCreateTime  autoCounting $autoCounting")
                            if (!autoCounting || autoCreateTime == 0L){
                                Log.d(C_TAG, "30秒前 不满足执行采集条件")
                                return@doSuccess
                            }

                            if (!autoCounting){
                                Log.d(C_TAG, "其他原因已经停止采集")
                                return@collect
                            }
                            autoCounting = false

                                mainViewModel.curBluetooth.value?.deviceName?.let { deviceName ->

                                    saveCollectEcg(TYPE_AUTO)?.let { file ->
                                        //检查deviceName  userId  保存到数据库 并分析
                                        mainViewModel.currentUser.value?.userId?.let { userId ->
                                            insertRecord(file, TYPE_AUTO, userId, deviceName)
//                                            Repository.uploadFile(file,TYPE_AUTO)
                                        } ?: run {
                                            Log.e(C_TAG, "userId is null ")
                                        }

                                    } ?: run{
                                        Log.e(C_TAG, "saveCollectEcg return null ")
                                        finishCollecting(false, TYPE_AUTO)
                                    }
                                } ?: run {
                                    Log.e(C_TAG, "deviceName is null ")
                                }
                            }

                        AUTO_EXIT ->{
                            Log.e(C_TAG, "自动采集中断  等待下一次执行")
                            finishCollecting(false, TYPE_AUTO)
                        }
                    }

                }

            }

    }

    // 将每个步骤写到一个flow中 应该会更好
    fun allFlow(): Flow<LpResult<Boolean>> = flow {

    }


    suspend fun manualCollect(vm: DashboardViewModel, mainViewModel: MainViewModel) {
        if (!this::collectService.isInitialized){
            Log.d(C_TAG, "!this::collectService.isInitialized")
            return
        }
        collectService.manualCount()
            .onStart {
                Log.d(C_TAG, "手动采集开始")
                cleanData()
                manualCreateTime = Calendar.getInstance().timeInMillis
                startTime = sDateFormat.format(manualCreateTime)
                Log.d(C_TAG, "start time: $startTime")
                manualCounting = true

            }
            .onCompletion {
                Log.d(C_TAG, "手动读秒结束")

            }
            .catch {
                Log.e(C_TAG, "读秒异常终止")
                age = ""
                gender = ""
                finishCollecting(false, TYPE_MANUAL,"采集异常终止")

            }.cancellable()
            .collect { result ->
                result.doFailure {
                    Log.e(C_TAG, "读秒 doFailure， $it")
                    collectSwitch = false
                    age = ""
                    gender = ""
                    finishCollecting(false, TYPE_MANUAL, "${it?.message}")
                }
                result.doSuccess { res ->
                    //刷新读秒UI
                    Log.d(C_TAG, "读秒 $res")
                    if (!manualCounting){
                        Log.d(C_TAG, "其他原因已经停止采集")
                        age = ""
                        gender = ""
                        return@collect
                    }

                    vm._collectBtnText.postValue("$res")
//                    vm.collectBtnTextChanged("$res S")

                    if (res == 0) {
                        saveResult(vm, mainViewModel)
                    }
                }

            }



    }

     fun saveResult(vm: DashboardViewModel, mainViewModel: MainViewModel){
        collectSwitch = false
        manualCounting = false
        vm._collectBtnText.postValue("采集")
//                        vm.collectBtnTextChanged("采集")
        Log.d(C_TAG, "读秒${MANUAL_DURATION_S} manualData :${manualData.size}")
        timeEnd = sDateFormat.format(Calendar.getInstance().timeInMillis)

        //保存txt -> record保存到DB -> 上传txt,等待返回分析结果 -> report保存到DB -> record更新DB 分析状态

        mainViewModel.curBluetooth.value?.deviceName?.let { deviceName ->

            saveCollectEcg(TYPE_MANUAL)?.let { file ->
                //检查deviceName  userId  保存到数据库 并分析
                mainViewModel.currentUser.value?.userId?.let { userId ->
                    insertRecord(file, TYPE_MANUAL, userId, deviceName)
//                                    Repository.uploadFile(file, TYPE_AUTO)
                } ?: run {
                    Log.e(C_TAG, "userId is null ")
                }
                //清空保存的年龄性别
                age = ""
                gender = ""
            } ?: {
                //清空保存的年龄性别
                age = ""
                gender = ""
                Log.e(C_TAG, "TYPE_MANUAL: saveCollectSpo2 is null  ")
                finishCollecting(false, TYPE_MANUAL, "文件保存失败")
            }

        } ?: run {
            Log.e(C_TAG, "deviceName is null ")
        }
    }



     private fun insertRecord(file: File, type: Int, userId: Long, deviceName: String) {
        Log.d(C_TAG, "into insertRecord... ")
//        val bytes: ByteArray = FileIOUtils.readFile2BytesByStream(file.absoluteFile)
        if (type == TYPE_AUTO && autoData.isEmpty()  || type == TYPE_MANUAL && manualData.isEmpty()){
            Log.d(C_TAG, "into insertRecord type =$type, data isEmpty ")
            return
        }
        RecordEntity.convert2RecordEntity(
            if (type == TYPE_AUTO) autoCreateTime else manualCreateTime,
            file.name,
            type,
            if (type == TYPE_AUTO) autoData else manualData,
            if (type == TYPE_AUTO) AUTO_DURATION_MILLS else MANUAL_DURATION_S,
            deviceName,
            userId

        ).let { record ->
            DBHelper.getInstance(context).let {
                GlobalScope.launch {
                    it.insertRecord(
                        record
                    ).collect { result ->
                        result.doFailure {
                            Log.e(C_TAG, "数据库存储失败")
                            finishCollecting(false, type, "数据库存储失败")
                        }
                        result.doSuccess { id ->
                            Log.d(C_TAG, "保存血氧记录到数据库成功, id: $id")
                            uploadFile(file, id, type,startTime,timeEnd,deviceName)


                        }
                    }
                }
            }

        }

    }

    /**
     * 分析接口可能存在超时，可以自主设计表记录在下次启动时重新尝试上传并分析
     * @param file File
     */
//    fun uploadFile(file: File, recordId: Long, type: Int) {
//
//        RetrofitManager.commonService.let { api ->
//            Log.d(C_TAG, "开始上传并分析文件...... ${file.name}")
//            GlobalScope.launch(Dispatchers.IO) {
//                RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
//                    .let { requestBody ->
//                        MultipartBody.Part.createFormData("ecgFile", file.name, requestBody)
//                            .let { body ->
//
//                                Log.d(C_TAG, "upload param...... ${body.body}")
//                                api.ecgAnalysis(body)
//                                    .enqueue(object :
//                                        retrofit2.Callback<RetrofitResponse<ReportEntity?>> {
//                                        override fun onResponse(
//                                            call: Call<RetrofitResponse<ReportEntity?>>,
//                                            response: Response<RetrofitResponse<ReportEntity?>>
//                                        ) {
//
//                                            if (response.isSuccessful)
//                                                response.body()?.let {
//                                                    Log.d(C_TAG, "分析结果 $it")
//                                                    when (it.isSuccess()) {
//                                                        true -> {
//                                                            it.data?.run {
//                                                                Log.d(C_TAG, "分析成功 采集类型$type")
//                                                                this.recordId = recordId
//                                                                this.pdfName =""
//                                                                insertReport(this, type)
//
//                                                            }
//                                                        }
//                                                        else -> {
//                                                            // 文件异常等
//                                                            Log.e(C_TAG, "分析失败 采集类型$type")
//                                                            updateRecordWithAi(recordId, type) //  根据自己的业务决定此处是否更新状态为已分析，如不标记则可以在合适的时候再次上传分析
//                                                            finishCollecting(false, type, "文件异常，分析失败")
//                                                        }
//                                                    }
//
//
//                                                } else finishCollecting(false, type)
//                                        }
//
//                                        override fun onFailure(
//                                            call: Call<RetrofitResponse<ReportEntity?>>,
//                                            t: Throwable
//                                        ) {
//                                            Log.e(C_TAG, "分析接口请求失败 $t")
//                                            finishCollecting(false, type, "接口响应异常")
//                                        }
//
//
//                                    })
//                            }
//
//                    }
//            }
//        }
//    }


    /**
     * 保存分析结果到数据库
     * @param reportEntity ReportEntity
     */
//    fun insertReport(reportEntity: DataEntity, type: Int) {
//
//        DBHelper.getInstance(context).let {
//            GlobalScope.launch {
//                it.insertReport(
//                    reportEntity
//                ).collect { result ->
//                    result.doFailure {
//                        Log.e(C_TAG, "保存分析结果保存到数据库失败, recordId: $it")
//                        finishCollecting(false, type, "分析结果存储失败")
//                    }
//                    result.doSuccess {
//                        Log.d(C_TAG, "保存分析结果到数据库成功, id: $it")
//                        //更新心电记录的分析状态
//                        updateRecordWithAi(it, type)
//                    }
//                }
//            }
//        }
//
//    }

//    fun updateRecordWithAi(recordId: Long, type: Int) {
//        DBHelper.getInstance(context).let {
//            GlobalScope.launch {
//                it.updateRecordWithAi(
//                    recordId
//                ).collect { result ->
//                    result.doFailure {
//                        Log.e(C_TAG, "分析结果状更新失败, 采集类型： $it")
//                        finishCollecting(false, type, "分析结果状更新失败")
//                    }
//                    result.doSuccess {
//                        Log.d(C_TAG, "心电记录分析状态更新成功, 采集类型： $it")
//                        //结束了整个上传分析流程
//                        finishCollecting(true, type, "分析成功")
//
//                    }
//                }
//            }
//        }
//
//
//    }


    /**
     * 不仅要采集数据到缓存中还要讲数据的坐标保存用来显示UI
     * @param type Int
     * @param feed FloatArray
     */
    fun actionCollectAuto(feed: ByteArray) {


        if (!autoCounting) {
            Log.e(C_TAG, "自动读秒已结束 不能再添加数据")
            return
        }
        autoData = ByteArray(autoData.size + feed.size).apply {
            autoData.copyInto(this)
            feed.copyInto(this, autoData.size)
        }

    }


    fun actionCollectManual(feed: ByteArray) {

        if (!manualCounting) {
            Log.e(C_TAG, "手动读秒已结束 不能再添加数据")
            return
        }

        manualData = ByteArray(manualData.size + feed.size).apply {
            manualData.copyInto(this)
            feed.copyInto(this, manualData.size)
        }

        Log.d(C_TAG, "复制后 手动 ，manualData Size = ${manualData.size}")



    }



    fun saveCollectEcg(type: Int): File? {


        Log.d(
            C_TAG,
            "saveCollectEcg = type$type, manualData:${manualData.size}, autoData: ${autoData.size}"
        )

        if (type == TYPE_MANUAL && manualData.isEmpty() || type == TYPE_AUTO && autoData.isEmpty()) {
            Log.d(C_TAG, "采集数据有误 无法保存")
            return null
        }
        if (type == TYPE_MANUAL && manualCreateTime == 0L || type == TYPE_AUTO && autoCreateTime == 0L) {
            Log.d(C_TAG, "createTime = 0 无法保存")
            return null
        }


        val fileName = if (type == TYPE_MANUAL) "${age}_${gender.toString()}_$manualCreateTime.txt" else "$autoCreateTime.txt"
       fileName.run {
            context.createFile(Constant.Dir.O2RingDir, this)?.let { file ->
//        context.getFile("${Dir.er1EcgDir}/20210412162855.txt")?.let { file ->

                if (!file.exists()) {
                    Log.d(C_TAG, "saveCollectSpo2  !file.exists")
                    return null
                }
                try {
                    BufferedWriter(FileWriter(file)).use { bufferedWriter ->

                        val data = if (type == TYPE_MANUAL)manualData else autoData

                        (data.size - 1).also {
//                            bufferedWriter.write("125,II,1500,")
                            bufferedWriter.write("W-0,125,255,")
                            for (i in 0..it) {
                                bufferedWriter.write((data[i]).toString())
                                bufferedWriter.write(",")
                            }
                        }

                        bufferedWriter.write(data[data.size - 1].toString())

                        bufferedWriter.close()

                        Log.d(C_TAG, "数据文件保存完成，${file.name} file path: ${file.absolutePath} file length ${file.length()} ")
                        return file

                    }
                } catch (e: IOException) {
                    Log.e(C_TAG, "write txt ai file error")
                    return null
                }

            }

        }
        return null
    }

    /**
     * 保存分析结果到数据库
     * @param dataEntity ReportEntity
     */
    fun insertData(dataEntity: DataEntity, type: Int) {

        DBHelper.getInstance(context).let {
            GlobalScope.launch {
                it.insertData(dataEntity).collect { result ->
                    result.doFailure {
                        Log.e(C_TAG, "保存分析结果保存到数据库失败, recordId: $it")
                        finishCollecting(false, type, "分析结果存储失败")
                    }
                    result.doSuccess {
                        Log.d(C_TAG, "保存分析结果到数据库成功, id: $it")
//                        CollectUtil.getInstance(context).getDataAll()
                        CollectUtil.getInstance(context).getLastData(dataEntity.deviceName)
                        //更新血氧记录的分析状态
                        updateRecordWithAi(it, type)
                    }
                }
            }
        }

    }

    /**
     * 获取所有分析结果
     */
    fun getDataAll(deviceName: String?){
        DBHelper.getInstance(context).let {
            GlobalScope.launch {
                it.getDataAll(deviceName).collect { result ->
                    result.doSuccess { list ->
                        LiveEventBus.get(Constant.Event.getAIResult).post(list)
                    }
                    result.doFailure {
                        Log.e(C_TAG, "获取所有分析结果失败, error: $it")
                    }
                }
            }
        }
    }

    fun getLastData(deviceName: String){
        DBHelper.getInstance(context).let {
            GlobalScope.launch {
                it.getLastData(deviceName).collect { result ->
                    result.doSuccess { list ->
                        LiveEventBus.get(Constant.Event.getAIResultOne).post(list)
                    }
                    result.doFailure {
                        Log.e(C_TAG, "获取所有分析结果失败, error: $it")
                    }
                }
            }
        }
    }

    fun updateRecordWithAi(recordId: Long, type: Int) {
        DBHelper.getInstance(context).let {
            GlobalScope.launch {
                it.updateRecordWithAi(
                    recordId
                ).collect { result ->
                    result.doFailure {
                        Log.e(C_TAG, "分析结果状态更新失败, 采集类型： $it")
                        finishCollecting(false, type, "分析结果状更新失败")
                    }
                    result.doSuccess {
                        Log.d(C_TAG, "血氧记录分析状态更新成功, 采集类型： $it")
                        //结束了整个上传分析流程
                        finishCollecting(true, type, "分析成功")

                    }
                }
            }
        }


    }





    fun finishCollecting(isSuccess: Boolean, type: Int, msg: String = "") {
        Log.d(C_TAG, "finishCollecting $isSuccess, $type")
        if (type == TYPE_MANUAL) {
            if (isSuccess) {
                LiveEventBus.get(Constant.Event.analysisProcessSuccess).post(msg)
            } else {
                LiveEventBus.get(Constant.Event.analysisProcessFailed).post(msg)
            }
            cleanData()

        } else {

            cleanAutoData()
        }


    }

    fun cleanData() {
        manualCounting = false
        manualData = ByteArray(0)
        manualCreateTime = 0L
        DataController.dataSrcCollect = null
        tempValueManual = false
        Log.d(C_TAG, "cleanData")
    }

    fun cleanAutoData() {
        autoCounting = false
        autoData = ByteArray(0)
        autoCreateTime = 0L
        tempValueAuto = false
    }

    fun releaseAll(){
        cleanData()
        cleanAutoData()
        unbindService()
    }

}


