package me.paco.datecalculator.ui.viewmodel

import android.content.Context
import android.util.Log
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
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LanguageUtils
import me.paco.datecalculator.util.LocationUtils
import me.paco.datecalculator.util.PreferenceUtils
import me.paco.datecalculator.util.RealTimeWeatherData
import me.paco.datecalculator.util.TemperatureUnit
import me.paco.datecalculator.util.WeatherUtils
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

enum class EventRepeatMode(val label: String) {
    NONE("不重复"),
    WEEKLY("每周"),
    MONTHLY("每月"),
    YEARLY("每年");

    fun getLocalizedName(language: AppLanguage): String {
        return when (language.getEffectiveLanguage()) {
            AppLanguage.SIMPLIFIED_CHINESE -> label
            AppLanguage.TRADITIONAL_CHINESE -> when (this) {
                NONE -> "不重複"; WEEKLY -> "每周"; MONTHLY -> "每月"; YEARLY -> "每年"
            }
            AppLanguage.ENGLISH -> when (this) {
                NONE -> "None"; WEEKLY -> "Weekly"; MONTHLY -> "Monthly"; YEARLY -> "Yearly"
            }
            AppLanguage.JAPANESE -> when (this) {
                NONE -> "なし"; WEEKLY -> "毎週"; MONTHLY -> "毎月"; YEARLY -> "毎年"
            }
            AppLanguage.KOREAN -> when (this) {
                NONE -> "없음"; WEEKLY -> "매주"; MONTHLY -> "매월"; YEARLY -> "매년"
            }
            else -> when (this) {
                NONE -> "None"; WEEKLY -> "Weekly"; MONTHLY -> "Monthly"; YEARLY -> "Yearly"
            }
        }
    }
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
    val daysInput: String = "",
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
    val isAgeModeSelected: Boolean = false,
    val includeStartDate: Boolean = false,
    val multiStagePlanTitle: String = "",
    val stages: List<CalculationStage> = listOf(
        CalculationStage(type = CalculationType.ADD, daysInput = "", remark = ""),
        CalculationStage(type = CalculationType.ADD, daysInput = "", remark = "")
    ),
    val showResult: Boolean = false,
    val historyList: List<HistoryItem> = emptyList(),
    val customEvents: List<CustomEventItem> = emptyList(),
    val anniversaryList: List<AnniversaryItem> = emptyList(),
    val fireworksTrigger: Int = 0,

    val isHapticFeedbackEnabled: Boolean = true,
    val themePreset: ThemeColorPreset = ThemeColorPreset.SYSTEM,
    val customPrimaryColorHex: Long = 0xFF2563EB,
    val darkThemeMode: DarkThemeMode = DarkThemeMode.SYSTEM,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val homeConfig: HomeConfig = HomeConfig(),
    val selectedBirthDate: LocalDate = LocalDate.of(2000, 1, 1),
    val currentCityName: String = "深圳市 · 南山区",
    val liveWeatherData: RealTimeWeatherData? = null,
    val showWatermarkCamera: Boolean = false
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
        val isHaptic = PreferenceUtils.isHapticFeedbackEnabled(context)
        val themePreset = PreferenceUtils.getThemePreset(context)
        val customColor = PreferenceUtils.getCustomPrimaryColorHex(context)
        val darkThemeMode = PreferenceUtils.getDarkThemeMode(context)
        val appLanguage = PreferenceUtils.getAppLanguage(context)
        val tempUnit = PreferenceUtils.getTemperatureUnit(context)
        val homeConfig = PreferenceUtils.getHomeConfig(context)
        val savedPinnedSet = PreferenceUtils.getPinnedPresetHolidays(context)
        val savedAnniversaries = PreferenceUtils.getAnniversaries(context)
        val savedCustomEvents = PreferenceUtils.getCustomEvents(context)

        val updatedCustomEvents = if (savedCustomEvents.isNotEmpty()) {
            savedCustomEvents.map { event ->
                if (savedPinnedSet.contains(event.name)) event.copy(isPinned = true) else event
            }
        } else _uiState.value.customEvents

        val savedCityName = PreferenceUtils.getLastCityName(context)

        // 每隔 7 天后台静默自动拉取同步全量 19 地区最新假期数据
        PreferenceUtils.syncAllRegionsHolidayData(context, forceManual = false)

