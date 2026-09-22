package me.paco.datecalculator.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import me.paco.datecalculator.data.RegionalHolidays
import me.paco.datecalculator.ui.components.HistoryOverlayDialog
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicSunkenBg
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import me.paco.datecalculator.ui.components.SettingsOverlayDialog
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LunarCalendarUtils
import me.paco.datecalculator.util.WeatherUtils
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val homeConfig = uiState.homeConfig

    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedCalendarDate by remember { mutableStateOf(LocalDate.now()) }

    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            viewModel.fetchCurrentGpsLocation(context)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchCurrentGpsLocation(context)
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val todayLunar = LunarCalendarUtils.solarToLunar(selectedCalendarDate)
    val almanac = LunarCalendarUtils.getAlmanacYiJi(selectedCalendarDate)
    val (constName, constEmoji) = LunarCalendarUtils.getConstellationInfo(selectedCalendarDate)
    val fortune = LunarCalendarUtils.getDailyFortune(selectedCalendarDate, constName)
    val weatherList = uiState.liveWeatherList ?: WeatherUtils.getWeatherForecast(selectedCalendarDate)

    HistoryOverlayDialog(
        visible = showHistoryDialog,
        onDismiss = { showHistoryDialog = false },
        viewModel = viewModel,
        uiState = uiState
    )

    SettingsOverlayDialog(
        visible = showSettingsDialog,
        onDismiss = { showSettingsDialog = false },
        viewModel = viewModel,
        uiState = uiState
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
    ) {
        // 顶栏 (36dp 高度, 15sp 标题, 右上角历史与设置图标)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = NeumorphicAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "日期计算器 首页",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeumorphicTextPrimary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // 历史记录图标
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                        .background(NeumorphicBg, shape = CircleShape)
                        .clip(CircleShape)
                        .clickable { showHistoryDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "查看历史记录",
                        tint = NeumorphicAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // 设置图标
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                        .background(NeumorphicBg, shape = CircleShape)
                        .clip(CircleShape)
                        .clickable { showSettingsDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "打开设置",
                        tint = NeumorphicAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ================= 上面 1/2: 月历视图 (无硬外框，沉浸于背景) =================
        if (homeConfig.showCalendar) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = NeumorphicAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${currentYearMonth.year}年 ${currentYearMonth.monthValue}月",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeumorphicTextPrimary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                                    .background(NeumorphicBg, shape = CircleShape)
                                    .clip(CircleShape)
                                    .clickable {
                                        currentYearMonth = currentYearMonth.minusMonths(1)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "上个月", tint = NeumorphicAccent, modifier = Modifier.size(16.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .height(32.dp)
                                    .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                                    .background(NeumorphicAccent, shape = CircleShape)
                                    .clip(CircleShape)
                                    .clickable {
                                        currentYearMonth = YearMonth.now()
                                        selectedCalendarDate = LocalDate.now()
                                    }
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("今天", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                                    .background(NeumorphicBg, shape = CircleShape)
                                    .clip(CircleShape)
                                    .clickable {
                                        currentYearMonth = currentYearMonth.plusMonths(1)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "下个月", tint = NeumorphicAccent, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val weekTitles = listOf("日", "一", "二", "三", "四", "五", "六")
                    Row(modifier = Modifier.fillMaxWidth()) {
                        weekTitles.forEachIndexed { idx, w ->
                            val isWeekendCol = (idx == 0 || idx == 6)
                            Text(
                                text = w,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isWeekendCol) Color(0xFFEF4444) else NeumorphicTextPrimary.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val firstDayOfMonth = currentYearMonth.atDay(1)
                    val daysInMonth = currentYearMonth.lengthOfMonth()
                    val startOffset = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Sunday
                    val totalGridCells = ((daysInMonth + startOffset + 6) / 7) * 7

                    val rows = totalGridCells / 7
                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (c in 0 until 7) {
                                val cellIdx = r * 7 + c
                                val dayNum = cellIdx - startOffset + 1

                                if (dayNum in 1..daysInMonth) {
                                    val cellDate = currentYearMonth.atDay(dayNum)
                                    val isToday = cellDate == LocalDate.now()
                                    val isSelected = cellDate == selectedCalendarDate

                                    val isStatutory = uiState.enableChineseHolidays && RegionalHolidays.isStatutoryHoliday(cellDate, uiState.holidayRegion)
                                    val isShift = uiState.enableChineseHolidays && !uiState.disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(cellDate, uiState.holidayRegion)
                                    val isWeekend = uiState.weekendRule.isWeekend(cellDate, uiState.isCurrentWeekBigWeek)

                                    val dayLunar = LunarCalendarUtils.solarToLunar(cellDate)
                                    val lunarText = if (dayLunar.solarTerm.isNotEmpty()) dayLunar.solarTerm
                                                    else if (dayLunar.festival.isNotEmpty()) dayLunar.festival.take(2)
                                                    else dayLunar.lunarDayName

                                    val cellShape = RoundedCornerShape(10.dp)
                                    val cellModifier = if (isSelected) {
                                        Modifier
                                            .neumorphicExtruded(shape = cellShape, elevation = 4.dp)
                                            .background(NeumorphicAccent, shape = cellShape)
                                    } else if (isToday) {
                                        Modifier
                                            .border(1.5.dp, NeumorphicAccent, shape = cellShape)
                                            .background(NeumorphicBg, shape = cellShape)
                                    } else {
                                        Modifier.background(Color.Transparent)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .then(cellModifier)
                                            .clip(cellShape)
                                            .clickable {
                                                selectedCalendarDate = cellDate
                                                viewModel.updateBaseDate(cellDate)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                val textColor = if (isSelected) Color.White
                                                else if (isStatutory) Color(0xFFEF4444)
                                                else if (isShift) Color(0xFF10B981)
                                                else if (isWeekend) Color(0xFFF59E0B)
                                                else NeumorphicTextPrimary

                                                Text(
                                                    text = dayNum.toString(),
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Bold,
                                                    color = textColor
                                                )

                                                if (isStatutory) {
                                                    Text(" 休", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color(0xFFEF4444))
                                                } else if (isShift) {
                                                    Text(" 班", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color(0xFF10B981))
                                                }
                                            }

                                            Text(
                                                text = lunarText,
                                                fontSize = 9.sp,
                                                color = if (isSelected) Color.White.copy(alpha = 0.85f) else NeumorphicTextPrimary.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // ================= 下面 1/2: 当日黄历、节气、农历、星座、运势、天气 =================

        // 1. 当日农历、节气与老黄历宜忌 二合一 Card (取消“当日老黄历宜忌”标题)
        if (homeConfig.showLunar || homeConfig.showSolarTerms || homeConfig.showAlmanac) {
            val combinedShape = RoundedCornerShape(20.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphicExtruded(shape = combinedShape, elevation = 5.dp)
                    .background(NeumorphicBg, shape = combinedShape)
                    .clip(combinedShape)
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = DateCalculatorUtils.formatDateWithWeek(selectedCalendarDate),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeumorphicTextPrimary
                            )
                        }

                        if (homeConfig.showSolarTerms && todayLunar.solarTerm.isNotEmpty()) {
                            val termIcon = LunarCalendarUtils.getSolarTermIcon(todayLunar.solarTerm)
                            SuggestionChip(
                                onClick = {},
                                shape = CircleShape,
                                label = { Text("$termIcon ${todayLunar.solarTerm}", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                            )
                        }
                    }

                    if (homeConfig.showLunar) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "农历 ${todayLunar.ganZhiYear} (${todayLunar.zodiac}) 年 ${if (todayLunar.isLeapMonth) "闰" else ""}${todayLunar.lunarMonthName}${todayLunar.lunarDayName}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeumorphicAccent
                        )
                    }

                    if (homeConfig.showAlmanac) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // 老黄历宜忌 (取消“当日老黄历宜忌”标题)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("宜", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            FlowRow(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                almanac.yiList.forEach { yiItem ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(yiItem, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF64748B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("忌", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            FlowRow(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                almanac.jiList.forEach { jiItem ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF64748B).copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(jiItem, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // 3. 星座与运势 Card
        if (homeConfig.showZodiacFortune) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphicExtruded(shape = RoundedCornerShape(20.dp), elevation = 5.dp)
                    .background(NeumorphicBg, shape = RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${fortune.emoji} ${fortune.constellation} (${fortune.dateRange}) 每日运势", fontWeight = FontWeight.Bold, color = NeumorphicAccent, fontSize = 14.sp)
                        }

                        Row {
                            repeat(fortune.starRating) {
                                Text("⭐", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = fortune.summary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = NeumorphicTextPrimary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("幸运数字: ${fortune.luckyNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeumorphicAccent)
                        Text("幸运颜色: ${fortune.luckyColor}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeumorphicAccent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // 4. 当日及未来三日天气预报 Card
        if (homeConfig.showWeather) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphicExtruded(shape = RoundedCornerShape(20.dp), elevation = 5.dp)
                    .background(NeumorphicBg, shape = RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.fetchCurrentGpsLocation(context) }
                    ) {
                        Icon(imageVector = Icons.Default.WbSunny, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📍 ${uiState.currentCityName} · 当地及未来三日天气推算", fontWeight = FontWeight.Bold, color = NeumorphicAccent, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        weatherList.forEach { weather ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .neumorphicInset(shape = RoundedCornerShape(14.dp), elevation = 3.dp)
                                    .background(NeumorphicSunkenBg, shape = RoundedCornerShape(14.dp))
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(weather.dayName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(weather.iconEmoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(weather.condition, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = NeumorphicTextPrimary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("${weather.tempMin}°~${weather.tempMax}°", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
