package me.paco.datecalculator.util

import android.content.Context
import android.content.SharedPreferences
import me.paco.datecalculator.data.AnniversaryItem
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.BackgroundMode
import me.paco.datecalculator.data.DarkThemeMode
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.HomeConfig
import me.paco.datecalculator.data.ThemeColorPreset
import me.paco.datecalculator.data.WeekendRule
import me.paco.datecalculator.ui.viewmodel.CustomEventItem
import me.paco.datecalculator.ui.viewmodel.EventRepeatMode
import java.time.LocalDate

object PreferenceUtils {

    private const val PREF_NAME = "date_calc_prefs"
    private const val KEY_HOLIDAY_REGION = "key_holiday_region"
    private const val KEY_WEEKEND_RULE = "key_weekend_rule"
    private const val KEY_IS_BIG_WEEK = "key_is_big_week"
    private const val KEY_IS_GPS_AUTO = "key_is_gps_auto"
    private const val KEY_DISABLE_CHINA_SHIFT = "key_disable_china_shift"
    private const val KEY_IS_HAPTIC_ENABLED = "key_is_haptic_enabled"
    private const val KEY_APP_LANGUAGE = "key_app_language"
    private const val KEY_DARK_THEME_MODE = "key_dark_theme_mode"
    private const val KEY_THEME_PRESET = "key_theme_preset"
    private const val KEY_CUSTOM_PRIMARY_COLOR = "key_custom_primary_color"
    private const val KEY_TEMP_UNIT = "key_temp_unit"
    private const val KEY_PINNED_PRESETS = "key_pinned_presets"
    private const val KEY_ANNIVERSARIES = "key_anniversaries_v2"
    private const val KEY_ANNIVERSARIES_INIT = "key_anniversaries_initialized_v2"
    private const val KEY_LAST_CITY_NAME = "key_last_city_name"
    private const val KEY_HOME_CONFIG_SHOW_SCREEN = "key_home_config_show_screen"
    private const val KEY_HOME_CONFIG_CALENDAR = "key_home_config_calendar"
    private const val KEY_HOME_CONFIG_ALMANAC = "key_home_config_almanac"
    private const val KEY_HOME_CONFIG_SOLAR_TERMS = "key_home_config_solar_terms"
    private const val KEY_HOME_CONFIG_LUNAR = "key_home_config_lunar"
    private const val KEY_HOME_CONFIG_ZODIAC = "key_home_config_zodiac"
    private const val KEY_HOME_CONFIG_WEATHER = "key_home_config_weather"
    private const val KEY_HOME_CONFIG_BACKGROUND_MODE = "key_home_config_background_mode"
    private const val KEY_HOME_CONFIG_SATELLITE_ALTITUDE = "key_home_config_satellite_altitude"
    private const val KEY_LAST_WEATHER_FETCH_TIME = "key_last_weather_fetch_time"
    private const val KEY_CUSTOM_EVENTS_LIST = "key_custom_events_list"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveLastCityName(context: Context, cityName: String) {
        if (cityName.isNotBlank()) {
            getPrefs(context).edit().putString(KEY_LAST_CITY_NAME, cityName).apply()
        }
    }

    fun getLastCityName(context: Context): String {
        return getPrefs(context).getString(KEY_LAST_CITY_NAME, "深圳市 · 南山区") ?: "深圳市 · 南山区"
    }

    fun saveLastWeatherFetchTime(context: Context, timeMillis: Long) {
        getPrefs(context).edit().putLong(KEY_LAST_WEATHER_FETCH_TIME, timeMillis).apply()
    }

