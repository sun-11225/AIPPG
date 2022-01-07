package com.viatom.bloodoxygendemo.data.entity

import androidx.room.DatabaseView

/**
 * author: wujuan
 * created on: 2021/4/19 9:03
 * description:
 */

//@Suppress("AndroidUnresolvedRoomSqlReference")
//@DatabaseView("SELECT record.userId, report.recordId,record.createTime,record.isAnalysed, record.collectType, report.hr, report.aiDiagnosis, report.aiResult FROM record INNER JOIN report ON record.id = report.recordId ")
//data class ReportDetail (
//    val createTime: Long,
//    val collectType: Int,
//    val hr: String,
//    val aiDiagnosis: String,
//    val recordId: Long,
//    val isAnalysed: Boolean,
//    val userId: Long,
//    val aiResult: String,
//)