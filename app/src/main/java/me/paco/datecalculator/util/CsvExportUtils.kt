package me.paco.datecalculator.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.StageSegmentResult
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
        segments: List<StageSegmentResult>
    ) {
        try {
            val titleName = planTitle.ifBlank { "排期计划" }
            val nowStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            val fileName = "DateSchedule_${titleName}_${nowStr}.csv"

            val totalDays = abs(ChronoUnit.DAYS.between(baseDate, finalDate))

            val sb = StringBuilder()
            // UTF-8 BOM 标头 (确保 Excel / WPS 打开中文不乱码)
            sb.append("\uFEFF")

            // 1. 计划概览与计算结果汇总
            sb.append("==== 日期计算器 - 多段排期推算结果汇总 ====\n")
            sb.append("排期计划名称,${titleName.replace(",", "，")}\n")
            sb.append("起始基准日期,${baseDate}\n")
            sb.append("推算最终日期,${finalDate}\n")
            sb.append("总历时天数,${totalDays} 自然日\n")
            sb.append("阶段总数量,${segments.size} 段\n\n")

            // 2. 多段时序明细表格 Header
            sb.append("阶段序号,备注事项,推算类型,设定天数,阶段起始日期,阶段结束日期,阶段实际历时\n")

            // 3. 逐行多段明细数据
            segments.forEachIndexed { index, seg ->
                val typeStr = if (seg.type == CalculationType.ADD) "加天数(+)" else "减天数(-)"
                val remarkStr = seg.remark.ifBlank { "第${index + 1}阶段" }.replace(",", "，")
                val segCalendarDays = abs(ChronoUnit.DAYS.between(seg.startDate, seg.endDate))
                sb.append("${index + 1},${remarkStr},${typeStr},${seg.daysCount},${seg.startDate},${seg.endDate},${segCalendarDays}天\n")
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
                putExtra(Intent.EXTRA_SUBJECT, "$titleName - 日期排期 CSV 表格")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "导出/分享 CSV 排期电子表格")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)

            Toast.makeText(context, "已生成 CSV 电子表格: $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "生成 CSV 文件失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
