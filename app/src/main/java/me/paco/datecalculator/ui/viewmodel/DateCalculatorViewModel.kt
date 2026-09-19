package me.paco.datecalculator.ui.viewmodel

import androidx.lifecycle.ViewModel
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.DateMode
import me.paco.datecalculator.data.HistoryItem
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.WeekendRule
import me.paco.datecalculator.util.DateCalculatorUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

enum class EventRepeatMode(val label: String) {
    NONE("不重复"),
    WEEKLY("每周"),
    MONTHLY("每月"),
    YEARLY("每年")
}

data class CustomEventItem(
    val id: Long = System.nanoTime(),
    val iconEmoji: String = "📌",
    val name: String,
    val targetDate: LocalDate,
    val repeatMode: EventRepeatMode = EventRepeatMode.NONE
) {
    /**
     * 根据当前基准日期自动滚动推算下一个周期的目标日期
     */
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
    val daysInput: String = "",
    val calculationType: CalculationType = CalculationType.ADD,
    val dateMode: DateMode = DateMode.WORKDAY,
    val weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
    val isCurrentWeekBigWeek: Boolean = true,
    val enableChineseHolidays: Boolean = true,
    val holidayRegion: HolidayRegion = HolidayRegion.CHINA,
    val showResult: Boolean = false,
    val historyList: List<HistoryItem> = emptyList(),
    val customEvents: List<CustomEventItem> = emptyList(),
    val fireworksTrigger: Int = 0
)

class DateCalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DateCalculatorUiState())
    val uiState: StateFlow<DateCalculatorUiState> = _uiState.asStateFlow()

    fun updateBaseDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(baseDate = date, showResult = false)
    }

    fun updateEndDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(endDate = date, showResult = false)
    }

    fun updateDaysInput(input: String) {
        _uiState.value = _uiState.value.copy(daysInput = input, showResult = false)
    }

    fun updateCalculationType(type: CalculationType) {
        _uiState.value = _uiState.value.copy(calculationType = type, showResult = false)
    }

    fun updateDateMode(mode: DateMode) {
        _uiState.value = _uiState.value.copy(dateMode = mode, showResult = false)
    }

    fun updateWeekendRule(rule: WeekendRule) {
        _uiState.value = _uiState.value.copy(weekendRule = rule, showResult = false)
    }

    fun updateCurrentWeekBigWeek(isBigWeek: Boolean) {
        _uiState.value = _uiState.value.copy(isCurrentWeekBigWeek = isBigWeek, showResult = false)
    }

    fun updateHolidayRegion(region: HolidayRegion) {
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

    /**
     * 按等于号按键：显示结果并自动存入历史记录
     */
    fun performCalculation(): LocalDate {
        val result = calculateTargetDate()
        val state = _uiState.value
        val rawDays = state.daysInput.toLongOrNull() ?: 0L

        val modeLabel = if (state.dateMode == DateMode.WORKDAY) "工作日" else "自然日"
        val title = "${modeLabel}计算: $result"
        val detail = "以 ${state.baseDate} 为基准，${state.calculationType.label} $rawDays 个$modeLabel [${state.holidayRegion.label}]"

        saveToHistory(title, detail, result, rawDays)
        _uiState.value = _uiState.value.copy(showResult = true)
        return result
    }

    fun setShowResult(show: Boolean) {
        _uiState.value = _uiState.value.copy(showResult = show)
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
                        base, rawDays, state.weekendRule, state.enableChineseHolidays, state.holidayRegion, state.isCurrentWeekBigWeek
                    )
                    CalculationType.SUBTRACT -> DateCalculatorUtils.addWorkdays(
                        base, -rawDays, state.weekendRule, state.enableChineseHolidays, state.holidayRegion, state.isCurrentWeekBigWeek
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

    fun saveToHistory(title: String, detail: String, resultDate: LocalDate?, resultDays: Long? = null) {
        val newItem = HistoryItem(
            id = System.nanoTime(),
            title = title,
            detail = detail,
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
