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
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.paco.datecalculator.data.AppLanguage
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
import me.paco.datecalculator.util.LanguageUtils
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
    val isDark = LocalDarkTheme.current
    val lang = uiState.appLanguage

    var showBirthDatePicker by remember { mutableStateOf(false) }
    var showTargetDatePicker by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    var targetBaseDate by remember { mutableStateOf(LocalDate.now()) }

    val birthDate = uiState.selectedBirthDate
    val ageResult = LunarCalendarUtils.calculateExactAge(birthDate, targetBaseDate)

    if (showBirthDatePicker) {
        DatePickerModal(
            selectedDate = birthDate,
            onDateSelected = { d ->
                viewModel.updateBirthDate(d)
                showBirthDatePicker = false
            },
            onDismiss = { showBirthDatePicker = false }
        )
    }

    if (showTargetDatePicker) {
        DatePickerModal(
            selectedDate = targetBaseDate,
            onDateSelected = { d ->
                targetBaseDate = d
                showTargetDatePicker = false
            },
            onDismiss = { showTargetDatePicker = false }
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
        // 顶栏 (36dp 高度, 15sp 标题)
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
                    text = LanguageUtils.getString("tab_age", lang),
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
                        contentDescription = LanguageUtils.getString("history_title", lang),
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
                        contentDescription = LanguageUtils.getString("settings_title", lang),
                        tint = NeumorphicAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1. 选择出生日期与目标基准日期双面板
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphicExtruded(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                    .background(NeumorphicBg, shape = RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showBirthDatePicker = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = NeumorphicAccent,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Column {
                            Text(
                                text = LanguageUtils.getString("select_birth_date", lang),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = DateCalculatorUtils.formatDateWithWeek(birthDate),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeumorphicTextPrimary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = "选择日期",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphicExtruded(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                    .background(NeumorphicBg, shape = RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showTargetDatePicker = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = NeumorphicAccent,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Column {
                            val targetLabel = when (lang) {
                                AppLanguage.ENGLISH -> "Target Date"
                                AppLanguage.JAPANESE -> "基準日"
                                AppLanguage.KOREAN -> "기준 날짜"
                                AppLanguage.TRADITIONAL_CHINESE -> "選擇目標基準日期"
                                else -> "选择目标基准日期"
                            }
                            Text(
                                text = targetLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = DateCalculatorUtils.formatDateWithWeek(targetBaseDate),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeumorphicTextPrimary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = "选择日期",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. 年龄计算核心结果卡片 (周岁 / 生辰 / 存活天数)
        val resultCardShape = RoundedCornerShape(22.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicExtruded(shape = resultCardShape, elevation = 6.dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f), shape = resultCardShape)
                .border(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f), shape = resultCardShape)
                .clip(resultCardShape)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val ageText = when (lang) {
                    AppLanguage.ENGLISH -> "${ageResult.years} yrs ${ageResult.months} mos ${ageResult.days} days"
                    AppLanguage.JAPANESE -> "${ageResult.years} 歳 ${ageResult.months} ヶ月 ${ageResult.days} 日"
                    AppLanguage.KOREAN -> "${ageResult.years} 세 ${ageResult.months} 개월 ${ageResult.days} 일"
                    AppLanguage.TRADITIONAL_CHINESE -> "${ageResult.years} 歲 ${ageResult.months} 個月 ${ageResult.days} 天"
                    else -> "${ageResult.years} 岁 ${ageResult.months} 个月 ${ageResult.days} 天"
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = LanguageUtils.getString("exact_age", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 周岁大字
                Text(
                    text = ageText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val zodiacSignLabel = LanguageUtils.getString("zodiac_sign", lang)
                    val localizedZodiac = LanguageUtils.getLocalizedZodiac(ageResult.zodiac, lang)
                    SuggestionChip(
                        onClick = {},
                        shape = CircleShape,
                        label = { Text("$zodiacSignLabel: $localizedZodiac", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )

                    SuggestionChip(
                        onClick = {},
                        shape = CircleShape,
                        label = { Text("${ageResult.constellationEmoji} ${ageResult.constellation}", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 四格子面板：已生活总天数、总月数、总周数、下一个生日倒计时
                val gridShape = RoundedCornerShape(16.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicInset(shape = gridShape, elevation = 3.dp)
                        .background(NeumorphicSunkenBg, shape = gridShape)
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(LanguageUtils.getString("total_days", lang), fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.6f))
                                Text("${ageResult.totalDays} ${LanguageUtils.getString("days_unit", lang)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(LanguageUtils.getString("months_unit", lang), fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.6f))
                                Text("${ageResult.totalMonths} ${LanguageUtils.getString("months_unit", lang)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(LanguageUtils.getString("total_weeks", lang), fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.6f))
                                Text("${ageResult.totalWeeks} ${LanguageUtils.getString("weeks_unit", lang)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent)
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(LanguageUtils.getString("next_birthday_days", lang), fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.6f))
                                Text("${ageResult.daysToNextBirthday} ${LanguageUtils.getString("days_unit", lang)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeumorphicIconButton(
                        icon = Icons.Default.ContentCopy,
                        onClick = {
                            val copyText = "Exact Age: ${ageResult.years} yrs ${ageResult.months} mos ${ageResult.days} days | Total Days: ${ageResult.totalDays}"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("AgeResult", copyText))
                            Toast.makeText(context, "Age result copied!", Toast.LENGTH_SHORT).show()
                        },
                        contentDescription = "Copy Age Result",
                        modifier = Modifier.weight(1f)
                    )

                    NeumorphicIconButton(
                        icon = Icons.Default.Share,
                        onClick = {
                            val shareText = "Exact Age Record:\nBirth Date: ${DateCalculatorUtils.formatDate(birthDate)}\nAge: ${ageResult.years} yrs ${ageResult.months} mos ${ageResult.days} days\nTotal Lived: ${ageResult.totalDays} days (${ageResult.totalWeeks} weeks)"
                            ShareUtils.shareText(context, "Age Record", shareText)
                        },
                        contentDescription = "Share Age Record",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
