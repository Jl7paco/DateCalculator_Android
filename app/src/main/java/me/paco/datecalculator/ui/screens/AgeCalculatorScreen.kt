package me.paco.datecalculator.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.paco.datecalculator.R
import me.paco.datecalculator.ui.components.DatePickerModal
import me.paco.datecalculator.ui.components.HistoryOverlayDialog
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicIconButton
import me.paco.datecalculator.ui.components.NeumorphicSunkenBg
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import me.paco.datecalculator.ui.components.SettingsOverlayDialog
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LunarCalendarUtils
import me.paco.datecalculator.util.ShareUtils
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeCalculatorScreen(
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showBirthDatePicker by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val birthDate = uiState.selectedBirthDate
    val ageResult = remember(birthDate) {
        LunarCalendarUtils.calculateExactAge(birthDate)
    }

    val fortune = remember(birthDate) {
        LunarCalendarUtils.getDailyFortune(LocalDate.now(), ageResult.constellation)
    }

    if (showBirthDatePicker) {
        DatePickerModal(
            selectedDate = birthDate,
            onDateSelected = { newDate ->
                viewModel.updateBirthDate(newDate)
                showBirthDatePicker = false

                val detail = "精确年龄: ${ageResult.years}岁 ${ageResult.months}个月 ${ageResult.days}天 (共 ${ageResult.totalDays} 天)"
                viewModel.saveToHistory(
                    category = "年龄计算",
                    title = "出生日期: ${DateCalculatorUtils.formatDate(newDate)}",
                    detail = detail
                )
            },
            onDismiss = { showBirthDatePicker = false }
        )
    }

    HistoryOverlayDialog(
        visible = showHistoryDialog,
        onDismiss = { showHistoryDialog = false },
        viewModel = viewModel,
        uiState = uiState
    )

    SettingsOverlayDialog(
        visible = showSettingsDialog,
        onDismiss = { showSettingsDialog = false },
        viewModel = viewModel,
        uiState = uiState
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
    ) {
        // 顶栏 (36dp 高度, 15sp 标题, 右上角历史记录与设置图标)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Cake,
                    contentDescription = null,
                    tint = NeumorphicAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "年龄计算 (精准到天)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeumorphicTextPrimary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                        .background(NeumorphicBg, shape = CircleShape)
                        .clip(CircleShape)
                        .clickable { showHistoryDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "查看历史记录",
                        tint = NeumorphicAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                        .background(NeumorphicBg, shape = CircleShape)
                        .clip(CircleShape)
                        .clickable { showSettingsDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "打开设置",
                        tint = NeumorphicAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showBirthDatePicker = true },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                    Column {
                        Text(
                            text = "选择出生日期",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = DateCalculatorUtils.formatDateWithWeek(birthDate),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.EditCalendar,
                    contentDescription = "修改出生日期",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicExtruded(shape = RoundedCornerShape(22.dp), elevation = 6.dp)
                .background(NeumorphicBg, shape = RoundedCornerShape(22.dp))
                .border(1.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "当前精准年龄",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeumorphicAccent
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${ageResult.years} 岁 ${ageResult.months} 个月 ${ageResult.days} 天",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeumorphicTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                SuggestionChip(
                    onClick = {},
                    shape = CircleShape,
                    label = {
                        Text(
                            text = "🎉 距离下次生日还有 ${ageResult.daysToNextBirthday} 天 (${ageResult.nextBirthdayDate})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .neumorphicInset(shape = RoundedCornerShape(14.dp), elevation = 3.dp)
                                .background(NeumorphicSunkenBg, shape = RoundedCornerShape(14.dp))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("生存总天数", fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.6f))
                                Text("${ageResult.totalDays} 天", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .neumorphicInset(shape = RoundedCornerShape(14.dp), elevation = 3.dp)
                                .background(NeumorphicSunkenBg, shape = RoundedCornerShape(14.dp))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("生存总周数", fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.6f))
                                Text("${ageResult.totalWeeks} 周", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .neumorphicInset(shape = RoundedCornerShape(14.dp), elevation = 3.dp)
                                .background(NeumorphicSunkenBg, shape = RoundedCornerShape(14.dp))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("生肖属相", fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.6f))
                                Text("${ageResult.zodiac} 年", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .neumorphicInset(shape = RoundedCornerShape(14.dp), elevation = 3.dp)
                                .background(NeumorphicSunkenBg, shape = RoundedCornerShape(14.dp))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("星座", fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.6f))
                                Text("${ageResult.constellationEmoji} ${ageResult.constellation}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 2.dp)
                        .background(NeumorphicSunkenBg, shape = RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("✨ ${ageResult.constellation} 今日运势简评", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeumorphicAccent)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(fortune.summary, fontSize = 12.sp, color = NeumorphicTextPrimary, lineHeight = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val shareText = "精确年龄: ${ageResult.years}岁 ${ageResult.months}个月 ${ageResult.days}天 (共 ${ageResult.totalDays} 天) | 距离下次生日还有 ${ageResult.daysToNextBirthday} 天"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeumorphicIconButton(
                        icon = Icons.Default.Share,
                        contentDescription = "分享年龄结果",
                        onClick = {
                            ShareUtils.shareText(context, shareText)
                        }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    NeumorphicIconButton(
                        icon = Icons.Default.ContentCopy,
                        contentDescription = "复制年龄结果",
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("AgeResult", shareText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, context.getString(R.string.toast_copied), Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
