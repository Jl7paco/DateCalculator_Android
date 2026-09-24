package me.paco.datecalculator.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import me.paco.datecalculator.MainActivity
import me.paco.datecalculator.R
import me.paco.datecalculator.util.PreferenceUtils
import me.paco.datecalculator.util.DateCalculatorUtils
import java.time.LocalDate
import kotlin.math.abs

class CountdownWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_countdown)

            val pinnedSet = PreferenceUtils.getPinnedEvents(context)
            val topEventName = pinnedSet.firstOrNull() ?: "🇨🇳 国庆节"

            val targetDate = LocalDate.now().withMonth(10).withDayOfMonth(1)
            val diff = DateCalculatorUtils.naturalDaysBetween(LocalDate.now(), targetDate)

            views.setTextViewText(R.id.widget_event_name, topEventName)
            views.setTextViewText(R.id.widget_days, "${abs(diff)} 天")
            views.setTextViewText(R.id.widget_date_detail, "目标日期: $targetDate")

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
