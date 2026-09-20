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
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.StageSegmentResult
import me.paco.datecalculator.util.CsvExporter
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.TimelineBlockType
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimelineDiagram(
    baseDate: LocalDate,
    finalDate: LocalDate,
    segments: List<StageSegmentResult>,
    modeLabel: String,
    regionLabel: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 精准拆解全量阶段的时间轴 Block 序列 (按时间先后顺序)
    val allChronologicalBlocks = segments.flatMap { seg ->
        DateCalculatorUtils.decomposeChronologicalBlocks(
            startDate = seg.startDate,
            endDate = seg.endDate
        )
    }

    val totalWorkdaysCount = allChronologicalBlocks.filter { it.type == TimelineBlockType.WORKDAY }.sumOf { it.daysCount }
    val totalWeekendDaysCount = allChronologicalBlocks.filter { it.type == TimelineBlockType.WEEKEND_REST }.sumOf { it.daysCount }
    val totalStatutoryDaysCount = allChronologicalBlocks.filter { it.type == TimelineBlockType.STATUTORY_HOLIDAY }.sumOf { it.daysCount }
    val grandTotalCalendarDays = allChronologicalBlocks.sumOf { it.daysCount }.coerceAtLeast(1L)

    val hasFuturePrediction = finalDate.year >= 2027

    Box(
        modifier = modifier
            .fillMaxWidth()
            .neumorphicExtruded(shape = RoundedCornerShape(20.dp), elevation = 5.dp)
            .background(NeumorphicBg, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Timeline, contentDescription = null, tint = NeumorphicAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("总时间安排示意", fontWeight = FontWeight.Bold, color = NeumorphicAccent, fontSize = 14.sp)
                }

                // 📊 导出 CSV 表格按键
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
                        Text("导出 CSV", fontSize = 11.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1. 统一标注汇总行 (按对应颜色与文字统一说明)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(NeumorphicAccent))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("工作日: $totalWorkdaysCount 天", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeumorphicAccent)
                }

                if (totalWeekendDaysCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("周末双休: $totalWeekendDaysCount 天", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                    }
                }

                if (totalStatutoryDaysCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("🎉 法定节假日: $totalStatutoryDaysCount 天", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                }

                Text("总历时: $grandTotalCalendarDays 自然日", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicTextPrimary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. 总时间轴：按实际时间先后顺序与真实比例交替显示 (----工作日----  休息日----工作日---休息日)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(NeumorphicSunkenBg)
            ) {
                allChronologicalBlocks.forEach { block ->
                    val blockColor = when (block.type) {
                        TimelineBlockType.WORKDAY -> NeumorphicAccent
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEF3C7))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⚠️ 2027年及以后的节假日及调休安排包含智能算法预测（官方公布后自动校准）",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(14.dp))

            // 3. 竖条分段时间轴 (按时间先后顺序严格排列在每个阶段旁边)
            segments.forEachIndexed { index, seg ->
                val numZh = when (index + 1) {
                    1 -> "一"; 2 -> "二"; 3 -> "三"; 4 -> "四"; 5 -> "五"; else -> "${index + 1}"
                }
                val defaultTitle = "第${numZh}段时间"
                val stageTitle = seg.remark.ifBlank { defaultTitle }
                val isAdd = (seg.type == CalculationType.ADD)
                val stageActionLabel = if (isAdd) "多段加" else "多段减"
                val symbol = if (isAdd) "+" else "-"

                // 该阶段内部精准拆解的 Blocks 序列
                val stageBlocks = DateCalculatorUtils.decomposeChronologicalBlocks(seg.startDate, seg.endDate)
                val stageTotalCalDays = stageBlocks.sumOf { it.daysCount }.coerceAtLeast(1L)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // 左侧竖向轴线与节点 Badge
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isAdd) NeumorphicAccent else Color(0xFFEF4444)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${index + 1}", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }

                        // 竖连线
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(50.dp)
                                .background(if (isAdd) NeumorphicAccent.copy(alpha = 0.35f) else Color(0xFFEF4444).copy(alpha = 0.35f))
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 右侧分段时间轴 (按实际先后顺序排列)
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 2.dp)
                                .background(NeumorphicBg, shape = RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(stageTitle, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(if (isAdd) NeumorphicAccent.copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(stageActionLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isAdd) NeumorphicAccent else Color(0xFFEF4444))
                                        }
                                    }

                                    Text("$symbol${seg.daysCount} $modeLabel", fontWeight = FontWeight.ExtraBold, color = if (isAdd) NeumorphicAccent else Color(0xFFEF4444), fontSize = 13.sp)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // 分段时间轴：按时间先后顺序交替展示工作日、双休与法定节日
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(NeumorphicSunkenBg)
                                ) {
                                    stageBlocks.forEach { b ->
                                        val c = when (b.type) {
                                            TimelineBlockType.WORKDAY -> if (isAdd) NeumorphicAccent else Color(0xFFEF4444)
                                            TimelineBlockType.WEEKEND_REST -> Color(0xFFF59E0B)
                                            TimelineBlockType.STATUTORY_HOLIDAY -> Color(0xFFEF4444)
                                        }
                                        val w = (b.daysCount.toFloat() / stageTotalCalDays).coerceAtLeast(0.01f)

                                        Box(
                                            modifier = Modifier
                                                .weight(w)
                                                .fillMaxHeight()
                                                .background(c)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${seg.startDate}  ➔  ${seg.endDate}", fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.7f))
                            }
                        }

                        // 分段休息/周末明细说明
                        if (seg.restDaysCount > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "☕ 经过 ${seg.restDaysCount} 天周末双休/节假日",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                    Text(
                                        text = "共 ${seg.totalCalendarDays} 自然日",
                                        fontSize = 10.sp,
                                        color = Color(0xFFD97706).copy(alpha = 0.8f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 终点标记里程碑
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏁", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 2.dp)
                        .background(NeumorphicBg, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("最终达成日期", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NeumorphicTextPrimary)
                    Text("$finalDate", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF10B981))
                }
            }
        }
    }
}
