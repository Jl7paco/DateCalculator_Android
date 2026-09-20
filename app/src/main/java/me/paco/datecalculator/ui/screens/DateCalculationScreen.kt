package me.paco.datecalculator.ui.screens

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
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
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
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.DateMode
import me.paco.datecalculator.data.WeekendRule
import me.paco.datecalculator.ui.components.DatePickerModal
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicSegmentedRow
import me.paco.datecalculator.ui.components.NeumorphicSunkenBg
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import me.paco.datecalculator.ui.components.NumericCalculatorInput
import me.paco.datecalculator.ui.components.QuickDateChips
import me.paco.datecalculator.ui.components.RangeBreakdownCard
import me.paco.datecalculator.ui.components.ResultCard
import me.paco.datecalculator.ui.components.TimelineDiagram
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.viewmodel.CalcSubMode
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.DateCalculatorUtils

/**
 * 未点击时不显示高光条带，点击聚焦时全选高亮整个 15，打字输入后替代高光并变为深色正常文字
 */
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

    val resultDate = viewModel.calculateTargetDate()
    val (multiFinalDate, multiSegments) = viewModel.calculateMultiStageTimeline()
    val rangeBreakdown = viewModel.calculateRangeBreakdown()

    // 基准日期切换时的 Pop Bounce 缩放动效
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

    // 结果展开后自动平滑下滑至视口完全展示
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
    ) {
        // 顶部子页面选框: [ 工作日 | 自然日 ]
        NeumorphicSegmentedRow(
            items = listOf("工作日", stringResource(R.string.tab_natural_day)),
            selectedIndex = if (uiState.dateMode == DateMode.WORKDAY) 0 else 1,
            onIndexSelected = { index ->
                viewModel.updateDateMode(if (index == 0) DateMode.WORKDAY else DateMode.NATURAL_DAY)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                val infoText = if (uiState.dateMode == DateMode.WORKDAY) {
                    val regionText = "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.nativeName}"
                    val ruleLabel = when (uiState.weekendRule) {
                        WeekendRule.STANDARD_FIVE_DAYS -> stringResource(R.string.rule_five_days)
                        WeekendRule.ALTERNATE_BIG_SMALL_WEEKS -> stringResource(R.string.rule_big_small_weeks)
                        WeekendRule.SIX_DAYS_SUNDAY -> stringResource(R.string.rule_six_days_sunday)
                        WeekendRule.SIX_DAYS_SATURDAY -> stringResource(R.string.rule_six_days_saturday)
                        WeekendRule.SEVEN_DAYS -> stringResource(R.string.rule_seven_days)
                    }
                    stringResource(R.string.label_rule_prefix, ruleLabel, regionText)
                } else {
                    stringResource(R.string.label_natural_mode_info)
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
                    scaleY = cardScale.value
                    alpha = cardAlpha.value
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
                            text = stringResource(R.string.label_start_date_section),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val dateFormattedWithWeek = DateCalculatorUtils.formatDateWithWeek(uiState.baseDate)
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
            selectedDate = uiState.baseDate,
            onSelectDate = { date ->
                viewModel.updateBaseDate(date)
                viewModel.performCalculation()
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 计算加减天数与反向拆算 Header Row (模式融合整合)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 模式二合一无缝分段选择器: [ ➕/➖ 加减天数 | ↔️ 区间拆算 ]
            NeumorphicSegmentedRow(
                items = listOf("加减天数", "区间拆算"),
                selectedIndex = if (uiState.calcSubMode == CalcSubMode.FORWARD_DAYS) 0 else 1,
                onIndexSelected = { idx ->
                    val mode = if (idx == 0) CalcSubMode.FORWARD_DAYS else CalcSubMode.REVERSE_RANGE
                    viewModel.updateCalcSubMode(mode)
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // 右侧“多段模式”切换按键 (区间拆算下置灰不可用)
            val isMultiStageDisabled = (uiState.calcSubMode == CalcSubMode.REVERSE_RANGE)

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
                            Toast.makeText(context, "区间拆算模式下多段模式不可操作", Toast.LENGTH_SHORT).show()
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
                        text = "多段模式",
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
            stringResource(R.string.label_days_workday)
        } else {
            stringResource(R.string.label_days_natural)
        }

        // 1. 模式 A: 正向加减天数输入
        if (!uiState.isMultiStageExtensionEnabled && uiState.calcSubMode == CalcSubMode.FORWARD_DAYS) {
            NumericCalculatorInput(
                daysInput = uiState.daysInput,
                onDaysInputChange = { viewModel.updateDaysInput(it) },
                selectedType = uiState.calculationType,
                onTypeSelected = { viewModel.updateCalculationType(it) },
                onEqualClick = { viewModel.performCalculation() },
                dayUnitLabel = unitLabel
            )
        }

        // 2. 模式 B: 反向区间拆算输入 (选择终止日期进行精准拆分)
        if (!uiState.isMultiStageExtensionEnabled && uiState.calcSubMode == CalcSubMode.REVERSE_RANGE) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphicExtruded(shape = RoundedCornerShape(20.dp), elevation = 5.dp)
                    .background(NeumorphicBg, shape = RoundedCornerShape(20.dp))
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
                                .background(NeumorphicAccent, shape = RoundedCornerShape(14.dp))
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

        // 3. 模式 C: 高级多段计算模式（平滑物理弹簧展开/折叠，无突兀闪烁）
        AnimatedVisibility(
            visible = uiState.isMultiStageExtensionEnabled,
            enter = fadeIn(animationSpec = tween(280)) +
                    expandVertically(
                        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy),
                        expandFrom = Alignment.Top
                    ),
            exit = fadeOut(animationSpec = tween(200)) +
                   shrinkVertically(
                       animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy),
                       shrinkTowards = Alignment.Top
                   ),
            modifier = Modifier.clipToBounds()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicExtruded(shape = RoundedCornerShape(20.dp), elevation = 5.dp)
                        .background(NeumorphicBg, shape = RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "多段模式",
                            fontWeight = FontWeight.ExtraBold,
                            color = NeumorphicAccent,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 生活化温馨提示语
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .neumorphicInset(shape = RoundedCornerShape(10.dp))
                                .border(1.5.dp, NeumorphicAccent.copy(alpha = 0.4f), shape = RoundedCornerShape(10.dp))
                                .background(NeumorphicSunkenBg, shape = RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (uiState.multiStagePlanTitle.isEmpty()) {
                                Text("给这段安排起个名字 (如: 毕业旅行 / 装修进度 / 减脂计划)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f))
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
                        Text(
                            text = "设置不同时间段的天数与想法，为你智能推算各个节点日期:",
                            fontSize = 11.sp,
                            color = NeumorphicTextPrimary.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 各阶段配置项列表
                        uiState.stages.forEachIndexed { index, stage ->
                            val numZh = when (index + 1) {
                                1 -> "一"; 2 -> "二"; 3 -> "三"; 4 -> "四"; 5 -> "五"; else -> "${index + 1}"
                            }
                            val defaultRemark = "第${numZh}段时间"

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .neumorphicInset(shape = RoundedCornerShape(14.dp), elevation = 3.dp)
                                    .border(1.5.dp, NeumorphicAccent.copy(alpha = 0.35f), shape = RoundedCornerShape(14.dp))
                                    .background(NeumorphicBg, shape = RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val actionLabel = if (stage.type == CalculationType.ADD) "多段加" else "多段减"
                                    Text("${stage.remark.ifBlank { defaultRemark }} ($actionLabel)", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = NeumorphicAccent)

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

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. 运算符按钮：仅显示 "+" 或 "-"
                                    Box(
                                        modifier = Modifier
                                            .width(52.dp)
                                            .height(42.dp)
                                            .neumorphicExtruded(shape = RoundedCornerShape(10.dp), elevation = 3.dp)
                                            .background(
                                                if (stage.type == CalculationType.ADD) NeumorphicAccent else Color(0xFFEF4444),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clip(RoundedCornerShape(10.dp))
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

                                    // 2. 明显突出的天数数字输入框 (点击聚焦时全选高亮 15，未点击时不带蓝色高光条带)
                                    Box(
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(42.dp)
                                            .neumorphicInset(shape = RoundedCornerShape(10.dp))
                                            .border(1.5.dp, NeumorphicAccent.copy(alpha = 0.4f), shape = RoundedCornerShape(10.dp))
                                            .background(NeumorphicSunkenBg, shape = RoundedCornerShape(10.dp))
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

                                // 3. 阶段备注输入框
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .neumorphicInset(shape = RoundedCornerShape(10.dp))
                                        .border(1.5.dp, NeumorphicAccent.copy(alpha = 0.4f), shape = RoundedCornerShape(10.dp))
                                        .background(NeumorphicSunkenBg, shape = RoundedCornerShape(10.dp))
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

                        // 添加下一段时间按钮 与纯粹 "=" 按键
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
                                    .weight(0.8f)
                                    .height(44.dp)
                                    .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
                                    .background(NeumorphicAccent, shape = CircleShape)
                                    .clip(CircleShape)
                                    .clickable { viewModel.performCalculation() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("=", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 轴向线性示意图展示
                TimelineDiagram(
                    baseDate = uiState.baseDate,
                    finalDate = multiFinalDate,
                    segments = multiSegments,
                    modeLabel = unitLabel,
                    regionLabel = "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.nativeName}"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 结果卡片渲染 (根据模式 A、B 对应展示)
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
                    resultDate = resultDate,
                    weekendRule = uiState.weekendRule,
                    enableHolidays = uiState.enableChineseHolidays,
                    holidayRegion = uiState.holidayRegion,
                    isCurrentWeekBigWeek = uiState.isCurrentWeekBigWeek
                )
            } else {
                RangeBreakdownCard(
                    visible = uiState.showResult,
                    result = rangeBreakdown,
                    regionLabel = "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.nativeName}"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
