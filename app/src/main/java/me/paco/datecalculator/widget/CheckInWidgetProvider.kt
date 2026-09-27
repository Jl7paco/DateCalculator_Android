package me.paco.datecalculator.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import me.paco.datecalculator.MainActivity
import me.paco.datecalculator.R
import me.paco.datecalculator.data.AnniversaryItem
import me.paco.datecalculator.util.LocationUtils
import me.paco.datecalculator.util.PreferenceUtils
import me.paco.datecalculator.util.WeatherUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CheckInWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_QUICK_CHECKIN) {
            val currentCity = LocationUtils.getCurrentCityName(context)
            val coords = WeatherUtils.getCityCoordinates(currentCity)
            val nowTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            val checkInItem = AnniversaryItem(
                title = "桌面1键打卡",
                date = LocalDate.now(),
                iconEmoji = "📍",
                isCheckIn = true,
                locationName = currentCity,
                latitude = coords.first,
                longitude = coords.second,
                checkInTimeStr = nowTimeStr,
                remark = "来自桌面小组件1键打卡"
            )

            val currentList = PreferenceUtils.getAnniversaries(context)
            val updatedList = listOf(checkInItem) + currentList
            PreferenceUtils.saveAnniversaries(context, updatedList)

            Toast.makeText(context, "🎉 打卡成功！已记录在重要纪念日", Toast.LENGTH_SHORT).show()

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, CheckInWidgetProvider::class.java))
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        const val ACTION_QUICK_CHECKIN = "me.paco.datecalculator.ACTION_QUICK_CHECKIN"

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_checkin)

            val anniversaries = PreferenceUtils.getAnniversaries(context)
            val checkIns = anniversaries.filter { it.isCheckIn }
            val lastCheckIn = checkIns.firstOrNull()

            if (lastCheckIn != null && lastCheckIn.locationName.isNotEmpty()) {
                views.setTextViewText(R.id.widget_checkin_location, "当前位置: ${lastCheckIn.locationName}")
            } else {
                views.setTextViewText(R.id.widget_checkin_location, "点击一键进入打卡")
            }

            // 1-Click Checkin intent
            val checkInIntent = Intent(context, CheckInWidgetProvider::class.java).apply { action = ACTION_QUICK_CHECKIN }
            val checkInPi = PendingIntent.getBroadcast(context, 303, checkInIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_checkin_btn, checkInPi)

            // Open App intent
            val appIntent = Intent(context, MainActivity::class.java)
            val appPi = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_checkin_title, appPi)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
