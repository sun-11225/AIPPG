package com.viatom.bloodoxygendemo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * author: wujuan
 * created on: 2021/3/31 15:44
 * description:
 *
 * 接口返回ai分析报告实体
 */
@Entity(tableName = "report")
data class ReportEntity @JvmOverloads constructor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var recordId: Long,
    val fragmentList: List<Fragment>?,
    val aiDiagnosisCode: String,
    val isShowAiResult: String, //1：是 0：否
    val shortRangeTime: String,
    val hr: String,
    val posList: List<Int>?,
    val sendTime: String,
    val levelCode: Int,
    val labelList: List<String>?,
    val aiResult: String,
    val aiResultList: List<AiResult>?,
    val aiDiagnosis: String,
    val aiSuggestion: String,
    var pdfName: String, //本地相对路径
    ) {

    /**
     *  "startPose": "3499",
    │                     "code": "13000",
    │                     "name": "最大心率",
    │                     "endPose": "5499"
     * @property startPose String
     * @property code String
     * @property name String
     * @property endPose String
     * @constructor
     */
    data class Fragment(
        val startPose: String,
        val code: String,
        val name: String,
        val endPose: String
    ){
        override fun equals(obj: Any?): Boolean {
            return if (obj !is Fragment) false else obj.startPose == startPose
        }
    }

    /**
     *  {
    │                     "cover": "",
    │                     "code": "101",
    │                     "aiDiagnosis": "窦性心律",
    │                     "video": "http:\/\/video.ixinzang.com\/101_窦性心律.mp4",
    │                     "phoneContent": "建议：\\n窦性心律为正常心脏节律，无需特殊检查；如果有症状，仍需要进一步检查。",
    │                     "content": "<div class='centent'><h3>定义：<\/h3><p>凡起源于窦房结的心律，称为窦性心律。窦性心律属于正常节律。<\/p><h3>临床意义:<\/h3><p>窦房结是心脏搏动的最高司令部，正常的心脏必须有正常的窦房结，正常的窦房结具有强大的自律性。窦房结自律性除受自主神经调节外，还受温度、血氧饱和度和其他代谢过程的影响。心脏的正常跳动就应该是窦性心律。<br\/>窦性心律的个体差异性受很多因素影响，包括年龄、性别和自主神经调节。6岁之后，随着年龄的增长，心律会逐渐减慢，青少年和成年人安静时心率大约65~85次\/分，到老年心率更趋缓慢。体温升高加速窦性心率，体温每升高1度，窦性心律增快8次\/分。血氧饱和度的增加可减慢窦性心率，血氧饱和度的降低则使窦性心律增加。<\/p><h3>建议：<\/h3><p>窦性心律为正常心脏节律，无需特殊检查；如果有症状，仍需要进一步检查。<\/p><\/div>"
    │                 }
     * @property aiDiagnosis String
     * @property code String
     * @property content String
     * @property cover String
     * @property phoneContent String
     * @property video String
     * @constructor
     */
    data class AiResult(
        val aiDiagnosis: String,
        val code: String,
        val content: String,
        val cover: String,
        val phoneContent: String,
        val video: String
    )



}

