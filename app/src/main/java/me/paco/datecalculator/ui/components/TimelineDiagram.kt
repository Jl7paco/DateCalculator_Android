package me.paco.datecalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import java.time.LocalDate

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

    val totalWorkdays = segments.sumOf { it.daysCount }
    val totalRestDays = segments.sumOf { it.restDaysCount }
    val grandTotalDays = (totalWorkdays + totalRestDays).coerceAtLeast(1L)

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
                    Text("多段加减工期排期示意图", fontWeight = FontWeight.Bold, color = NeumorphicAccent, fontSize = 14.sp)
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

            // 整体天数比例全景条 (Proportional Overview Bar)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "推算天数: $totalWorkdays 天",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeumorphicAccent
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "☕ 双休休市: $totalRestDays 天",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 比例双色进度条
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(NeumorphicSunkenBg)
                ) {
                    val workWeight = (totalWorkdays.toFloat() / grandTotalDays).coerceAtLeast(0.05f)
                    val restWeight = (totalRestDays.toFloat() / grandTotalDays).coerceAtLeast(0.01f)

                    Box(
                        modifier = Modifier
                            .weight(workWeight)
                            .fillMaxHeight()
                            .background(NeumorphicAccent)
                    )
                    if (totalRestDays > 0) {
                        Box(
                            modifier = Modifier
                                .weight(restWeight)
                                .fillMaxHeight()
                                .background(Color(0xFFF59E0B))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(14.dp))

            // 竖条轴向比例节点树 (按时间先后顺序严格排列)
            segments.forEachIndexed { index, seg ->
                val numZh = when (index + 1) {
                    1 -> "一"; 2 -> "二"; 3 -> "三"; 4 -> "四"; 5 -> "五"; else -> "${index + 1}"
                }
                val defaultTitle = "第${numZh}段时间"
                val stageTitle = seg.remark.ifBlank { defaultTitle }
                val isAdd = (seg.type == CalculationType.ADD)
                val stageActionLabel = if (isAdd) "多段加" else "多段减"
                val symbol = if (isAdd) "+" else "-"
                val stageRatio = (seg.daysCount.toFloat() / grandTotalDays).coerceIn(0.1f, 1f)

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

                    // 右侧竖条比例阶段卡片
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

                                Spacer(modifier = Modifier.height(4.dp))

                                // 阶段天数占比比例条 (Ratio Segment)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(NeumorphicSunkenBg)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction = stageRatio)
                                            .background(if (isAdd) NeumorphicAccent else Color(0xFFEF4444), shape = CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${seg.startDate}  ➔  ${seg.endDate}", fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.7f))
                            }
                        }

                        // 按先后顺序排列：中途休假/周末比例条段 (双休与法定节日区分色彩)
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
                                            text = "☕ 经过 ${seg.restDaysCount} 天周末双休/假期",
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
