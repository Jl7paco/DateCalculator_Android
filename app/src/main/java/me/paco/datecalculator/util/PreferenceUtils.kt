package me.paco.datecalculator.util

import android.content.Context
import android.content.SharedPreferences
import me.paco.datecalculator.data.DarkThemeMode
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.HomeConfig
import me.paco.datecalculator.data.ThemeColorPreset
import me.paco.datecalculator.data.WeekendRule

object PreferenceUtils {

    private const val PREF_NAME = "date_calculator_prefs"
    private const val KEY_HOLIDAY_REGION = "holiday_region"
    private const val KEY_WEEKEND_RULE = "weekend_rule"
    private const val KEY_BIG_WEEK = "is_big_week"
    private const val KEY_GPS_AUTO = "is_gps_auto"
    private const val KEY_DISABLE_CHINA_SHIFT = "disable_china_shift"

    private const val KEY_THEME_PRESET = "theme_preset"
    private const val KEY_CUSTOM_PRIMARY_COLOR = "custom_primary_color"
    private const val KEY_DARK_THEME_MODE = "dark_theme_mode"

    private const val KEY_HOME_SHOW = "home_show"
    private const val KEY_HOME_CALENDAR = "home_calendar"
    private const val KEY_HOME_ALMANAC = "home_almanac"
    private const val KEY_HOME_SOLAR_TERMS = "home_solar_terms"
    private const val KEY_HOME_LUNAR = "home_lunar"
    private const val KEY_HOME_ZODIAC = "home_zodiac"
    private const val KEY_HOME_WEATHER = "home_weather"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
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
        getPrefs(context).edit().putBoolean(KEY_BIG_WEEK, isBigWeek).apply()
    }

    fun getIsBigWeek(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BIG_WEEK, true)
    }

    fun saveIsGpsAuto(context: Context, isGpsAuto: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_GPS_AUTO, isGpsAuto).apply()
    }

    fun getIsGpsAuto(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_GPS_AUTO, true)
    }

    fun saveDisableChinaShift(context: Context, disable: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_DISABLE_CHINA_SHIFT, disable).apply()
    }

    fun getDisableChinaShift(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DISABLE_CHINA_SHIFT, false)
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

    fun saveCustomPrimaryColor(context: Context, colorLong: Long) {
        getPrefs(context).edit().putLong(KEY_CUSTOM_PRIMARY_COLOR, colorLong).apply()
    }

    fun getCustomPrimaryColor(context: Context): Long {
        return getPrefs(context).getLong(KEY_CUSTOM_PRIMARY_COLOR, 0xFF2563EB)
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

    fun saveHomeConfig(context: Context, config: HomeConfig) {
        getPrefs(context).edit()
            .putBoolean(KEY_HOME_SHOW, config.showHomeScreen)
            .putBoolean(KEY_HOME_CALENDAR, config.showCalendar)
            .putBoolean(KEY_HOME_ALMANAC, config.showAlmanac)
            .putBoolean(KEY_HOME_SOLAR_TERMS, config.showSolarTerms)
            .putBoolean(KEY_HOME_LUNAR, config.showLunar)
            .putBoolean(KEY_HOME_ZODIAC, config.showZodiacFortune)
            .putBoolean(KEY_HOME_WEATHER, config.showWeather)
            .apply()
    }

    fun getHomeConfig(context: Context): HomeConfig {
        val prefs = getPrefs(context)
        return HomeConfig(
            showHomeScreen = prefs.getBoolean(KEY_HOME_SHOW, true),
            showCalendar = prefs.getBoolean(KEY_HOME_CALENDAR, true),
            showAlmanac = prefs.getBoolean(KEY_HOME_ALMANAC, true),
            showSolarTerms = prefs.getBoolean(KEY_HOME_SOLAR_TERMS, true),
            showLunar = prefs.getBoolean(KEY_HOME_LUNAR, true),
            showZodiacFortune = prefs.getBoolean(KEY_HOME_ZODIAC, true),
            showWeather = prefs.getBoolean(KEY_HOME_WEATHER, true)
        )
    }
}
