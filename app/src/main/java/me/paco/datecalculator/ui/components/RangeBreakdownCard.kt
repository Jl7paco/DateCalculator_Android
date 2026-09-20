package me.paco.datecalculator.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import me.paco.datecalculator.ui.viewmodel.RangeBreakdownResult

@Composable
fun RangeBreakdownCard(
    visible: Boolean,
    result: RangeBreakdownResult,
    regionLabel: String,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val context = LocalContext.current
    val total = result.totalNaturalDays.coerceAtLeast(1L)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .neumorphicExtruded(shape = RoundedCornerShape(24.dp), elevation = 6.dp)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f), shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "区间拆算结果",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = NeumorphicTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${result.startDate}  ➔  ${result.endDate}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NeumorphicAccent
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 拆算数据统计网格 (4 个独立维度)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // 1. 自然日
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("总自然日", fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.7f))
                    Text("${result.totalNaturalDays} 天", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicTextPrimary)
                }

                // 2. 工作日
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💼 工作日", fontSize = 11.sp, color = NeumorphicAccent)
                    Text("${result.workdaysCount} 天", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent)
                }

                // 3. 周末双休
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("☕ 周末双休", fontSize = 11.sp, color = Color(0xFFD97706))
                    Text("${result.regularWeekendDaysCount} 天", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD97706))
                }

                // 4. 法定节假日
                if (result.statutoryHolidaysCount > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎉 节假日", fontSize = 11.sp, color = Color(0xFFEF4444))
                        Text("${result.statutoryHolidaysCount} 天", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEF4444))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 比例三色条形图
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(NeumorphicSunkenBg)
            ) {
                val workWeight = (result.workdaysCount.toFloat() / total).coerceAtLeast(0.01f)
                val weekendWeight = (result.regularWeekendDaysCount.toFloat() / total).coerceAtLeast(0.01f)
                val holidayWeight = (result.statutoryHolidaysCount.toFloat() / total).coerceAtLeast(0.01f)

                Box(
                    modifier = Modifier
                        .weight(workWeight)
                        .fillMaxHeight()
                        .background(NeumorphicAccent)
                )
                if (result.regularWeekendDaysCount > 0) {
                    Box(
                        modifier = Modifier
                            .weight(weekendWeight)
                            .fillMaxHeight()
                            .background(Color(0xFFF59E0B))
                    )
                }
                if (result.statutoryHolidaysCount > 0) {
                    Box(
                        modifier = Modifier
                            .weight(holidayWeight)
                            .fillMaxHeight()
                            .background(Color(0xFFEF4444))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(10.dp))

            // 底部地区描述与右侧 32dp 纯复制图标按键 (无任何文字重叠)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "节假日标准: $regionLabel",
                    fontSize = 11.sp,
                    color = NeumorphicTextPrimary.copy(alpha = 0.7f),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                val buttonShape = RoundedCornerShape(10.dp)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .neumorphicExtruded(shape = buttonShape, elevation = 3.dp)
                        .background(NeumorphicBg, shape = buttonShape)
                        .clip(buttonShape)
                        .clickable {
                            val clipText = "区间拆算 [${result.startDate} ➔ ${result.endDate}]: 总自然日 ${result.totalNaturalDays}天 | 工作日 ${result.workdaysCount}天 | 周末双休 ${result.regularWeekendDaysCount}天 | 节假日 ${result.statutoryHolidaysCount}天"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("RangeBreakdown", clipText))
                            Toast.makeText(context, "拆算结果已复制到剪贴板", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "复制拆算结果",
                        tint = NeumorphicAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
