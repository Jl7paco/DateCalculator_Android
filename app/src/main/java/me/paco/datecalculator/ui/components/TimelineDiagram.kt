package me.paco.datecalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.StageSegmentResult
import me.paco.datecalculator.util.ChronologicalBlock
import me.paco.datecalculator.util.CsvExporter
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LanguageUtils
import me.paco.datecalculator.util.TimelineBlockType
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimelineDiagram(
    baseDate: LocalDate,
    finalDate: LocalDate,
    segments: List<StageSegmentResult>,
    modeLabel: String = "工作日",
    regionLabel: String = "🇨🇳 中国大陆",
    showOuterCard: Boolean = true,
    showSubTimeline: Boolean = true,
    showExportButton: Boolean = true,
    language: AppLanguage = AppLanguage.SIMPLIFIED_CHINESE,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val allChronologicalBlocks = DateCalculatorUtils.decomposeChronologicalBlocks(
        startDate = baseDate,
        endDate = finalDate
    )

    val totalWorkdaysCount = allChronologicalBlocks.filter { it.type == TimelineBlockType.WORKDAY }.sumOf { it.daysCount }
    val totalWeekendDaysCount = allChronologicalBlocks.filter { it.type == TimelineBlockType.WEEKEND_REST }.sumOf { it.daysCount }
    val totalStatutoryDaysCount = allChronologicalBlocks.filter { it.type == TimelineBlockType.STATUTORY_HOLIDAY }.sumOf { it.daysCount }
    val grandTotalCalendarDays = allChronologicalBlocks.sumOf { it.daysCount }.coerceAtLeast(1L)

    val hasFuturePrediction = finalDate.year >= 2027
    val isSubtractMode = segments.firstOrNull()?.type == CalculationType.SUBTRACT

    val overviewTitle = LanguageUtils.getString("timeline_title", language)

    val totalDurationLabel = when (language) {
        AppLanguage.ENGLISH -> "Total: $grandTotalCalendarDays Days"
        AppLanguage.JAPANESE -> "総所要: $grandTotalCalendarDays 日"
        AppLanguage.KOREAN -> "총 기간: $grandTotalCalendarDays 일"
        AppLanguage.TRADITIONAL_CHINESE -> "總歷時: $grandTotalCalendarDays 自然日"
        else -> "总历时: $grandTotalCalendarDays 自然日"
    }

    val diagramContent = @Composable {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Timeline, contentDescription = null, tint = NeumorphicAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(overviewTitle, fontWeight = FontWeight.Bold, color = NeumorphicAccent, fontSize = 14.sp)
                }

                // 📊 导出 CSV 表格按键
                if (showExportButton) {
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                            .background(NeumorphicBg, shape = CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                CsvExporter.exportStagesToCsv(
                                    context = context,
                                    baseDate = baseDate,
                                    finalDate = finalDate,
                                    segments = segments,
                                    modeLabel = modeLabel,
                                    regionLabel = regionLabel
                                )
                            }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(LanguageUtils.getString("export_csv", language), fontSize = 11.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1. 统一标注汇总行
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(NeumorphicAccent))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${LanguageUtils.getString("workday", language)}: $totalWorkdaysCount ${LanguageUtils.getString("days_unit", language)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeumorphicAccent)
                }

                if (totalWeekendDaysCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${LanguageUtils.getString("weekend_rest", language)}: $totalWeekendDaysCount ${LanguageUtils.getString("days_unit", language)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                    }
                }

                if (totalStatutoryDaysCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${LanguageUtils.getString("statutory_holiday", language)}: $totalStatutoryDaysCount ${LanguageUtils.getString("days_unit", language)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                }

                Text(totalDurationLabel, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicTextPrimary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. 总时间轴：按实际时间先后顺序与真实比例交替显示
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(NeumorphicSunkenBg)
            ) {
                allChronologicalBlocks.forEach { block ->
                    val blockColor = when (block.type) {
                        TimelineBlockType.WORKDAY -> if (isSubtractMode) Color(0xFFEF4444) else NeumorphicAccent
                        TimelineBlockType.WEEKEND_REST -> Color(0xFFF59E0B)
                        TimelineBlockType.STATUTORY_HOLIDAY -> Color(0xFFEF4444)
                    }
                    val blockWeight = (block.daysCount.toFloat() / grandTotalCalendarDays).coerceAtLeast(0.01f)

                    Box(
                        modifier = Modifier
                            .weight(blockWeight)
                            .fillMaxHeight()
                            .background(blockColor)
                    )
                }
            }

            // 远期预测提示横幅
            if (hasFuturePrediction) {
                Spacer(modifier = Modifier.height(8.dp))
                val predictionText = when (language) {
                    AppLanguage.ENGLISH -> "⚠️ Holiday schedules for 2027+ include algorithmic predictions."
                    AppLanguage.JAPANESE -> "⚠️ 2027年以降の祝日情報は予測アルゴリズムを含みます。"
                    AppLanguage.KOREAN -> "⚠️ 2027년 이후 공휴일 일정은 예측 알고리즘이 포함됩니다."
                    AppLanguage.TRADITIONAL_CHINESE -> "⚠️ 2027年及以後的節假日及調休安排包含智能算法預測。"
                    else -> "⚠️ 2027年及以后的节假日及调休安排包含智能算法预测（官方公布后自动校准）"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEF3C7))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = predictionText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )
                }
            }

            // 3. 竖条分段时间轴
            if (showSubTimeline && segments.size > 1) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.12f))
                Spacer(modifier = Modifier.height(14.dp))

                segments.forEachIndexed { index, seg ->
                    val stageTitleText = if (seg.remark.isNotBlank()) seg.remark else LanguageUtils.getLocalizedStageTitle(index + 1, language)

                    val isAdd = (seg.type == CalculationType.ADD)
                    val stageActionLabel = if (isAdd) LanguageUtils.getString("stage_add_label", language) else LanguageUtils.getString("stage_sub_label", language)
                    val symbol = if (isAdd) "+" else "-"

                    val stageBlocks = DateCalculatorUtils.decomposeChronologicalBlocks(seg.startDate, seg.endDate)
                    val stageTotalCalDays = stageBlocks.sumOf { it.daysCount }.coerceAtLeast(1L)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(54.dp)
                                .clip(CircleShape)
                                .background(if (isAdd) NeumorphicAccent else Color(0xFFEF4444))
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$stageTitleText ($stageActionLabel)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAdd) NeumorphicAccent else Color(0xFFEF4444)
                                )

                                Text(
                                    text = "$symbol${seg.daysCount} $modeLabel (共 $stageTotalCalDays 日)",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeumorphicTextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(NeumorphicSunkenBg)
                            ) {
                                stageBlocks.forEach { block ->
                                    val bColor = when (block.type) {
                                        TimelineBlockType.WORKDAY -> if (isSubtractMode) Color(0xFFEF4444) else NeumorphicAccent
                                        TimelineBlockType.WEEKEND_REST -> Color(0xFFF59E0B)
                                        TimelineBlockType.STATUTORY_HOLIDAY -> Color(0xFFEF4444)
                                    }
                                    val bWeight = (block.daysCount.toFloat() / stageTotalCalDays).coerceAtLeast(0.01f)

                                    Box(
                                        modifier = Modifier
                                            .weight(bWeight)
                                            .fillMaxHeight()
                                            .background(bColor)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${DateCalculatorUtils.formatDate(seg.startDate, language)} ➔ ${DateCalculatorUtils.formatDate(seg.endDate, language)}",
                                fontSize = 10.5.sp,
                                color = NeumorphicTextPrimary.copy(alpha = 0.65f)
                            )
                        }
                    }

                    if (index < segments.size - 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    if (showOuterCard) {
        val outerShape22 = RoundedCornerShape(22.dp)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .neumorphicExtruded(shape = outerShape22, elevation = 5.dp)
                .background(NeumorphicBg, shape = outerShape22)
                .clip(outerShape22)
                .padding(14.dp)
        ) {
            diagramContent()
        }
    } else {
        diagramContent()
    }
}
