package me.paco.datecalculator.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import me.paco.datecalculator.data.ThemeColorPreset

private val BrightNeumorphicBg = Color(0xFFF2F5FA)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E2FF),
    onPrimaryContainer = Color(0xFF2D3748),
    secondary = Color(0xFF2563EB),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8E2FF),
    onSecondaryContainer = Color(0xFF2D3748),
    surface = BrightNeumorphicBg,
    onSurface = Color(0xFF2D3748),
    surfaceVariant = BrightNeumorphicBg,
    onSurfaceVariant = Color(0xFF2D3748),
    background = BrightNeumorphicBg,
    onBackground = Color(0xFF2D3748)
)

@Composable
fun DateCalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themePreset: ThemeColorPreset = ThemeColorPreset.SYSTEM,
    customPrimaryColorHex: Long = 0xFF2563EB,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val baseScheme = when {
        themePreset == ThemeColorPreset.SYSTEM && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> {
            val primaryColor = if (themePreset == ThemeColorPreset.CUSTOM) {
                Color(customPrimaryColorHex)
            } else {
                Color(themePreset.primaryColorHex)
            }

            if (darkTheme) {
                DarkColorScheme.copy(
                    primary = primaryColor,
                    secondary = primaryColor
                )
            } else {
                LightColorScheme.copy(
                    primary = primaryColor,
                    onPrimary = Color.White,
                    primaryContainer = primaryColor.copy(alpha = 0.18f),
                    onPrimaryContainer = Color(0xFF2D3748),
                    secondary = primaryColor,
                    onSecondary = Color.White,
                    secondaryContainer = primaryColor.copy(alpha = 0.18f),
                    onSecondaryContainer = Color(0xFF2D3748)
                )
            }
        }
    }

    val colorScheme = if (!darkTheme) {
        baseScheme.copy(
            surface = BrightNeumorphicBg,
            surfaceVariant = BrightNeumorphicBg,
            background = BrightNeumorphicBg,
            onSurface = Color(0xFF2D3748),
            onSurfaceVariant = Color(0xFF2D3748),
            onBackground = Color(0xFF2D3748)
        )
    } else {
        baseScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surfaceVariant.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
