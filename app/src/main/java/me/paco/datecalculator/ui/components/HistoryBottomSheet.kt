package me.paco.datecalculator.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.paco.datecalculator.R
import me.paco.datecalculator.data.DarkThemeMode
import me.paco.datecalculator.ui.screens.HistoryScreen
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel

@Composable
fun HistoryOverlayDialog(
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
            val scrimColor = if (isDark) {
                Color.Black.copy(alpha = 0.55f)
            } else {
                Color(0xFFCBD5E1).copy(alpha = 0.40f)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scrimColor)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                val dialogShape = RoundedCornerShape(26.dp)

                // 弹窗专属 DropShadow 修饰符：彻底清除左上角向外发光的白色阴影，边框严丝合缝
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.86f)
                        .clickable(enabled = false) {}
                        .dialogDropShadow3D(shape = dialogShape)
                ) {
                    Surface(
                        shape = dialogShape,
                        color = NeumorphicBg,
                        border = BorderStroke(
                            1.dp,
                            if (isDark) Color.White.copy(alpha = 0.12f) else NeumorphicAccent.copy(alpha = 0.20f)
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = NeumorphicAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.label_history),
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
                                        contentDescription = "关闭",
                                        tint = NeumorphicAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            HistoryScreen(viewModel = viewModel, uiState = uiState)
                        }
                    }
                }
            }
        }
    }
}
