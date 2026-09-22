package me.paco.datecalculator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.RegionalHolidays
import me.paco.datecalculator.data.WeekendRule
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import me.paco.datecalculator.util.DateCalculatorUtils
import java.time.LocalDate
import java.time.YearMonth

/**
 * 动态根据当前实际时间与所选 HolidayRegion 独立切换的 3D 拟物月历水印背景 (深色模式显著提升对比度与清晰度)
 */
@Composable
fun DynamicCalendarWatermarkBg(
    modifier: Modifier = Modifier,
    weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
    enableHolidays: Boolean = true,
    holidayRegion: HolidayRegion = HolidayRegion.CHINA,
    isCurrentWeekBigWeek: Boolean = true
) {
    val isDark = LocalDarkTheme.current
    val today = remember { LocalDate.now() }
    val yearMonth = remember(today) { YearMonth.of(today.year, today.month) }

    // 深色模式提升水印不透明度至 28% (0.28f)，清晰可辨且沉稳高雅；浅色模式维持 12% 自然隐约效果
    val watermarkAlpha = if (isDark) 0.28f else 0.12f
    val textColor = if (isDark) Color.White.copy(alpha = watermarkAlpha) else Color(0xFF1E293B).copy(alpha = watermarkAlpha)
    val highlightColor = if (isDark) Color(0xFF60A5FA).copy(alpha = (watermarkAlpha * 2.8f).coerceAtMost(0.95f)) else NeumorphicAccent.copy(alpha = watermarkAlpha * 3.5f)
    val restDayColor = if (isDark) Color(0xFFFBBF24).copy(alpha = (watermarkAlpha * 2.2f).coerceAtMost(0.85f)) else Color(0xFFD97706).copy(alpha = watermarkAlpha * 2.2f)
    val shiftWorkColor = if (isDark) Color(0xFF38BDF8).copy(alpha = (watermarkAlpha * 2.5f).coerceAtMost(0.90f)) else Color(0xFF0284C7).copy(alpha = watermarkAlpha * 2.5f)

    val firstDayOfWeek = remember(yearMonth) { yearMonth.atDay(1).dayOfWeek.value % 7 } // 0=Sunday
    val daysInMonth = remember(yearMonth) { yearMonth.lengthOfMonth() }

    val monthHeaderStr = remember(today, holidayRegion) {
        DateCalculatorUtils.getWatermarkMonthHeader(today.monthValue, holidayRegion)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // 1. 巨幕年份 (极具艺术感的精细超细字体 FontWeight.ExtraLight) 与月份底纹
            Text(
                text = "${today.year}",
                fontSize = 105.sp,
                fontWeight = FontWeight.ExtraLight,
                color = textColor,
                letterSpacing = 8.sp,
                lineHeight = 105.sp
            )

            Text(
                text = monthHeaderStr,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                letterSpacing = 6.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. 7 列表格网图水印
            val weekHeaders = listOf("日", "一", "二", "三", "四", "五", "六")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekHeaders.forEach { header ->
                    Text(
                        text = header,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 渲染该月 1..daysInMonth 的数字网格
            val totalCells = firstDayOfWeek + daysInMonth
            val totalRows = (totalCells + 6) / 7

            for (row in 0 until totalRows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val cellDay = cellIndex - firstDayOfWeek + 1

                        if (cellDay in 1..daysInMonth) {
                            val date = yearMonth.atDay(cellDay)
                            val isToday = (cellDay == today.dayOfMonth)

                            val isShiftWork = RegionalHolidays.isShiftWorkday(date, holidayRegion)
                            val isStatutoryHoliday = enableHolidays && RegionalHolidays.isStatutoryHoliday(date, holidayRegion)
                            val isWeekend = weekendRule.isWeekend(date, isCurrentWeekBigWeek)

                            val isRestDay = (isStatutoryHoliday || (isWeekend && !isShiftWork))

                            val cellColor = when {
                                isToday -> highlightColor       // 今日高光
                                isShiftWork -> shiftWorkColor  // 调休补班日 (蓝色系)
                                isRestDay -> restDayColor      // 休假/节假日/周末 (暖金/琥珀色系)
                                else -> textColor              // 普通工作日
                            }

                            Text(
                                text = "$cellDay",
                                fontSize = 13.sp,
                                fontWeight = if (isToday || isShiftWork || isRestDay) FontWeight.ExtraBold else FontWeight.Medium,
                                color = cellColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
