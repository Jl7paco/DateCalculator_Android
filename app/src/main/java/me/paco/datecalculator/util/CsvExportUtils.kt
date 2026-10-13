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
            val fileName = "DateSchedule_${titleName}_${nowStr}.csv"

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
            // UTF-8 BOM 标头 (确保 Excel / WPS 打开中文 100% 不乱码)
            sb.append("\uFEFF")

            val legendLabel = when (language.getEffectiveLanguage()) {
                AppLanguage.ENGLISH -> "Legend"
                AppLanguage.JAPANESE -> "凡例"
                AppLanguage.KOREAN -> "범례"
                AppLanguage.TRADITIONAL_CHINESE -> "圖例說明"
                else -> "图例说明"
            }

            val workdayLegend = when (language.getEffectiveLanguage()) {
                AppLanguage.ENGLISH -> "🟦 Workday"
                AppLanguage.JAPANESE -> "🟦 稼働日"
                AppLanguage.KOREAN -> "🟦 근무일"
                AppLanguage.TRADITIONAL_CHINESE -> "🟦 工作日"
                else -> "🟦 工作日"
            }

            val weekendLegend = when (language.getEffectiveLanguage()) {
                AppLanguage.ENGLISH -> "🟨 Weekend"
                AppLanguage.JAPANESE -> "🟨 週末休"
                AppLanguage.KOREAN -> "🟨 주말 휴일"
                AppLanguage.TRADITIONAL_CHINESE -> "🟨 週末雙休"
                else -> "🟨 周末双休"
            }

            val holidayLegend = when (language.getEffectiveLanguage()) {
                AppLanguage.ENGLISH -> "🟥 Holiday"
                AppLanguage.JAPANESE -> "🟥 祝日"
                AppLanguage.KOREAN -> "🟥 공휴일"
                AppLanguage.TRADITIONAL_CHINESE -> "🟥 法定節假日"
                else -> "🟥 法定节假日"
            }

            // ===== 第一部分：排期计划概览与图例说明 =====
            sb.append("==== 日期计算器 - 排期推算汇总 ====\n")
            sb.append("排期计划名称,${titleName.replace(",", "，")}\n")
            sb.append("起始基准日期,${DateCalculatorUtils.formatDateWithWeek(baseDate, language)}\n")
            sb.append("推算最终日期,${DateCalculatorUtils.formatDateWithWeek(finalDate, language)}\n")
            sb.append("总历时天数,${totalCalendarDays} 自然日\n")
            sb.append("包含工作日,${totalWorkdays} 天\n")
            sb.append("包含周末双休,${totalWeekendDays} 天\n")
            sb.append("包含法定节假日,${totalStatutoryHolidays} 天\n")
            sb.append("${legendLabel},${workdayLegend},${weekendLegend},${holidayLegend}\n\n")

            // ===== 第二部分：真正的纯色块横向甘特图排期电子表格 (Horizontal Color Block Gantt Chart) =====
            sb.append("==== 全日程横向甘特图排期表 (Horizontal Gantt Chart) ====\n")

            // A. 横向甘特图 Header 行：A-F 为阶段固定信息，G 列开始按日期从左到右依次展开列!
            val headerSb = StringBuilder()
            headerSb.append("阶段序号,备注事项,推算类型,设定天数,阶段起始日期,阶段结束日期")
            dailyList.forEach { (date, _) ->
                val monthDayStr = date.format(DateTimeFormatter.ofPattern("MM/dd"))
                val weekDayStr = DateCalculatorUtils.getLocalizedDayOfWeek(date, language)
                headerSb.append(",${monthDayStr}(${weekDayStr})")
            }
            sb.append("${headerSb}\n")

            // B. 逐个阶段的横向甘特图数据行 (单元格内仅填入纯色块 symbol，无多余文字，连成平滑纯色甘特条)
            segments.forEachIndexed { index, seg ->
                val typeStr = if (seg.type == CalculationType.ADD) "加(+)" else "减(-)"
                val remarkStr = seg.remark.ifBlank { "第${index + 1}阶段" }.replace(",", "，")

                val stageRowSb = StringBuilder()
                stageRowSb.append("${index + 1},${remarkStr},${typeStr},${seg.daysCount},${seg.startDate},${seg.endDate}")

                val segStart = if (seg.startDate.isBefore(seg.endDate)) seg.startDate else seg.endDate
                val segEnd = if (seg.startDate.isBefore(seg.endDate)) seg.endDate else seg.startDate

                dailyList.forEach { (date, type) ->
                    if (!date.isBefore(segStart) && !date.isAfter(segEnd)) {
                        val cellSymbol = when (type) {
                            TimelineBlockType.WORKDAY, TimelineBlockType.SHIFT_WORKDAY -> "🟦"
                            TimelineBlockType.STATUTORY_HOLIDAY -> "🟥"
                            TimelineBlockType.WEEKEND -> "🟨"
                        }
                        stageRowSb.append(",${cellSymbol}")
                    } else {
                        stageRowSb.append(",") // 阶段外日期留空，完美形成平滑纯色甘特条！
                    }
                }

                sb.append("${stageRowSb}\n")
            }

            // C. 总体全周期横向进度总结行 (纯色块平滑甘特条)
            val globalSummarySb = StringBuilder()
            globalSummarySb.append("总体时间轴,全周期汇总,全排期,${totalCalendarDays}天,${baseDate},${finalDate}")
            dailyList.forEach { (_, type) ->
                val cellSymbol = when (type) {
                    TimelineBlockType.WORKDAY, TimelineBlockType.SHIFT_WORKDAY -> "🟦"
                    TimelineBlockType.STATUTORY_HOLIDAY -> "🟥"
                    TimelineBlockType.WEEKEND -> "🟨"
                }
                globalSummarySb.append(",${cellSymbol}")
            }
            sb.append("${globalSummarySb}\n")

            // 保存到缓存目录下的 csv 临时文件
            val cacheDir = File(context.cacheDir, "csv_exports")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val csvFile = File(cacheDir, fileName)
            csvFile.writeText(sb.toString(), Charsets.UTF_8)

            // 使用 FileProvider 生成安全 URI 并分享 `.csv` 电子表格文件
            val authority = "${context.packageName}.fileprovider"
            val fileUri = FileProvider.getUriForFile(context, authority, csvFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "$titleName - 纯色块甘特图 CSV 表格")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "导出/分享纯色块甘特图 CSV 电子表格")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)

            Toast.makeText(context, "已生成纯色块甘特图 CSV 电子表格: $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "生成 CSV 文件失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
