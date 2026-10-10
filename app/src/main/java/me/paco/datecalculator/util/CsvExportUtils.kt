package me.paco.datecalculator.util

import android.content.Context
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.StageSegmentResult
import java.time.LocalDate

object CsvExportUtils {
    fun exportMultiStageCsv(
        context: Context,
        planTitle: String,
        baseDate: LocalDate,
        finalDate: LocalDate,
        segments: List<StageSegmentResult>
    ) {
        val sb = StringBuilder()
        sb.append("阶段序号,备注事项,推算类型,天数,起始日期,结束日期\n")
        segments.forEach { seg ->
            val typeStr = if (seg.type == CalculationType.ADD) "加" else "减"
            sb.append("${seg.stageIndex},${seg.remark},$typeStr,${seg.daysCount},${seg.startDate},${seg.endDate}\n")
        }
        ShareUtils.shareText(context, "$planTitle 导出数据:\n$sb")
    }
}
