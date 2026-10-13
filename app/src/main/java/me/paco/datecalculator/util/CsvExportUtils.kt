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
            val nowStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
            val fileName = "DateSchedule_${titleName}_${nowStr}.csv"

            val isForward = !baseDate.isAfter(finalDate)
            val startDate = if (isForward) baseDate else finalDate
            val endDate = if (isForward) finalDate else baseDate
            val totalCalendarDays = abs(ChronoUnit.DAYS.between(startDate, endDate)).coerceAtLeast(1L)

            // 1. 生成全局每日序列列表用于绘制 1:1 颜色对齐的可视化图表
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

            // 2. 构建全局 1:1 彩色方块可视化时间轴 (🟦 工作日 / 🟨 周末双休 / 🟥 法定节假日)
            val globalBarSb = StringBuilder()
            dailyList.forEach { (_, type) ->
                when (type) {
                    TimelineBlockType.WORKDAY, TimelineBlockType.SHIFT_WORKDAY -> globalBarSb.append("🟦")
                    TimelineBlockType.WEEKEND -> globalBarSb.append("🟨")
                    TimelineBlockType.STATUTORY_HOLIDAY -> globalBarSb.append("🟥")
                }
            }
            val globalVisualBar = globalBarSb.toString()

            val sb = StringBuilder()
            // UTF-8 BOM 标头 (确保 Excel / WPS 打开中文 100% 不乱码)
            sb.append("\uFEFF")

            // ===== 第一部分：计划概览与计算结果汇总 =====
            sb.append("==== 日期计算器 - 多段排期推算结果汇总 ====\n")
            sb.append("排期计划名称,${titleName.replace(",", "，")}\n")
            sb.append("起始基准日期,${DateCalculatorUtils.formatDateWithWeek(baseDate, language)}\n")
            sb.append("推算最终日期,${DateCalculatorUtils.formatDateWithWeek(finalDate, language)}\n")
            sb.append("总历时天数,${totalCalendarDays} 自然日\n")
            sb.append("包含工作日,${totalWorkdays} 天 (🟦)\n")
            sb.append("包含周末双休,${totalWeekendDays} 天 (🟨)\n")
            sb.append("包含法定节假日,${totalStatutoryHolidays} 天 (🟥)\n")
            sb.append("全局 1:1 彩色可视化时间轴,${globalVisualBar}\n\n")

            // ===== 第二部分：多段时序拆算明细表 =====
            sb.append("==== 多段时序拆算明细表 ====\n")
            sb.append("阶段序号,备注事项,推算类型,设定天数,阶段起始日期,阶段结束日期,实际历时,含休假天数,阶段 1:1 彩色时间轴\n")

            segments.forEachIndexed { index, seg ->
                val typeStr = if (seg.type == CalculationType.ADD) "加天数(+)" else "减天数(-)"
                val remarkStr = seg.remark.ifBlank { "第${index + 1}阶段" }.replace(",", "，")
                val segTotal = abs(ChronoUnit.DAYS.between(seg.startDate, seg.endDate)).coerceAtLeast(1L)

                // 生成该阶段的 1:1 彩色可视化时间轴柱条
                val segBarSb = StringBuilder()
                var segCurr = if (seg.startDate.isBefore(seg.endDate)) seg.startDate else seg.endDate
                val segEnd = if (seg.startDate.isBefore(seg.endDate)) seg.endDate else seg.startDate
                var segRestCount = 0L

                while (!segCurr.isAfter(segEnd)) {
                    val isShift = enableHolidays && !disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(segCurr, holidayRegion)
                    val isStat = enableHolidays && RegionalHolidays.isStatutoryHoliday(segCurr, holidayRegion)
                    val isWeekend = weekendRule.isWeekend(segCurr, isCurrentWeekBigWeek)

                    when {
                        isShift || (!isStat && !isWeekend) -> segBarSb.append("🟦")
                        isStat -> { segBarSb.append("🟥"); segRestCount++ }
                        else -> { segBarSb.append("🟨"); segRestCount++ }
                    }
                    segCurr = segCurr.plusDays(1)
                }

                sb.append("${index + 1},${remarkStr},${typeStr},${seg.daysCount},${seg.startDate},${seg.endDate},${segTotal}天,${segRestCount}天,${segBarSb}\n")
            }

            sb.append("\n")

            // ===== 第三部分：全日程逐日 1:1 彩色甘特图排期表 =====
            sb.append("==== 全日程逐日彩色甘特图排期表 ====\n")
            sb.append("序号,日期明细,日期属性,对应阶段备注,彩色甘特图进度\n")

            dailyList.forEachIndexed { idx, (date, type) ->
                val dateWithWeek = DateCalculatorUtils.formatDateWithWeek(date, language)
                val (typeDesc, visualIcon) = when (type) {
                    TimelineBlockType.WORKDAY -> "工作日" to "🟦🟦🟦🟦🟦 [工作日]"
                    TimelineBlockType.SHIFT_WORKDAY -> "调休上班" to "🟦🟦🟦🟦🟦 [调休班]"
                    TimelineBlockType.STATUTORY_HOLIDAY -> "法定节假日" to "🟥🟥🟥🟥🟥 [法定假]"
                    TimelineBlockType.WEEKEND -> "周末双休" to "🟨🟨🟨🟨🟨 [周末休]"
                }

                // 查找该日期落在哪个阶段
                val matchedSeg = segments.find { seg ->
                    val s = if (seg.startDate.isBefore(seg.endDate)) seg.startDate else seg.endDate
                    val e = if (seg.startDate.isBefore(seg.endDate)) seg.endDate else seg.startDate
                    !date.isBefore(s) && !date.isAfter(e)
                }
                val segName = matchedSeg?.remark?.ifBlank { "第${matchedSeg.stageIndex + 1}阶段" }?.replace(",", "，") ?: "主排期"

                sb.append("${idx + 1},${dateWithWeek},${typeDesc},${segName},${visualIcon}\n")
            }

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
                putExtra(Intent.EXTRA_SUBJECT, "$titleName - 1:1 彩色可视化排期 CSV 表格")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "导出/分享彩色 CSV 排期电子表格")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)

            Toast.makeText(context, "已生成彩色 CSV 电子表格: $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "生成 CSV 文件失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
