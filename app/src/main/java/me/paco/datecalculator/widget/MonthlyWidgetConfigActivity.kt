package me.paco.datecalculator.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.paco.datecalculator.ui.theme.DateCalculatorTheme

class MonthlyWidgetConfigActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            DateCalculatorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF1F5F9)) {
                    ConfigScreen()
                }
            }
        }
    }

    @Composable
    fun ConfigScreen() {
        var showAlmanac by remember { mutableStateOf(true) }
        var showFortune by remember { mutableStateOf(true) }
        var showWeather by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("全能月历组件设置", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            Text("请勾选您希望在桌面小组件中显示的信息模块", fontSize = 14.sp, color = Color(0xFF64748B))

            Spacer(modifier = Modifier.height(32.dp))

            ConfigItem(title = "📜 今日黄历 (宜忌)", isChecked = showAlmanac, onCheckedChange = { showAlmanac = it })
            Spacer(modifier = Modifier.height(16.dp))
            ConfigItem(title = "✨ 每日运势 (星座)", isChecked = showFortune, onCheckedChange = { showFortune = it })
            Spacer(modifier = Modifier.height(16.dp))
            ConfigItem(title = "☀️ 实时天气", isChecked = showWeather, onCheckedChange = { showWeather = it })

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2563EB))
                    .clickable {
                        savePrefs(showAlmanac, showFortune, showWeather)
                        val appWidgetManager = AppWidgetManager.getInstance(this@MonthlyWidgetConfigActivity)
                        MonthlyWidgetProvider.updateWidget(this@MonthlyWidgetConfigActivity, appWidgetManager, appWidgetId)

                        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        setResult(RESULT_OK, resultValue)
                        finish()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("完成并添加至桌面", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    @Composable
    fun ConfigItem(title: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .clickable { onCheckedChange(!isChecked) }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF2563EB))
            )
        }
    }

    private fun savePrefs(almanac: Boolean, fortune: Boolean, weather: Boolean) {
        val prefs = getSharedPreferences("monthly_widget_$appWidgetId", MODE_PRIVATE)
        prefs.edit()
            .putBoolean("show_almanac", almanac)
            .putBoolean("show_fortune", fortune)
            .putBoolean("show_weather", weather)
            .apply()
    }
}
