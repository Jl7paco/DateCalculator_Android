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
import kotlinx.coroutines.launch
import me.paco.datecalculator.R
import me.paco.datecalculator.data.DateMode
import me.paco.datecalculator.data.HolidayRegion
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
            .padding(16.dp)
    ) {
        // 顶部子页面选框: [ 工作日 | 自然日 ] (新拟物双胶囊 Toggle)
        NeumorphicSegmentedRow(
            items = listOf("工作日", stringResource(R.string.tab_natural_day)),
            selectedIndex = if (uiState.dateMode == DateMode.WORKDAY) 0 else 1,
            onIndexSelected = { index ->
                viewModel.updateDateMode(if (index == 0) DateMode.WORKDAY else DateMode.NATURAL_DAY)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 提示卡片 (新拟物 Extruded 凸起卡片)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicExtruded(shape = RoundedCornerShape(16.dp), elevation = 3.dp)
                .background(NeumorphicBg, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = NeumorphicAccent
                )
                Spacer(modifier = Modifier.width(8.dp))
                val infoText = if (uiState.dateMode == DateMode.WORKDAY) {
                    val regionText = if (uiState.enableChineseHolidays && uiState.holidayRegion != HolidayRegion.NONE) {
                        "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.nativeName}"
                    } else {
                        stringResource(R.string.label_no_holiday_active)
                    }
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
                    color = NeumorphicTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 显眼放大版基准起始日期 Hero Card (柔和主题底色)
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
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.label_start_date_section),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val dateFormattedWithWeek = DateCalculatorUtils.formatDateWithWeek(uiState.baseDate)
                        Text(
                            text = dateFormattedWithWeek,
                            fontSize = 18.sp,
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

        Spacer(modifier = Modifier.height(10.dp))

        // 点击快捷键高亮选中并直接触发计算与结果展示
        QuickDateChips(
            selectedDate = uiState.baseDate,
            onSelectDate = { date ->
                viewModel.updateBaseDate(date)
                viewModel.performCalculation()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(20.dp))

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

        Spacer(modifier = Modifier.height(24.dp))
    }
}
