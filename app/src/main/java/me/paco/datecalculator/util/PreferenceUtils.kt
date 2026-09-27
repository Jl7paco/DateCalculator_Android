package me.paco.datecalculator.util

import android.content.Context
import android.content.SharedPreferences
import me.paco.datecalculator.data.AnniversaryItem
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.DarkThemeMode
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.HomeConfig
import me.paco.datecalculator.data.ThemeColorPreset
import me.paco.datecalculator.data.WeekendRule
import me.paco.datecalculator.ui.viewmodel.CustomEventItem
import me.paco.datecalculator.ui.viewmodel.EventRepeatMode
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

object PreferenceUtils {

    private const val PREF_NAME = "date_calculator_prefs"

    private const val KEY_HOLIDAY_REGION = "key_holiday_region"
    private const val KEY_WEEKEND_RULE = "key_weekend_rule"
    private const val KEY_IS_BIG_WEEK = "key_is_big_week"
    private const val KEY_IS_GPS_AUTO = "key_is_gps_auto"
    private const val KEY_DISABLE_CHINA_SHIFT = "key_disable_china_shift"

    private const val KEY_THEME_PRESET = "key_theme_preset"
    private const val KEY_CUSTOM_PRIMARY_COLOR = "key_custom_primary_color"
    private const val KEY_DARK_THEME_MODE = "key_dark_theme_mode"
    private const val KEY_APP_LANGUAGE = "key_app_language"

    private const val KEY_HOME_SHOW = "key_home_show"
    private const val KEY_HOME_CALENDAR = "key_home_calendar"
    private const val KEY_HOME_ALMANAC = "key_home_almanac"
    private const val KEY_HOME_SOLAR_TERMS = "key_home_solar_terms"
    private const val KEY_HOME_LUNAR = "key_home_lunar"
    private const val KEY_HOME_ZODIAC = "key_home_zodiac"
    private const val KEY_HOME_WEATHER = "key_home_weather"

    private const val KEY_PINNED_EVENTS = "key_pinned_events"
    private const val KEY_ANNIVERSARIES_LIST = "key_anniversaries_list"
    private const val KEY_CUSTOM_EVENTS_LIST = "key_custom_events_list"

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
        getPrefs(context).edit().putBoolean(KEY_IS_BIG_WEEK, isBigWeek).apply()
    }

    fun getIsBigWeek(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_BIG_WEEK, true)
    }

    fun saveIsGpsAuto(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_IS_GPS_AUTO, enabled).apply()
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

    fun saveCustomPrimaryColor(context: Context, colorHex: Long) {
        getPrefs(context).edit().putLong(KEY_CUSTOM_PRIMARY_COLOR, colorHex).apply()
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

    fun saveAppLanguage(context: Context, language: AppLanguage) {
        getPrefs(context).edit().putString(KEY_APP_LANGUAGE, language.name).apply()
    }

    fun getAppLanguage(context: Context): AppLanguage {
        val name = getPrefs(context).getString(KEY_APP_LANGUAGE, AppLanguage.SYSTEM.name)
        return try {
            AppLanguage.valueOf(name ?: AppLanguage.SYSTEM.name)
        } catch (_: Exception) {
            AppLanguage.SYSTEM
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

    fun savePinnedEvents(context: Context, pinnedSet: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_PINNED_EVENTS, pinnedSet).apply()
    }

    fun getPinnedEvents(context: Context): Set<String> {
        val prefs = getPrefs(context)
        if (!prefs.contains(KEY_PINNED_EVENTS)) {
            val defaultPinned = setOf("🇨🇳 国庆节", "🎆 元旦")
            prefs.edit().putStringSet(KEY_PINNED_EVENTS, defaultPinned).apply()
            return defaultPinned
        }
        return prefs.getStringSet(KEY_PINNED_EVENTS, emptySet()) ?: emptySet()
    }

    fun saveAnniversaries(context: Context, items: List<AnniversaryItem>) {
        val limitedList = items.take(999)
        val jsonArray = JSONArray()
        limitedList.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("date", item.date.toString())
                put("iconEmoji", item.iconEmoji)
                put("isCheckIn", item.isCheckIn)
                put("locationName", item.locationName)
                put("latitude", item.latitude ?: JSONObject.NULL)
                put("longitude", item.longitude ?: JSONObject.NULL)
                put("checkInTimeStr", item.checkInTimeStr)
                put("remark", item.remark)
                put("isPinned", item.isPinned)
            }
            jsonArray.put(obj)
        }
        getPrefs(context).edit().putString(KEY_ANNIVERSARIES_LIST, jsonArray.toString()).apply()
    }

    fun getAnniversaries(context: Context): List<AnniversaryItem> {
        val jsonStr = getPrefs(context).getString(KEY_ANNIVERSARIES_LIST, null) ?: return emptyList()
        val list = mutableListOf<AnniversaryItem>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optLong("id", System.currentTimeMillis())
                val title = obj.getString("title")
                val date = LocalDate.parse(obj.getString("date"))
                val iconEmoji = obj.optString("iconEmoji", "❤️")
                val isCheckIn = obj.optBoolean("isCheckIn", false)
                val locationName = obj.optString("locationName", "")
                val lat = if (obj.isNull("latitude")) null else obj.optDouble("latitude")
                val lng = if (obj.isNull("longitude")) null else obj.optDouble("longitude")
                val checkInTimeStr = obj.optString("checkInTimeStr", "")
                val remark = obj.optString("remark", "")
                val isPinned = obj.optBoolean("isPinned", false)

                list.add(
                    AnniversaryItem(
                        id = id,
                        title = title,
                        date = date,
                        iconEmoji = iconEmoji,
                        isCheckIn = isCheckIn,
                        locationName = locationName,
                        latitude = lat,
                        longitude = lng,
                        checkInTimeStr = checkInTimeStr,
                        remark = remark,
                        isPinned = isPinned
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun saveCustomEvents(context: Context, items: List<CustomEventItem>) {
        val limitedList = items.take(999)
        val jsonArray = JSONArray()
        limitedList.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("iconEmoji", item.iconEmoji)
                put("name", item.name)
                put("targetDate", item.targetDate.toString())
                put("repeatMode", item.repeatMode.name)
                put("isPinned", item.isPinned)
            }
            jsonArray.put(obj)
        }
        getPrefs(context).edit().putString(KEY_CUSTOM_EVENTS_LIST, jsonArray.toString()).apply()
    }

    fun getCustomEvents(context: Context): List<CustomEventItem> {
        val jsonStr = getPrefs(context).getString(KEY_CUSTOM_EVENTS_LIST, null) ?: return emptyList()
        val list = mutableListOf<CustomEventItem>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optLong("id", System.nanoTime())
                val iconEmoji = obj.optString("iconEmoji", "📌")
                val name = obj.getString("name")
                val targetDate = LocalDate.parse(obj.getString("targetDate"))
                val repeatModeName = obj.optString("repeatMode", EventRepeatMode.NONE.name)
                val repeatMode = try { EventRepeatMode.valueOf(repeatModeName) } catch (_: Exception) { EventRepeatMode.NONE }
                val isPinned = obj.optBoolean("isPinned", false)

                list.add(
                    CustomEventItem(
                        id = id,
                        iconEmoji = iconEmoji,
                        name = name,
                        targetDate = targetDate,
                        repeatMode = repeatMode,
                        isPinned = isPinned
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }
}
