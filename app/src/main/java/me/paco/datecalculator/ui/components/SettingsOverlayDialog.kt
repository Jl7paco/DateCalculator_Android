package me.paco.datecalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.paco.datecalculator.data.DarkThemeMode
import me.paco.datecalculator.ui.screens.SettingsScreen
import me.paco.datecalculator.ui.theme.DateCalculatorTheme
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.LanguageUtils

@Composable
fun SettingsOverlayDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState
) {
    if (visible) {
        val isDark = when (uiState.darkThemeMode) {
            DarkThemeMode.SYSTEM -> isSystemInDarkTheme()
            DarkThemeMode.ON -> true
            DarkThemeMode.OFF -> false
        }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            DateCalculatorTheme(
                darkTheme = isDark,
                themePreset = uiState.themePreset,
                customPrimaryColorHex = uiState.customPrimaryColorHex
            ) {
                // 柔和半透明暗化背景，避免过度涂黑
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.28f))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    val dialogShape = RoundedCornerShape(24.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .fillMaxHeight(0.86f)
                            .clickable(enabled = false) {}
                            .neumorphicExtruded(shape = dialogShape, elevation = 3.5.dp)
                            .background(NeumorphicBg, shape = dialogShape)
                            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f), shape = dialogShape)
                            .clip(dialogShape)
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null,
                                        tint = NeumorphicAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = LanguageUtils.getString("settings_title", uiState.appLanguage),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NeumorphicTextPrimary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                                        .background(NeumorphicBg, shape = CircleShape)
                                        .clip(CircleShape)
                                        .clickable { onDismiss() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = NeumorphicAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            SettingsScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                showTitleHeader = false,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
