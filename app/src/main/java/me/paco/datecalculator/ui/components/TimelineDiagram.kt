package me.paco.datecalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.ChronologicalBlock
import me.paco.datecalculator.data.StageSegmentResult
import me.paco.datecalculator.data.TimelineBlockType
import me.paco.datecalculator.data.decomposeChronologicalBlocks
import me.paco.datecalculator.util.CsvExportUtils
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LanguageUtils
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/**
 * 1:1 还原参考图的高质感多段/单段时序安排示意图 (配色 100% 随主题色 NeumorphicAccent 动态切换)
 * 支持 isEmbedded 嵌入模式：在 ResultCard 内部内嵌时完美消除白底背板溢出
 */
@Composable
fun TimelineDiagram(
    segments: List<StageSegmentResult>,
    finalDate: LocalDate,
    baseDate: LocalDate,
    planTitle: String = "",
    isEmbedded: Boolean = false,
    language: AppLanguage = AppLanguage.SIMPLIFIED_CHINESE,
    modifier: Modifier = Modifier
) {
    if (segments.isEmpty()) return

    val context = LocalContext.current
    val cardShape = RoundedCornerShape(22.dp)
    val effectiveLang = language.getEffectiveLanguage()

    // 全量时序分解块
    val allBlocks = remember(baseDate, finalDate) {
        decomposeChronologicalBlocks(
            startDate = baseDate,
            endDate = finalDate
        )
    }

    val totalDurationDays = remember(baseDate, finalDate) {
        abs(ChronoUnit.DAYS.between(baseDate, finalDate)).coerceAtLeast(1)
    }

    val totalWorkdays = remember(allBlocks) {
        allBlocks.filter { it.type == TimelineBlockType.WORKDAY || it.type == TimelineBlockType.SHIFT_WORKDAY }.sumOf { it.daysCount }
    }

    val totalHolidays = remember(allBlocks) {
        allBlocks.filter { it.type == TimelineBlockType.STATUTORY_HOLIDAY || it.type == TimelineBlockType.WEEKEND }.sumOf { it.daysCount }
    }

    val containerModifier = if (isEmbedded) {
        modifier.fillMaxWidth()
    } else {
        modifier
            .fillMaxWidth()
            .neumorphicExtruded(shape = cardShape, elevation = 5.dp)
            .background(NeumorphicBg, shape = cardShape)
            .padding(16.dp)
    }

    Box(modifier = containerModifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. 标头行：图标 + 标题 + 右侧 3D 导出 CSV 按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = NeumorphicAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = planTitle.ifBlank { LanguageUtils.getString("timeline_title", language) },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeumorphicAccent
                    )
                }

                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .neumorphicExtruded(shape = CircleShape, elevation = 2.dp)
                        .background(NeumorphicBg, shape = CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            val exportTitle = planTitle.ifBlank { LanguageUtils.getString("timeline_title", language) }
                            CsvExportUtils.exportMultiStageCsv(context, exportTitle, baseDate, finalDate, segments)
                        }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = NeumorphicAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = LanguageUtils.getString("export_csv", language),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeumorphicAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. 总历时与类型汇总行 (对照参考图：● 工作日: 3 天  ● 法定节假日: 7 天  总历时: 10 自然日)
            val workdayLabel = when (effectiveLang) {
                AppLanguage.ENGLISH -> "Workday"
                AppLanguage.JAPANESE -> "稼働日"
                AppLanguage.KOREAN -> "근무일"
                AppLanguage.TRADITIONAL_CHINESE -> "工作日"
                else -> "工作日"
            }

            val holidayLabel = when (effectiveLang) {
                AppLanguage.ENGLISH -> "Holidays"
                AppLanguage.JAPANESE -> "祝日・休日"
                AppLanguage.KOREAN -> "휴일"
                AppLanguage.TRADITIONAL_CHINESE -> "法定節假日"
                else -> "法定节假日"
            }

            val durationLabel = when (effectiveLang) {
                AppLanguage.ENGLISH -> "Total"
                AppLanguage.JAPANESE -> "総所要"
                AppLanguage.KOREAN -> "총 기간"
                AppLanguage.TRADITIONAL_CHINESE -> "總歷時"
                else -> "总历时"
            }

            val daysUnit = LanguageUtils.getString("days_unit", language)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeumorphicAccent)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$workdayLabel: $totalWorkdays $daysUnit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeumorphicAccent
                    )
                }

                if (totalHolidays > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$holidayLabel: $totalHolidays $daysUnit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }

                Text(
                    text = "$durationLabel: $totalDurationDays $daysUnit",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeumorphicTextPrimary.copy(alpha = 0.85f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. 全局总时序进度条 (对照参考图：圆角胶囊进度条，工作日跟随 NeumorphicAccent 主题色)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .neumorphicInset(shape = CircleShape, elevation = 2.dp)
                    .background(NeumorphicSunkenBg, shape = CircleShape)
                    .clip(CircleShape)
            ) {
                if (allBlocks.isNotEmpty()) {
                    allBlocks.forEach { block ->
                        val weight = (block.daysCount.toFloat() / totalDurationDays).coerceAtLeast(0.01f)
                        val blockColor = when (block.type) {
                            TimelineBlockType.WORKDAY, TimelineBlockType.SHIFT_WORKDAY -> NeumorphicAccent
                            TimelineBlockType.STATUTORY_HOLIDAY -> Color(0xFFEF4444)
                            TimelineBlockType.WEEKEND -> Color(0xFFF59E0B)
                        }
                        Box(
                            modifier = Modifier
                                .weight(weight)
                                .fillMaxHeight()
                                .background(blockColor)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(NeumorphicAccent)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(12.dp))

            // 4. 多段/单段子项目详细清单
            segments.forEachIndexed { idx, seg ->
                val isAdd = (seg.type == CalculationType.ADD)
                val typeTag = if (isAdd) LanguageUtils.getString("stage_add_label", language) else LanguageUtils.getString("stage_sub_label", language)
                val signSymbol = if (isAdd) "+" else "-"

                val segTotalDays = abs(ChronoUnit.DAYS.between(seg.startDate, seg.endDate)).coerceAtLeast(1L)
                val segWorkdays = seg.daysCount
                val segRestDays = (segTotalDays - segWorkdays).coerceAtLeast(0L)

                // 核心修复：单段推算时，不显示“(多段加)”标头字样
                val isSingleStage = (segments.size == 1 && (seg.remark == "单段推算" || isEmbedded))
                val titleText = if (isSingleStage) seg.remark else "${seg.remark} ($typeTag)"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // 左侧垂直主题色指示线 (高度贯穿子项，跟随 NeumorphicAccent 动态主题色)
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(52.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isAdd) NeumorphicAccent else Color(0xFFEF4444))
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        // 子项标题行：名称与多段类型 (左) vs 天数统计 (右)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = titleText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeumorphicAccent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            Text(
                                text = "$signSymbol${seg.daysCount} $workdayLabel (共 $segTotalDays 日)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeumorphicTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        // 子项专属时序比例条 (1:1 还原参考图)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .neumorphicInset(shape = CircleShape, elevation = 1.5.dp)
                                .background(NeumorphicSunkenBg, shape = CircleShape)
                                .clip(CircleShape)
                        ) {
                            val workWeight = (segWorkdays.toFloat() / segTotalDays).coerceAtLeast(0.01f)
                            val restWeight = (segRestDays.toFloat() / segTotalDays).coerceAtLeast(0.01f)

                            Box(
                                modifier = Modifier
                                    .weight(workWeight)
                                    .fillMaxHeight()
                                    .background(NeumorphicAccent)
                            )
                            if (segRestDays > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(restWeight)
                                        .fillMaxHeight()
                                        .background(Color(0xFFEF4444))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        // 日期区间底纹 (2026年09月27日 ➔ 2026年09月29日)
                        Text(
                            text = "${DateCalculatorUtils.formatDate(seg.startDate, language)}  ➔  ${DateCalculatorUtils.formatDate(seg.endDate, language)}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = NeumorphicTextPrimary.copy(alpha = 0.65f)
                        )
                    }
                }

                if (idx < segments.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
