package com.viatom.bloodoxygendemo.data.local

import androidx.paging.PagingSource
import androidx.room.*
import com.viatom.bloodoxygendemo.data.entity.RecordAndReport
import com.viatom.bloodoxygendemo.data.entity.RecordEntity
//import com.viatom.bloodoxygendemo.data.entity.ReportDetail

/**
 * author: wujuan
 * created on: 2021/4/15 10:32
 * description:
 */
@Dao
interface RecordDao {
    @Insert
    fun insertRecord(recordEntity: RecordEntity) :Long

    @Query("SELECT * FROM record WHERE id= :recordId")
    fun getRecord(recordId: Long) : RecordEntity


//    @Transaction
//    @Query("SELECT* FROM record WHERE id=:recordId")
//    fun getRecordAndReportDetail(recordId: Long): RecordAndReport

    @Query("UPDATE record SET isAnalysed = :isAnalysed WHERE id= :recordId")
    fun updateWithAnalysed(recordId: Long, isAnalysed: Boolean )


//    @Query("SELECT* FROM reportdetail WHERE userId=:userId ORDER BY recordId DESC ")
//    fun getRecordAndReportList(userId: Long): PagingSource<Int, ReportDetail>




}