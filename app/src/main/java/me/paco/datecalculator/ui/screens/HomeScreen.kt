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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.HomeConfig
import me.paco.datecalculator.data.RegionalHolidays
import me.paco.datecalculator.data.ZodiacFortune
import me.paco.datecalculator.ui.components.HistoryOverlayDialog
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicIconButton
import me.paco.datecalculator.ui.components.NeumorphicSunkenBg
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import me.paco.datecalculator.ui.components.SettingsOverlayDialog
import me.paco.datecalculator.ui.components.dialogDropShadow3D
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.AlmanacYiJi
import me.paco.datecalculator.util.DailyWeather
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LanguageUtils
import me.paco.datecalculator.util.LunarCalendarUtils
import me.paco.datecalculator.util.LunarDate
import me.paco.datecalculator.util.RealTimeWeatherData
import me.paco.datecalculator.util.SixHourForecast
import me.paco.datecalculator.util.TemperatureUnit
import me.paco.datecalculator.util.WeatherUtils
import java.time.LocalDate
import java.time.YearMonth

/**
 * 模块化、极速 JIT 机器码编译重构的首页 (通过函数分块将字节码控制在编译器上限以内，恢复 120Hz 极速流畅体验)
 */
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
    val isDark = LocalDarkTheme.current

    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedCalendarDate by remember { mutableStateOf(LocalDate.now()) }

    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var selectedWeatherDayDetail by remember { mutableStateOf<Pair<DailyWeather, Int>?>(null) }

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

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            viewModel.fetchCurrentGpsLocation(context, force = true)
        }
    }

    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            viewModel.fetchCurrentGpsLocation(context, force = true)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val todayLunar = remember(selectedCalendarDate) { LunarCalendarUtils.solarToLunar(selectedCalendarDate) }
    val almanac = remember(selectedCalendarDate) { LunarCalendarUtils.getAlmanacYiJi(selectedCalendarDate) }
    val (constName, _) = remember(selectedCalendarDate) { LunarCalendarUtils.getConstellationInfo(selectedCalendarDate) }
    val fortune = remember(selectedCalendarDate, constName, lang) { LunarCalendarUtils.getDailyFortune(selectedCalendarDate, constName, lang) }
    val realWeatherData = uiState.liveWeatherData ?: remember(selectedCalendarDate) { WeatherUtils.getWeatherForecast(selectedCalendarDate) }

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
        // 1. 顶栏 (标题 + 历史/设置按键)
        HomeHeaderSection(
            lang = lang,
            onHistoryClick = { showHistoryDialog = true },
            onSettingsClick = { showSettingsDialog = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. 月历视图卡片
        if (homeConfig.showCalendar) {
            HomeCalendarSection(
                currentYearMonth = currentYearMonth,
                selectedCalendarDate = selectedCalendarDate,
                uiState = uiState,
                lang = lang,
                isChineseLanguage = isChineseLanguage,
                isSimplifiedChinese = isSimplifiedChinese,
                onMonthChange = { currentYearMonth = it },
                onTodayClick = {
                    currentYearMonth = YearMonth.now()
                    selectedCalendarDate = LocalDate.now()
                },
                onDateSelect = { cellDate ->
                    selectedCalendarDate = cellDate
                    viewModel.updateBaseDate(cellDate)
                }
            )

            Spacer(modifier = Modifier.height(28.dp))
        }

        // 3. 当日天气预报 Card (置顶)
        if (homeConfig.showWeather) {
            HomeWeatherSection(
                uiState = uiState,
                realWeatherData = realWeatherData,
                lang = lang,
                effectiveLang = effectiveLang,
                weatherFloatScale = weatherFloatScale,
                onLocationClick = { viewModel.fetchCurrentGpsLocation(context) },
                onDayClick = { weather, idx -> selectedWeatherDayDetail = Pair(weather, idx) }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // 4. 当日农历、节气与老黄历宜忌 Card
        if (isChineseLanguage && (homeConfig.showLunar || homeConfig.showSolarTerms || homeConfig.showAlmanac)) {
            HomeAlmanacSection(
                selectedCalendarDate = selectedCalendarDate,
                todayLunar = todayLunar,
                almanac = almanac,
                homeConfig = homeConfig,
                lang = lang,
                effectiveLang = effectiveLang,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // 5. 星座与每日运势 Card
        if (homeConfig.showZodiacFortune) {
            HomeFortuneSection(
                fortune = fortune,
                homeConfig = homeConfig,
                lang = lang
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (selectedWeatherDayDetail != null) {
            val (_, dayIdx) = selectedWeatherDayDetail!!
            WeatherDetailOverlayDialog(
                visible = true,
                weatherList = realWeatherData.dailyList,
                dayIndex = dayIdx,
                cityName = uiState.currentCityName,
                temperatureUnit = uiState.temperatureUnit,
                language = lang,
                onDismiss = { selectedWeatherDayDetail = null }
            )
        }
    }
}

/**
 * 1. 首页顶栏 (独立子组件)
 */
@Composable
private fun HomeHeaderSection(
    lang: AppLanguage,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
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
                onClick = onHistoryClick,
                contentDescription = LanguageUtils.getString("history_title", lang),
                size = 32.dp
            )

            NeumorphicIconButton(
                icon = Icons.Default.Settings,
                onClick = onSettingsClick,
                contentDescription = LanguageUtils.getString("settings_title", lang),
                size = 32.dp
            )
        }
    }
}

/**
 * 2. 首页月历视图卡片 (独立重组隔离子组件)
 */
@Composable
private fun HomeCalendarSection(
    currentYearMonth: YearMonth,
    selectedCalendarDate: LocalDate,
    uiState: DateCalculatorUiState,
    lang: AppLanguage,
    isChineseLanguage: Boolean,
    isSimplifiedChinese: Boolean,
    onMonthChange: (YearMonth) -> Unit,
    onTodayClick: () -> Unit,
    onDateSelect: (LocalDate) -> Unit
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = NeumorphicAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .neumorphicExtruded(shape = CircleShape, elevation = 2.dp)
                            .background(NeumorphicBg, shape = CircleShape)
                            .clip(CircleShape)
                            .clickable { onMonthChange(currentYearMonth.minusMonths(1)) },
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

                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .neumorphicExtruded(shape = CircleShape, elevation = 2.dp)
                            .background(NeumorphicBg, shape = CircleShape)
                            .clip(CircleShape)
                            .clickable { onMonthChange(currentYearMonth.plusMonths(1)) },
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

                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .neumorphicExtruded(shape = CircleShape, elevation = 2.dp)
                        .background(NeumorphicAccent, shape = CircleShape)
                        .clip(CircleShape)
                        .clickable { onTodayClick() }
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
            val startOffset = firstDayOfMonth.dayOfWeek.value % 7
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
                                    .clickable { onDateSelect(cellDate) },
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
}

/**
 * 3. 首页天气预报 Card (独立重组隔离子组件)
 */
@Composable
private fun HomeWeatherSection(
    uiState: DateCalculatorUiState,
    realWeatherData: RealTimeWeatherData,
    lang: AppLanguage,
    effectiveLang: AppLanguage,
    weatherFloatScale: Float,
    onLocationClick: () -> Unit,
    onDayClick: (DailyWeather, Int) -> Unit
) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLocationClick() }
                ) {
                    Icon(imageVector = Icons.Default.WbSunny, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    val localizedCity = LanguageUtils.getLocalizedCityName(uiState.currentCityName, lang)
                    Text("📍 $localizedCity", fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent, fontSize = 13.5.sp)
                }

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
                realWeatherData.dailyList.forEachIndexed { i, weather ->
                    val displayMin = unit.convertTemp(weather.tempMin)
                    val displayMax = unit.convertTemp(weather.tempMax)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 2.dp)
                            .background(NeumorphicSunkenBg, shape = RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onDayClick(weather, i) }
                            .padding(vertical = 8.dp, horizontal = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = LanguageUtils.getDayName(weather.dayName, lang),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeumorphicTextPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = weather.iconEmoji,
                                fontSize = 17.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        scaleX = weatherFloatScale
                                        scaleY = weatherFloatScale
                                    }
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = LanguageUtils.getWeatherCondition(weather.condition, lang),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = NeumorphicTextPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "💧 ${weather.precipProb}%",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "$displayMin${unit.symbol}~$displayMax${unit.symbol}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeumorphicAccent,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4. 农历、节气与老黄历 Card (独立重组隔离子组件)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeAlmanacSection(
    selectedCalendarDate: LocalDate,
    todayLunar: LunarDate,
    almanac: AlmanacYiJi,
    homeConfig: HomeConfig,
    lang: AppLanguage,
    effectiveLang: AppLanguage,
    isDark: Boolean
) {
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

                if (homeConfig.showSolarTerms) {
                    val (nextTerm, countdownDays) = LunarCalendarUtils.getNextSolarTermCountdown(selectedCalendarDate)
                    val termIcon = LunarCalendarUtils.getSolarTermIcon(nextTerm)
                    val localizedTerm = LunarCalendarUtils.getLocalizedSolarTerm(nextTerm, lang)

                    val chipText = if (countdownDays == 0) {
                        "$termIcon $localizedTerm"
                    } else {
                        when (effectiveLang) {
                            AppLanguage.ENGLISH -> "⏳ ${countdownDays}d to $localizedTerm"
                            AppLanguage.JAPANESE -> "⏳ ${localizedTerm}まで${countdownDays}日"
                            AppLanguage.KOREAN -> "⏳ ${localizedTerm}까지 ${countdownDays}일"
                            AppLanguage.TRADITIONAL_CHINESE -> "⏳ 距$localizedTerm $countdownDays 天"
                            else -> "⏳ 距$localizedTerm $countdownDays 天"
                        }
                    }

                    SuggestionChip(
                        onClick = {},
                        shape = CircleShape,
                        label = { Text(chipText, fontWeight = FontWeight.Bold, fontSize = 10.5.sp) }
                    )
                }
            }

            if (homeConfig.showLunar) {
                Spacer(modifier = Modifier.height(2.dp))
                val lunarPrefix = LanguageUtils.getString("lunar", lang)
                val leapStr = if (todayLunar.isLeapMonth) {
                    when (effectiveLang) {
                        AppLanguage.ENGLISH -> "Leap "
                        AppLanguage.JAPANESE -> "閏"
                        AppLanguage.KOREAN -> "윤"
                        AppLanguage.TRADITIONAL_CHINESE -> "閏"
                        else -> "闰"
                    }
                } else ""
                Text(
                    text = "$lunarPrefix ${todayLunar.ganZhiYear} (${todayLunar.zodiac}) 年 $leapStr${todayLunar.lunarMonthName}${todayLunar.lunarDayName}",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeumorphicAccent
                )
            }

            if (homeConfig.showAlmanac) {
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(6.dp))

                val yiChipBg = if (isDark) Color(0xFF065F46).copy(alpha = 0.55f) else Color(0xFFD1FAE5)
                val yiChipBorder = if (isDark) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFF10B981).copy(alpha = 0.35f)
                val yiChipText = if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857)

                val jiChipBg = if (isDark) Color(0xFF334155).copy(alpha = 0.65f) else Color(0xFFF1F5F9)
                val jiChipBorder = if (isDark) Color(0xFF94A3B8).copy(alpha = 0.5f) else Color(0xFF94A3B8).copy(alpha = 0.35f)
                val jiChipText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)

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
}

