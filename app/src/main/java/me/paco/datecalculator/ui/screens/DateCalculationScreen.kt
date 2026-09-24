package me.paco.datecalculator.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.paco.datecalculator.R
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.DateMode
import me.paco.datecalculator.data.StageSegmentResult
import me.paco.datecalculator.data.WeekendRule
import me.paco.datecalculator.ui.components.DatePickerModal
import me.paco.datecalculator.ui.components.HistoryOverlayDialog
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicSegmentedRow
import me.paco.datecalculator.ui.components.NeumorphicSunkenBg
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
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

@Composable
fun InsertDaysTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "15"
) {
    val isDefault = (value.isEmpty() || value == placeholder)
    val displayValue = if (value.isEmpty()) placeholder else value

    var isFocused by remember { mutableStateOf(false) }

    var textFieldValueState by remember(displayValue, isFocused) {
        mutableStateOf(
            TextFieldValue(
                text = displayValue,
                selection = if (isFocused && isDefault) TextRange(0, displayValue.length) else TextRange(displayValue.length)
            )
        )
    }

    val isDark = isSystemInDarkTheme()
    val textColor = if (isDefault) {
        if (isDark) Color(0xFF808D9E) else Color(0xFF94A3B8)
    } else {
        NeumorphicTextPrimary
    }

    BasicTextField(
        value = textFieldValueState,
        onValueChange = { newTFV ->
            val newText = newTFV.text
            if (newText.isEmpty() || newText.all { it.isDigit() }) {
                val nextSelection = if (newText != placeholder && isDefault) {
                    TextRange(newText.length)
                } else {
                    newTFV.selection
                }
                textFieldValueState = newTFV.copy(selection = nextSelection)
                onValueChange(newText)
            }
        },
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.onFocusChanged { focusState ->
            isFocused = focusState.isFocused
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateCalculationScreen(
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showReverseEndDatePicker by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val resultDate = viewModel.calculateTargetDate()
    val (multiFinalDate, multiSegments) = viewModel.calculateMultiStageTimeline()
    val rangeBreakdown = viewModel.calculateRangeBreakdown()

    val cardScale = remember { Animatable(1.0f) }
    val cardAlpha = remember { Animatable(1.0f) }
    var isInitialLoad by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.baseDate) {
        if (isInitialLoad) {
            isInitialLoad = false
            return@LaunchedEffect
        }
        launch {
            cardScale.animateTo(1.04f, animationSpec = tween(100))
            cardScale.animateTo(1.00f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        }
        launch {
            cardAlpha.snapTo(0.4f)
            cardAlpha.animateTo(1.0f, animationSpec = tween(250))
        }
    }

    LaunchedEffect(uiState.showResult) {
        if (uiState.showResult) {
            delay(180)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    if (showDatePicker) {
        DatePickerModal(
            selectedDate = uiState.baseDate,
            onDateSelected = {
                viewModel.updateBaseDate(it)
                viewModel.performCalculation()
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showReverseEndDatePicker) {
        DatePickerModal(
            selectedDate = uiState.reverseEndDate,
            onDateSelected = {
                viewModel.updateReverseEndDate(it)
                viewModel.performCalculation()
            },
            onDismiss = { showReverseEndDatePicker = false }
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
    ) {
        // 顶栏 (36dp 高度, 15sp 标题, 右上角历史记录与设置图标)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = NeumorphicAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = LanguageUtils.getString("tab_calc", uiState.appLanguage),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
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

        // 紧凑型规则说明条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicExtruded(shape = RoundedCornerShape(12.dp), elevation = 2.dp)
                .background(NeumorphicBg, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = NeumorphicAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val lang = uiState.appLanguage
                val infoText = if (uiState.dateMode == DateMode.WORKDAY) {
                    val regionText = "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.nativeName}"
                    val ruleLabel = when (uiState.weekendRule) {
                        WeekendRule.STANDARD_FIVE_DAYS -> when (lang) {
                            AppLanguage.ENGLISH -> "5-Day Workweek"
                            AppLanguage.JAPANESE -> "完全週休2日"
                            AppLanguage.KOREAN -> "주5일제"
                            AppLanguage.TRADITIONAL_CHINESE -> "雙休 (周六日休息)"
                            else -> "双休 (周六日休息)"
                        }
                        WeekendRule.ALTERNATE_BIG_SMALL_WEEKS -> when (lang) {
                            AppLanguage.ENGLISH -> "Alternate Big/Small Weeks"
                            AppLanguage.JAPANESE -> "隔週週休2日"
                            AppLanguage.KOREAN -> "격주 휴무"
                            AppLanguage.TRADITIONAL_CHINESE -> "大小周 (單雙休輪替)"
                            else -> "大小周 (单双休轮替)"
                        }
                        WeekendRule.SIX_DAYS_SUNDAY -> when (lang) {
                            AppLanguage.ENGLISH -> "6-Day (Sun Off)"
                            AppLanguage.JAPANESE -> "週休1日 (日曜休)"
                            AppLanguage.KOREAN -> "주6일 (일요일 휴무)"
                            AppLanguage.TRADITIONAL_CHINESE -> "單休 (僅周日休息)"
                            else -> "单休 (仅周日休息)"
                        }
                        WeekendRule.SIX_DAYS_SATURDAY -> when (lang) {
                            AppLanguage.ENGLISH -> "6-Day (Sat Off)"
                            AppLanguage.JAPANESE -> "週休1日 (土曜休)"
                            AppLanguage.KOREAN -> "주6일 (토요일 휴무)"
                            AppLanguage.TRADITIONAL_CHINESE -> "單休 (僅周六休息)"
                            else -> "单休 (仅周六休息)"
                        }
                        WeekendRule.SEVEN_DAYS -> when (lang) {
                            AppLanguage.ENGLISH -> "7-Day Workweek"
                            AppLanguage.JAPANESE -> "無休 (7日勤務)"
                            AppLanguage.KOREAN -> "무휴 (7일 근무)"
                            AppLanguage.TRADITIONAL_CHINESE -> "無休 (七天工作)"
                            else -> "无休 (七天工作)"
                        }
                    }
                    when (lang) {
                        AppLanguage.ENGLISH -> "Rule: $ruleLabel | Holidays: $regionText"
                        AppLanguage.JAPANESE -> "規則: $ruleLabel | 祝日: $regionText"
                        AppLanguage.KOREAN -> "규칙: $ruleLabel | 공휴일: $regionText"
                        AppLanguage.TRADITIONAL_CHINESE -> "規則: $ruleLabel | 節假日: $regionText"
                        else -> "规则: $ruleLabel | 节假日: $regionText"
                    }
                } else {
                    when (lang) {
                        AppLanguage.ENGLISH -> "Natural Day Mode: Includes all consecutive calendar days"
                        AppLanguage.JAPANESE -> "自然日モード: 祝日や週末を含むすべての暦日"
                        AppLanguage.KOREAN -> "자연일 모드: 모든 연속 달력 일수 포함"
                        AppLanguage.TRADITIONAL_CHINESE -> "自然日模式：包含所有連續日曆天數"
                        else -> "自然日模式: 包含所有连续日历天数"
                    }
                }

                Text(
                    text = infoText,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = NeumorphicTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 起始日期 Hero Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = cardScale.value
                    scaleY = cardAlpha.value
                }
                .clickable { showDatePicker = true },
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
                            text = LanguageUtils.getString("select_start_date", uiState.appLanguage),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val dateFormattedWithWeek = DateCalculatorUtils.formatDateWithWeek(uiState.baseDate, uiState.appLanguage)
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

        // 工作日和自然日切换拨动开关 (无外圈卡片，同时显示工作日与自然日，高亮选中文字)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(1f)) {
                QuickDateChips(
                    selectedDate = uiState.baseDate,
                    onSelectDate = { date ->
                        viewModel.updateBaseDate(date)
                        viewModel.performCalculation()
                    },
                    language = uiState.appLanguage
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            WorkdayNaturalSwitch(
                isWorkday = uiState.dateMode == DateMode.WORKDAY,
                onWorkdayChanged = { isWorkday ->
                    viewModel.updateDateMode(if (isWorkday) DateMode.WORKDAY else DateMode.NATURAL_DAY)
                },
                language = uiState.appLanguage,
                modifier = Modifier.width(140.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeumorphicSegmentedRow(
                items = listOf(LanguageUtils.getString("mode_forward", uiState.appLanguage), LanguageUtils.getString("mode_reverse", uiState.appLanguage)),
                selectedIndex = if (uiState.calcSubMode == CalcSubMode.FORWARD_DAYS) 0 else 1,
                onIndexSelected = { idx ->
                    val mode = if (idx == 0) CalcSubMode.FORWARD_DAYS else CalcSubMode.REVERSE_RANGE
                    viewModel.updateCalcSubMode(mode)
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(10.dp))

            val isMultiStageDisabled = (uiState.calcSubMode == CalcSubMode.REVERSE_RANGE)
            val lang = uiState.appLanguage

            Box(
                modifier = Modifier
                    .height(30.dp)
                    .neumorphicExtruded(
                        shape = CircleShape,
                        elevation = if (isMultiStageDisabled) 1.dp else if (uiState.isMultiStageExtensionEnabled) 2.dp else 4.dp
                    )
                    .background(
                        if (isMultiStageDisabled) NeumorphicBg.copy(alpha = 0.5f)
                        else if (uiState.isMultiStageExtensionEnabled) NeumorphicAccent
                        else NeumorphicBg,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .clickable {
                        if (isMultiStageDisabled) {
                            val disabledToast = when (lang) {
                                AppLanguage.ENGLISH -> "Multi-Stage is disabled in Date Interval mode"
                                AppLanguage.JAPANESE -> "期間計算モードでは複数段階は使用できません"
                                AppLanguage.KOREAN -> "기간 계산 모드에서는 다단계 모드를 사용할 수 없습니다"
                                AppLanguage.TRADITIONAL_CHINESE -> "區間拆算模式下多段模式不可操作"
                                else -> "区间拆算模式下多段模式不可操作"
                            }
                            Toast.makeText(context, disabledToast, Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.toggleMultiStageExtension(!uiState.isMultiStageExtensionEnabled)
                        }
                    }
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "切换多段模式",
                        tint = if (isMultiStageDisabled) NeumorphicTextPrimary.copy(alpha = 0.35f)
                               else if (uiState.isMultiStageExtensionEnabled) Color.White
                               else NeumorphicAccent,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = LanguageUtils.getString("multi_stage_btn", uiState.appLanguage),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isMultiStageDisabled) NeumorphicTextPrimary.copy(alpha = 0.35f)
                               else if (uiState.isMultiStageExtensionEnabled) Color.White
                               else NeumorphicAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val unitLabel = if (uiState.dateMode == DateMode.WORKDAY) {
            LanguageUtils.getString("workday", uiState.appLanguage)
        } else {
            LanguageUtils.getString("natural_day", uiState.appLanguage)
        }

        // 1. 模式 A: 正向加减天数输入
        if (!uiState.isMultiStageExtensionEnabled && uiState.calcSubMode == CalcSubMode.FORWARD_DAYS) {
            NumericCalculatorInput(
                daysInput = uiState.daysInput,
                onDaysInputChange = { viewModel.updateDaysInput(it) },
                selectedType = uiState.calculationType,
                onTypeSelected = { viewModel.updateCalculationType(it) },
                onEqualClick = { viewModel.performCalculation() },
                dayUnitLabel = unitLabel,
                language = uiState.appLanguage
            )
        }

        // 2. 模式 B: 反向区间拆算输入
        if (!uiState.isMultiStageExtensionEnabled && uiState.calcSubMode == CalcSubMode.REVERSE_RANGE) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphicExtruded(shape = RoundedCornerShape(20.dp), elevation = 5.dp)
                    .background(NeumorphicBg, shape = RoundedCornerShape(20.dp))
                    .border(1.5.dp, NeumorphicAccent.copy(alpha = 0.35f), shape = RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("选择终止日期拆算包含的天数:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .neumorphicInset(shape = RoundedCornerShape(14.dp), elevation = 3.dp)
                                .border(1.dp, NeumorphicAccent.copy(alpha = 0.3f), shape = RoundedCornerShape(14.dp))
                                .background(NeumorphicBg, shape = RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { showReverseEndDatePicker = true }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("终止日期: ${DateCalculatorUtils.formatDate(uiState.reverseEndDate)}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = NeumorphicTextPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .height(52.dp)
                                .neumorphicExtruded(shape = RoundedCornerShape(14.dp), elevation = 4.dp)
                                .background(Color(0xFFEF4444), shape = RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { viewModel.performCalculation() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("=", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                }
            }
        }

        // 3. 模式 C: 高级多段计算模式
        val multiStageAlpha by animateFloatAsState(
            targetValue = if (uiState.isMultiStageExtensionEnabled) 1f else 0f,
            animationSpec = tween(
                durationMillis = if (uiState.isMultiStageExtensionEnabled) 160 else 120,
                easing = if (uiState.isMultiStageExtensionEnabled) FastOutSlowInEasing else FastOutLinearInEasing
            ),
            label = "MultiStageAlphaGpuAnim"
        )

        val cardShape22 = RoundedCornerShape(22.dp)

        AnimatedVisibility(
            visible = uiState.isMultiStageExtensionEnabled,
            enter = fadeIn(animationSpec = tween(durationMillis = 140, easing = LinearOutSlowInEasing)) +
                    expandVertically(
                        animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
                        expandFrom = Alignment.Top
                    ),
            exit = fadeOut(animationSpec = tween(durationMillis = 100, easing = FastOutLinearInEasing)) +
                   shrinkVertically(
                       animationSpec = tween(durationMillis = 120, easing = FastOutLinearInEasing),
                       shrinkTowards = Alignment.Top
                   )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = multiStageAlpha
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                        .neumorphicExtruded(shape = cardShape22, elevation = 5.dp)
                        .background(NeumorphicBg, shape = cardShape22)
                        .border(1.2.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = cardShape22),
                    shape = cardShape22,
                    color = Color.Transparent
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        val lang = uiState.appLanguage
                        val multiStageHeaderTitle = when (lang) {
                            AppLanguage.ENGLISH -> "Multi-Stage Schedule"
                            AppLanguage.JAPANESE -> "複数段階計算"
                            AppLanguage.KOREAN -> "다단계 일정 산출"
                            AppLanguage.TRADITIONAL_CHINESE -> "多段模式"
                            else -> "多段模式"
                        }
                        Text(
                            text = multiStageHeaderTitle,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeumorphicAccent,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

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
                                Text(planPlaceholder, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f))
                            }
                            BasicTextField(
                                value = uiState.multiStagePlanTitle,
                                onValueChange = { viewModel.updateMultiStagePlanTitle(it) },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val planSubtitle = when (lang) {
                            AppLanguage.ENGLISH -> "Set days and notes for each stage to calculate milestone dates:"
                            AppLanguage.JAPANESE -> "各段階の日数とノートを設定してマイルストーン日程を算定:"
                            AppLanguage.KOREAN -> "각 단계별 일수와 메모를 설정하여 마일스톤 날짜 산출:"
                            AppLanguage.TRADITIONAL_CHINESE -> "設置不同時間段的天數與想法，為你智能推算各個節點日期:"
                            else -> "设置不同时间段的天数与想法，为你智能推算各个节点日期:"
                        }
                        Text(
                            text = planSubtitle,
                            fontSize = 11.sp,
                            color = NeumorphicTextPrimary.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        uiState.stages.forEachIndexed { index, stage ->
                            val numZh = when (index + 1) {
                                1 -> "一"; 2 -> "二"; 3 -> "三"; 4 -> "四"; 5 -> "五"; else -> "${index + 1}"
                            }
                            val defaultRemark = "第${numZh}段时间"
                            val stageShape16 = RoundedCornerShape(16.dp)

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .neumorphicInset(shape = stageShape16, elevation = 3.dp)
                                    .border(1.2.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = stageShape16)
                                    .background(NeumorphicBg, shape = stageShape16)
                                    .clip(stageShape16)
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val actionLabel = if (stage.type == CalculationType.ADD) "多段加" else "多段减"
                                    Text("${stage.remark.ifBlank { defaultRemark }} ($actionLabel)", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = NeumorphicAccent)

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (index > 0) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowUp,
                                                contentDescription = "向上调换",
                                                tint = NeumorphicAccent,
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clickable {
                                                        viewModel.reorderCalculationStages(index, index - 1)
                                                    }
                                            )
                                        }

                                        if (index < uiState.stages.size - 1) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "向下调换",
                                                tint = NeumorphicAccent,
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clickable {
                                                        viewModel.reorderCalculationStages(index, index + 1)
                                                    }
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "复制阶段",
                                            tint = NeumorphicAccent,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable {
                                                    viewModel.duplicateCalculationStage(stage.id)
                                                    Toast.makeText(context, "已复制阶段", Toast.LENGTH_SHORT).show()
                                                }
                                        )

                                        if (uiState.stages.size > 1) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "删除时间段",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clickable { viewModel.removeCalculationStage(stage.id) }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val opBtnShape = RoundedCornerShape(10.dp)
                                    Box(
                                        modifier = Modifier
                                            .width(52.dp)
                                            .height(42.dp)
                                            .neumorphicExtruded(shape = opBtnShape, elevation = 3.dp)
                                            .background(
                                                if (stage.type == CalculationType.ADD) NeumorphicAccent else Color(0xFFEF4444),
                                                shape = opBtnShape
                                            )
                                            .clip(opBtnShape)
                                            .clickable {
                                                val nextType = if (stage.type == CalculationType.ADD) CalculationType.SUBTRACT else CalculationType.ADD
                                                viewModel.updateStageType(stage.id, nextType)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (stage.type == CalculationType.ADD) "+" else "-",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    val inputShape10 = RoundedCornerShape(10.dp)
                                    Box(
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(42.dp)
                                            .neumorphicInset(shape = inputShape10)
                                            .border(1.2.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = inputShape10)
                                            .background(NeumorphicSunkenBg, shape = inputShape10)
                                            .clip(inputShape10)
                                            .padding(horizontal = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        InsertDaysTextField(
                                            value = stage.daysInput,
                                            onValueChange = { newValue ->
                                                viewModel.updateStageDaysInput(stage.id, newValue)
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(unitLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val remarkShape10 = RoundedCornerShape(10.dp)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .neumorphicInset(shape = remarkShape10)
                                        .border(1.2.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = remarkShape10)
                                        .background(NeumorphicSunkenBg, shape = remarkShape10)
                                        .clip(remarkShape10)
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (stage.remark.isEmpty()) {
                                        Text("记下这段时间要安排的事 (如: 方案准备 / 旅程第一站)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f))
                                    }
                                    BasicTextField(
                                        value = stage.remark,
                                        onValueChange = { viewModel.updateStageRemark(stage.id, it) },
                                        singleLine = true,
                                        textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(44.dp)
                                    .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                                    .background(NeumorphicBg, shape = CircleShape)
                                    .clip(CircleShape)
                                    .clickable { viewModel.addCalculationStage() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("添加下一段时间", fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
                                    .background(NeumorphicAccent, shape = CircleShape)
                                    .clip(CircleShape)
                                    .clickable { viewModel.performCalculation() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(LanguageUtils.getString("save_record", uiState.appLanguage), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TimelineDiagram(
                    baseDate = uiState.baseDate,
                    finalDate = multiFinalDate,
                    segments = multiSegments,
                    modeLabel = unitLabel,
                    regionLabel = "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.nativeName}",
                    language = uiState.appLanguage
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 结果卡片与单段时间轴渲染
        if (!uiState.isMultiStageExtensionEnabled) {
            if (uiState.calcSubMode == CalcSubMode.FORWARD_DAYS) {
                val resultTitle = if (uiState.dateMode == DateMode.WORKDAY) {
                    stringResource(R.string.label_result_title_workday)
                } else {
                    stringResource(R.string.label_result_title_natural)
                }

                ResultCard(
                    visible = uiState.showResult,
                    title = resultTitle,
                    baseDate = uiState.baseDate,
                    resultDate = resultDate,
                    calculationType = uiState.calculationType,
                    daysInput = uiState.daysInput,
                    dateMode = uiState.dateMode,
                    weekendRule = uiState.weekendRule,
                    enableHolidays = uiState.enableChineseHolidays,
                    holidayRegion = uiState.holidayRegion,
                    isCurrentWeekBigWeek = uiState.isCurrentWeekBigWeek,
                    appLanguage = uiState.appLanguage
                )
            } else {
                RangeBreakdownCard(
                    visible = uiState.showResult,
                    result = rangeBreakdown,
                    regionLabel = "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.nativeName}",
                    language = uiState.appLanguage
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
