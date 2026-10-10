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
import me.paco.datecalculator.data.ChronologicalBlock
import me.paco.datecalculator.data.StageSegmentResult
import me.paco.datecalculator.data.TimelineBlockType
import me.paco.datecalculator.data.decomposeChronologicalBlocks
import me.paco.datecalculator.util.CsvExportUtils
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LanguageUtils
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimelineDiagram(
    segments: List<StageSegmentResult>,
    finalDate: LocalDate,
    baseDate: LocalDate,
    planTitle: String = "",
    language: AppLanguage = AppLanguage.SIMPLIFIED_CHINESE,
    modifier: Modifier = Modifier
) {
    if (segments.isEmpty()) return

    val context = LocalContext.current
    val cardShape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .neumorphicExtruded(shape = cardShape, elevation = 5.dp)
            .background(NeumorphicBg, shape = cardShape)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                        .background(Color(0xFF10B981), shape = CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            val exportTitle = planTitle.ifBlank { LanguageUtils.getString("timeline_title", language) }
                            CsvExportUtils.exportMultiStageCsv(context, exportTitle, baseDate, finalDate, segments)
                        }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = LanguageUtils.getString("export_csv", language),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 绘制多段阶段时间轴项
            segments.forEachIndexed { idx, seg ->
                val isAdd = (seg.type == CalculationType.ADD)
                val typeTag = if (isAdd) LanguageUtils.getString("stage_add_label", language) else LanguageUtils.getString("stage_sub_label", language)
                val badgeBg = if (isAdd) Color(0xFF10B981) else Color(0xFFEF4444)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = typeTag,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${seg.remark} (${seg.daysCount} 天)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeumorphicTextPrimary
                        )
                        Text(
                            text = "${DateCalculatorUtils.formatDate(seg.startDate, language)} ➔ ${DateCalculatorUtils.formatDate(seg.endDate, language)}",
                            fontSize = 11.sp,
                            color = NeumorphicTextPrimary.copy(alpha = 0.65f)
                        )
                    }
                }

                if (idx < segments.size - 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}