/**
 * 5. 星座与运势 Card (独立重组隔离子组件)
 */
@Composable
private fun HomeFortuneSection(
    fortune: ZodiacFortune,
    homeConfig: HomeConfig,
    lang: AppLanguage
) {
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
                        modifier = Modifier.size(16.dp)
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
}

/**
 * 二级天气 6 小时精细预报弹窗卡片 (100% 深度复刻主页 Weather Card 外观、尺寸、间距与字体风格)
 */
@Composable
fun WeatherDetailOverlayDialog(
    visible: Boolean,
    weatherList: List<DailyWeather>,
    dayIndex: Int,
    cityName: String,
    temperatureUnit: TemperatureUnit,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val weather = weatherList.getOrNull(dayIndex)
    if (!visible || weather == null) return

    val isDark = LocalDarkTheme.current
    val cardShape = RoundedCornerShape(22.dp)

    val scrimColor = if (isDark) {
        Color.Black.copy(alpha = 0.55f)
    } else {
        Color(0xFFCBD5E1).copy(alpha = 0.40f)
    }

    val sixHourList = remember(weatherList, dayIndex, language) {
        WeatherUtils.generateSixHourForecasts(weatherList, dayIndex, language)
    }

    val localizedCity = LanguageUtils.getLocalizedCityName(cityName, language)
    val dayTitle = LanguageUtils.getDayName(weather.dayName, language)

    val minDisplay = temperatureUnit.convertTemp(weather.tempMin)
    val maxDisplay = temperatureUnit.convertTemp(weather.tempMax)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(enabled = false) {}
                    .dialogDropShadow3D(shape = cardShape)
                    .background(NeumorphicBg, shape = cardShape)
                    .border(
                        1.dp,
                        if (isDark) Color.White.copy(alpha = 0.12f) else NeumorphicAccent.copy(alpha = 0.20f),
                        shape = cardShape
                    )
                    .clip(cardShape)
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 1. 顶栏：100% 深度复刻主页天气卡片标头风格
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = NeumorphicAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "📍 $localizedCity · $dayTitle",
                                fontWeight = FontWeight.ExtraBold,
                                color = NeumorphicAccent,
                                fontSize = 13.5.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$minDisplay°~$maxDisplay°${temperatureUnit.symbol}",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeumorphicAccent
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .neumorphicExtruded(shape = CircleShape, elevation = 2.dp)
                                    .background(NeumorphicBg, shape = CircleShape)
                                    .clip(CircleShape)
                                    .clickable { onDismiss() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = NeumorphicAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. 恢复的全天天气详情 Card (包含大型气象 Emoji、天气状况、全天温阶、全天最高降水概率)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NeumorphicSunkenBg, shape = RoundedCornerShape(16.dp))
                            .neumorphicInset(shape = RoundedCornerShape(16.dp), elevation = 3.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(weather.iconEmoji, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    val condLocalized = WeatherUtils.getLocalizedCondition(weather.condition, language)
                                    Text(
                                        text = condLocalized,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NeumorphicTextPrimary
                                    )
                                    Text(
                                        text = "$minDisplay° ~ $maxDisplay°${temperatureUnit.symbol}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeumorphicAccent
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "💧 ${weather.precipProb}%",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0284C7)
                                )
                                val precipLabel = when (language.getEffectiveLanguage()) {
                                    AppLanguage.ENGLISH -> "Max Precip"
                                    AppLanguage.JAPANESE -> "最高降水率"
                                    AppLanguage.KOREAN -> "최고 강수확률"
                                    AppLanguage.TRADITIONAL_CHINESE -> "最高降水概率"
                                    else -> "全天最高降水概率"
                                }
                                Text(
                                    text = precipLabel,
                                    fontSize = 11.sp,
                                    color = NeumorphicTextPrimary.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val hourlyLabel = when (language.getEffectiveLanguage()) {
                        AppLanguage.ENGLISH -> "6-Hour Detailed Forecast"
                        AppLanguage.JAPANESE -> "6時間毎の詳細天気"
                        AppLanguage.KOREAN -> "6시간 단위 상세 예보"
                        AppLanguage.TRADITIONAL_CHINESE -> "以6小時為單元的詳細預報"
                        else -> "以 6 小时为单元的详细预报"
                    }

                    Text(
                        text = hourlyLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeumorphicTextPrimary.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. 4 个 6 小时时段卡片 (全文本绝对水平居中)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sixHourList.forEach { item ->
                            val itemTemp = temperatureUnit.convertTemp(item.temp)
                            val condLoc = WeatherUtils.getLocalizedCondition(item.condition, language)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 2.dp)
                                    .background(NeumorphicSunkenBg, shape = RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .padding(vertical = 8.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    // Line 1: 今天/明天 + 时段名 (例如: 今天下午 / Tomorrow) 居中对齐
                                    Text(
                                        text = "${item.dayPrefix}${item.timeLabel}".trim(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeumorphicTextPrimary,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(3.dp))

                                    // Line 2: 气象图标
                                    Text(
                                        text = item.iconEmoji,
                                        fontSize = 17.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(3.dp))

                                    // Line 3: 状况说明
                                    Text(
                                        text = condLoc,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = NeumorphicTextPrimary,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    // Line 4: 降水概率
                                    Text(
                                        text = "💧 ${item.precipProb}%",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0284C7),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(3.dp))

                                    // Line 5: 温度
                                    Text(
                                        text = "$itemTemp${temperatureUnit.symbol}",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NeumorphicAccent,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
