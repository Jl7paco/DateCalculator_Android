package me.paco.datecalculator.ui.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import me.paco.datecalculator.data.CalculationStage
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.DateMode
import me.paco.datecalculator.data.HistoryItem
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.RegionalHolidays
import me.paco.datecalculator.data.StageSegmentResult
import me.paco.datecalculator.data.WeekendRule
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LocationUtils
import me.paco.datecalculator.util.PreferenceUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

enum class EventRepeatMode(val label: String) {
    NONE("不重复"),
    WEEKLY("每周"),
    MONTHLY("每月"),
    YEARLY("每年")
}

enum class CalcSubMode(val label: String) {
    FORWARD_DAYS("加减天数"),
    REVERSE_RANGE("区间拆算")
}

data class RangeBreakdownResult(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalNaturalDays: Long,
    val workdaysCount: Long,
    val statutoryHolidaysCount: Long,
    val regularWeekendDaysCount: Long
)

data class CustomEventItem(
    val id: Long = System.nanoTime(),
    val iconEmoji: String = "📌",
    val name: String,
    val targetDate: LocalDate,
    val repeatMode: EventRepeatMode = EventRepeatMode.NONE
) {
    fun getNextUpcomingDate(baseDate: LocalDate = LocalDate.now()): LocalDate {
        if (repeatMode == EventRepeatMode.NONE || !targetDate.isBefore(baseDate)) {
            return targetDate
        }

        var upcoming = targetDate
        when (repeatMode) {
            EventRepeatMode.WEEKLY -> {
                while (upcoming.isBefore(baseDate)) {
                    upcoming = upcoming.plusWeeks(1)
                }
            }
            EventRepeatMode.MONTHLY -> {
                while (upcoming.isBefore(baseDate)) {
                    upcoming = upcoming.plusMonths(1)
                }
            }
            EventRepeatMode.YEARLY -> {
                while (upcoming.isBefore(baseDate)) {
                    upcoming = upcoming.plusYears(1)
                }
            }
            EventRepeatMode.NONE -> {}
        }
        return upcoming
    }
}

data class DateCalculatorUiState(
    val baseDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now().plusDays(30),
    val reverseEndDate: LocalDate = LocalDate.now().plusDays(30),
    val daysInput: String = "15",
    val calculationType: CalculationType = CalculationType.ADD,
    val dateMode: DateMode = DateMode.WORKDAY,
    val calcSubMode: CalcSubMode = CalcSubMode.FORWARD_DAYS,
    val weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
    val isCurrentWeekBigWeek: Boolean = true,
    val enableChineseHolidays: Boolean = true,
    val holidayRegion: HolidayRegion = HolidayRegion.CHINA,
    val isGpsAutoDetectEnabled: Boolean = true,
    val disableChinaShiftWorkdays: Boolean = false,
    val disabledPresetHolidays: Set<String> = emptySet(),
    val isMultiStageExtensionEnabled: Boolean = false,
    val multiStagePlanTitle: String = "",
    val stages: List<CalculationStage> = listOf(
        CalculationStage(type = CalculationType.ADD, days = 15L, remark = "第一段时间"),
        CalculationStage(type = CalculationType.ADD, days = 15L, remark = "第二段时间")
    ),
    val showResult: Boolean = false,
    val historyList: List<HistoryItem> = emptyList(),
    val customEvents: List<CustomEventItem> = emptyList(),
    val fireworksTrigger: Int = 0
)

class DateCalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DateCalculatorUiState())
    val uiState: StateFlow<DateCalculatorUiState> = _uiState.asStateFlow()

    fun initPreferences(context: Context) {
        val savedRegion = PreferenceUtils.getHolidayRegion(context)
        val savedRule = PreferenceUtils.getWeekendRule(context)
        val savedBigWeek = PreferenceUtils.getIsBigWeek(context)
        val savedGpsAuto = PreferenceUtils.getIsGpsAuto(context)
        val savedDisableShift = PreferenceUtils.getDisableChinaShift(context)

        _uiState.value = _uiState.value.copy(
            holidayRegion = savedRegion,
            weekendRule = savedRule,
            isCurrentWeekBigWeek = savedBigWeek,
            isGpsAutoDetectEnabled = savedGpsAuto,
            disableChinaShiftWorkdays = savedDisableShift
        )

        // 启动 App 时静默同步当前地区放假安排，只同步一次且不弹 Toast
        if (savedGpsAuto) {
            val detected = LocationUtils.detectCurrentRegion(context)
            updateHolidayRegion(detected, context, showToast = false)
        } else {
            autoSyncHolidayData(context, savedRegion, showToast = false)
        }
    }

    private fun autoSyncHolidayData(context: Context, region: HolidayRegion, showToast: Boolean = false) {
        if (showToast) {
            val regionName = "${region.flagEmoji} ${region.nativeName}"
            Toast.makeText(context, "已自动同步 $regionName 最新节假日数据", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateBaseDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(baseDate = date, showResult = false)
    }

    fun updateEndDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(endDate = date, showResult = false)
    }

    fun updateReverseEndDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(reverseEndDate = date, showResult = false)
    }

    fun updateDaysInput(input: String) {
        _uiState.value = _uiState.value.copy(daysInput = input, showResult = false)
    }

    fun updateCalculationType(type: CalculationType) {
        val updatedStages = _uiState.value.stages.map { it.copy(type = type) }
        _uiState.value = _uiState.value.copy(
            calculationType = type,
            stages = updatedStages,
            showResult = false
        )
    }

    fun updateDateMode(mode: DateMode) {
        _uiState.value = _uiState.value.copy(dateMode = mode, showResult = false)
    }

    fun updateCalcSubMode(mode: CalcSubMode) {
        // 切换至区间拆算模式时，自动屏蔽/关闭多段模式
        val disableMulti = if (mode == CalcSubMode.REVERSE_RANGE) false else _uiState.value.isMultiStageExtensionEnabled
        _uiState.value = _uiState.value.copy(
            calcSubMode = mode,
            isMultiStageExtensionEnabled = disableMulti,
            showResult = false
        )
    }

    fun updateWeekendRule(rule: WeekendRule, context: Context? = null) {
        _uiState.value = _uiState.value.copy(weekendRule = rule, showResult = false)
        context?.let { PreferenceUtils.saveWeekendRule(it, rule) }
    }

    fun updateCurrentWeekBigWeek(isBigWeek: Boolean, context: Context? = null) {
        _uiState.value = _uiState.value.copy(isCurrentWeekBigWeek = isBigWeek, showResult = false)
        context?.let { PreferenceUtils.saveIsBigWeek(it, isBigWeek) }
    }

    fun updateHolidayRegion(region: HolidayRegion, context: Context? = null, showToast: Boolean = true) {
        val rule = if (region == HolidayRegion.CHINA || region == HolidayRegion.HONG_KONG || region == HolidayRegion.MACAO) {
            _uiState.value.weekendRule
        } else {
            WeekendRule.STANDARD_FIVE_DAYS
        }

        _uiState.value = _uiState.value.copy(
            holidayRegion = region,
            enableChineseHolidays = true,
            weekendRule = rule,
            showResult = false
        )
        context?.let {
            PreferenceUtils.saveHolidayRegion(it, region)
            autoSyncHolidayData(it, region, showToast = showToast)
        }
    }

    fun updateGpsAutoDetect(enabled: Boolean, context: Context? = null) {
        _uiState.value = _uiState.value.copy(isGpsAutoDetectEnabled = enabled)
        context?.let {
            PreferenceUtils.saveIsGpsAuto(it, enabled)
            if (enabled) {
                val detected = LocationUtils.detectCurrentRegion(it)
                updateHolidayRegion(detected, it, showToast = true)
            }
        }
    }

    fun updateDisableChinaShift(disable: Boolean, context: Context? = null) {
        _uiState.value = _uiState.value.copy(disableChinaShiftWorkdays = disable, showResult = false)
        context?.let { PreferenceUtils.saveDisableChinaShift(it, disable) }
    }

    fun updateMultiStagePlanTitle(title: String) {
        _uiState.value = _uiState.value.copy(multiStagePlanTitle = title)
    }

    fun toggleMultiStageExtension(enabled: Boolean) {
        if (enabled && _uiState.value.stages.isNotEmpty()) {
            val mainType = _uiState.value.calculationType
            val current = _uiState.value.stages.map { it.copy(type = mainType) }
            _uiState.value = _uiState.value.copy(
                stages = current,
                isMultiStageExtensionEnabled = true,
                showResult = false
            )
        } else {
            _uiState.value = _uiState.value.copy(
                isMultiStageExtensionEnabled = enabled,
                showResult = false
            )
        }
    }

    fun addCalculationStage() {
        val current = _uiState.value.stages.toMutableList()
        val nextIdx = current.size + 1
        val numZh = when (nextIdx) {
            1 -> "一"; 2 -> "二"; 3 -> "三"; 4 -> "四"; 5 -> "五"; else -> "$nextIdx"
        }
        val mainType = _uiState.value.calculationType
        current.add(CalculationStage(type = mainType, days = 15L, remark = "第${numZh}段时间"))
        _uiState.value = _uiState.value.copy(stages = current, showResult = false)
    }

    fun removeCalculationStage(stageId: Long) {
        val current = _uiState.value.stages.filterNot { it.id == stageId }
        if (current.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(stages = current, showResult = false)
        }
    }

    fun updateStageDays(stageId: Long, days: Long) {
        val current = _uiState.value.stages.map {
            if (it.id == stageId) it.copy(days = days) else it
        }
        _uiState.value = _uiState.value.copy(stages = current, showResult = false)
    }

    fun updateStageType(stageId: Long, type: CalculationType) {
        val updatedStages = _uiState.value.stages.map { it.copy(type = type) }
        _uiState.value = _uiState.value.copy(
            calculationType = type,
            stages = updatedStages,
            showResult = false
        )
    }

    fun updateStageRemark(stageId: Long, remark: String) {
        val current = _uiState.value.stages.map {
            if (it.id == stageId) it.copy(remark = remark) else it
        }
        _uiState.value = _uiState.value.copy(stages = current)
    }

    fun togglePresetHolidayEnabled(holidayName: String) {
        val currentDisabled = _uiState.value.disabledPresetHolidays.toMutableSet()
        if (currentDisabled.contains(holidayName)) {
            currentDisabled.remove(holidayName)
        } else {
            currentDisabled.add(holidayName)
        }
        _uiState.value = _uiState.value.copy(disabledPresetHolidays = currentDisabled.toSet())
    }

    fun setToday() {
        _uiState.value = _uiState.value.copy(baseDate = LocalDate.now(), showResult = false)
    }

    fun triggerFireworks() {
        _uiState.value = _uiState.value.copy(fireworksTrigger = _uiState.value.fireworksTrigger + 1)
    }

    fun resetFireworks() {
        if (_uiState.value.fireworksTrigger != 0) {
            _uiState.value = _uiState.value.copy(fireworksTrigger = 0)
        }
    }

    fun addCustomEvent(
        name: String,
        targetDate: LocalDate,
        iconEmoji: String = "📌",
        repeatMode: EventRepeatMode = EventRepeatMode.NONE
    ) {
        val fullName = if (name.startsWith(iconEmoji)) name else "$iconEmoji $name"
        val newEvent = CustomEventItem(
            name = fullName,
            targetDate = targetDate,
            iconEmoji = iconEmoji,
            repeatMode = repeatMode
        )
        val updatedEvents = _uiState.value.customEvents + newEvent
        _uiState.value = _uiState.value.copy(customEvents = updatedEvents)
    }

    fun deleteCustomEvent(event: CustomEventItem) {
        val updatedEvents = _uiState.value.customEvents.filterNot { it.id == event.id }
        _uiState.value = _uiState.value.copy(customEvents = updatedEvents)
    }

    fun calculateRangeBreakdown(): RangeBreakdownResult {
        val state = _uiState.value
        val start = if (state.baseDate.isBefore(state.reverseEndDate)) state.baseDate else state.reverseEndDate
        val end = if (state.baseDate.isBefore(state.reverseEndDate)) state.reverseEndDate else state.baseDate

        val totalNatDays = abs(ChronoUnit.DAYS.between(start, end))
        var workdays = 0L
        var statutory = 0L
        var regularWeekends = 0L

        var curr = start.plusDays(1)
        while (!curr.isAfter(end)) {
            val isShift = state.enableChineseHolidays && !state.disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(curr, state.holidayRegion)
            val isStat = state.enableChineseHolidays && RegionalHolidays.isStatutoryHoliday(curr, state.holidayRegion)
            val isWeekend = state.weekendRule.isWeekend(curr, state.isCurrentWeekBigWeek)

            if (isShift) {
                workdays++
            } else if (isStat) {
                statutory++
            } else if (isWeekend) {
                regularWeekends++
            } else {
                workdays++
            }

            curr = curr.plusDays(1)
        }

        return RangeBreakdownResult(
            startDate = start,
            endDate = end,
            totalNaturalDays = totalNatDays,
            workdaysCount = workdays,
            statutoryHolidaysCount = statutory,
            regularWeekendDaysCount = regularWeekends
        )
    }

    fun calculateMultiStageTimeline(): Pair<LocalDate, List<StageSegmentResult>> {
        val state = _uiState.value
        var currDate = state.baseDate
        val results = mutableListOf<StageSegmentResult>()

        state.stages.forEachIndexed { index, stage ->
            val startDate = currDate
            val endDate = when (state.dateMode) {
                DateMode.WORKDAY -> {
                    when (stage.type) {
                        CalculationType.ADD -> DateCalculatorUtils.addWorkdays(
                            currDate, stage.days, state.weekendRule, state.enableChineseHolidays, state.holidayRegion, state.isCurrentWeekBigWeek, state.disableChinaShiftWorkdays
                        )
                        CalculationType.SUBTRACT -> DateCalculatorUtils.addWorkdays(
                            currDate, -stage.days, state.weekendRule, state.enableChineseHolidays, state.holidayRegion, state.isCurrentWeekBigWeek, state.disableChinaShiftWorkdays
                        )
                    }
                }
                DateMode.NATURAL_DAY -> {
                    when (stage.type) {
                        CalculationType.ADD -> DateCalculatorUtils.addNaturalDays(currDate, stage.days)
                        CalculationType.SUBTRACT -> DateCalculatorUtils.addNaturalDays(currDate, -stage.days)
                    }
                }
            }

            val totalCalDays = abs(ChronoUnit.DAYS.between(startDate, endDate))
            val restDays = if (state.dateMode == DateMode.WORKDAY) totalCalDays - stage.days else 0L

            results.add(
                StageSegmentResult(
                    stageIndex = index,
                    remark = stage.remark,
                    type = stage.type,
                    daysCount = stage.days,
                    startDate = startDate,
                    endDate = endDate,
                    totalCalendarDays = totalCalDays,
                    restDaysCount = restDays
                )
            )

            currDate = endDate
        }

        return Pair(currDate, results)
    }

    fun calculateTargetDate(overrideMode: DateMode? = null): LocalDate {
        val state = _uiState.value
        val mode = overrideMode ?: state.dateMode
        val base = state.baseDate
        val rawDays = state.daysInput.toLongOrNull() ?: 0L

        return when (mode) {
            DateMode.WORKDAY -> {
                when (state.calculationType) {
                    CalculationType.ADD -> DateCalculatorUtils.addWorkdays(
                        base, rawDays, state.weekendRule, state.enableChineseHolidays, state.holidayRegion, state.isCurrentWeekBigWeek, state.disableChinaShiftWorkdays
                    )
                    CalculationType.SUBTRACT -> DateCalculatorUtils.addWorkdays(
                        base, -rawDays, state.weekendRule, state.enableChineseHolidays, state.holidayRegion, state.isCurrentWeekBigWeek, state.disableChinaShiftWorkdays
                    )
                }
            }
            DateMode.NATURAL_DAY -> {
                when (state.calculationType) {
                    CalculationType.ADD -> DateCalculatorUtils.addNaturalDays(base, rawDays)
                    CalculationType.SUBTRACT -> DateCalculatorUtils.addNaturalDays(base, -rawDays)
                }
            }
        }
    }

    fun performCalculation(): LocalDate {
        val state = _uiState.value
        val modeLabel = if (state.dateMode == DateMode.WORKDAY) "工作日" else "自然日"

        val resultDate = if (state.isMultiStageExtensionEnabled) {
            val (finalDate, segments) = calculateMultiStageTimeline()
            val totalDays = segments.sumOf { it.daysCount }
            val customTitle = state.multiStagePlanTitle.ifBlank { "多段安排 (${DateCalculatorUtils.formatDate(finalDate)})" }
            val actionTypeLabel = if (state.calculationType == CalculationType.ADD) "多段加" else "多段减"
            val detail = "$actionTypeLabel (${segments.size} 段时间): 共推算 ${totalDays} ${modeLabel} ➔ 完成: ${DateCalculatorUtils.formatDate(finalDate)}"
            val regionTag = "${state.holidayRegion.flagEmoji} ${state.holidayRegion.nativeName}"

            saveToHistory(category = "多段加减", title = customTitle, detail = detail, regionTag = regionTag, resultDate = finalDate, resultDays = totalDays)
            finalDate
        } else if (state.calcSubMode == CalcSubMode.REVERSE_RANGE) {
            val bd = calculateRangeBreakdown()
            val title = "区间拆算: ${bd.totalNaturalDays} 自然日"
            val detail = "${bd.startDate} ➔ ${bd.endDate} | 工作日: ${bd.workdaysCount}天, 周末: ${bd.regularWeekendDaysCount}天, 节假日: ${bd.statutoryHolidaysCount}天"
            val regionTag = "${state.holidayRegion.flagEmoji} ${state.holidayRegion.nativeName}"

            saveToHistory(category = "区间拆算", title = title, detail = detail, regionTag = regionTag, resultDate = bd.endDate, resultDays = bd.totalNaturalDays)
            bd.endDate
        } else {
            val res = calculateTargetDate()
            val rawDays = state.daysInput.toLongOrNull() ?: 0L
            val title = "推算目标日: ${DateCalculatorUtils.formatDate(res)}"
            val detail = "${state.baseDate}  ➔  ${state.calculationType.symbol} ${rawDays} ${modeLabel}"
            val regionTag = "${state.holidayRegion.flagEmoji} ${state.holidayRegion.nativeName}"

            saveToHistory(category = "日期计算", title = title, detail = detail, regionTag = regionTag, resultDate = res, resultDays = rawDays)
            res
        }

        _uiState.value = _uiState.value.copy(showResult = true)
        return resultDate
    }

    fun setShowResult(show: Boolean) {
        _uiState.value = _uiState.value.copy(showResult = show)
    }

    fun updateHistoryItemTitle(itemId: Long, newTitle: String) {
        val currentHistory = _uiState.value.historyList.map {
            if (it.id == itemId) it.copy(title = newTitle) else it
        }
        _uiState.value = _uiState.value.copy(historyList = currentHistory)
    }

    fun saveToHistory(
        category: String = "日期计算",
        title: String,
        detail: String,
        regionTag: String = "🇨🇳 中国大陆",
        resultDate: LocalDate? = null,
        resultDays: Long? = null
    ) {
        val newItem = HistoryItem(
            id = System.nanoTime(),
            category = category,
            title = title,
            detail = detail,
            regionTag = regionTag,
            resultDate = resultDate,
            resultDays = resultDays,
            timestamp = System.currentTimeMillis()
        )
        val currentHistory = _uiState.value.historyList.toMutableList()
        currentHistory.add(0, newItem)
        _uiState.value = _uiState.value.copy(historyList = currentHistory.toList())
    }

    fun clearHistory() {
        _uiState.value = _uiState.value.copy(historyList = emptyList())
    }

    fun deleteHistoryItem(item: HistoryItem) {
        val currentHistory = _uiState.value.historyList.filterNot { it.id == item.id }
        _uiState.value = _uiState.value.copy(historyList = currentHistory.toList())
    }
}
