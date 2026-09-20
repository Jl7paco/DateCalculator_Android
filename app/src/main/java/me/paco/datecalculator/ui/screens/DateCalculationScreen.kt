package me.paco.datecalculator.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.paco.datecalculator.R
import me.paco.datecalculator.data.DateMode
import me.paco.datecalculator.data.WeekendRule
import me.paco.datecalculator.ui.components.DatePickerModal
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicSegmentedRow
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import me.paco.datecalculator.ui.components.NumericCalculatorInput
import me.paco.datecalculator.ui.components.QuickDateChips
import me.paco.datecalculator.ui.components.ResultCard
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.DateCalculatorUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateCalculationScreen(
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showDatePicker by remember { mutableStateOf(false) }

    val resultDate = viewModel.calculateTargetDate()

    // 基准日期切换时的 Pop Bounce 缩放与渐变高亮动效
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

    // 结果展开后自动平滑下滑至结果区域完全展示 (无需用户手动下滑)
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

        // 紧凑型规则说明条 (占用极小高度，提升一屏空间展现率)
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

        // 紧凑精致版起始日期 Hero Card
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

        // 快捷选项 Chips
        QuickDateChips(
            selectedDate = uiState.baseDate,
            onSelectDate = { date ->
                viewModel.updateBaseDate(date)
                viewModel.performCalculation()
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        val unitLabel = if (uiState.dateMode == DateMode.WORKDAY) {
            stringResource(R.string.label_days_workday)
        } else {
            stringResource(R.string.label_days_natural)
        }

        NumericCalculatorInput(
            daysInput = uiState.daysInput,
            onDaysInputChange = { viewModel.updateDaysInput(it) },
            selectedType = uiState.calculationType,
            onTypeSelected = { viewModel.updateCalculationType(it) },
            onEqualClick = { viewModel.performCalculation() },
            dayUnitLabel = unitLabel
        )

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(16.dp))
    }
}
