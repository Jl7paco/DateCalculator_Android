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
    val lang = uiState.appLanguage
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
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = NeumorphicAccent,
                        modifier = Modifier.size(18.dp)
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
                            contentDescription = LanguageUtils.getString("history_title", lang),
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
                            contentDescription = LanguageUtils.getString("settings_title", lang),
                            tint = NeumorphicAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
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
                        }
                        1 -> {
                            viewModel.toggleMultiStageExtension(false)
                            viewModel.updateCalcSubMode(CalcSubMode.REVERSE_RANGE)
                        }
                        2 -> {
                            viewModel.toggleMultiStageExtension(true)
                        }
                    }
                    viewModel.performCalculation()
                },
                height = 36.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 1. 基准起算日期面板
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphicExtruded(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                    .background(NeumorphicBg, shape = RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
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
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Column {
                            Text(
                                text = LanguageUtils.getString("select_start_date", lang),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val dateFormattedWithWeek = DateCalculatorUtils.formatDateWithWeek(uiState.baseDate, lang)
                            Text(
                                text = dateFormattedWithWeek,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeumorphicTextPrimary
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

            // 工作日和自然日切换拨动开关
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
                        language = lang
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                WorkdayNaturalSwitch(
                    isWorkday = uiState.dateMode == DateMode.WORKDAY,
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
                    dayUnitLabel = if (uiState.dateMode == DateMode.WORKDAY) LanguageUtils.getString("workday", lang) else LanguageUtils.getString("natural_day", lang),
                    language = lang
                )
            }

            // 2. 模式 B: 区间拆算面板
            if (!uiState.isMultiStageExtensionEnabled && uiState.calcSubMode == CalcSubMode.REVERSE_RANGE) {
                val cardShape18 = RoundedCornerShape(18.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicExtruded(shape = cardShape18, elevation = 4.dp)
                        .background(NeumorphicBg, shape = cardShape18)
                        .clip(cardShape18)
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = LanguageUtils.getString("reverse_end_date_title", lang),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            color = NeumorphicTextPrimary.copy(alpha = 0.75f)
                        )

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
                                    Text("${LanguageUtils.getString("end_date_label", lang)}: ${DateCalculatorUtils.formatDate(uiState.reverseEndDate, lang)}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = NeumorphicTextPrimary)
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

            if (uiState.isMultiStageExtensionEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                            alpha = multiStageAlpha
                        }
                        .neumorphicExtruded(shape = cardShape22, elevation = 5.dp)
                        .background(NeumorphicBg, shape = cardShape22)
                        .clip(cardShape22)
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(LanguageUtils.getString("multi_stage_btn", lang), fontWeight = FontWeight.Bold, color = NeumorphicAccent, fontSize = 14.sp)
                            }
                        }

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
                            val defaultRemark = LanguageUtils.getLocalizedStageTitle(index + 1, lang)
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
                                    val actionLabel = if (stage.type == CalculationType.ADD) LanguageUtils.getString("stage_add_label", lang) else LanguageUtils.getString("stage_sub_label", lang)
                                    Text("${stage.remark.ifBlank { defaultRemark }} ($actionLabel)", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = NeumorphicAccent)

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (index > 0) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowUp,
                                                contentDescription = "Up",
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
                                                contentDescription = "Down",
                                                tint = NeumorphicAccent,
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clickable {
                                                        viewModel.reorderCalculationStages(index, index + 1)
                                                    }
                                            )
                                        }

                                        if (uiState.stages.size > 1) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Delete",
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
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.width(110.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val isAdd = stage.type == CalculationType.ADD
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isAdd) NeumorphicAccent else NeumorphicSunkenBg)
                                                .clickable { viewModel.updateStageType(stage.id, CalculationType.ADD) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("+", fontWeight = FontWeight.ExtraBold, color = if (isAdd) Color.White else NeumorphicTextPrimary, fontSize = 14.sp)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (!isAdd) Color(0xFFEF4444) else NeumorphicSunkenBg)
                                                .clickable { viewModel.updateStageType(stage.id, CalculationType.SUBTRACT) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("-", fontWeight = FontWeight.ExtraBold, color = if (!isAdd) Color.White else NeumorphicTextPrimary, fontSize = 14.sp)
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .neumorphicInset(shape = RoundedCornerShape(8.dp), elevation = 2.dp)
                                            .background(NeumorphicSunkenBg, shape = RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        InsertDaysTextField(
                                            value = stage.daysInput,
                                            onValueChange = { viewModel.updateStageDaysInput(stage.id, it) },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = "15"
                                        )
                                    }

                                    val modeUnit = if (uiState.dateMode == DateMode.WORKDAY) LanguageUtils.getString("workday", lang) else LanguageUtils.getString("natural_day", lang)
                                    Text(modeUnit, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NeumorphicTextPrimary)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .neumorphicInset(shape = RoundedCornerShape(8.dp), elevation = 2.dp)
                                        .background(NeumorphicSunkenBg, shape = RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (stage.remark.isEmpty()) {
                                        Text(LanguageUtils.getString("stage_remark_hint", lang), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f))
                                    }
                                    BasicTextField(
                                        value = stage.remark,
                                        onValueChange = { viewModel.updateStageRemark(stage.id, it) },
                                        singleLine = true,
                                        textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = NeumorphicTextPrimary),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .neumorphicExtruded(shape = RoundedCornerShape(12.dp), elevation = 4.dp)
                                    .background(NeumorphicBg, shape = RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.addCalculationStage() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(LanguageUtils.getString("add_stage_btn", lang), fontWeight = FontWeight.Bold, color = NeumorphicAccent, fontSize = 12.5.sp)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .neumorphicExtruded(shape = RoundedCornerShape(12.dp), elevation = 4.dp)
                                    .background(NeumorphicAccent, shape = RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        val customTitle = uiState.multiStagePlanTitle.ifBlank {
                                            when (lang) {
                                                AppLanguage.ENGLISH -> "Multi-Stage Calculation"
                                                AppLanguage.JAPANESE -> "マルチステージ計算"
                                                AppLanguage.KOREAN -> "다단계 날짜 계산"
                                                AppLanguage.TRADITIONAL_CHINESE -> "多段天數計算"
                                                else -> "多段天数计算"
                                            }
                                        }
                                        val detailStr = multiSegments.joinToString(" ➔ ") { seg ->
                                            val symbol = if (seg.type == CalculationType.ADD) "+" else "-"
                                            val remark = if (seg.remark.isNotBlank()) " (${seg.remark})" else ""
                                            "$symbol${seg.daysCount}${if (uiState.dateMode == DateMode.WORKDAY) "工作日" else "自然日"}$remark: ${DateCalculatorUtils.formatDate(seg.endDate, lang)}"
                                        }

                                        viewModel.saveToHistory(
                                            category = LanguageUtils.getString("tab_calc", lang),
                                            title = customTitle,
                                            detail = detailStr,
                                            resultDate = multiFinalDate
                                        )

                                        Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(LanguageUtils.getString("save_record", lang), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.5.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. 模式 A 结果卡片
            if (!uiState.isMultiStageExtensionEnabled && uiState.calcSubMode == CalcSubMode.FORWARD_DAYS) {
                ResultCard(
                    visible = uiState.showResult,
                    baseDate = uiState.baseDate,
                    resultDate = resultDate,
                    calculationType = uiState.calculationType,
                    daysInput = uiState.daysInput,
                    dateMode = uiState.dateMode,
                    weekendRule = uiState.weekendRule,
                    enableHolidays = uiState.enableChineseHolidays,
                    holidayRegion = uiState.holidayRegion,
                    appLanguage = lang
                )
            }

            // 4. 模式 B 结果卡片 (区间拆算)
            if (!uiState.isMultiStageExtensionEnabled && uiState.calcSubMode == CalcSubMode.REVERSE_RANGE) {
                RangeBreakdownCard(
                    visible = uiState.showResult,
                    result = rangeBreakdown,
                    regionLabel = "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.getLocalizedName(lang)}",
                    language = lang
                )
            }

            // 4. 模式 C 结果卡片 (多段连算排期主时间轴)
            if (uiState.isMultiStageExtensionEnabled) {
                val modeLabel = if (uiState.dateMode == DateMode.WORKDAY) LanguageUtils.getString("workday", lang) else LanguageUtils.getString("natural_day", lang)
                val regionLabel = "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.getLocalizedName(lang)}"

                TimelineDiagram(
                    baseDate = uiState.baseDate,
                    finalDate = multiFinalDate,
                    segments = multiSegments,
                    modeLabel = modeLabel,
                    regionLabel = regionLabel,
                    showOuterCard = true,
                    language = lang
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
