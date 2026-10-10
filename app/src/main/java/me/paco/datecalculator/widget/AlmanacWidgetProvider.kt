package me.paco.datecalculator.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import me.paco.datecalculator.MainActivity
import me.paco.datecalculator.R
import me.paco.datecalculator.util.LunarCalendarUtils
import me.paco.datecalculator.util.PreferenceUtils
import me.paco.datecalculator.util.WeatherUtils
import java.time.LocalDate

class AlmanacWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_almanac)

            val today = LocalDate.now()
            val lunar = LunarCalendarUtils.solarToLunar(today)
            val almanac = LunarCalendarUtils.getAlmanacYiJi(today)

            val monthNamesEn = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
            val monthHeader = "${today.monthValue}月 · ${monthNamesEn.getOrElse(today.monthValue - 1) { "" }}"

            val weekDayStr = when (today.dayOfWeek.value) {
                1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"; 5 -> "周五"; 6 -> "周六"; else -> "周日"
            }

            val yiStr = almanac.yiList.take(2).joinToString("·")
            val jiStr = almanac.jiList.take(2).joinToString("·")

            val weather = WeatherUtils.getWeatherForecast(today)
            val curTemp = weather.currentTemp
            val feelsTemp = weather.currentFeelsLikeTemp
            val city = "深圳市 · 南山区"

            views.setTextViewText(R.id.widget_month_header, monthHeader)
            views.setTextViewText(R.id.widget_day_number, "${today.dayOfMonth}")
            views.setTextViewText(R.id.widget_week_lunar, "$weekDayStr · ${lunar.lunarMonthName}${lunar.lunarDayName}")
            views.setTextViewText(R.id.widget_location, "📍 $city")
            views.setTextViewText(R.id.widget_weather_temp, "${weather.dailyList.firstOrNull()?.iconEmoji ?: "☀️"} $curTemp°C")
            views.setTextViewText(R.id.widget_weather_feels, " (体感 $feelsTemp°C)")
            views.setTextViewText(R.id.widget_weather_range, "晴朗 · ${weather.dailyList.firstOrNull()?.tempMin ?: 22}°C~${weather.dailyList.firstOrNull()?.tempMax ?: 28}°C")
            views.setTextViewText(R.id.widget_yi_ji, "宜: $yiStr | 忌: $jiStr")

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_month_header, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
