package me.paco.datecalculator.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
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

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_anniversary)

            val anniversaries = PreferenceUtils.getAnniversaries(context)
            val topItem = anniversaries.firstOrNull()

            if (topItem != null) {
                val elapsed = DateCalculatorUtils.naturalDaysBetween(topItem.date, LocalDate.now())
                val upcoming = topItem.getNextUpcomingDate(LocalDate.now())
                val nextRemains = DateCalculatorUtils.naturalDaysBetween(LocalDate.now(), upcoming)

                views.setTextViewText(R.id.widget_anniversary_name, "${topItem.iconEmoji} ${topItem.title}")
                views.setTextViewText(R.id.widget_anniversary_days, "${abs(elapsed)} 天")
                views.setTextViewText(R.id.widget_anniversary_sub, "下个周年还剩 $nextRemains 天")
            } else {
                views.setTextViewText(R.id.widget_anniversary_name, "❤️ 暂无纪念日")
                views.setTextViewText(R.id.widget_anniversary_days, "0 天")
                views.setTextViewText(R.id.widget_anniversary_sub, "点击添加记录美好时刻")
            }

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_anniversary_title, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