        _uiState.value = _uiState.value.copy(
            holidayRegion = region,
            weekendRule = weekendRule,
            isCurrentWeekBigWeek = isBigWeek,
            isGpsAutoDetectEnabled = isGpsAuto,
            disableChinaShiftWorkdays = disableShift,
            isHapticFeedbackEnabled = isHaptic,
            themePreset = themePreset,
            customPrimaryColorHex = customColor,
            darkThemeMode = darkThemeMode,
            appLanguage = appLanguage,
            temperatureUnit = tempUnit,
            homeConfig = homeConfig,
            currentCityName = savedCityName,
            pinnedPresetHolidays = savedPinnedSet,
            customEvents = updatedCustomEvents,
            anniversaryList = savedAnniversaries
        )
    }

    fun loadAnniversaries(context: Context) {
        val saved = PreferenceUtils.getAnniversaries(context)
        _uiState.value = _uiState.value.copy(anniversaryList = saved)
    }

    fun syncAllRegionsHolidays(context: Context, forceManual: Boolean = true): Boolean {
        return PreferenceUtils.syncAllRegionsHolidayData(context, forceManual)
    }

    fun addAnniversary(item: AnniversaryItem, context: Context) {
        val current = _uiState.value.anniversaryList
        if (current.size >= 999) {
            Toast.makeText(context, "上限 999", Toast.LENGTH_SHORT).show()
            return
        }
        val updated = listOf(item) + current
        _uiState.value = _uiState.value.copy(anniversaryList = updated)
        PreferenceUtils.saveAnniversaries(context, updated)
    }

    fun deleteAnniversary(itemId: Long, context: Context) {
        val targetItem = _uiState.value.anniversaryList.find { it.id == itemId }
        val updatedAnniversaries = _uiState.value.anniversaryList.filterNot { it.id == itemId }
        _uiState.value = _uiState.value.copy(anniversaryList = updatedAnniversaries)
        PreferenceUtils.saveAnniversaries(context, updatedAnniversaries)

        if (targetItem != null) {
            val rawTitle = targetItem.title.trim()
            val emojiTitle = "${targetItem.iconEmoji} $rawTitle".trim()

            val updatedCustomEvents = _uiState.value.customEvents.filterNot { event ->
                val eventClean = event.name.trim()
                eventClean == rawTitle || eventClean == emojiTitle ||
                eventClean.contains(rawTitle) || (rawTitle.isNotEmpty() && eventClean.endsWith(rawTitle))
            }

            val updatedPinnedSet = _uiState.value.pinnedPresetHolidays.filterNot { label ->
                val labelClean = label.trim()
                labelClean == rawTitle || labelClean == emojiTitle ||
                labelClean.contains(rawTitle) || (rawTitle.isNotEmpty() && labelClean.endsWith(rawTitle))
            }.toSet()

            _uiState.value = _uiState.value.copy(
                customEvents = updatedCustomEvents,
                pinnedPresetHolidays = updatedPinnedSet
            )

            PreferenceUtils.saveCustomEvents(context, updatedCustomEvents)
            PreferenceUtils.savePinnedPresetHolidays(context, updatedPinnedSet)
        }
    }

    fun setWatermarkCameraVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showWatermarkCamera = visible)
    }

    fun togglePinAnniversary(itemId: Long, context: Context) {
        val updated = _uiState.value.anniversaryList.map {
            if (it.id == itemId) it.copy(isPinned = !it.isPinned) else it
        }
        _uiState.value = _uiState.value.copy(anniversaryList = updated)
        PreferenceUtils.saveAnniversaries(context, updated)
    }

    private var lastLocationFetchTime = 0L
    private var isFetchingLocation = false

    fun fetchCurrentGpsLocation(context: Context, force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && isFetchingLocation) {
            Log.d("PerfLog", "Skipping fetchCurrentGpsLocation - already in flight.")
            return
        }
        if (!force && lastLocationFetchTime > 0L && (now - lastLocationFetchTime < 10 * 60 * 1000L)) {
            Log.d("PerfLog", "Skipping fetchCurrentGpsLocation - fetched recently (${(now - lastLocationFetchTime) / 1000}s ago).")
            return
        }

        isFetchingLocation = true
        lastLocationFetchTime = now
        val startMs = System.currentTimeMillis()
        Log.d("PerfLog", "[THREAD: ${Thread.currentThread().name}] Starting fetchCurrentGpsLocation...")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val netStartMs = System.currentTimeMillis()
                val districtLoc = LocationUtils.getCurrentLocationWithDistrict(context)
                val region = LocationUtils.detectCurrentRegion(context)
                val realWeather = WeatherUtils.fetchRealLiveWeather(districtLoc.latitude, districtLoc.longitude, region)
                Log.d("PerfLog", "[THREAD: ${Thread.currentThread().name}] Weather API fetch took ${System.currentTimeMillis() - netStartMs}ms")

                PreferenceUtils.saveLastCityName(context, districtLoc.fullDisplayName)

                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        holidayRegion = region,
                        currentCityName = districtLoc.fullDisplayName,
                        liveWeatherData = realWeather ?: _uiState.value.liveWeatherData ?: WeatherUtils.getWeatherForecast(LocalDate.now())
                    )
                    Log.d("PerfLog", "[THREAD: ${Thread.currentThread().name}] Main UI state updated for weather in ${System.currentTimeMillis() - startMs}ms total")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isFetchingLocation = false
            }
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
        _uiState.value = _uiState.value.copy(calculationType = type, showResult = false)
    }

    fun updateDateMode(mode: DateMode) {
        _uiState.value = _uiState.value.copy(dateMode = mode, showResult = false)
    }

    fun updateCalcSubMode(subMode: CalcSubMode) {
        _uiState.value = _uiState.value.copy(calcSubMode = subMode, showResult = false)
    }

    fun setAgeModeSelected(selected: Boolean) {
        _uiState.value = _uiState.value.copy(isAgeModeSelected = selected)
    }

    fun updateIncludeStartDate(include: Boolean) {
        _uiState.value = _uiState.value.copy(includeStartDate = include, showResult = false)
        performCalculation()
    }

    fun updateWeekendRule(rule: WeekendRule, context: Context? = null) {
        _uiState.value = _uiState.value.copy(weekendRule = rule, showResult = false)
        context?.let { PreferenceUtils.saveWeekendRule(it, rule) }
    }

    fun updateIsBigWeek(isBigWeek: Boolean, context: Context? = null) {
        _uiState.value = _uiState.value.copy(isCurrentWeekBigWeek = isBigWeek, showResult = false)
        context?.let { PreferenceUtils.saveIsBigWeek(it, isBigWeek) }
    }

    fun updateHolidayRegion(region: HolidayRegion, context: Context? = null) {
        _uiState.value = _uiState.value.copy(holidayRegion = region, showResult = false)
        context?.let { PreferenceUtils.saveHolidayRegion(it, region) }
    }

    fun updateAppLanguage(language: AppLanguage, context: Context? = null) {
        _uiState.value = _uiState.value.copy(appLanguage = language)
        context?.let { PreferenceUtils.saveAppLanguage(it, language) }
    }

    fun updateDarkThemeMode(mode: DarkThemeMode, context: Context? = null) {
        _uiState.value = _uiState.value.copy(darkThemeMode = mode)
        context?.let { PreferenceUtils.saveDarkThemeMode(it, mode) }
    }

    fun updateThemePreset(preset: ThemeColorPreset, context: Context? = null) {
        _uiState.value = _uiState.value.copy(themePreset = preset)
        context?.let { PreferenceUtils.saveThemePreset(it, preset) }
    }

    fun updateCustomPrimaryColor(colorHex: Long, context: Context? = null) {
        _uiState.value = _uiState.value.copy(customPrimaryColorHex = colorHex)
        context?.let { PreferenceUtils.saveCustomPrimaryColorHex(it, colorHex) }
    }

    fun updateTemperatureUnit(unit: TemperatureUnit, context: Context? = null) {
        _uiState.value = _uiState.value.copy(temperatureUnit = unit)
        context?.let { PreferenceUtils.saveTemperatureUnit(it, unit) }
    }

    fun updateHomeConfig(config: HomeConfig, context: Context? = null) {
        _uiState.value = _uiState.value.copy(homeConfig = config)
        context?.let { PreferenceUtils.saveHomeConfig(it, config) }
    }

    fun updateGpsAutoDetect(enabled: Boolean, context: Context? = null) {
        _uiState.value = _uiState.value.copy(isGpsAutoDetectEnabled = enabled)
        context?.let { PreferenceUtils.saveIsGpsAuto(it, enabled) }
    }

    fun updateDisableChinaShift(disable: Boolean, context: Context? = null) {
        _uiState.value = _uiState.value.copy(disableChinaShiftWorkdays = disable, showResult = false)
        context?.let { PreferenceUtils.saveDisableChinaShift(it, disable) }
    }

    fun updateHapticFeedbackEnabled(enabled: Boolean, context: Context? = null) {
        _uiState.value = _uiState.value.copy(isHapticFeedbackEnabled = enabled)
        context?.let { PreferenceUtils.saveIsHapticFeedbackEnabled(it, enabled) }
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
            PreferenceUtils.savePinnedPresetHolidays(ctx, newSet + customPinnedNames)
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
        val defaultRemark = "阶段 ${currentStages.size + 1}"
        val lastType = currentStages.lastOrNull()?.type ?: _uiState.value.calculationType
        val newStage = CalculationStage(
            type = lastType,
            daysInput = "",
            remark = defaultRemark
        )
        _uiState.value = _uiState.value.copy(stages = currentStages + newStage)
    }

    fun removeCalculationStage(stageId: Long) {
        val currentStages = _uiState.value.stages
        if (currentStages.size <= 1) return
        _uiState.value = _uiState.value.copy(stages = currentStages.filterNot { it.id == stageId })
    }

    fun updateStageDaysInput(stageId: Long, daysInput: String) {
        val filtered = daysInput.filter { it.isDigit() }.take(5)
        val updated = _uiState.value.stages.map {
            if (it.id == stageId) it.copy(daysInput = filtered) else it
        }
        _uiState.value = _uiState.value.copy(stages = updated)
    }

    fun updateStageTargetDeadlineDate(stageId: Long, targetDeadlineDate: LocalDate) {
        val state = _uiState.value
        val (_, multiSegments) = calculateMultiStageTimeline()
        val index = state.stages.indexOfFirst { it.id == stageId }
        if (index == -1) return

        val prevEndDate = if (index == 0) {
            state.baseDate
        } else {
            multiSegments.getOrNull(index - 1)?.endDate ?: state.baseDate
        }

        val requiredDays = DateCalculatorUtils.calculateDaysBetweenDates(
            startDate = prevEndDate,
            targetDate = targetDeadlineDate,
            isWorkdayMode = (state.dateMode == DateMode.WORKDAY),
            weekendRule = state.weekendRule,
            enableHolidays = state.enableChineseHolidays,
            holidayRegion = state.holidayRegion,
            isCurrentWeekBigWeek = state.isCurrentWeekBigWeek,
            disableChinaShiftWorkdays = state.disableChinaShiftWorkdays
        )

        val newType = if (targetDeadlineDate.isBefore(prevEndDate)) CalculationType.SUBTRACT else CalculationType.ADD

        val updated = state.stages.map {
            if (it.id == stageId) it.copy(daysInput = requiredDays.toString(), type = newType) else it
        }
        _uiState.value = state.copy(stages = updated)
    }

    fun updateStageType(stageId: Long, type: CalculationType) {
        val updated = _uiState.value.stages.map {
            if (it.id == stageId) it.copy(type = type) else it
        }
        _uiState.value = _uiState.value.copy(stages = updated)
    }

    fun updateStageRemark(stageId: Long, remark: String) {
        val updated = _uiState.value.stages.map {
            if (it.id == stageId) it.copy(remark = remark) else it
        }
        _uiState.value = _uiState.value.copy(stages = updated)
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
        _uiState.value = _uiState.value.copy(showResult = true, fireworksTrigger = _uiState.value.fireworksTrigger + 1)
    }

    fun calculateTargetDate(): LocalDate {
        val state = _uiState.value
        val rawDays = state.daysInput.toIntOrNull() ?: 7
        return DateCalculatorUtils.calculateTargetDate(
            baseDate = state.baseDate,
            days = rawDays,
            type = state.calculationType,
            isWorkdayMode = (state.dateMode == DateMode.WORKDAY),
            weekendRule = state.weekendRule,
            enableHolidays = state.enableChineseHolidays,
            holidayRegion = state.holidayRegion,
            isCurrentWeekBigWeek = state.isCurrentWeekBigWeek,
            disableChinaShiftWorkdays = state.disableChinaShiftWorkdays,
            includeStartDate = state.includeStartDate
        )
    }

    fun calculateMultiStageTimeline(): Pair<LocalDate, List<StageSegmentResult>> {
        val startMs = System.currentTimeMillis()
        val state = _uiState.value
        val result = DateCalculatorUtils.computeMultiStageSequence(
            baseDate = state.baseDate,
            stages = state.stages,
            isWorkdayMode = (state.dateMode == DateMode.WORKDAY),
            weekendRule = state.weekendRule,
            enableHolidays = state.enableChineseHolidays,
            holidayRegion = state.holidayRegion,
            isCurrentWeekBigWeek = state.isCurrentWeekBigWeek,
            disableChinaShiftWorkdays = state.disableChinaShiftWorkdays,
            includeStartDate = state.includeStartDate
        )
        val elapsed = System.currentTimeMillis() - startMs
        if (elapsed > 2) {
            Log.d("PerfLog", "[THREAD: ${Thread.currentThread().name}] calculateMultiStageTimeline took ${elapsed}ms for ${state.stages.size} stages")
        }
        return result
    }

    fun calculateRangeBreakdown(): RangeBreakdownResult {
        val state = _uiState.value
        val (work, stat, week) = DateCalculatorUtils.calculateBreakdown(
            startDate = state.baseDate,
            endDate = state.reverseEndDate,
            weekendRule = state.weekendRule,
            enableHolidays = state.enableChineseHolidays,
            holidayRegion = state.holidayRegion,
            isCurrentWeekBigWeek = state.isCurrentWeekBigWeek,
            disableChinaShiftWorkdays = state.disableChinaShiftWorkdays,
            includeStartDate = state.includeStartDate
        )
        val nat = DateCalculatorUtils.naturalDaysBetween(state.baseDate, state.reverseEndDate)
        val totalNat = if (state.includeStartDate) abs(nat) + 1 else abs(nat)
        return RangeBreakdownResult(
            startDate = state.baseDate,
            endDate = state.reverseEndDate,
            totalNaturalDays = totalNat,
            workdaysCount = work,
            statutoryHolidaysCount = stat,
            regularWeekendDaysCount = week
        )
    }

    fun addCustomEvent(
        name: String,
        targetDate: LocalDate,
        iconEmoji: String = "📌",
        repeatMode: EventRepeatMode = EventRepeatMode.NONE,
        isPinned: Boolean = false,
        context: Context? = null
    ) {
        val currentEvents = _uiState.value.customEvents
        val newItem = CustomEventItem(
            name = name,
            targetDate = targetDate,
            iconEmoji = iconEmoji,
            repeatMode = repeatMode,
            isPinned = isPinned
        )
        val updatedEvents = listOf(newItem) + currentEvents
        _uiState.value = _uiState.value.copy(customEvents = updatedEvents)

        context?.let { PreferenceUtils.saveCustomEvents(it, updatedEvents) }
    }

    fun deleteCustomEvent(item: CustomEventItem, context: Context? = null) {
        val updatedEvents = _uiState.value.customEvents.filterNot { it.id == item.id }
        _uiState.value = _uiState.value.copy(customEvents = updatedEvents)

        context?.let { PreferenceUtils.saveCustomEvents(it, updatedEvents) }
    }

    fun togglePinCustomEvent(item: CustomEventItem, context: Context? = null) {
        val updatedEvents = _uiState.value.customEvents.map {
            if (it.id == item.id) it.copy(isPinned = !it.isPinned) else it
        }
        _uiState.value = _uiState.value.copy(customEvents = updatedEvents)

        context?.let { ctx ->
            PreferenceUtils.saveCustomEvents(ctx, updatedEvents)
            val customPinnedNames = updatedEvents.filter { it.isPinned }.map { it.name }.toSet()
            val presetPinned = _uiState.value.pinnedPresetHolidays
            PreferenceUtils.savePinnedPresetHolidays(ctx, presetPinned + customPinnedNames)
        }
    }

    fun updateSelectedBirthDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedBirthDate = date)
    }

    fun updateBirthDate(date: LocalDate) {
        updateSelectedBirthDate(date)
    }

    fun saveToHistory(category: String, title: String, detail: String) {
        val currentHistory = _uiState.value.historyList.toMutableList()
        currentHistory.add(0, HistoryItem(category = category, title = title, detail = detail))
        if (currentHistory.size > 100) {
            _uiState.value = _uiState.value.copy(historyList = currentHistory.take(100))
        } else {
            _uiState.value = _uiState.value.copy(historyList = currentHistory)
        }
    }

    fun renameHistoryItem(id: Long, newTitle: String) {
        val updated = _uiState.value.historyList.map {
            if (it.id == id) it.copy(title = newTitle) else it
        }
        _uiState.value = _uiState.value.copy(historyList = updated)
    }

    fun removeHistoryItem(id: Long) {
        deleteHistoryItem(id)
    }

    fun resetFireworks() {
        _uiState.value = _uiState.value.copy(fireworksTrigger = 0)
    }

    fun clearHistory() {
        _uiState.value = _uiState.value.copy(historyList = emptyList())
    }

    fun deleteHistoryItem(id: Long) {
        val updated = _uiState.value.historyList.filterNot { it.id == id }
        _uiState.value = _uiState.value.copy(historyList = updated)
    }
}
