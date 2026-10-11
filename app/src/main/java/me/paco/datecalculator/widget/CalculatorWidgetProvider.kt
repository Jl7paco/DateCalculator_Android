package me.paco.datecalculator.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import me.paco.datecalculator.MainActivity
import me.paco.datecalculator.R
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.WeekendRule
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.PreferenceUtils
import java.time.LocalDate
import kotlin.math.abs

class CalculatorWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return
        val prefs = context.getSharedPreferences("widget_4x4_calc_prefs", Context.MODE_PRIVATE)

        when {
            action == ACTION_TOGGLE_MODE -> {
                val isMulti = prefs.getBoolean("is_multi_mode", false)
                prefs.edit().putBoolean("is_multi_mode", !isMulti).apply()
            }
            action.startsWith(ACTION_SINGLE_CALC_PREFIX) -> {
                val valStr = action.removePrefix(ACTION_SINGLE_CALC_PREFIX)
                val daysVal = valStr.toIntOrNull() ?: 0
                prefs.edit().putInt("single_days", daysVal).apply()
            }
            action == ACTION_MULTI_ADD_STAGE -> {
                val currStages = prefs.getString("multi_stages", "15") ?: "15"
                val newStages = if (currStages.isEmpty()) "10" else "$currStages,10"
                prefs.edit().putString("multi_stages", newStages).apply()
            }
            action == ACTION_MULTI_RESET -> {
                prefs.edit().putString("multi_stages", "15").apply()
            }
        }

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, CalculatorWidgetProvider::class.java))
        onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        const val ACTION_TOGGLE_MODE = "me.paco.datecalculator.ACTION_4X4_TOGGLE_MODE"
        const val ACTION_SINGLE_CALC_PREFIX = "me.paco.datecalculator.ACTION_4X4_SINGLE_"
        const val ACTION_MULTI_ADD_STAGE = "me.paco.datecalculator.ACTION_4X4_MULTI_ADD"
        const val ACTION_MULTI_RESET = "me.paco.datecalculator.ACTION_4X4_MULTI_RESET"

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_4x4_calculator)
            val prefs = context.getSharedPreferences("widget_4x4_calc_prefs", Context.MODE_PRIVATE)

            val isMultiMode = prefs.getBoolean("is_multi_mode", false)
            val baseDate = LocalDate.now()
            val weekendRule = PreferenceUtils.getWeekendRule(context)

            views.setTextViewText(R.id.tv_calc_base_date, "📅 起算: ${DateCalculatorUtils.formatDateWithWeek(baseDate, AppLanguage.SIMPLIFIED_CHINESE)}")

            if (!isMultiMode) {
                // 单段计算展示
                views.setViewVisibility(R.id.panel_single_calc_keys, View.VISIBLE)
                views.setViewVisibility(R.id.panel_multi_stage_container, View.GONE)
                views.setTextViewText(R.id.btn_switch_calc_mode, "切换多段 ➔")

                val singleDays = prefs.getInt("single_days", 15)
                val isAdd = singleDays >= 0
                val absDays = abs(singleDays)
                val type = if (isAdd) CalculationType.ADD else CalculationType.SUBTRACT

                val targetDate = DateCalculatorUtils.calculateTargetDate(baseDate, absDays, type, true, weekendRule)
                val (work, stat, week) = DateCalculatorUtils.calculateBreakdown(baseDate, targetDate, weekendRule)
                val natDays = abs(DateCalculatorUtils.naturalDaysBetween(baseDate, targetDate))

                val dirStr = if (isAdd) "后是" else "前是"
                views.setTextViewText(R.id.tv_calc_result_expression, "$absDays 个工作日$dirStr")
                views.setTextViewText(R.id.tv_calc_result_date, DateCalculatorUtils.formatDate(targetDate, AppLanguage.SIMPLIFIED_CHINESE))
                views.setTextViewText(R.id.tv_calc_result_breakdown, "共 $natDays 天 | 含 $week 天周末 $stat 天节假日")
            } else {
                // 多段计算展示
                views.setViewVisibility(R.id.panel_single_calc_keys, View.GONE)
                views.setViewVisibility(R.id.panel_multi_stage_container, View.VISIBLE)
                views.setTextViewText(R.id.btn_switch_calc_mode, "➔ 单段模式")

                val stageStr = prefs.getString("multi_stages", "15") ?: "15"
                val stageList = stageStr.split(",").mapNotNull { it.trim().toIntOrNull() }

                var currStart = baseDate
                val sb = StringBuilder()

                stageList.forEachIndexed { idx, days ->
                    val isAdd = days >= 0
                    val absDays = abs(days)
                    val type = if (isAdd) CalculationType.ADD else CalculationType.SUBTRACT
                    val segEnd = DateCalculatorUtils.calculateTargetDate(currStart, absDays, type, true, weekendRule)

                    if (idx > 0) sb.append("  ➔  ")
                    sb.append("阶段${idx + 1}: ${if (isAdd) "+" else "-"}${absDays}天")
                    currStart = segEnd
                }

                val finalNatDays = abs(DateCalculatorUtils.naturalDaysBetween(baseDate, currStart))
                val (work, stat, week) = DateCalculatorUtils.calculateBreakdown(baseDate, currStart, weekendRule)

                views.setTextViewText(R.id.tv_calc_result_expression, "多段累计推算 (${stageList.size} 个阶段)")
                views.setTextViewText(R.id.tv_calc_result_date, DateCalculatorUtils.formatDate(currStart, AppLanguage.SIMPLIFIED_CHINESE))
                views.setTextViewText(R.id.tv_calc_result_breakdown, "总历时: $finalNatDays 天 ($work 工作日 | ${stat + week} 休息)")
                views.setTextViewText(R.id.tv_multi_stage_list, sb.toString())
            }

            // Pending Intents for Mode Toggle
            val toggleIntent = Intent(context, CalculatorWidgetProvider::class.java).apply { action = ACTION_TOGGLE_MODE }
            val togglePi = PendingIntent.getBroadcast(context, 1000, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.btn_switch_calc_mode, togglePi)

            // Single calc keys: +5, +15, +30, +100 / -5, -15, -30, -100
            val btnMap = mapOf(
                R.id.btn_add_5 to "5", R.id.btn_add_15 to "15", R.id.btn_add_30 to "30", R.id.btn_add_100 to "100",
                R.id.btn_sub_5 to "-5", R.id.btn_sub_15 to "-15", R.id.btn_sub_30 to "-30", R.id.btn_sub_100 to "-100"
            )

            btnMap.forEach { (btnId, valStr) ->
                val intent = Intent(context, CalculatorWidgetProvider::class.java).apply { action = "$ACTION_SINGLE_CALC_PREFIX$valStr" }
                val pi = PendingIntent.getBroadcast(context, btnId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                views.setOnClickPendingIntent(btnId, pi)
            }

            // Multi calc keys
            val addStageIntent = Intent(context, CalculatorWidgetProvider::class.java).apply { action = ACTION_MULTI_ADD_STAGE }
            val addStagePi = PendingIntent.getBroadcast(context, 2001, addStageIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.btn_multi_add_stage, addStagePi)

            val resetIntent = Intent(context, CalculatorWidgetProvider::class.java).apply { action = ACTION_MULTI_RESET }
            val resetPi = PendingIntent.getBroadcast(context, 2002, resetIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.btn_multi_reset, resetPi)

            // App Intent
            val appIntent = Intent(context, MainActivity::class.java)
            val appPi = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.tv_calc_widget_title, appPi)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
