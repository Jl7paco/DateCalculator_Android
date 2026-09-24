package me.paco.datecalculator.ui.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.paco.datecalculator.data.AnniversaryItem
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.CalculationStage
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.DarkThemeMode
import me.paco.datecalculator.data.DateMode
import me.paco.datecalculator.data.HistoryItem
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.HomeConfig
import me.paco.datecalculator.data.RegionalHolidays
import me.paco.datecalculator.data.StageSegmentResult
import me.paco.datecalculator.data.ThemeColorPreset
import me.paco.datecalculator.data.WeekendRule
import me.paco.datecalculator.util.DailyWeather
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LocationUtils
import me.paco.datecalculator.util.PreferenceUtils
import me.paco.datecalculator.util.WeatherUtils
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
    val repeatMode: EventRepeatMode = EventRepeatMode.NONE,
    val isPinned: Boolean = false
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
    val pinnedPresetHolidays: Set<String> = emptySet(),
    val isMultiStageExtensionEnabled: Boolean = false,
    val multiStagePlanTitle: String = "",
    val stages: List<CalculationStage> = listOf(
        CalculationStage(type = CalculationType.ADD, daysInput = "", remark = "第一段时间"),
        CalculationStage(type = CalculationType.ADD, daysInput = "", remark = "第二段时间")
    ),
    val showResult: Boolean = false,
    val historyList: List<HistoryItem> = emptyList(),
    val customEvents: List<CustomEventItem> = emptyList(),
    val anniversaryList: List<AnniversaryItem> = emptyList(),
    val fireworksTrigger: Int = 0,

    // V3.0 新增状态
    val themePreset: ThemeColorPreset = ThemeColorPreset.SYSTEM,
    val customPrimaryColorHex: Long = 0xFF2563EB,
    val darkThemeMode: DarkThemeMode = DarkThemeMode.SYSTEM,
    val appLanguage: AppLanguage = AppLanguage.SIMPLIFIED_CHINESE,
    val homeConfig: HomeConfig = HomeConfig(),
    val selectedBirthDate: LocalDate = LocalDate.of(2000, 1, 1),
    val currentCityName: String = "北京市",
    val liveWeatherList: List<DailyWeather>? = null
)

class DateCalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DateCalculatorUiState())
    val uiState: StateFlow<DateCalculatorUiState> = _uiState.asStateFlow()

    fun initPreferences(context: Context) {
        val region = PreferenceUtils.getHolidayRegion(context)
        val weekendRule = PreferenceUtils.getWeekendRule(context)
        val isBigWeek = PreferenceUtils.getIsBigWeek(context)
        val isGpsAuto = PreferenceUtils.getIsGpsAuto(context)
        val disableShift = PreferenceUtils.getDisableChinaShift(context)
        val themePreset = PreferenceUtils.getThemePreset(context)
        val customColor = PreferenceUtils.getCustomPrimaryColor(context)
        val darkThemeMode = PreferenceUtils.getDarkThemeMode(context)
        val appLanguage = PreferenceUtils.getAppLanguage(context)
        val homeConfig = PreferenceUtils.getHomeConfig(context)
        val savedPinnedSet = PreferenceUtils.getPinnedEvents(context)
        val savedAnniversaries = PreferenceUtils.getAnniversaries(context)

        val updatedCustomEvents = if (savedPinnedSet.isNotEmpty()) {
            _uiState.value.customEvents.map { event ->
                if (savedPinnedSet.contains(event.name)) event.copy(isPinned = true) else event
            }
        } else _uiState.value.customEvents

        _uiState.value = _uiState.value.copy(
            holidayRegion = region,
            weekendRule = weekendRule,
            isCurrentWeekBigWeek = isBigWeek,
            isGpsAutoDetectEnabled = isGpsAuto,
            disableChinaShiftWorkdays = disableShift,
            themePreset = themePreset,
            customPrimaryColorHex = customColor,
            darkThemeMode = darkThemeMode,
            appLanguage = appLanguage,
            homeConfig = homeConfig,
            pinnedPresetHolidays = savedPinnedSet,
            customEvents = updatedCustomEvents,
            anniversaryList = savedAnniversaries
        )
    }

    fun loadAnniversaries(context: Context) {
        val saved = PreferenceUtils.getAnniversaries(context)
        _uiState.value = _uiState.value.copy(anniversaryList = saved)
    }

    fun addAnniversary(item: AnniversaryItem, context: Context) {
        val current = _uiState.value.anniversaryList
        if (current.size >= 999) {
            Toast.makeText(context, "重要纪念日卡片已达 999 个上限", Toast.LENGTH_SHORT).show()
            return
        }
        val updated = listOf(item) + current
        _uiState.value = _uiState.value.copy(anniversaryList = updated)
        PreferenceUtils.saveAnniversaries(context, updated)
    }

    fun deleteAnniversary(itemId: Long, context: Context) {
        val updated = _uiState.value.anniversaryList.filterNot { it.id == itemId }
        _uiState.value = _uiState.value.copy(anniversaryList = updated)
        PreferenceUtils.saveAnniversaries(context, updated)
    }

    fun togglePinAnniversary(itemId: Long, context: Context) {
        val updated = _uiState.value.anniversaryList.map {
            if (it.id == itemId) it.copy(isPinned = !it.isPinned) else it
        }
        _uiState.value = _uiState.value.copy(anniversaryList = updated)
        PreferenceUtils.saveAnniversaries(context, updated)
    }

    fun fetchCurrentGpsLocation(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val cityName = LocationUtils.getCurrentCityName(context)
            val region = LocationUtils.detectCurrentRegion(context)
            val coords = WeatherUtils.getCityCoordinates(cityName)
            val realWeather = WeatherUtils.fetchRealLiveWeather(coords.first, coords.second)
                ?: WeatherUtils.getWeatherForecast(LocalDate.now())

            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    holidayRegion = region,
                    currentCityName = cityName,
                    liveWeatherList = realWeather
                )
            }
        }
    }

    fun updateHolidayRegion(region: HolidayRegion, context: Context? = null) {
        _uiState.value = _uiState.value.copy(
            holidayRegion = region,
            showResult = false
        )
        context?.let {
            PreferenceUtils.saveHolidayRegion(it, region)
            val regionName = "${region.flagEmoji} ${region.nativeName}"
            Toast.makeText(context, "已自动同步 $regionName 最新节假日数据", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateThemePreset(preset: ThemeColorPreset, context: Context? = null) {
        _uiState.value = _uiState.value.copy(themePreset = preset)
        context?.let { PreferenceUtils.saveThemePreset(it, preset) }
    }

    fun updateDarkThemeMode(mode: DarkThemeMode, context: Context? = null) {
        _uiState.value = _uiState.value.copy(darkThemeMode = mode)
        context?.let { PreferenceUtils.saveDarkThemeMode(it, mode) }
    }

    fun updateAppLanguage(language: AppLanguage, context: Context? = null) {
        _uiState.value = _uiState.value.copy(appLanguage = language)
        context?.let { PreferenceUtils.saveAppLanguage(it, language) }
    }

    fun updateCustomPrimaryColor(colorHex: Long, context: Context? = null) {
        _uiState.value = _uiState.value.copy(
            customPrimaryColorHex = colorHex,
            themePreset = ThemeColorPreset.CUSTOM
        )
        context?.let {
            PreferenceUtils.saveCustomPrimaryColor(it, colorHex)
            PreferenceUtils.saveThemePreset(it, ThemeColorPreset.CUSTOM)
        }
    }

    fun updateHomeConfig(config: HomeConfig, context: Context? = null) {
        _uiState.value = _uiState.value.copy(homeConfig = config)
        context?.let { PreferenceUtils.saveHomeConfig(it, config) }
    }

    fun updateBirthDate(birthDate: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedBirthDate = birthDate)
    }

    fun updateBaseDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(baseDate = date, showResult = false)
    }

    fun updateEndDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(endDate = date, showResult = false)
    }

    fun updateReverseEndDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(reverseEndDate = date)
    }

    fun updateDaysInput(input: String) {
        _uiState.value = _uiState.value.copy(
            daysInput = input,
            showResult = false
        )
    }

    fun updateCalculationType(type: CalculationType) {
        _uiState.value = _uiState.value.copy(
            calculationType = type,
            showResult = false
        )
    }

    fun updateDateMode(mode: DateMode) {
        _uiState.value = _uiState.value.copy(
            dateMode = mode,
            showResult = false
        )
    }

    fun updateCalcSubMode(subMode: CalcSubMode) {
        _uiState.value = _uiState.value.copy(
            calcSubMode = subMode,
            showResult = false
        )
    }

    fun updateWeekendRule(rule: WeekendRule, context: Context? = null) {
        _uiState.value = _uiState.value.copy(
            weekendRule = rule,
            showResult = false
        )
        context?.let { PreferenceUtils.saveWeekendRule(it, rule) }
    }

    fun updateIsBigWeek(isBigWeek: Boolean, context: Context? = null) {
        _uiState.value = _uiState.value.copy(
            isCurrentWeekBigWeek = isBigWeek,
            showResult = false
        )
        context?.let { PreferenceUtils.saveIsBigWeek(it, isBigWeek) }
    }

    fun updateCurrentWeekBigWeek(isBigWeek: Boolean, context: Context? = null) {
        updateIsBigWeek(isBigWeek, context)
    }

    fun updateGpsAutoDetect(enabled: Boolean, context: Context? = null) {
        _uiState.value = _uiState.value.copy(isGpsAutoDetectEnabled = enabled)
        context?.let { PreferenceUtils.saveIsGpsAuto(it, enabled) }
    }

    fun updateDisableChinaShift(disable: Boolean, context: Context? = null) {
        _uiState.value = _uiState.value.copy(disableChinaShiftWorkdays = disable, showResult = false)
        context?.let { PreferenceUtils.saveDisableChinaShift(it, disable) }
    }

    fun togglePresetHolidayEnabled(holidayName: String) {
        val currentDisabled = _uiState.value.disabledPresetHolidays
        val newDisabled = if (currentDisabled.contains(holidayName)) {
            currentDisabled - holidayName
        } else {
            currentDisabled + holidayName
        }
        _uiState.value = _uiState.value.copy(disabledPresetHolidays = newDisabled)
    }

    fun togglePinPresetHoliday(presetName: String, context: Context? = null) {
        val currentSet = _uiState.value.pinnedPresetHolidays
        val newSet = if (currentSet.contains(presetName)) {
            currentSet - presetName
        } else {
            currentSet + presetName
        }
        _uiState.value = _uiState.value.copy(pinnedPresetHolidays = newSet)

        context?.let { ctx ->
            val customPinnedNames = _uiState.value.customEvents.filter { it.isPinned }.map { it.name }.toSet()
            PreferenceUtils.savePinnedEvents(ctx, newSet + customPinnedNames)
        }
    }

    fun toggleMultiStageExtension(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isMultiStageExtensionEnabled = enabled)
    }

    fun updateMultiStagePlanTitle(title: String) {
        _uiState.value = _uiState.value.copy(multiStagePlanTitle = title)
    }

    fun addCalculationStage() {
        val currentStages = _uiState.value.stages
        val numZh = when (currentStages.size + 1) {
            1 -> "一"; 2 -> "二"; 3 -> "三"; 4 -> "四"; 5 -> "五"; else -> "${currentStages.size + 1}"
        }
        val defaultRemark = "第${numZh}段时间"
        val lastType = currentStages.lastOrNull()?.type ?: _uiState.value.calculationType
        val newStage = CalculationStage(
            type = lastType,
            daysInput = "",
            remark = defaultRemark
        )
        _uiState.value = _uiState.value.copy(stages = currentStages + newStage)
    }

    fun duplicateCalculationStage(stageId: Long) {
        val currentStages = _uiState.value.stages
        val index = currentStages.indexOfFirst { it.id == stageId }
        if (index != -1) {
            val target = currentStages[index]
            val copyStage = CalculationStage(
                type = target.type,
                daysInput = target.daysInput,
                remark = "${target.remark} (副本)"
            )
            val mutableList = currentStages.toMutableList()
            mutableList.add(index + 1, copyStage)
            _uiState.value = _uiState.value.copy(stages = mutableList)
        }
    }

    fun removeCalculationStage(stageId: Long) {
        val currentStages = _uiState.value.stages
        if (currentStages.size > 1) {
            _uiState.value = _uiState.value.copy(stages = currentStages.filterNot { it.id == stageId })
        }
    }

    fun updateStageDaysInput(stageId: Long, input: String) {
        val updatedStages = _uiState.value.stages.map {
            if (it.id == stageId) it.copy(daysInput = input) else it
        }
        _uiState.value = _uiState.value.copy(stages = updatedStages)
    }

    fun updateStageType(stageId: Long, type: CalculationType) {
        val updatedStages = _uiState.value.stages.map {
            if (it.id == stageId) it.copy(type = type) else it
        }
        _uiState.value = _uiState.value.copy(stages = updatedStages)
    }

    fun updateStageRemark(stageId: Long, remark: String) {
        val updatedStages = _uiState.value.stages.map {
            if (it.id == stageId) it.copy(remark = remark) else it
        }
        _uiState.value = _uiState.value.copy(stages = updatedStages)
    }

    fun reorderCalculationStages(fromIndex: Int, toIndex: Int) {
        val currentStages = _uiState.value.stages.toMutableList()
        if (fromIndex in currentStages.indices && toIndex in currentStages.indices) {
            val item = currentStages.removeAt(fromIndex)
            currentStages.add(toIndex, item)
            _uiState.value = _uiState.value.copy(stages = currentStages)
        }
    }

    fun performCalculation() {
        calculateResult()
    }

    fun calculateResult() {
        _uiState.value = _uiState.value.copy(showResult = true)
        val state = _uiState.value
        val resultDate = calculateTargetDate()
        val formattedDate = DateCalculatorUtils.formatDate(resultDate)

        val isWorkdayMode = (state.dateMode == DateMode.WORKDAY)
        val modeText = if (isWorkdayMode) "工作日" else "自然日"
        val opText = if (state.calculationType == CalculationType.ADD) "加" else "减"
        val daysText = "${state.daysInput}$modeText"

        val title = "$modeText 计算结果: $formattedDate"
        val detail = "起始日期: ${state.baseDate} $opText $daysText ➔ 目标日期: $formattedDate"
        val regionTag = "${state.holidayRegion.flagEmoji} ${state.holidayRegion.nativeName}"

        saveToHistory(
            category = "日期计算",
            title = title,
            detail = detail,
            regionTag = regionTag,
            resultDate = resultDate
        )
    }

    fun calculateTargetDate(): LocalDate {
        val state = _uiState.value
        val days = state.daysInput.toIntOrNull() ?: 15
        return DateCalculatorUtils.calculateTargetDate(
            baseDate = state.baseDate,
            days = days,
            type = state.calculationType,
            isWorkdayMode = (state.dateMode == DateMode.WORKDAY),
            weekendRule = state.weekendRule,
            enableHolidays = state.enableChineseHolidays,
            holidayRegion = state.holidayRegion,
            isCurrentWeekBigWeek = state.isCurrentWeekBigWeek,
            disableChinaShiftWorkdays = state.disableChinaShiftWorkdays
        )
    }

    fun calculateMultiStageTimeline(): Pair<LocalDate, List<StageSegmentResult>> {
        val results = computeMultiStageSequence()
        val finalDate = results.lastOrNull()?.endDate ?: _uiState.value.baseDate
        return Pair(finalDate, results)
    }

    fun computeMultiStageSequence(): List<StageSegmentResult> {
        val state = _uiState.value
        val isWorkdayMode = (state.dateMode == DateMode.WORKDAY)
        var currentBase = state.baseDate
        val results = mutableListOf<StageSegmentResult>()

        state.stages.forEachIndexed { index, stage ->
            val numZh = when (index + 1) {
                1 -> "一"; 2 -> "二"; 3 -> "三"; 4 -> "四"; 5 -> "五"; else -> "${index + 1}"
            }
            val defaultRemark = "第${numZh}段时间"
            val effectiveRemark = stage.remark.ifBlank { defaultRemark }

            val days = stage.days.toInt()
            val segmentEndDate = DateCalculatorUtils.calculateTargetDate(
                baseDate = currentBase,
                days = days,
                type = stage.type,
                isWorkdayMode = isWorkdayMode,
                weekendRule = state.weekendRule,
                enableHolidays = state.enableChineseHolidays,
                holidayRegion = state.holidayRegion,
                isCurrentWeekBigWeek = state.isCurrentWeekBigWeek,
                disableChinaShiftWorkdays = state.disableChinaShiftWorkdays
            )

            val totalCalendarDays = abs(ChronoUnit.DAYS.between(currentBase, segmentEndDate))
            val restDaysCount = if (isWorkdayMode) totalCalendarDays - days else 0L

            results.add(
                StageSegmentResult(
                    stageIndex = index + 1,
                    remark = effectiveRemark,
                    type = stage.type,
                    daysCount = days.toLong(),
                    startDate = currentBase,
                    endDate = segmentEndDate,
                    totalCalendarDays = totalCalendarDays,
                    restDaysCount = restDaysCount
                )
            )

            currentBase = segmentEndDate
        }

        return results
    }

    fun saveToHistory(
        category: String,
        title: String,
        detail: String,
        regionTag: String = "${_uiState.value.holidayRegion.flagEmoji} ${_uiState.value.holidayRegion.nativeName}",
        resultDate: LocalDate? = null,
        resultDays: Long? = null
    ) {
        val newItem = HistoryItem(
            category = category,
            title = title,
            detail = detail,
            regionTag = regionTag,
            resultDate = resultDate,
            resultDays = resultDays
        )

        val updatedList = listOf(newItem) + _uiState.value.historyList.filterNot {
            it.title == title && it.detail == detail
        }

        _uiState.value = _uiState.value.copy(historyList = updatedList)
    }

    fun updateHistoryItemTitle(itemId: Long, newTitle: String) {
        renameHistoryItem(itemId, newTitle)
    }

    fun renameHistoryItem(itemId: Long, newTitle: String) {
        val updatedList = _uiState.value.historyList.map {
            if (it.id == itemId) it.copy(title = newTitle) else it
        }
        _uiState.value = _uiState.value.copy(historyList = updatedList)
    }

    fun deleteHistoryItem(itemId: Long) {
        removeHistoryItem(itemId)
    }

    fun deleteHistoryItem(item: HistoryItem) {
        removeHistoryItem(item.id)
    }

    fun removeHistoryItem(itemId: Long) {
        val updatedList = _uiState.value.historyList.filterNot { it.id == itemId }
        _uiState.value = _uiState.value.copy(historyList = updatedList)
    }

    fun clearHistory() {
        _uiState.value = _uiState.value.copy(historyList = emptyList())
    }

    fun triggerFireworks() {
        _uiState.value = _uiState.value.copy(fireworksTrigger = _uiState.value.fireworksTrigger + 1)
    }

    fun resetFireworks() {
        _uiState.value = _uiState.value.copy(fireworksTrigger = 0)
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

    fun togglePinCustomEvent(event: CustomEventItem, context: Context? = null) {
        val updatedEvents = _uiState.value.customEvents.map {
            if (it.id == event.id || it.name == event.name) it.copy(isPinned = !it.isPinned) else it
        }
        _uiState.value = _uiState.value.copy(customEvents = updatedEvents)

        context?.let { ctx ->
            val customPinnedNames = updatedEvents.filter { it.isPinned }.map { it.name }.toSet()
            PreferenceUtils.savePinnedEvents(ctx, _uiState.value.pinnedPresetHolidays + customPinnedNames)
        }
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
}
