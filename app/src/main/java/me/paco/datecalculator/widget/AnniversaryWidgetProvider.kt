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

class AnniversaryWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_NEXT_ANNIVERSARY) {
            val prefs = context.getSharedPreferences("anniversary_widget_prefs", Context.MODE_PRIVATE)
            val currIndex = prefs.getInt("anniversary_index", 0)
            prefs.edit().putInt("anniversary_index", currIndex + 1).apply()

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, AnniversaryWidgetProvider::class.java))
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        const val ACTION_NEXT_ANNIVERSARY = "me.paco.datecalculator.ACTION_NEXT_ANNIVERSARY"

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_anniversary)

            val anniversaries = PreferenceUtils.getAnniversaries(context)
            val prefs = context.getSharedPreferences("anniversary_widget_prefs", Context.MODE_PRIVATE)
            val currIndex = prefs.getInt("anniversary_index", 0)

            if (anniversaries.isNotEmpty()) {
                val topItem = anniversaries[currIndex % anniversaries.size]
                val elapsed = DateCalculatorUtils.naturalDaysBetween(topItem.date, LocalDate.now())
                val upcoming = topItem.getNextUpcomingDate(LocalDate.now())
                val nextRemains = DateCalculatorUtils.naturalDaysBetween(LocalDate.now(), upcoming)

                views.setTextViewText(R.id.widget_anniversary_name, "${topItem.iconEmoji} ${topItem.title}")
                views.setTextViewText(R.id.widget_anniversary_days, "${abs(elapsed)} 天")
                views.setTextViewText(R.id.widget_anniversary_base, "起始: ${topItem.date} | 下个周年还剩 $nextRemains 天")
            } else {
                views.setTextViewText(R.id.widget_anniversary_name, "❤️ 暂无纪念日")
                views.setTextViewText(R.id.widget_anniversary_days, "0 天")
                views.setTextViewText(R.id.widget_anniversary_base, "点击添加记录美好时刻")
            }

            // Open App intent
            val appIntent = Intent(context, MainActivity::class.java)
            val appPi = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_anniversary_title, appPi)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
