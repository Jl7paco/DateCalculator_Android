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
import me.paco.datecalculator.util.PreferenceUtils
import me.paco.datecalculator.util.WeatherUtils
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs

class HomeDashboardWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_4x4_home_dashboard)

            val today = LocalDate.now()
            val yearMonth = YearMonth.from(today)
            val weather = WeatherUtils.getWeatherForecast(today)

            // Header
            views.setTextViewText(R.id.tv_dash_month_title, "${today.year}年 ${today.monthValue}月")
            views.setTextViewText(R.id.tv_dash_weather, "${weather.dailyList.firstOrNull()?.iconEmoji ?: "☀️"} ${weather.currentTemp}°C 深圳市")

            // Calendar Grid
            val firstDayOfMonth = yearMonth.atDay(1)
            val daysInMonth = yearMonth.lengthOfMonth()
            val startOffset = firstDayOfMonth.dayOfWeek.value % 7
            val totalGridCells = 42

            for (i in 0 until totalGridCells) {
                val dayNum = i - startOffset + 1
                val cellId = context.resources.getIdentifier("dash_cell_$i", "id", context.packageName)

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

            // Bottom Pinned Event
            val anniversaries = PreferenceUtils.getAnniversaries(context)
            val customEvents = PreferenceUtils.getCustomEvents(context)

            val pinnedCustom = customEvents.firstOrNull { it.isPinned }
            val pinnedAnniversary = anniversaries.firstOrNull { it.isCheckIn } ?: anniversaries.firstOrNull()

            if (pinnedCustom != null) {
                val upcoming = pinnedCustom.getNextUpcomingDate(today)
                val diffDays = DateCalculatorUtils.naturalDaysBetween(today, upcoming)
                views.setTextViewText(R.id.tv_dash_event_title, "📌 ${pinnedCustom.name}")
                views.setTextViewText(R.id.tv_dash_event_days, "还有 ${abs(diffDays)} 天 (${DateCalculatorUtils.formatDate(upcoming, AppLanguage.SIMPLIFIED_CHINESE)})")
            } else if (pinnedAnniversary != null) {
                val elapsed = abs(DateCalculatorUtils.naturalDaysBetween(pinnedAnniversary.date, today))
                views.setTextViewText(R.id.tv_dash_event_title, "❤️ ${pinnedAnniversary.title}")
                views.setTextViewText(R.id.tv_dash_event_days, "已相伴 $elapsed 天 (起始: ${pinnedAnniversary.date})")
            } else {
                views.setTextViewText(R.id.tv_dash_event_title, "📌 考研倒计时")
                val gaokaoDate = LocalDate.of(today.year, 12, 22)
                val targetDate = if (gaokaoDate.isBefore(today)) gaokaoDate.plusYears(1) else gaokaoDate
                val remDays = DateCalculatorUtils.naturalDaysBetween(today, targetDate)
                views.setTextViewText(R.id.tv_dash_event_days, "还有 $remDays 天 (${DateCalculatorUtils.formatDate(targetDate, AppLanguage.SIMPLIFIED_CHINESE)})")
            }

            // App Intent
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_dashboard_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
