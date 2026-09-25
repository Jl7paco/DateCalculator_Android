package me.paco.datecalculator.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import me.paco.datecalculator.MainActivity
import me.paco.datecalculator.R
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.PreferenceUtils
import java.time.LocalDate
import kotlin.math.abs

class CountdownWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_NEXT_COUNTDOWN) {
            val prefs = context.getSharedPreferences("countdown_widget_prefs", Context.MODE_PRIVATE)
            val currIndex = prefs.getInt("countdown_index", 0)
            prefs.edit().putInt("countdown_index", currIndex + 1).apply()

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, CountdownWidgetProvider::class.java))
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        const val ACTION_NEXT_COUNTDOWN = "me.paco.datecalculator.ACTION_NEXT_COUNTDOWN"

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_countdown)

            val pinnedSet = PreferenceUtils.getPinnedEvents(context).toList()
            val list = if (pinnedSet.isNotEmpty()) pinnedSet else listOf("🇨🇳 国庆节", "🎆 元旦", "🧧 春节")

            val prefs = context.getSharedPreferences("countdown_widget_prefs", Context.MODE_PRIVATE)
            val currIndex = prefs.getInt("countdown_index", 0)
            val eventName = list[currIndex % list.size]

            val targetDate = LocalDate.now().withMonth(10).withDayOfMonth(1)
            val diff = DateCalculatorUtils.naturalDaysBetween(LocalDate.now(), targetDate)

            views.setTextViewText(R.id.widget_event_name, eventName)
            views.setTextViewText(R.id.widget_days, "${abs(diff)} 天")
            views.setTextViewText(R.id.widget_date_detail, "目标: $targetDate")

            // Next button intent
            val nextIntent = Intent(context, CountdownWidgetProvider::class.java).apply { action = ACTION_NEXT_COUNTDOWN }
            val nextPi = PendingIntent.getBroadcast(context, 101, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.btn_next_countdown, nextPi)

            // Open App intent
            val appIntent = Intent(context, MainActivity::class.java)
            val appPi = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_title, appPi)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
