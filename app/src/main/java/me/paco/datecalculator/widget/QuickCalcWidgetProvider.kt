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
import java.time.LocalDate

class QuickCalcWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        val days = when (action) {
            ACTION_CALC_15 -> 15L
            ACTION_CALC_30 -> 30L
            ACTION_CALC_100 -> 100L
            else -> null
        }

        if (days != null) {
            val targetDate = LocalDate.now().plusDays(days)
            val prefs = context.getSharedPreferences("quick_calc_widget_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("last_calc_result", "${days}天后: ${DateCalculatorUtils.formatDate(targetDate)}").apply()

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, QuickCalcWidgetProvider::class.java))
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        const val ACTION_CALC_15 = "me.paco.datecalculator.ACTION_CALC_15"
        const val ACTION_CALC_30 = "me.paco.datecalculator.ACTION_CALC_30"
        const val ACTION_CALC_100 = "me.paco.datecalculator.ACTION_CALC_100"

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_calc)

            val prefs = context.getSharedPreferences("quick_calc_widget_prefs", Context.MODE_PRIVATE)
            val lastResult = prefs.getString("last_calc_result", "点击下方按键实时计算目标日期")

            views.setTextViewText(R.id.widget_calc_result, lastResult)

            // Click +15
            val intent15 = Intent(context, QuickCalcWidgetProvider::class.java).apply { action = ACTION_CALC_15 }
            val pi15 = PendingIntent.getBroadcast(context, 15, intent15, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.btn_calc_15, pi15)

            // Click +30
            val intent30 = Intent(context, QuickCalcWidgetProvider::class.java).apply { action = ACTION_CALC_30 }
            val pi30 = PendingIntent.getBroadcast(context, 30, intent30, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.btn_calc_30, pi30)

            // Click +100
            val intent100 = Intent(context, QuickCalcWidgetProvider::class.java).apply { action = ACTION_CALC_100 }
            val pi100 = PendingIntent.getBroadcast(context, 100, intent100, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.btn_calc_100, pi100)

            // Open App
            val intentApp = Intent(context, MainActivity::class.java)
            val piApp = PendingIntent.getActivity(context, 0, intentApp, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_calc_title, piApp)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
