package me.paco.datecalculator.ui.components

import androidx.compose.foundation.background
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .fillMaxHeight(0.88f)
                        .neumorphicExtruded(shape = RoundedCornerShape(24.dp), elevation = 8.dp)
                        .background(NeumorphicBg, shape = RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
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
                                    text = "设置",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeumorphicTextPrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                                    .background(NeumorphicBg, shape = CircleShape)
                                    .clip(CircleShape)
                                    .clickable { onDismiss() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "关闭",
                                    tint = NeumorphicAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        SettingsScreen(
                            viewModel = viewModel,
                            uiState = uiState,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
