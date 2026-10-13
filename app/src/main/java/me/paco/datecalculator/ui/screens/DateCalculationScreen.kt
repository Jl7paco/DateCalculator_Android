package me.paco.datecalculator.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import me.paco.datecalculator.ui.components.NeumorphicIconHeaderBadge
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.DateMode
import me.paco.datecalculator.ui.components.DatePickerModal
import me.paco.datecalculator.ui.components.HistoryOverlayDialog
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicIconButton
import me.paco.datecalculator.ui.components.NeumorphicSegmentedRow
import me.paco.datecalculator.ui.components.NeumorphicSunkenBg
import me.paco.datecalculator.ui.components.NumericCalculatorInput
import me.paco.datecalculator.ui.components.QuickDateChips
import me.paco.datecalculator.ui.components.RangeBreakdownCard
import me.paco.datecalculator.ui.components.ResultCard
import me.paco.datecalculator.ui.components.SettingsOverlayDialog
import me.paco.datecalculator.ui.components.TimelineDiagram
import me.paco.datecalculator.ui.components.WorkdayNaturalSwitch
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.viewmodel.CalcSubMode
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LanguageUtils
import me.paco.datecalculator.util.ShareUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun DateCalculationScreen(
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val lang = uiState.appLanguage
    val effectiveLang = lang.getEffectiveLanguage()

    var showDatePicker by remember { mutableStateOf(false) }
    var showReverseEndDatePicker by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val targetDate = viewModel.calculateTargetDate()

    val cardScale = remember { Animatable(1.0f) }
    val cardAlpha = remember { Animatable(1.0f) }

    LaunchedEffect(uiState.showResult) {
        if (uiState.showResult) {
            launch {
                cardScale.animateTo(1.03f, animationSpec = tween(120))
                cardScale.animateTo(1.00f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            }
            launch {
                cardAlpha.snapTo(0.6f)
                cardAlpha.animateTo(1.0f, animationSpec = tween(180))
            }
        }
    }

    if (showDatePicker) {
        DatePickerModal(
            selectedDate = uiState.baseDate,
            onDateSelected = { date ->
                viewModel.updateBaseDate(date)
                viewModel.performCalculation()
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showReverseEndDatePicker) {
        DatePickerModal(
            selectedDate = uiState.reverseEndDate,
            onDateSelected = { date ->
                viewModel.updateReverseEndDate(date)
                viewModel.performCalculation()
                showReverseEndDatePicker = false
            },
            onDismiss = { showReverseEndDatePicker = false }
        )
    }

    var showSingleDeadlinePicker by remember { mutableStateOf(false) }
    val singleTargetDate = viewModel.calculateTargetDate()
    val singleDeadlineText = remember(uiState.baseDate, uiState.daysInput, uiState.calculationType, uiState.dateMode, uiState.weekendRule, uiState.enableChineseHolidays, uiState.holidayRegion, uiState.isCurrentWeekBigWeek, uiState.disableChinaShiftWorkdays, lang) {
        if (uiState.daysInput.isBlank()) {
            when (lang.getEffectiveLanguage()) {
                AppLanguage.SIMPLIFIED_CHINESE -> "选择日期"
                AppLanguage.TRADITIONAL_CHINESE -> "選擇日期"
                AppLanguage.JAPANESE -> "日付を選択"
                AppLanguage.KOREAN -> "날짜 선택"
                AppLanguage.ENGLISH -> "Select Date"
                else -> "选择日期"
            }
        } else {
            val monthDay = singleTargetDate.format(DateTimeFormatter.ofPattern("MM-dd"))
            when (lang.getEffectiveLanguage()) {
                AppLanguage.SIMPLIFIED_CHINESE -> "截止 $monthDay"
                AppLanguage.TRADITIONAL_CHINESE -> "截止 $monthDay"
                AppLanguage.JAPANESE -> "$monthDay 迄"
                AppLanguage.KOREAN -> "$monthDay 마감"
                AppLanguage.ENGLISH -> "Due $monthDay"
                else -> "截止 $monthDay"
            }
        }
    }

    if (showSingleDeadlinePicker) {
        DatePickerModal(
            selectedDate = singleTargetDate,
            onDateSelected = { selectedDate ->
                val requiredDays = DateCalculatorUtils.calculateDaysBetweenDates(
                    startDate = uiState.baseDate,
                    targetDate = selectedDate,
                    isWorkdayMode = (uiState.dateMode == DateMode.WORKDAY),
                    weekendRule = uiState.weekendRule,
                    enableHolidays = uiState.enableChineseHolidays,
                    holidayRegion = uiState.holidayRegion,
                    isCurrentWeekBigWeek = uiState.isCurrentWeekBigWeek,
                    disableChinaShiftWorkdays = uiState.disableChinaShiftWorkdays
                )
                viewModel.updateDaysInput(requiredDays.toString())
                viewModel.updateCalculationType(if (selectedDate.isBefore(uiState.baseDate)) CalculationType.SUBTRACT else CalculationType.ADD)
                viewModel.performCalculation()
                showSingleDeadlinePicker = false
            },
            onDismiss = { showSingleDeadlinePicker = false }
        )
    }

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

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // 顶栏 (36dp 高度, 15sp 标题)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NeumorphicIconHeaderBadge(
                        icon = Icons.Default.Calculate,
                        size = 28.dp,
                        iconSize = 14.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LanguageUtils.getString("tab_calc", lang),
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

            Spacer(modifier = Modifier.height(10.dp))

            // 模式三向推栈分段按钮 (加减天数 | 区间拆算 | 多段模式)
            val subModes = listOf(
                LanguageUtils.getString("mode_forward", lang),
                LanguageUtils.getString("mode_reverse", lang),
                LanguageUtils.getString("multi_stage_btn", lang)
            )
            val selectedSubIndex = when {
                uiState.isMultiStageExtensionEnabled -> 2
                uiState.calcSubMode == CalcSubMode.REVERSE_RANGE -> 1
                else -> 0
            }

            NeumorphicSegmentedRow(
                items = subModes,
                selectedIndex = selectedSubIndex,
                onIndexSelected = { index ->
                    when (index) {
                        0 -> {
                            viewModel.toggleMultiStageExtension(false)
                            viewModel.updateCalcSubMode(CalcSubMode.FORWARD_DAYS)
                            viewModel.performCalculation()
                        }
                        1 -> {
                            viewModel.toggleMultiStageExtension(false)
                            viewModel.updateCalcSubMode(CalcSubMode.REVERSE_RANGE)
                            viewModel.performCalculation()
                        }
                        2 -> {
                            viewModel.toggleMultiStageExtension(true)
                            // 切换至多段模式时，先清空旧的单段结果，等待用户点击等于号生成
                        }
                    }
                },
                height = 36.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 1. 基准起算日期面板
            val inputShape18 = RoundedCornerShape(18.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .neumorphicInset(shape = inputShape18, elevation = 4.dp)
                    .border(1.dp, NeumorphicAccent.copy(alpha = 0.20f), shape = inputShape18)
                    .background(NeumorphicBg, shape = inputShape18)
                    .clip(inputShape18)
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
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
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "${LanguageUtils.getString("base_date", lang)}: ${DateCalculatorUtils.formatDateWithWeek(uiState.baseDate, lang)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeumorphicTextPrimary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = "Select Date",
                        tint = NeumorphicAccent.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 快捷起算日期 Chip 规则
            QuickDateChips(
                selectedDate = uiState.baseDate,
                onSelectDate = { date ->
                    viewModel.updateBaseDate(date)
                    viewModel.performCalculation()
                },
                language = lang
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 推算规则选择器行 (工作日/自然日胶囊开关)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val calcRuleText = when (effectiveLang) {
                    AppLanguage.ENGLISH -> "Calculation Rule"
                    AppLanguage.JAPANESE -> "計算ルール"
                    AppLanguage.KOREAN -> "계산 규칙"
                    AppLanguage.TRADITIONAL_CHINESE -> "計算規則"
                    else -> "计算规则"
                }

                Text(
                    text = calcRuleText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeumorphicTextPrimary
                )

                WorkdayNaturalSwitch(
                    isWorkday = (uiState.dateMode == DateMode.WORKDAY),
                    onWorkdayChanged = { isWorkday ->
                        viewModel.updateDateMode(if (isWorkday) DateMode.WORKDAY else DateMode.NATURAL_DAY)
                    },
                    language = lang
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. 模式 A: 单段加减算面板
            if (!uiState.isMultiStageExtensionEnabled && uiState.calcSubMode == CalcSubMode.FORWARD_DAYS) {
                NumericCalculatorInput(
                    daysInput = uiState.daysInput,
                    onDaysInputChange = { viewModel.updateDaysInput(it) },
                    selectedType = uiState.calculationType,
                    onTypeSelected = { viewModel.updateCalculationType(it) },
                    onEqualClick = { viewModel.performCalculation() },
                    onDeadlineDatePickerClick = { showSingleDeadlinePicker = true },
                    targetDeadlineDateText = singleDeadlineText,
                    dayUnitLabel = if (uiState.dateMode == DateMode.WORKDAY) LanguageUtils.getString("workday", lang) else LanguageUtils.getString("natural_day", lang),
                    language = lang
                )
            }

            // 模式 B: 区间拆算输入面板
            if (!uiState.isMultiStageExtensionEnabled && uiState.calcSubMode == CalcSubMode.REVERSE_RANGE) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicInset(shape = RoundedCornerShape(18.dp), elevation = 4.dp)
                        .background(NeumorphicBg, shape = RoundedCornerShape(18.dp))
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = LanguageUtils.getString("reverse_end_date_title", lang),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeumorphicAccent
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp)
                                    .neumorphicInset(shape = inputShape18, elevation = 4.dp)
                                    .border(1.dp, NeumorphicAccent.copy(alpha = 0.20f), shape = inputShape18)
                                    .background(NeumorphicBg, shape = inputShape18)
                                    .clip(inputShape18)
                                    .clickable { showReverseEndDatePicker = true }
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${LanguageUtils.getString("target_date", lang)}: ${DateCalculatorUtils.formatDate(uiState.reverseEndDate, lang)}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeumorphicTextPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(58.dp)
                                    .neumorphicExtruded(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                                    .background(NeumorphicAccent, shape = RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { viewModel.performCalculation() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("=", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // 3. 模式 C: 高级多段计算模式
            val cardShape22 = RoundedCornerShape(22.dp)

            val focusManager = LocalFocusManager.current
            val stageDaysFocusRequesters = remember(uiState.stages.size) {
                List(uiState.stages.size) { FocusRequester() }
            }
            var stagePickerStageId by remember { mutableStateOf<Long?>(null) }

            val (_, calculatedMultiSegments) = remember(uiState.baseDate, uiState.stages, uiState.dateMode, uiState.weekendRule, uiState.enableChineseHolidays, uiState.holidayRegion, uiState.isCurrentWeekBigWeek, uiState.disableChinaShiftWorkdays) {
                viewModel.calculateMultiStageTimeline()
            }

            if (uiState.isMultiStageExtensionEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicExtruded(shape = cardShape22, elevation = 5.dp)
                        .background(NeumorphicBg, shape = cardShape22)
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                NeumorphicIconHeaderBadge(
                                    icon = Icons.Default.Timeline,
                                    size = 26.dp,
                                    iconSize = 14.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(LanguageUtils.getString("multi_stage_btn", lang), fontWeight = FontWeight.Bold, color = NeumorphicAccent, fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val planPlaceholder = when (lang) {
                            AppLanguage.ENGLISH -> "Name this plan (e.g. Vacation / Renovation)"
                            AppLanguage.JAPANESE -> "プラン名を入力 (例: 旅行計画 / リフォーム)"
                            AppLanguage.KOREAN -> "일정 이름 입력 (예: 졸업 여행 / 리모델링)"
                            AppLanguage.TRADITIONAL_CHINESE -> "給這段安排起個名字 (如: 畢業旅行 / 裝修進度)"
                            else -> "给这段安排起个名字 (如: 毕业旅行 / 装修进度 / 减脂计划)"
                        }

                        val sunkenShape12 = RoundedCornerShape(12.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .neumorphicInset(shape = sunkenShape12, elevation = 3.dp)
                                .border(1.2.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = sunkenShape12)
                                .background(NeumorphicSunkenBg, shape = sunkenShape12)
                                .clip(sunkenShape12)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (uiState.multiStagePlanTitle.isEmpty()) {
                                Text(planPlaceholder, fontSize = 12.sp, color = NeumorphicTextPrimary.copy(alpha = 0.5f))
                            }
                            BasicTextField(
                                value = uiState.multiStagePlanTitle,
                                onValueChange = { viewModel.updateMultiStagePlanTitle(it) },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary),
                                cursorBrush = SolidColor(NeumorphicAccent),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 多阶段列表
                        uiState.stages.forEachIndexed { index, stage ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Reorder",
                                    tint = NeumorphicAccent,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .padding(end = 6.dp)
                                        .clickable {
                                            if (uiState.stages.size > 1) {
                                                val nextIndex = (index + 1) % uiState.stages.size
                                                viewModel.reorderCalculationStages(index, nextIndex)
                                            }
                                        }
                                )

                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(38.dp)
                                        .neumorphicExtruded(shape = RoundedCornerShape(8.dp), elevation = 2.dp)
                                        .background(
                                            if (stage.type == CalculationType.ADD) Color(0xFF10B981) else Color(0xFFEF4444),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            val newType = if (stage.type == CalculationType.ADD) CalculationType.SUBTRACT else CalculationType.ADD
                                            viewModel.updateStageType(stage.id, newType)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (stage.type == CalculationType.ADD) Icons.Default.Add else Icons.Default.Remove,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // 天数输入框 (收紧尺寸至 52.dp)
                                Box(
                                    modifier = Modifier
                                        .width(52.dp)
                                        .height(38.dp)
                                        .neumorphicInset(shape = RoundedCornerShape(8.dp), elevation = 2.dp)
                                        .background(NeumorphicSunkenBg, shape = RoundedCornerShape(8.dp))
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (stage.daysInput.isEmpty()) {
                                        val daysPlaceholder = when (lang) {
                                            AppLanguage.ENGLISH -> "Days"
                                            AppLanguage.JAPANESE -> "日数"
                                            AppLanguage.KOREAN -> "일수"
                                            AppLanguage.TRADITIONAL_CHINESE -> "天數"
                                            else -> "天数"
                                        }
                                        Text(
                                            text = daysPlaceholder,
                                            fontSize = 11.5.sp,
                                            color = NeumorphicTextPrimary.copy(alpha = 0.45f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    BasicTextField(
                                        value = stage.daysInput,
                                        onValueChange = {
                                            viewModel.updateStageDaysInput(stage.id, it)
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary),
                                        cursorBrush = SolidColor(NeumorphicAccent),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = if (index < uiState.stages.lastIndex) ImeAction.Next else ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = {
                                                if (stage.daysInput.isBlank()) {
                                                    viewModel.updateStageDaysInput(stage.id, "0")
                                                }
                                                if (index + 1 in stageDaysFocusRequesters.indices) {
                                                    stageDaysFocusRequesters[index + 1].requestFocus()
                                                }
                                            },
                                            onDone = {
                                                if (stage.daysInput.isBlank()) {
                                                    viewModel.updateStageDaysInput(stage.id, "0")
                                                }
                                                keyboardController?.hide()
                                                focusManager.clearFocus()
                                                viewModel.performCalculation()
                                            }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(stageDaysFocusRequesters.getOrElse(index) { FocusRequester() })
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // 新增：截止日期选择按钮与提示词 (当输入框文字是灰色/为空时不作联动，显示默认截至日期提示)
                                val stageCalculatedEndDate = if (stage.daysInput.isNotBlank()) calculatedMultiSegments.getOrNull(index)?.endDate else null
                                val deadlinePromptText = if (stageCalculatedEndDate != null) {
                                    val monthDay = stageCalculatedEndDate.format(DateTimeFormatter.ofPattern("MM-dd"))
                                    when (effectiveLang) {
                                        AppLanguage.ENGLISH -> "Due $monthDay"
                                        AppLanguage.JAPANESE -> "$monthDay 迄"
                                        AppLanguage.KOREAN -> "$monthDay 마감"
                                        AppLanguage.TRADITIONAL_CHINESE -> "截止 $monthDay"
                                        else -> "截止 $monthDay"
                                    }
                                } else {
                                    when (effectiveLang) {
                                        AppLanguage.SIMPLIFIED_CHINESE -> "选择日期"
                                        AppLanguage.TRADITIONAL_CHINESE -> "選擇日期"
                                        AppLanguage.JAPANESE -> "日付を選択"
                                        AppLanguage.KOREAN -> "날짜 선택"
                                        AppLanguage.ENGLISH -> "Select Date"
                                        else -> "选择日期"
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .width(88.dp)
                                        .height(38.dp)
                                        .neumorphicInset(shape = RoundedCornerShape(8.dp), elevation = 2.dp)
                                        .background(NeumorphicSunkenBg, shape = RoundedCornerShape(8.dp))
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            stagePickerStageId = stage.id
                                        }
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EditCalendar,
                                            contentDescription = "Select Deadline",
                                            tint = NeumorphicAccent,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = deadlinePromptText,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (stageCalculatedEndDate != null) NeumorphicAccent else NeumorphicTextPrimary.copy(alpha = 0.5f),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // 事项/备注输入框 (缩减尺寸后自适应占据剩余空间)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .neumorphicInset(shape = RoundedCornerShape(8.dp), elevation = 2.dp)
                                        .background(NeumorphicSunkenBg, shape = RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (stage.remark.isEmpty()) {
                                        val defaultRemarkPlaceholder = when (lang) {
                                            AppLanguage.ENGLISH -> "Note ${index + 1}"
                                            AppLanguage.JAPANESE -> "第${index + 1}段階メモ"
                                            AppLanguage.KOREAN -> "${index + 1}단계 메모"
                                            AppLanguage.TRADITIONAL_CHINESE -> "第${index + 1}段想法"
                                            else -> "第${index + 1}段时间想法"
                                        }
                                        Text(
                                            text = defaultRemarkPlaceholder,
                                            fontSize = 11.sp,
                                            color = NeumorphicTextPrimary.copy(alpha = 0.45f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    BasicTextField(
                                        value = stage.remark,
                                        onValueChange = {
                                            viewModel.updateStageRemark(stage.id, it)
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(fontSize = 12.sp, color = NeumorphicTextPrimary),
                                        cursorBrush = SolidColor(NeumorphicAccent),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete Stage",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable {
                                            viewModel.removeCalculationStage(stage.id)
                                        }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 底部操作行：左侧“添加阶段”按键 + 右下角等于号“=”计算主按键
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                                    .background(NeumorphicAccent, shape = CircleShape)
                                    .clip(CircleShape)
                                    .clickable {
                                        viewModel.addCalculationStage()
                                    }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(LanguageUtils.getString("add_stage_btn", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            // 右下角 3D 新拟物等于号按键
                            Box(
                                modifier = Modifier
                                    .width(64.dp)
                                    .height(38.dp)
                                    .neumorphicExtruded(shape = RoundedCornerShape(12.dp), elevation = 4.dp)
                                    .background(NeumorphicAccent, shape = RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        keyboardController?.hide()
                                        viewModel.performCalculation()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("=", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. 计算结果展示层 (根据 Mode 严格渲染：单段加减天数 / 区间拆算 / 多段时间轴)
            if (uiState.showResult) {
                if (uiState.isMultiStageExtensionEnabled) {
                    val hasValidInput = uiState.stages.any { it.daysInput.isNotBlank() && (it.daysInput.toLongOrNull() ?: 0L) > 0L }
                    if (hasValidInput) {
                        // 多段加减模式：仅当存在有效天数且用户点击等于号时渲染多段 TimelineDiagram
                        val (finalDate, multiSegments) = viewModel.calculateMultiStageTimeline()
                        TimelineDiagram(
                            segments = multiSegments,
                            finalDate = finalDate,
                            baseDate = uiState.baseDate,
                            planTitle = uiState.multiStagePlanTitle,
                            weekendRule = uiState.weekendRule,
                            enableHolidays = uiState.enableChineseHolidays,
                            holidayRegion = uiState.holidayRegion,
                            language = lang,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else if (uiState.calcSubMode == CalcSubMode.REVERSE_RANGE) {
                    // 区间拆算模式：渲染 RangeBreakdownCard
                    val breakdown = viewModel.calculateRangeBreakdown()
                    val regionLabel = "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.getLocalizedName(lang)}"
                    RangeBreakdownCard(
                        visible = true,
                        result = breakdown,
                        regionLabel = regionLabel,
                        language = lang,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // 单段加减天数模式：渲染 ResultCard
                    ResultCard(
                        visible = true,
                        baseDate = uiState.baseDate,
                        resultDate = targetDate,
                        calculationType = uiState.calculationType,
                        daysInput = uiState.daysInput,
                        dateMode = uiState.dateMode,
                        weekendRule = uiState.weekendRule,
                        enableHolidays = uiState.enableChineseHolidays,
                        holidayRegion = uiState.holidayRegion,
                        isCurrentWeekBigWeek = uiState.isCurrentWeekBigWeek,
                        appLanguage = lang,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (stagePickerStageId != null) {
                val targetStageIndex = uiState.stages.indexOfFirst { it.id == stagePickerStageId }
                val initialDate = if (targetStageIndex >= 0) {
                    calculatedMultiSegments.getOrNull(targetStageIndex)?.endDate ?: uiState.baseDate
                } else {
                    uiState.baseDate
                }

                DatePickerModal(
                    selectedDate = initialDate,
                    onDateSelected = { selectedDate ->
                        stagePickerStageId?.let { stageId ->
                            viewModel.updateStageTargetDeadlineDate(stageId, selectedDate)
                        }
                        stagePickerStageId = null
                    },
                    onDismiss = { stagePickerStageId = null }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
