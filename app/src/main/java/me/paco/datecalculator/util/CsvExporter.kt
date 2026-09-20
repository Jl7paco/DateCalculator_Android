package me.paco.datecalculator.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.StageSegmentResult
import java.io.File
import java.time.LocalDate

object CsvExporter {

    fun exportStagesToCsv(
        context: Context,
        baseDate: LocalDate,
        finalDate: LocalDate,
        segments: List<StageSegmentResult>,
        modeLabel: String,
        regionLabel: String
    ) {
        val sb = StringBuilder()
        // CSV BOM 头，确保 Excel / 网页打开无乱码
        sb.append("\uFEFF")
        sb.append("阶段序号,阶段备注,计算模式,类型,推算天数,起始日期,结束日期,所含自然日天数,休息日/休市天数,国家地区\n")

        var totalDaysSum = 0L
        segments.forEach { seg ->
            val symbol = if (seg.type == CalculationType.ADD) "+" else "-"
            val cleanRemark = seg.remark.ifBlank { "阶段 ${seg.stageIndex + 1}" }.replace(",", "，")
            sb.append("${seg.stageIndex + 1},\"$cleanRemark\",$modeLabel,$symbol,${seg.daysCount},${seg.startDate},${seg.endDate},${seg.totalCalendarDays},${seg.restDaysCount},\"$regionLabel\"\n")
            totalDaysSum += seg.daysCount
        }

        sb.append("\n汇总统计,总阶段数: ${segments.size},最终到达日期: $finalDate,累计推算总天数: $totalDaysSum $modeLabel,基准起始日: $baseDate,,,\n")

        val csvContent = sb.toString()

        try {
            // 写入本地 cache 缓存区并共享
            val file = File(context.cacheDir, "DateCalculator_Timeline_${System.currentTimeMillis()}.csv")
            file.writeText(csvContent, Charsets.UTF_8)

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "📊 导出/分享 CSV 流程图表")
            context.startActivity(chooser)
            Toast.makeText(context, "已成功生成 CSV 图表文件！", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            // 兜底方案：复制完整 CSV 内容至剪贴板
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("TimelineCSV", csvContent))
            Toast.makeText(context, "CSV 内容已成功复制至剪贴板", Toast.LENGTH_LONG).show()
        }
    }
}
