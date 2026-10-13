package me.paco.datecalculator.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.RegionalHolidays
import me.paco.datecalculator.data.StageSegmentResult
import me.paco.datecalculator.data.TimelineBlockType
import me.paco.datecalculator.data.WeekendRule
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

object CsvExportUtils {

    fun exportMultiStageCsv(
        context: Context,
        planTitle: String,
        baseDate: LocalDate,
        finalDate: LocalDate,
        segments: List<StageSegmentResult>,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true,
        disableChinaShiftWorkdays: Boolean = false,
        language: AppLanguage = AppLanguage.SIMPLIFIED_CHINESE
    ) {
        try {
            val titleName = planTitle.ifBlank { "排期计划" }
            val nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
            val fileName = "DateSchedule_${titleName}_${nowStr}.xls"

            val isForward = !baseDate.isAfter(finalDate)
            val startDate = if (isForward) baseDate else finalDate
            val endDate = if (isForward) finalDate else baseDate
            val totalCalendarDays = abs(ChronoUnit.DAYS.between(startDate, endDate)).coerceAtLeast(1L)

            // 1. 生成全局每日序列列表
            val dailyList = mutableListOf<Pair<LocalDate, TimelineBlockType>>()
            var curr = startDate
            var totalWorkdays = 0L
            var totalWeekendDays = 0L
            var totalStatutoryHolidays = 0L

            while (!curr.isAfter(endDate)) {
                val isShift = enableHolidays && !disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(curr, holidayRegion)
                val isStat = enableHolidays && RegionalHolidays.isStatutoryHoliday(curr, holidayRegion)
                val isWeekend = weekendRule.isWeekend(curr, isCurrentWeekBigWeek)

                val t = when {
                    isShift -> TimelineBlockType.SHIFT_WORKDAY
                    isStat -> TimelineBlockType.STATUTORY_HOLIDAY
                    isWeekend -> TimelineBlockType.WEEKEND
                    else -> TimelineBlockType.WORKDAY
                }

                when (t) {
                    TimelineBlockType.WORKDAY, TimelineBlockType.SHIFT_WORKDAY -> totalWorkdays++
                    TimelineBlockType.WEEKEND -> totalWeekendDays++
                    TimelineBlockType.STATUTORY_HOLIDAY -> totalStatutoryHolidays++
                }

                dailyList.add(curr to t)
                curr = curr.plusDays(1)
            }

            val sb = StringBuilder()
            sb.append("""
                <!DOCTYPE html>
                <html>
                <head>
                <meta charset="UTF-8">
                <style>
                  table { border-collapse: collapse; font-family: 'Microsoft YaHei', 'Segoe UI', Arial, sans-serif; font-size: 12px; }
                  th { background-color: #1E293B; color: #FFFFFF; font-weight: bold; padding: 8px 10px; border: 1px solid #CBD5E1; text-align: center; }
                  td { padding: 6px 10px; border: 1px solid #CBD5E1; text-align: center; }
                  .section-header { font-size: 14px; font-weight: bold; color: #1E293B; background-color: #F1F5F9; text-align: left; padding: 8px 12px; }
                  .workday { background-color: #2563EB; color: #FFFFFF; font-weight: bold; }
                  .weekend { background-color: #F59E0B; color: #FFFFFF; font-weight: bold; }
                  .holiday { background-color: #EF4444; color: #FFFFFF; font-weight: bold; }
                </style>
                </head>
                <body>
            """.trimIndent())

            // ===== 第一部分：排期计划概览与图例说明 =====
            sb.append("""
                <table>
                  <tr><td colspan="6" class="section-header">==== 日期计算器 - 排期推算汇总 ====</td></tr>
                  <tr><td style="font-weight:bold; background:#F8FAFC;">排期计划名称</td><td colspan="5" style="text-align:left;">${titleName}</td></tr>
                  <tr><td style="font-weight:bold; background:#F8FAFC;">起始基准日期</td><td colspan="5" style="text-align:left;">${DateCalculatorUtils.formatDateWithWeek(baseDate, language)}</td></tr>
                  <tr><td style="font-weight:bold; background:#F8FAFC;">推算最终日期</td><td colspan="5" style="text-align:left;">${DateCalculatorUtils.formatDateWithWeek(finalDate, language)}</td></tr>
                  <tr><td style="font-weight:bold; background:#F8FAFC;">总历时天数</td><td colspan="5" style="text-align:left;">${totalCalendarDays} 自然日</td></tr>
                  <tr><td style="font-weight:bold; background:#F8FAFC;">包含工作日</td><td colspan="5" style="text-align:left;">${totalWorkdays} 天</td></tr>
                  <tr><td style="font-weight:bold; background:#F8FAFC;">包含周末双休</td><td colspan="5" style="text-align:left;">${totalWeekendDays} 天</td></tr>
                  <tr><td style="font-weight:bold; background:#F8FAFC;">包含法定节假日</td><td colspan="5" style="text-align:left;">${totalStatutoryHolidays} 天</td></tr>
                  <tr>
                    <td style="font-weight:bold; background:#F8FAFC;">图例说明</td>
                    <td class="workday">工作日</td>
                    <td class="weekend">周末双休</td>
                    <td class="holiday">法定节假日</td>
                    <td colspan="2" style="background:#F8FAFC;"></td>
                  </tr>
                </table>
                <br>
            """.trimIndent())

            // ===== 第二部分：真正的彩色单元格填充横向甘特图排期电子表格 (Horizontal Color Cell Fill Gantt Chart) =====
            sb.append("<table>\n")
            sb.append("<tr><th colspan=\"${6 + dailyList.size}\" class=\"section-header\">==== 全日程横向彩色单元格填充甘特图排期表 (Gantt Chart) ====</th></tr>\n")

            // A. 横向甘特图 Header 行：A-F 为阶段固定信息，G 列开始按日期从左到右依次展开列!
            sb.append("<tr>")
            sb.append("<th>阶段序号</th><th>备注事项</th><th>推算类型</th><th>设定天数</th><th>阶段起始日期</th><th>阶段结束日期</th>")
            dailyList.forEach { (date, _) ->
                val monthDayStr = date.format(DateTimeFormatter.ofPattern("MM/dd"))
                val weekDayStr = DateCalculatorUtils.getLocalizedDayOfWeek(date, language)
                sb.append("<th>${monthDayStr}<br>(${weekDayStr})</th>")
            }
            sb.append("</tr>\n")

            // B. 逐个阶段的横向甘特图数据行 (单元格背景直接填充彩色，Excel / WPS 打开呈现极致纯色甘特条)
            segments.forEachIndexed { index, seg ->
                val typeStr = if (seg.type == CalculationType.ADD) "加(+)" else "减(-)"
                val remarkStr = seg.remark.ifBlank { "第${index + 1}阶段" }

                val segStart = if (seg.startDate.isBefore(seg.endDate)) seg.startDate else seg.endDate
                val segEnd = if (seg.startDate.isBefore(seg.endDate)) seg.endDate else seg.startDate

                sb.append("<tr>")
                sb.append("<td>${index + 1}</td><td>${remarkStr}</td><td>${typeStr}</td><td>${seg.daysCount}</td><td>${seg.startDate}</td><td>${seg.endDate}</td>")

                dailyList.forEach { (date, type) ->
                    if (!date.isBefore(segStart) && !date.isAfter(segEnd)) {
                        val (cssClass, label) = when (type) {
                            TimelineBlockType.WORKDAY, TimelineBlockType.SHIFT_WORKDAY -> "workday" to "工作日"
                            TimelineBlockType.STATUTORY_HOLIDAY -> "holiday" to "法定假"
                            TimelineBlockType.WEEKEND -> "weekend" to "周末休"
                        }
                        sb.append("<td class=\"$cssClass\">$label</td>")
                    } else {
                        sb.append("<td></td>") // 阶段外日期留空，完美形成平滑纯色甘特条！
                    }
                }
                sb.append("</tr>\n")
            }

            // C. 总体全周期横向进度总结行
            sb.append("<tr>")
            sb.append("<td style=\"font-weight:bold; background:#E2E8F0;\">总体时间轴</td><td style=\"font-weight:bold; background:#E2E8F0;\">全周期汇总</td><td style=\"background:#E2E8F0;\">全排期</td><td style=\"background:#E2E8F0;\">${totalCalendarDays}天</td><td style=\"background:#E2E8F0;\">${baseDate}</td><td style=\"background:#E2E8F0;\">${finalDate}</td>")
            dailyList.forEach { (_, type) ->
                val (cssClass, label) = when (type) {
                    TimelineBlockType.WORKDAY, TimelineBlockType.SHIFT_WORKDAY -> "workday" to "工作日"
                    TimelineBlockType.STATUTORY_HOLIDAY -> "holiday" to "法定假"
                    TimelineBlockType.WEEKEND -> "weekend" to "周末休"
                }
                sb.append("<td class=\"$cssClass\">$label</td>")
            }
            sb.append("</tr>\n")

            sb.append("</table></body></html>")

            // 保存到缓存目录下的 xls 临时文件
            val cacheDir = File(context.cacheDir, "csv_exports")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val xlsFile = File(cacheDir, fileName)
            xlsFile.writeText(sb.toString(), Charsets.UTF_8)

            // 使用 FileProvider 生成安全 URI 并分享 `.xls` 电子表格文件
            val authority = "${context.packageName}.fileprovider"
            val fileUri = FileProvider.getUriForFile(context, authority, xlsFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.ms-excel"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "$titleName - 彩色单元格甘特图电子表格")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "导出/分享彩色单元格甘特图电子表格")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)

            Toast.makeText(context, "已生成彩色单元格甘特图表格: $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "生成表格文件失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
