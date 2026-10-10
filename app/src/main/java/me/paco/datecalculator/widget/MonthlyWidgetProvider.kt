package me.paco.datecalculator.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import me.paco.datecalculator.MainActivity
import me.paco.datecalculator.R
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LunarCalendarUtils
import me.paco.datecalculator.util.WeatherUtils
import java.time.LocalDate
import java.time.YearMonth

class MonthlyWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            context.getSharedPreferences("monthly_widget_$appWidgetId", Context.MODE_PRIVATE)
                .edit().clear().apply()
        }
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prefs = context.getSharedPreferences("monthly_widget_$appWidgetId", Context.MODE_PRIVATE)
            val showAlmanac = prefs.getBoolean("show_almanac", true)
            val showFortune = prefs.getBoolean("show_fortune", true)
            val showWeather = prefs.getBoolean("show_weather", true)

            val views = RemoteViews(context.packageName, R.layout.widget_monthly_comprehensive)

            val today = LocalDate.now()
            val yearMonth = YearMonth.from(today)
            val lunar = LunarCalendarUtils.solarToLunar(today)

            // Header Date
            views.setTextViewText(R.id.tv_month_title, "${today.year}年 ${today.monthValue}月")

            // Setup Calendar Grid
            val firstDayOfMonth = yearMonth.atDay(1)
            val daysInMonth = yearMonth.lengthOfMonth()
            val startOffset = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Sunday
            val totalGridCells = 42

            for (i in 0 until totalGridCells) {
                val dayNum = i - startOffset + 1
                val cellId = context.resources.getIdentifier("cell_day_$i", "id", context.packageName)

                if (cellId != 0) {
                    if (dayNum in 1..daysInMonth) {
                        val cellDate = yearMonth.atDay(dayNum)
                        val isToday = (cellDate == today)

                        views.setViewVisibility(cellId, View.VISIBLE)
                        views.setTextViewText(cellId, dayNum.toString())

                        if (isToday) {
                            views.setTextColor(cellId, Color.WHITE)
                            views.setInt(cellId, "setBackgroundResource", R.drawable.widget_bg_neumorphic_accent_circle)
                        } else {
                            val isWeekend = (i % 7 == 0 || i % 7 == 6)
                            views.setTextColor(cellId, if (isWeekend) Color.parseColor("#EF4444") else Color.parseColor("#1E293B"))
                            views.setInt(cellId, "setBackgroundResource", android.R.color.transparent)
                        }
                    } else {
                        views.setViewVisibility(cellId, View.INVISIBLE)
                    }
                }
            }

            // Info Panel
            if (!showAlmanac && !showFortune && !showWeather) {
                views.setViewVisibility(R.id.panel_info, View.GONE)
            } else {
                views.setViewVisibility(R.id.panel_info, View.VISIBLE)

                // Almanac
                if (showAlmanac) {
                    views.setViewVisibility(R.id.row_almanac, View.VISIBLE)
                    val almanac = LunarCalendarUtils.getAlmanacYiJi(today)
                    val yi = almanac.yiList.take(2).joinToString("·")
                    val ji = almanac.jiList.take(2).joinToString("·")
                    views.setTextViewText(R.id.tv_lunar, "农历 ${lunar.lunarMonthName}${lunar.lunarDayName}")
                    views.setTextViewText(R.id.tv_yiji, "宜: $yi | 忌: $ji")
                } else {
                    views.setViewVisibility(R.id.row_almanac, View.GONE)
                }

                // Fortune
                if (showFortune) {
                    views.setViewVisibility(R.id.row_fortune, View.VISIBLE)
                    val (constName, constEmoji) = LunarCalendarUtils.getConstellationInfo(today)
                    val fortune = LunarCalendarUtils.getDailyFortune(today, constName, AppLanguage.SIMPLIFIED_CHINESE)
                    views.setTextViewText(R.id.tv_fortune_title, "$constEmoji $constName")
                    views.setTextViewText(R.id.tv_fortune_desc, "✨ ${fortune.summary}")
                } else {
                    views.setViewVisibility(R.id.row_fortune, View.GONE)
                }

                // Weather
                if (showWeather) {
                    views.setViewVisibility(R.id.row_weather, View.VISIBLE)
                    val weather = WeatherUtils.getWeatherForecast(today)
                    views.setTextViewText(R.id.tv_weather_temp, "${weather.currentTemp}°C")
                    views.setTextViewText(R.id.tv_weather_desc, "${weather.dailyList.firstOrNull()?.iconEmoji ?: "☀️"} ${weather.dailyList.firstOrNull()?.condition ?: "晴"}")
                } else {
                    views.setViewVisibility(R.id.row_weather, View.GONE)
                }
            }

            // App Intent
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
