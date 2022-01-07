package com.viatom.bloodoxygendemo.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * author: wujuan
 * created on: 2021/4/15 10:29
 * description:
 */
data class RecordAndReport(
    @Embedded val recordEntity: RecordEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "recordId"
    )

    val reportEntity: ReportEntity,
){

}