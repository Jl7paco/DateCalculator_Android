package me.paco.datecalculator.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.paco.datecalculator.R
import me.paco.datecalculator.ui.components.DatePickerModal
import me.paco.datecalculator.ui.components.FireworksAnimation
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicCopyButton
import me.paco.datecalculator.ui.components.NeumorphicCustomPopup
import me.paco.datecalculator.ui.components.NeumorphicSegmentedRow
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import me.paco.datecalculator.ui.components.QuickDateChips
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LunarCalendarUtils
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunarConverterScreen(
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Mode: 0 = 阳历转农历, 1 = 农历转阳历
    var convertMode by remember { mutableIntStateOf(0) }

    // State for Solar -> Lunar
    var solarDate by remember { mutableStateOf(LocalDate.now()) }
    var showSolarPicker by remember { mutableStateOf(false) }

    // State for Lunar -> Solar
    val todayLunar = remember { LunarCalendarUtils.solarToLunar(LocalDate.now()) }
    var lunarRefSolarDate by remember { mutableStateOf(LocalDate.now()) }
    var showLunarRefPicker by remember { mutableStateOf(false) }

    var lunarYearInput by remember { mutableStateOf(todayLunar.year.toString()) }
    var lunarMonth by remember { mutableIntStateOf(todayLunar.month) }
    var lunarDay by remember { mutableIntStateOf(todayLunar.day) }
    var isLeapMonth by remember { mutableStateOf(todayLunar.isLeapMonth) }

    var showLunarResult by remember { mutableStateOf(true) }

    // Bounce 动效触发器：只在主动点击下方四个快捷按键时触发
    val cardScale = remember { Animatable(1.0f) }
    val cardAlpha = remember { Animatable(1.0f) }
    var triggerBounce by remember { mutableIntStateOf(0) }

    LaunchedEffect(triggerBounce) {
        if (triggerBounce > 0) {
            launch {
                cardScale.animateTo(1.04f, animationSpec = tween(100))
                cardScale.animateTo(1.00f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            }
            launch {
                cardAlpha.snapTo(0.4f)
                cardAlpha.animateTo(1.0f, animationSpec = tween(250))
            }
        }
    }

    if (showSolarPicker) {
        DatePickerModal(
            selectedDate = solarDate,
            onDateSelected = { date ->
                solarDate = date
                showLunarResult = true
                val lunarResult = LunarCalendarUtils.solarToLunar(date)
                viewModel.saveToHistory(
                    category = "农历公历",
                    title = "${lunarResult.lunarMonthName}${lunarResult.lunarDayName}",
                    detail = "公历 ${date}  ➔  ${lunarResult.getFullDescription()}"
                )
            },
            onDismiss = { showSolarPicker = false }
        )
    }

    if (showLunarRefPicker) {
        DatePickerModal(
            selectedDate = lunarRefSolarDate,
            onDateSelected = { date ->
                lunarRefSolarDate = date
                val l = LunarCalendarUtils.solarToLunar(date)
                lunarYearInput = l.year.toString()
                lunarMonth = l.month
                lunarDay = l.day
                isLeapMonth = l.isLeapMonth
                showLunarResult = true
            },
            onDismiss = { showLunarRefPicker = false }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = NeumorphicAccent
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.label_lunar_conv_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeumorphicTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            NeumorphicSegmentedRow(
                items = listOf(stringResource(R.string.label_solar_to_lunar), stringResource(R.string.label_lunar_to_solar)),
                selectedIndex = convertMode,
                onIndexSelected = {
                    convertMode = it
                    showLunarResult = true
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (convertMode == 0) {
                // ================= 公历转农历 =================
                Text(
                    text = stringResource(R.string.label_select_solar_date),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeumorphicTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 显眼放大版新拟物 Hero Card (对齐第一页 Hero Card 14.dp, 10.dp 规格)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = cardScale.value
                            scaleY = cardScale.value
                            alpha = cardAlpha.value
                        }
                        .clickable { showSolarPicker = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            Column {
                                Text(
                                    text = "选择公历日期",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val dateFormattedWithWeek = DateCalculatorUtils.formatDateWithWeek(solarDate)
                                Text(
                                    text = dateFormattedWithWeek,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            contentDescription = "选择日期",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                QuickDateChips(
                    selectedDate = solarDate,
                    onSelectDate = { date ->
                        solarDate = date
                        showLunarResult = true
                        triggerBounce++
                        val lunarResult = LunarCalendarUtils.solarToLunar(date)
                        viewModel.saveToHistory(
                            category = "农历公历",
                            title = "${lunarResult.lunarMonthName}${lunarResult.lunarDayName}",
                            detail = "公历 ${date}  ➔  ${lunarResult.getFullDescription()}"
                        )
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                val lunarResult = LunarCalendarUtils.solarToLunar(solarDate)

                AnimatedVisibility(
                    visible = showLunarResult,
                    enter = fadeIn(animationSpec = tween(150)) + expandVertically(animationSpec = tween(150)),
                    exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = tween(150))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neumorphicExtruded(shape = RoundedCornerShape(24.dp), elevation = 6.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f), shape = RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.label_lunar_result),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            val leapTag = if (lunarResult.isLeapMonth) "闰" else ""
                            Text(
                                text = "$leapTag${lunarResult.lunarMonthName}${lunarResult.lunarDayName}",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${lunarResult.ganZhiYear} (${lunarResult.zodiac})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                            )

                            // 传统节日提示 Chip：点击触发烟花动效
                            if (lunarResult.festival.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                SuggestionChip(
                                    onClick = { viewModel.triggerFireworks() },
                                    shape = CircleShape,
                                    label = { Text(stringResource(R.string.label_traditional_festival, lunarResult.festival), fontWeight = FontWeight.Bold) }
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            NeumorphicCopyButton(
                                text = stringResource(R.string.label_copy_lunar),
                                onClick = {
                                    val clipText = "Solar ${solarDate} ➔ ${lunarResult.getFullDescription()}"
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("LunarDate", clipText))
                                    Toast.makeText(context, context.getString(R.string.toast_copied), Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

            } else {
                // ================= 农历转公历 =================
                Text(
                    text = "参考公历日期选择",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeumorphicTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = cardScale.value
                            scaleY = cardAlpha.value
                        }
                        .clickable { showLunarRefPicker = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            Column {
                                Text(
                                    text = "快速对齐公历基准日",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val dateFormattedWithWeek = DateCalculatorUtils.formatDateWithWeek(lunarRefSolarDate)
                                Text(
                                    text = dateFormattedWithWeek,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            contentDescription = "选择日期",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.label_select_lunar_date),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeumorphicTextPrimary
                )

                val parsedYear = lunarYearInput.toIntOrNull()

                Spacer(modifier = Modifier.height(4.dp))
                val hintText = if (parsedYear != null && parsedYear in 1900..2100) {
                    val ganZhiZodiacStr = LunarCalendarUtils.getYearGanZhiAndZodiac(parsedYear)
                    stringResource(R.string.label_gregorian_year_hint, ganZhiZodiacStr)
                } else {
                    "请输入公历年份 (例如: 2026)"
                }

                Text(
                    text = hintText,
                    style = MaterialTheme.typography.bodySmall,
                    color = NeumorphicTextPrimary.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. 公历年份
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .neumorphicInset(shape = RoundedCornerShape(18.dp), elevation = 4.dp)
                                .border(1.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = RoundedCornerShape(18.dp))
                                .background(NeumorphicBg, shape = RoundedCornerShape(18.dp))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.label_gregorian_year),
                                    fontSize = 11.sp,
                                    color = NeumorphicAccent,
                                    fontWeight = FontWeight.Bold
                                )
                                BasicTextField(
                                    value = lunarYearInput,
                                    onValueChange = { newValue ->
                                        if (newValue.isEmpty() || (newValue.length <= 4 && newValue.all { it.isDigit() })) {
                                            lunarYearInput = newValue
                                            showLunarResult = false
                                        }
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeumorphicTextPrimary
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // 2. 农历月份选择
                        var monthExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .neumorphicInset(shape = RoundedCornerShape(18.dp), elevation = 4.dp)
                                    .border(1.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = RoundedCornerShape(18.dp))
                                    .background(NeumorphicBg, shape = RoundedCornerShape(18.dp))
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { monthExpanded = true }
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = stringResource(R.string.label_lunar_month), fontSize = 11.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                                        Text(text = LunarCalendarUtils.getLunarMonthName(lunarMonth), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                                    }
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = NeumorphicAccent)
                                }
                            }

                            NeumorphicCustomPopup(
                                expanded = monthExpanded,
                                onDismissRequest = { monthExpanded = false }
                            ) {
                                (1..12).forEach { m ->
                                    val isCurrent = (lunarMonth == m)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isCurrent) NeumorphicAccent.copy(alpha = 0.12f) else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                lunarMonth = m
                                                monthExpanded = false
                                                showLunarResult = false
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = LunarCalendarUtils.getLunarMonthName(m),
                                            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (isCurrent) NeumorphicAccent else NeumorphicTextPrimary,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        // 3. 农历日期选择
                        var dayExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .neumorphicInset(shape = RoundedCornerShape(18.dp), elevation = 4.dp)
                                    .border(1.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = RoundedCornerShape(18.dp))
                                    .background(NeumorphicBg, shape = RoundedCornerShape(18.dp))
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { dayExpanded = true }
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = stringResource(R.string.label_lunar_day), fontSize = 11.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                                        Text(text = LunarCalendarUtils.getLunarDayName(lunarDay), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                                    }
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = NeumorphicAccent)
                                }
                            }

                            NeumorphicCustomPopup(
                                expanded = dayExpanded,
                                onDismissRequest = { dayExpanded = false }
                            ) {
                                (1..30).forEach { d ->
                                    val isCurrent = (lunarDay == d)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isCurrent) NeumorphicAccent.copy(alpha = 0.12f) else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                lunarDay = d
                                                dayExpanded = false
                                                showLunarResult = false
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = LunarCalendarUtils.getLunarDayName(d),
                                            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (isCurrent) NeumorphicAccent else NeumorphicTextPrimary,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    val convertedSolarDate = if (parsedYear != null) {
                        LunarCalendarUtils.lunarToSolar(parsedYear, lunarMonth, lunarDay, isLeapMonth)
                    } else null

                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(58.dp)
                            .neumorphicExtruded(shape = RoundedCornerShape(18.dp), elevation = 5.dp)
                            .background(NeumorphicAccent, shape = RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                if (parsedYear == null) {
                                    Toast.makeText(context, "请先输入公历年份", Toast.LENGTH_SHORT).show()
                                } else {
                                    showLunarResult = true
                                    if (convertedSolarDate != null) {
                                        val solarStr = DateCalculatorUtils.formatDate(convertedSolarDate)
                                        viewModel.saveToHistory(
                                            category = "农历公历",
                                            title = solarStr,
                                            detail = "农历 ${parsedYear}年${LunarCalendarUtils.getLunarMonthName(lunarMonth)}${LunarCalendarUtils.getLunarDayName(lunarDay)}  ➔  公历 ${solarStr}"
                                        )
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("=", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }

                val currentLeapMonth = if (parsedYear != null) LunarCalendarUtils.getLeapMonth(parsedYear) else 0
                if (currentLeapMonth == lunarMonth && currentLeapMonth > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isLeapMonth,
                            onCheckedChange = {
                                isLeapMonth = it
                                showLunarResult = false
                            }
                        )
                        Text(stringResource(R.string.label_leap_month_check, LunarCalendarUtils.getLunarMonthName(lunarMonth)), color = NeumorphicTextPrimary)
                    }
                } else {
                    isLeapMonth = false
                }

                Spacer(modifier = Modifier.height(14.dp))

                val convertedSolarDate = if (parsedYear != null) {
                    LunarCalendarUtils.lunarToSolar(parsedYear, lunarMonth, lunarDay, isLeapMonth)
                } else null

                AnimatedVisibility(
                    visible = showLunarResult && parsedYear != null,
                    enter = fadeIn(animationSpec = tween(150)) + expandVertically(animationSpec = tween(150)),
                    exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = tween(150))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neumorphicExtruded(shape = RoundedCornerShape(24.dp), elevation = 6.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f), shape = RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.label_solar_result),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (convertedSolarDate != null && parsedYear != null) {
                                val solarStr = DateCalculatorUtils.formatDate(convertedSolarDate)
                                val descStr = DateCalculatorUtils.getDateDescription(convertedSolarDate)
                                val festivalStr = LunarCalendarUtils.solarToLunar(convertedSolarDate).festival

                                Text(
                                    text = solarStr,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                if (festivalStr.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    SuggestionChip(
                                        onClick = { viewModel.triggerFireworks() },
                                        shape = CircleShape,
                                        label = { Text(stringResource(R.string.label_traditional_festival, festivalStr), fontWeight = FontWeight.Bold) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = descStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                NeumorphicCopyButton(
                                    text = stringResource(R.string.label_copy_solar),
                                    onClick = {
                                        val clipText = "Lunar ${parsedYear}/${lunarMonth}/${lunarDay} ➔ Solar: $solarStr"
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("SolarDate", clipText))
                                        Toast.makeText(context, context.getString(R.string.toast_copied), Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                Text("Invalid date range (1900-2100)", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 传统节日烟花粒子图层
        FireworksAnimation(
            trigger = uiState.fireworksTrigger,
            onAnimationFinished = { viewModel.resetFireworks() }
        )
    }
}
