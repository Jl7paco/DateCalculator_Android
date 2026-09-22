package me.paco.datecalculator.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import me.paco.datecalculator.data.ThemeColorPreset

val LocalDarkTheme = compositionLocalOf { false }

private val BrightNeumorphicBg = Color(0xFFF2F5FA)
private val DarkNeumorphicBg = Color(0xFF1B232A)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A).copy(alpha = 0.5f),
    onPrimaryContainer = Color(0xFFE2E8F0),
    secondary = Color(0xFF60A5FA),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E3A8A).copy(alpha = 0.5f),
    onSecondaryContainer = Color(0xFFE2E8F0),
    surface = DarkNeumorphicBg,
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = DarkNeumorphicBg,
    onSurfaceVariant = Color(0xFFE2E8F0),
    background = DarkNeumorphicBg,
    onBackground = Color(0xFFE2E8F0)
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

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

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
        baseScheme.copy(
            surface = DarkNeumorphicBg,
            surfaceVariant = DarkNeumorphicBg,
            background = DarkNeumorphicBg,
            onSurface = Color(0xFFE2E8F0),
            onSurfaceVariant = Color(0xFFE2E8F0),
            onBackground = Color(0xFFE2E8F0)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            if (activity != null) {
                val window = activity.window
                window.statusBarColor = colorScheme.surfaceVariant.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
