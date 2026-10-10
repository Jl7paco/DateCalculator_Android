package me.paco.datecalculator.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
import me.paco.datecalculator.ui.components.ResultCard
import me.paco.datecalculator.ui.components.SettingsOverlayDialog
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
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

            // 1. 基准起算日期面板 (与其他页面100%统一的 3D 新拟物内凹槽输入框，高度 58dp，圆角 18dp)
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

            // 工作日 / 自然日同页胶囊拨动开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageUtils.getString("calc_rule_title", lang),
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
                    dayUnitLabel = if (uiState.dateMode == DateMode.WORKDAY) LanguageUtils.getString("workday", lang) else LanguageUtils.getString("natural_day", lang),
                    language = lang
                )
            }

            // 2. 模式 B: 区间拆算面板 (统一使用 18dp 圆角 3D 凹槽材质)
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
                                Text("=", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                            alpha = multiStageAlpha
                        }
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
                                Text(planPlaceholder, fontSize = 12.sp, color = NeumorphicTextPrimary.copy(alpha = 0.5f))
                            }
                            BasicTextField(
                                value = uiState.multiStagePlanTitle,
                                onValueChange = { viewModel.updateMultiStagePlanTitle(it) },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary),
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
                                    imageVector = Icons.Default.DragHandle,
                                    contentDescription = "Reorder",
                                    tint = NeumorphicAccent.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(end = 4.dp)
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
                                            viewModel.performCalculation()
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

                                Spacer(modifier = Modifier.width(6.dp))

                                Box(
                                    modifier = Modifier
                                        .width(70.dp)
                                        .height(38.dp)
                                        .neumorphicInset(shape = RoundedCornerShape(8.dp), elevation = 2.dp)
                                        .background(NeumorphicSunkenBg, shape = RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp),
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
                                            fontSize = 12.sp,
                                            color = NeumorphicTextPrimary.copy(alpha = 0.45f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    BasicTextField(
                                        value = stage.daysInput,
                                        onValueChange = {
                                            viewModel.updateStageDaysInput(stage.id, it)
                                            viewModel.performCalculation()
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

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
                                            AppLanguage.ENGLISH -> "Stage ${index + 1} Note"
                                            AppLanguage.JAPANESE -> "第${index + 1}段階メモ"
                                            AppLanguage.KOREAN -> "${index + 1}단계 메모"
                                            AppLanguage.TRADITIONAL_CHINESE -> "第${index + 1}段想法/事項"
                                            else -> "第${index + 1}段时间想法/事项"
                                        }
                                        Text(
                                            text = defaultRemarkPlaceholder,
                                            fontSize = 11.5.sp,
                                            color = NeumorphicTextPrimary.copy(alpha = 0.45f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    BasicTextField(
                                        value = stage.remark,
                                        onValueChange = {
                                            viewModel.updateStageRemark(stage.id, it)
                                            viewModel.performCalculation()
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(fontSize = 12.sp, color = NeumorphicTextPrimary),
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
                                            viewModel.performCalculation()
                                        }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(32.dp)
                                    .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                                    .background(NeumorphicAccent, shape = CircleShape)
                                    .clip(CircleShape)
                                    .clickable {
                                        viewModel.addCalculationStage()
                                        viewModel.performCalculation()
                                    }
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(LanguageUtils.getString("add_stage_btn", lang), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. 计算结果展示层
            if (uiState.showResult) {
                ResultCard(
                    visible = true,
                    baseDate = uiState.baseDate,
                    resultDate = resultDate,
                    calculationType = uiState.calculationType,
                    daysInput = uiState.daysInput,
                    dateMode = uiState.dateMode,
                    weekendRule = uiState.weekendRule,
                    enableHolidays = uiState.enableChineseHolidays,
                    holidayRegion = uiState.holidayRegion,
                    isCurrentWeekBigWeek = uiState.isCurrentWeekBigWeek,
                    appLanguage = uiState.appLanguage,
                    modifier = Modifier.graphicsLayer {
                        scaleX = cardScale.value
                        scaleY = cardAlpha.value
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
