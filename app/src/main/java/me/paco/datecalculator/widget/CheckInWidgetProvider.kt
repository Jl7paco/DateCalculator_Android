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

class CheckInWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_checkin)

            val anniversaries = PreferenceUtils.getAnniversaries(context)
            val checkIns = anniversaries.filter { it.isCheckIn }
            val lastCheckIn = checkIns.firstOrNull()

            if (lastCheckIn != null && lastCheckIn.locationName.isNotEmpty()) {
                views.setTextViewText(R.id.widget_checkin_location, "📍 ${lastCheckIn.locationName}")
            } else {
                views.setTextViewText(R.id.widget_checkin_location, "📍 点击一键进入打卡")
            }

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_checkin_btn, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_checkin_title, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
