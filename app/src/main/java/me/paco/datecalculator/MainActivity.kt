package me.paco.datecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import me.paco.datecalculator.data.DarkThemeMode
import me.paco.datecalculator.ui.MainScreen
import me.paco.datecalculator.ui.theme.DateCalculatorTheme
import me.paco.datecalculator.data.BackgroundMode
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.WeatherUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: DateCalculatorViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            val (cityLat, cityLon) = remember(uiState.currentCityName) {
                WeatherUtils.getCityCoordinates(uiState.currentCityName)
            }

            val isDark = if (uiState.homeConfig.backgroundMode == BackgroundMode.EARTH_3D) {
                // 3D 卫星地球模式：强行重写主题色设置，根据当地实时昼夜判定（白昼浅色，黑夜深色）
                !DateCalculatorUtils.isDaylightAtLocation(cityLat, cityLon)
            } else {
                when (uiState.darkThemeMode) {
                    DarkThemeMode.SYSTEM -> isSystemInDarkTheme()
                    DarkThemeMode.ON -> true
                    DarkThemeMode.OFF -> false
                }
            }

            DateCalculatorTheme(
                darkTheme = isDark,
                themePreset = uiState.themePreset,
                customPrimaryColorHex = uiState.customPrimaryColorHex
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Date Calculator Main Preview")
@Composable
fun DateCalculatorAppPreview() {
    DateCalculatorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainScreen()
        }
    }
}
