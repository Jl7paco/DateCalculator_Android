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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicChip
import me.paco.datecalculator.ui.components.NeumorphicCopyButton
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
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DateDiffScreen(
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var showPickerForStart by remember { mutableStateOf(false) }
    var showPickerForEnd by remember { mutableStateOf(false) }

    // 0 = 阳历目标日期, 1 = 农历目标日期
    var targetCalendarType by remember { mutableIntStateOf(0) }

    // 农历输入状态
    var targetLunarYear by remember { mutableIntStateOf(LocalDate.now().year) }
    var targetLunarMonth by remember { mutableIntStateOf(8) }
    var targetLunarDay by remember { mutableIntStateOf(15) }
    var targetIsLeapMonth by remember { mutableStateOf(false) }

    var showDiffResult by remember { mutableStateOf(false) }

    // 基准日期切换时的动效提醒
    val cardScale = remember { Animatable(1.0f) }
    val cardAlpha = remember { Animatable(1.0f) }

    LaunchedEffect(uiState.baseDate) {
        launch {
            cardScale.animateTo(1.04f, animationSpec = tween(100))
            cardScale.animateTo(1.00f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        }
        launch {
            cardAlpha.snapTo(0.4f)
            cardAlpha.animateTo(1.0f, animationSpec = tween(250))
        }
    }

    if (showPickerForStart) {
        DatePickerModal(
            selectedDate = uiState.baseDate,
            onDateSelected = {
                viewModel.updateBaseDate(it)
                showDiffResult = false
            },
            onDismiss = { showPickerForStart = false }
        )
    }

    if (showPickerForEnd) {
        DatePickerModal(
            selectedDate = uiState.endDate,
            onDateSelected = {
                viewModel.updateEndDate(it)
                showDiffResult = false
            },
            onDismiss = { showPickerForEnd = false }
        )
    }

    // 计算真实的结束阳历日期
    val effectiveEndDate: LocalDate = if (targetCalendarType == 0) {
        uiState.endDate
    } else {
        LunarCalendarUtils.lunarToSolar(targetLunarYear, targetLunarMonth, targetLunarDay, targetIsLeapMonth)
            ?: uiState.endDate
    }

    val totalNaturalDays = DateCalculatorUtils.naturalDaysBetween(uiState.baseDate, effectiveEndDate)
    val totalWorkdays = DateCalculatorUtils.workdaysBetween(
        uiState.baseDate, effectiveEndDate, uiState.weekendRule, uiState.enableChineseHolidays, uiState.holidayRegion, uiState.isCurrentWeekBigWeek
    )
    val weekendDays = totalNaturalDays - totalWorkdays
    val periodStr = DateCalculatorUtils.formatPeriod(uiState.baseDate, effectiveEndDate)
    val totalWeeks = totalNaturalDays / 7.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.label_date_diff_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NeumorphicTextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 起始基准日期 Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.label_base_date_default), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = NeumorphicTextPrimary)
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
                    .background(NeumorphicBg, shape = CircleShape)
                    .clip(CircleShape)
                    .clickable {
                        viewModel.setToday()
                        showDiffResult = false
                    }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.label_set_today), fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 显眼放大版新拟物基准起始日期 Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = cardScale.value
                    scaleY = cardScale.value
                    alpha = cardAlpha.value
                }
                .neumorphicExtruded(shape = RoundedCornerShape(20.dp), elevation = 5.dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f), shape = RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .clickable { showPickerForStart = true }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = NeumorphicAccent,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        Text(
                            text = "基准起始日期",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeumorphicAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val dateFormattedWithWeek = DateCalculatorUtils.formatDateWithWeek(uiState.baseDate)
                        Text(
                            text = dateFormattedWithWeek,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeumorphicTextPrimary
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.EditCalendar,
                    contentDescription = "选择日期",
                    tint = NeumorphicAccent.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        QuickDateChips(
            selectedDate = uiState.baseDate,
            onSelectDate = {
                viewModel.updateBaseDate(it)
                showDiffResult = false
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 快速选项
        Text(
            text = stringResource(R.string.label_preset_countdown),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = NeumorphicTextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        val base = uiState.baseDate
        val presetCountdowns = listOf(
            stringResource(R.string.preset_national_day) to calculateNextSolarDate(base, 10, 1),
            stringResource(R.string.preset_new_year) to calculateNextSolarDate(base, 1, 1),
            stringResource(R.string.preset_spring_festival) to calculateNextLunarDate(base, 1, 1),
            stringResource(R.string.preset_mid_autumn) to calculateNextLunarDate(base, 8, 15),
            stringResource(R.string.preset_dragon_boat) to calculateNextLunarDate(base, 5, 5),
            stringResource(R.string.preset_gaokao) to calculateNextSolarDate(base, 6, 7),
            "📚 中考" to calculateNextSolarDate(base, 6, 21)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presetCountdowns.forEach { (label, targetDate) ->
                val isSelected = (targetCalendarType == 0 && uiState.endDate == targetDate)
                NeumorphicChip(
                    text = label,
                    selected = isSelected,
                    onClick = {
                        targetCalendarType = 0
                        viewModel.updateEndDate(targetDate)
                        showDiffResult = true
                    },
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 目标特定日期自定义
        Text(stringResource(R.string.label_custom_target_date), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        NeumorphicSegmentedRow(
            items = listOf(stringResource(R.string.label_solar), stringResource(R.string.label_lunar)),
            selectedIndex = targetCalendarType,
            onIndexSelected = {
                targetCalendarType = it
                showDiffResult = false
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 目标特定日期选框与等于号按键
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (targetCalendarType == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .neumorphicExtruded(shape = RoundedCornerShape(18.dp), elevation = 5.dp)
                            .background(NeumorphicBg, shape = RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { showPickerForEnd = true }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = NeumorphicAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            val dateFormatted = DateCalculatorUtils.formatDate(uiState.endDate)
                            Text(text = stringResource(R.string.label_base_date_fmt, dateFormatted), fontWeight = FontWeight.ExtraBold, color = NeumorphicTextPrimary, fontSize = 16.sp)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1. 公历年份输入框
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .neumorphicInset(shape = RoundedCornerShape(18.dp), elevation = 4.dp)
                                .background(NeumorphicBg, shape = RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            OutlinedTextField(
                                value = targetLunarYear.toString(),
                                onValueChange = {
                                    targetLunarYear = it.toIntOrNull() ?: targetLunarYear
                                    showDiffResult = false
                                },
                                label = { Text(stringResource(R.string.label_gregorian_year), color = NeumorphicAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                )
                            )
                        }

                        // 2. 农历月份选择 (绝对无遮挡)
                        var monthExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = monthExpanded,
                            onExpandedChange = { monthExpanded = !monthExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .neumorphicInset(shape = RoundedCornerShape(18.dp), elevation = 4.dp)
                                    .background(NeumorphicBg, shape = RoundedCornerShape(18.dp))
                                    .clip(RoundedCornerShape(18.dp))
                                    .menuAnchor()
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
                                        Text(text = LunarCalendarUtils.getLunarMonthName(targetLunarMonth), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                                    }
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = NeumorphicAccent)
                                }
                            }
                            ExposedDropdownMenu(
                                expanded = monthExpanded,
                                onDismissRequest = { monthExpanded = false }
                            ) {
                                (1..12).forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text(LunarCalendarUtils.getLunarMonthName(m)) },
                                        onClick = {
                                            targetLunarMonth = m
                                            monthExpanded = false
                                            showDiffResult = false
                                        }
                                    )
                                }
                            }
                        }

                        // 3. 农历日期选择 (绝对无遮挡)
                        var dayExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = dayExpanded,
                            onExpandedChange = { dayExpanded = !dayExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .neumorphicInset(shape = RoundedCornerShape(18.dp), elevation = 4.dp)
                                    .background(NeumorphicBg, shape = RoundedCornerShape(18.dp))
                                    .clip(RoundedCornerShape(18.dp))
                                    .menuAnchor()
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
                                        Text(text = LunarCalendarUtils.getLunarDayName(targetLunarDay), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                                    }
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = NeumorphicAccent)
                                }
                            }
                            ExposedDropdownMenu(
                                expanded = dayExpanded,
                                onDismissRequest = { dayExpanded = false }
                            ) {
                                (1..30).forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text(LunarCalendarUtils.getLunarDayName(d)) },
                                        onClick = {
                                            targetLunarDay = d
                                            dayExpanded = false
                                            showDiffResult = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // 新拟物蓝色凸起等于号按键 (=)
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
                        showDiffResult = true
                        val title = "倒计时: ${abs(totalNaturalDays)}天 (${abs(totalWorkdays)}工作日)"
                        val detail = "${uiState.baseDate} ➔ $effectiveEndDate [${uiState.holidayRegion.label}]"
                        viewModel.saveToHistory(title, detail, effectiveEndDate, totalNaturalDays)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("=", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }

        if (targetCalendarType == 0) {
            Spacer(modifier = Modifier.height(6.dp))
            QuickDateChips(
                selectedDate = uiState.endDate,
                onSelectDate = {
                    viewModel.updateEndDate(it)
                    showDiffResult = false
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 未按等于号时结果整个UI不显示，按下后以 150ms 动画渐进展开
        AnimatedVisibility(
            visible = showDiffResult,
            enter = fadeIn(animationSpec = tween(150)) + expandVertically(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphicExtruded(shape = RoundedCornerShape(24.dp), elevation = 6.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f), shape = RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.label_result_diff_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val statusLabel = when {
                        totalNaturalDays > 0 -> stringResource(R.string.label_status_future, totalNaturalDays)
                        totalNaturalDays < 0 -> stringResource(R.string.label_status_past, abs(totalNaturalDays))
                        else -> stringResource(R.string.label_status_same)
                    }

                    SuggestionChip(
                        onClick = {},
                        shape = CircleShape,
                        label = { Text(statusLabel, fontWeight = FontWeight.Bold) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.label_days_diff), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text(
                                text = "${abs(totalNaturalDays)} ${stringResource(R.string.label_days)}",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.label_workdays_diff), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text(
                                text = "${abs(totalWorkdays)} ${stringResource(R.string.label_days)}",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(stringResource(R.string.label_period_len, periodStr), color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text(stringResource(R.string.label_weeks_len, String.format("%.1f", abs(totalWeeks))), color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text(stringResource(R.string.label_holiday_std, "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.nativeName}"), color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text(stringResource(R.string.label_rest_days_len, abs(weekendDays).toString()), color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    NeumorphicCopyButton(
                        text = stringResource(R.string.label_copy_comparison),
                        onClick = {
                            val clipText = "Base ${uiState.baseDate} ➔ Target ${effectiveEndDate}: $totalNaturalDays days ($totalWorkdays workdays)"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("DateDiff", clipText))
                            Toast.makeText(context, context.getString(R.string.toast_copied), Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun calculateNextSolarDate(baseDate: LocalDate, month: Int, day: Int): LocalDate {
    val thisYearDate = LocalDate.of(baseDate.year, month, day)
    return if (!baseDate.isAfter(thisYearDate)) {
        thisYearDate
    } else {
        LocalDate.of(baseDate.year + 1, month, day)
    }
}

private fun calculateNextLunarDate(baseDate: LocalDate, lunarMonth: Int, lunarDay: Int): LocalDate {
    val thisYearSolar = LunarCalendarUtils.lunarToSolar(baseDate.year, lunarMonth, lunarDay)
    return if (thisYearSolar != null && !baseDate.isAfter(thisYearSolar)) {
        thisYearSolar
    } else {
        LunarCalendarUtils.lunarToSolar(baseDate.year + 1, lunarMonth, lunarDay) ?: baseDate
    }
}
