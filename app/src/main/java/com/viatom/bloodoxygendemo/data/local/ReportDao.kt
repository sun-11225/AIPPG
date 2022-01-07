package com.viatom.bloodoxygendemo.data.local

import androidx.room.*
import com.viatom.bloodoxygendemo.data.entity.ReportEntity

/**
 * author: wujuan
 * created on: 2021/4/15 11:38
 * description:
 */
@Dao
interface ReportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReport(reportEntity: ReportEntity): Long

    @Query("UPDATE report SET pdfName = :pdfName WHERE id= :reportId")
    fun updateWithPdf(reportId: Long, pdfName: String)



}