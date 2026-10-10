package me.paco.datecalculator.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import me.paco.datecalculator.ui.theme.LocalDarkTheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.RegionalHolidays
import me.paco.datecalculator.ui.components.HistoryOverlayDialog
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicIconButton
import me.paco.datecalculator.ui.components.NeumorphicSunkenBg
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import me.paco.datecalculator.ui.components.SettingsOverlayDialog
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LanguageUtils
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
    val lang = uiState.appLanguage
    val effectiveLang = lang.getEffectiveLanguage()
    val isChineseLanguage = (effectiveLang == AppLanguage.SIMPLIFIED_CHINESE || effectiveLang == AppLanguage.TRADITIONAL_CHINESE)
    val isSimplifiedChinese = (effectiveLang == AppLanguage.SIMPLIFIED_CHINESE)

    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedCalendarDate by remember { mutableStateOf(LocalDate.now()) }

    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "IconAnimation")

    val weatherFloatScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WeatherFloatScale"
    )

    val starPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "StarPulseScale"
    )

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
    val fortune = LunarCalendarUtils.getDailyFortune(selectedCalendarDate, constName, lang)
    val realWeatherData = uiState.liveWeatherData ?: WeatherUtils.getWeatherForecast(selectedCalendarDate)
    val weatherList = realWeatherData.dailyList

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
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // 顶栏 (36dp 高度, 15sp 标题，跟随语言设置动态翻译)
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
                    text = LanguageUtils.getString("app_title", lang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeumorphicTextPrimary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NeumorphicIconButton(
                    icon = Icons.Default.History,
                    onClick = { showHistoryDialog = true },
                    contentDescription = LanguageUtils.getString("history_title", lang),
                    size = 32.dp
                )

                NeumorphicIconButton(
                    icon = Icons.Default.Settings,
                    onClick = { showSettingsDialog = true },
                    contentDescription = LanguageUtils.getString("settings_title", lang),
                    size = 32.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ================= 月历视图 (切换月份的箭头直接贴合在月份两侧，避免与“今天”产生误解) =================
        if (homeConfig.showCalendar) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左侧组：日历图标 + 上个月 [<] 年月标题 [>] 下个月
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = NeumorphicAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))

                            // 上个月箭头 [<]
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .neumorphicExtruded(shape = CircleShape, elevation = 2.dp)
                                    .background(NeumorphicBg, shape = CircleShape)
                                    .clip(CircleShape)
                                    .clickable {
                                        currentYearMonth = currentYearMonth.minusMonths(1)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "上个月",
                                    tint = NeumorphicAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = LanguageUtils.getLocalizedYearMonth(currentYearMonth, lang),
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeumorphicTextPrimary
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            // 下个月箭头 [>]
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .neumorphicExtruded(shape = CircleShape, elevation = 2.dp)
                                    .background(NeumorphicBg, shape = CircleShape)
                                    .clip(CircleShape)
                                    .clickable {
                                        currentYearMonth = currentYearMonth.plusMonths(1)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "下个月",
                                    tint = NeumorphicAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        // 右侧：独立“今天”跳转胶囊按键
                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .neumorphicExtruded(shape = CircleShape, elevation = 2.dp)
                                .background(NeumorphicAccent, shape = CircleShape)
                                .clip(CircleShape)
                                .clickable {
                                    currentYearMonth = YearMonth.now()
                                    selectedCalendarDate = LocalDate.now()
                                }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = LanguageUtils.getString("today", lang),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val weekTitles = LanguageUtils.getWeekHeaders(lang)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        weekTitles.forEachIndexed { idx, w ->
                            val isWeekendCol = (idx == 0 || idx == 6)
                            Text(
                                text = w,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isWeekendCol) Color(0xFFEF4444) else NeumorphicTextPrimary.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

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

                                    // 核心规则：仅在简体中文/繁体中文界面设置下展示农历
                                    val dayLunar = LunarCalendarUtils.solarToLunar(cellDate)
                                    val lunarText = if (isChineseLanguage) {
                                        if (dayLunar.solarTerm.isNotEmpty()) dayLunar.solarTerm
                                        else if (dayLunar.festival.isNotEmpty()) dayLunar.festival.take(2)
                                        else dayLunar.lunarDayName
                                    } else ""

                                    val cellShape = RoundedCornerShape(10.dp)
                                    val cellModifier = if (isSelected) {
                                        Modifier
                                            .neumorphicExtruded(shape = cellShape, elevation = 3.dp)
                                            .background(NeumorphicAccent, shape = cellShape)
                                    } else if (isToday) {
                                        Modifier
                                            .border(1.2.dp, NeumorphicAccent, shape = cellShape)
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
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                val textColor = if (isSelected) Color.White
                                                else if (isStatutory) Color(0xFFEF4444)
                                                else if (isShift) Color(0xFF10B981)
                                                else if (isWeekend) Color(0xFFF59E0B)
                                                else NeumorphicTextPrimary

                                                Text(
                                                    text = dayNum.toString(),
                                                    fontSize = 12.5.sp,
                                                    fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Bold,
                                                    color = textColor,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )

                                                // 规则：休与班的脚标字只在简体中文设置下才显示
                                                if (isSimplifiedChinese && isStatutory) {
                                                    Text(" 休", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color(0xFFEF4444))
                                                } else if (isSimplifiedChinese && isShift) {
                                                    Text(" 班", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color(0xFF10B981))
                                                }
                                            }

                                            if (lunarText.isNotEmpty()) {
                                                Text(
                                                    text = lunarText,
                                                    fontSize = 9.sp,
                                                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else NeumorphicTextPrimary.copy(alpha = 0.6f),
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
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

            // 增加透气拉开的舒适间距，防止下面的信息卡片紧贴月历底部
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ================= 当日黄历、节气、农历、天气、星座运势 =================

        // 1. 当日农历、节气与老黄历宜忌 Card (核心规则：仅在简体中文/繁体中文界面设置下展示)
        if (isChineseLanguage && (homeConfig.showLunar || homeConfig.showSolarTerms || homeConfig.showAlmanac)) {
            val combinedShape = RoundedCornerShape(22.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphicExtruded(shape = combinedShape, elevation = 5.dp)
                    .background(NeumorphicBg, shape = combinedShape)
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = DateCalculatorUtils.formatDateWithWeek(selectedCalendarDate, lang),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeumorphicTextPrimary
                            )
                        }

                        if (homeConfig.showSolarTerms && todayLunar.solarTerm.isNotEmpty()) {
                            val termIcon = LunarCalendarUtils.getSolarTermIcon(todayLunar.solarTerm)
                            SuggestionChip(
                                onClick = {},
                                shape = CircleShape,
                                label = { Text("$termIcon ${todayLunar.solarTerm}", fontWeight = FontWeight.Bold, fontSize = 10.5.sp) }
                            )
                        }
                    }

                    if (homeConfig.showLunar) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "农历 ${todayLunar.ganZhiYear} (${todayLunar.zodiac}) 年 ${if (todayLunar.isLeapMonth) "闰" else ""}${todayLunar.lunarMonthName}${todayLunar.lunarDayName}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeumorphicAccent
                        )
                    }

                    if (homeConfig.showAlmanac) {
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(6.dp))

                        val isDark = LocalDarkTheme.current
                        val yiChipBg = if (isDark) Color(0xFF065F46).copy(alpha = 0.55f) else Color(0xFFD1FAE5)
                        val yiChipBorder = if (isDark) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFF10B981).copy(alpha = 0.35f)
                        val yiChipText = if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857)

                        val jiChipBg = if (isDark) Color(0xFF334155).copy(alpha = 0.65f) else Color(0xFFF1F5F9)
                        val jiChipBorder = if (isDark) Color(0xFF94A3B8).copy(alpha = 0.5f) else Color(0xFF94A3B8).copy(alpha = 0.35f)
                        val jiChipText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)

                        // 老黄历宜忌 (跟随语言翻译)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(LanguageUtils.getString("yi_label", lang), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            FlowRow(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                almanac.yiList.forEach { yiItem ->
                                    val translatedYi = LanguageUtils.getLocalizedAlmanacItem(yiItem, lang)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(yiChipBg)
                                            .border(0.5.dp, yiChipBorder, shape = RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(translatedYi, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = yiChipText)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF64748B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(LanguageUtils.getString("ji_label", lang), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            FlowRow(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                almanac.jiList.forEach { jiItem ->
                                    val translatedJi = LanguageUtils.getLocalizedAlmanacItem(jiItem, lang)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(jiChipBg)
                                            .border(0.5.dp, jiChipBorder, shape = RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(translatedJi, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = jiChipText)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // 2. 当日及未来三日天气预报 Card (当前实时温度与当前体感置于卡片右上角，每日预报移除体感)
        if (homeConfig.showWeather) {
            val weatherShape = RoundedCornerShape(22.dp)
            val unit = uiState.temperatureUnit

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphicExtruded(shape = weatherShape, elevation = 5.dp)
                    .background(NeumorphicBg, shape = weatherShape)
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左上角：定位位置
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.fetchCurrentGpsLocation(context) }
                        ) {
                            Icon(imageVector = Icons.Default.WbSunny, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            val localizedCity = LanguageUtils.getLocalizedCityName(uiState.currentCityName, lang)
                            Text("📍 $localizedCity", fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent, fontSize = 13.5.sp)
                        }

                        // 右上角：当前温度与当前体感温度
                        val curDisplayTemp = unit.convertTemp(realWeatherData.currentTemp)
                        val curDisplayFeels = unit.convertTemp(realWeatherData.currentFeelsLikeTemp)
                        val feelsLabel = when (effectiveLang) {
                            AppLanguage.ENGLISH -> "Feels"
                            AppLanguage.JAPANESE -> "体感"
                            AppLanguage.KOREAN -> "체감"
                            AppLanguage.TRADITIONAL_CHINESE -> "體感"
                            else -> "体感"
                        }

                        Text(
                            text = "$curDisplayTemp${unit.symbol} · $feelsLabel $curDisplayFeels${unit.symbol}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeumorphicAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        weatherList.forEach { weather ->
                            val displayMin = unit.convertTemp(weather.tempMin)
                            val displayMax = unit.convertTemp(weather.tempMax)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 2.dp)
                                    .background(NeumorphicSunkenBg, shape = RoundedCornerShape(12.dp))
                                    .padding(vertical = 8.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(LanguageUtils.getDayName(weather.dayName, lang), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = weather.iconEmoji,
                                        fontSize = 17.sp,
                                        modifier = Modifier.graphicsLayer {
                                            scaleX = weatherFloatScale
                                            scaleY = weatherFloatScale
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(LanguageUtils.getWeatherCondition(weather.condition, lang), fontSize = 10.sp, fontWeight = FontWeight.Medium, color = NeumorphicTextPrimary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$displayMin${unit.symbol}~$displayMax${unit.symbol}", fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // 3. 星座与运势 Card (星座名称多语言翻译 + 星动微缩放)
        if (homeConfig.showZodiacFortune) {
            val fortuneShape = RoundedCornerShape(22.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphicExtruded(shape = fortuneShape, elevation = 5.dp)
                    .background(NeumorphicBg, shape = fortuneShape)
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
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NeumorphicAccent,
                                modifier = Modifier
                                    .size(16.dp)
                                    .graphicsLayer {
                                        scaleX = starPulseScale
                                        scaleY = starPulseScale
                                    }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val localizedConstellation = LanguageUtils.getLocalizedConstellation(fortune.constellation, lang)
                            Text("$localizedConstellation (${fortune.dateRange}) ${LanguageUtils.getString("fortune_suffix", lang)}", fontWeight = FontWeight.Bold, color = NeumorphicAccent, fontSize = 13.sp)
                        }

                        Row {
                            repeat(fortune.starRating) {
                                Text("⭐", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = fortune.summary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = NeumorphicTextPrimary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("${LanguageUtils.getString("lucky_number", lang)}: ${fortune.luckyNumber}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = NeumorphicAccent)
                        Text("${LanguageUtils.getString("lucky_color", lang)}: ${fortune.luckyColor}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = NeumorphicAccent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
