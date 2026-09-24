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

            val yiStr = almanac.yiList.take(2).joinToString("·")
            val jiStr = almanac.jiList.take(2).joinToString("·")

            views.setTextViewText(R.id.widget_solar_date, "${today.year}年${today.monthValue}月${today.dayOfMonth}日")
            views.setTextViewText(R.id.widget_lunar_date, "农历 ${lunar.ganZhiYear}(${lunar.zodiac})年 ${lunar.lunarMonthName}${lunar.lunarDayName}")
            views.setTextViewText(R.id.widget_yi_ji, "宜: $yiStr | 忌: $jiStr")

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_almanac_title, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
