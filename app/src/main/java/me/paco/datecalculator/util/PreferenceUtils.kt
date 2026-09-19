package me.paco.datecalculator.util

import android.content.Context
import android.content.SharedPreferences
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.WeekendRule

object PreferenceUtils {

    private const val PREF_NAME = "date_calculator_prefs"
    private const val KEY_HOLIDAY_REGION = "holiday_region"
    private const val KEY_WEEKEND_RULE = "weekend_rule"
    private const val KEY_BIG_WEEK = "is_big_week"
    private const val KEY_GPS_AUTO = "is_gps_auto"

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
        return getPrefs(context).getBoolean(KEY_GPS_AUTO, false)
    }
}