    fun getLastWeatherFetchTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_WEATHER_FETCH_TIME, 0L)
    }

    fun saveHolidayRegion(context: Context, region: HolidayRegion) {
        getPrefs(context).edit().putString(KEY_HOLIDAY_REGION, region.name).apply()
    }

    fun getHolidayRegion(context: Context): HolidayRegion {
        val name = getPrefs(context).getString(KEY_HOLIDAY_REGION, HolidayRegion.CHINA.name)
        return try {
            HolidayRegion.valueOf(name ?: HolidayRegion.CHINA.name)
        } catch (_: Exception) {
            HolidayRegion.CHINA
        }
    }

    fun saveWeekendRule(context: Context, rule: WeekendRule) {
        getPrefs(context).edit().putString(KEY_WEEKEND_RULE, rule.name).apply()
    }

    fun getWeekendRule(context: Context): WeekendRule {
        val name = getPrefs(context).getString(KEY_WEEKEND_RULE, WeekendRule.STANDARD_FIVE_DAYS.name)
        return try {
            WeekendRule.valueOf(name ?: WeekendRule.STANDARD_FIVE_DAYS.name)
        } catch (_: Exception) {
            WeekendRule.STANDARD_FIVE_DAYS
        }
    }

    fun saveIsBigWeek(context: Context, isBigWeek: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_IS_BIG_WEEK, isBigWeek).apply()
    }

    fun getIsBigWeek(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_BIG_WEEK, true)
    }

    fun saveIsGpsAuto(context: Context, isGpsAuto: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_IS_GPS_AUTO, isGpsAuto).apply()
    }

    fun getIsGpsAuto(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_GPS_AUTO, true)
    }

    fun saveDisableChinaShift(context: Context, disable: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_DISABLE_CHINA_SHIFT, disable).apply()
    }

    fun getDisableChinaShift(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DISABLE_CHINA_SHIFT, false)
    }

    fun saveIsHapticFeedbackEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_IS_HAPTIC_ENABLED, enabled).apply()
    }

    fun isHapticFeedbackEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_HAPTIC_ENABLED, true)
    }

    fun saveAppLanguage(context: Context, language: AppLanguage) {
        getPrefs(context).edit().putString(KEY_APP_LANGUAGE, language.name).apply()
    }

    fun getAppLanguage(context: Context): AppLanguage {
        val name = getPrefs(context).getString(KEY_APP_LANGUAGE, AppLanguage.SIMPLIFIED_CHINESE.name)
        return try {
            AppLanguage.valueOf(name ?: AppLanguage.SIMPLIFIED_CHINESE.name)
        } catch (_: Exception) {
            AppLanguage.SIMPLIFIED_CHINESE
        }
    }

    fun saveDarkThemeMode(context: Context, mode: DarkThemeMode) {
        getPrefs(context).edit().putString(KEY_DARK_THEME_MODE, mode.name).apply()
    }

    fun getDarkThemeMode(context: Context): DarkThemeMode {
        val name = getPrefs(context).getString(KEY_DARK_THEME_MODE, DarkThemeMode.SYSTEM.name)
        return try {
            DarkThemeMode.valueOf(name ?: DarkThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            DarkThemeMode.SYSTEM
        }
    }

    fun saveThemePreset(context: Context, preset: ThemeColorPreset) {
        getPrefs(context).edit().putString(KEY_THEME_PRESET, preset.name).apply()
    }

    fun getThemePreset(context: Context): ThemeColorPreset {
        val name = getPrefs(context).getString(KEY_THEME_PRESET, ThemeColorPreset.SYSTEM.name)
        return try {
            ThemeColorPreset.valueOf(name ?: ThemeColorPreset.SYSTEM.name)
        } catch (_: Exception) {
            ThemeColorPreset.SYSTEM
        }
    }

    fun saveCustomPrimaryColorHex(context: Context, colorHex: Long) {
        getPrefs(context).edit().putLong(KEY_CUSTOM_PRIMARY_COLOR, colorHex).apply()
    }

    fun getCustomPrimaryColorHex(context: Context): Long {
        return getPrefs(context).getLong(KEY_CUSTOM_PRIMARY_COLOR, 0xFF2563EB)
    }

    fun saveTemperatureUnit(context: Context, unit: TemperatureUnit) {
        getPrefs(context).edit().putString(KEY_TEMP_UNIT, unit.name).apply()
    }

    fun getTemperatureUnit(context: Context): TemperatureUnit {
        val name = getPrefs(context).getString(KEY_TEMP_UNIT, TemperatureUnit.CELSIUS.name)
        return try {
            TemperatureUnit.valueOf(name ?: TemperatureUnit.CELSIUS.name)
        } catch (_: Exception) {
            TemperatureUnit.CELSIUS
        }
    }

    fun savePinnedPresetHolidays(context: Context, pinnedSet: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_PINNED_PRESETS, pinnedSet).apply()
    }

    fun getPinnedPresetHolidays(context: Context): Set<String> {
        val defaults = emptySet<String>()
        return getPrefs(context).getStringSet(KEY_PINNED_PRESETS, defaults) ?: defaults
    }

    fun saveHomeConfig(context: Context, config: HomeConfig) {
        getPrefs(context).edit()
            .putBoolean(KEY_HOME_CONFIG_SHOW_SCREEN, config.showHomeScreen)
            .putBoolean(KEY_HOME_CONFIG_CALENDAR, config.showCalendar)
            .putBoolean(KEY_HOME_CONFIG_ALMANAC, config.showAlmanac)
            .putBoolean(KEY_HOME_CONFIG_SOLAR_TERMS, config.showSolarTerms)
            .putBoolean(KEY_HOME_CONFIG_LUNAR, config.showLunar)
            .putBoolean(KEY_HOME_CONFIG_ZODIAC, config.showZodiacFortune)
            .putBoolean(KEY_HOME_CONFIG_WEATHER, config.showWeather)
            .putString(KEY_HOME_CONFIG_BACKGROUND_MODE, config.backgroundMode.name)
            .putInt(KEY_HOME_CONFIG_SATELLITE_ALTITUDE, config.satelliteAltitudeKm)
            .apply()
    }

    fun getHomeConfig(context: Context): HomeConfig {
        val prefs = getPrefs(context)
        val modeStr = prefs.getString(KEY_HOME_CONFIG_BACKGROUND_MODE, BackgroundMode.CALENDAR.name) ?: BackgroundMode.CALENDAR.name
        val bgMode = try { BackgroundMode.valueOf(modeStr) } catch (_: Exception) { BackgroundMode.CALENDAR }
        val altitude = prefs.getInt(KEY_HOME_CONFIG_SATELLITE_ALTITUDE, 12000)

        return HomeConfig(
            showHomeScreen = prefs.getBoolean(KEY_HOME_CONFIG_SHOW_SCREEN, true),
            showCalendar = prefs.getBoolean(KEY_HOME_CONFIG_CALENDAR, true),
            showAlmanac = prefs.getBoolean(KEY_HOME_CONFIG_ALMANAC, true),
            showSolarTerms = prefs.getBoolean(KEY_HOME_CONFIG_SOLAR_TERMS, true),
            showLunar = prefs.getBoolean(KEY_HOME_CONFIG_LUNAR, true),
            showZodiacFortune = prefs.getBoolean(KEY_HOME_CONFIG_ZODIAC, true),
            showWeather = prefs.getBoolean(KEY_HOME_CONFIG_WEATHER, true),
            backgroundMode = bgMode,
            satelliteAltitudeKm = altitude
        )
    }

    fun saveAnniversaries(context: Context, list: List<AnniversaryItem>) {
        val prefs = getPrefs(context)
        val sb = StringBuilder()
        list.forEachIndexed { idx, item ->
            if (idx > 0) sb.append(";;;")
            sb.append("${item.id}:::${item.title}:::${item.date}:::${item.iconEmoji}:::${item.isCheckIn}:::${item.locationName}:::${item.latitude}:::${item.longitude}:::${item.checkInTimeStr}:::${item.remark}:::${item.isPinned}")
        }
        prefs.edit().putString(KEY_ANNIVERSARIES, sb.toString()).apply()
    }

    fun getAnniversaries(context: Context): List<AnniversaryItem> {
        val prefs = getPrefs(context)
        val raw = prefs.getString(KEY_ANNIVERSARIES, "") ?: ""

        if (raw.isBlank()) {
            return emptyList()
        }

        return try {
            raw.split(";;;").mapNotNull { token ->
                val parts = token.split(":::")
                if (parts.size >= 11) {
                    AnniversaryItem(
                        id = parts[0].toLongOrNull() ?: System.currentTimeMillis(),
                        title = parts[1],
                        date = LocalDate.parse(parts[2]),
                        iconEmoji = parts[3],
                        isCheckIn = parts[4].toBooleanStrictOrNull() ?: false,
                        locationName = parts[5],
                        latitude = parts[6].toDoubleOrNull() ?: 0.0,
                        longitude = parts[7].toDoubleOrNull() ?: 0.0,
                        checkInTimeStr = parts[8],
                        remark = parts[9],
                        isPinned = parts[10].toBooleanStrictOrNull() ?: false
                    )
                } else null
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveCustomEvents(context: Context, list: List<CustomEventItem>) {
        val prefs = getPrefs(context)
        val sb = StringBuilder()
        list.forEachIndexed { idx, item ->
            if (idx > 0) sb.append(";;;")
            sb.append("${item.id}:::${item.iconEmoji}:::${item.name}:::${item.targetDate}:::${item.repeatMode.name}:::${item.isPinned}")
        }
        prefs.edit().putString(KEY_CUSTOM_EVENTS_LIST, sb.toString()).apply()
    }

    fun getCustomEvents(context: Context): List<CustomEventItem> {
        val prefs = getPrefs(context)
        val raw = prefs.getString(KEY_CUSTOM_EVENTS_LIST, "") ?: ""
        if (raw.isBlank()) return emptyList()

        return try {
            raw.split(";;;").mapNotNull { token ->
                val parts = token.split(":::")
                if (parts.size >= 6) {
                    CustomEventItem(
                        id = parts[0].toLongOrNull() ?: System.currentTimeMillis(),
                        iconEmoji = parts[1],
                        name = parts[2],
                        targetDate = LocalDate.parse(parts[3]),
                        repeatMode = try {
                            EventRepeatMode.valueOf(parts[4])
                        } catch (_: Exception) {
                            EventRepeatMode.NONE
                        },
                        isPinned = parts[5].toBooleanStrictOrNull() ?: false
                    )
                } else null
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
