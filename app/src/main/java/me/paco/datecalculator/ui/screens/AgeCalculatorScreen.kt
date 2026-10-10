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
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.paco.datecalculator.R
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.ui.components.DatePickerModal
import me.paco.datecalculator.ui.components.HistoryOverlayDialog
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicIconButton
import me.paco.datecalculator.ui.components.NeumorphicSunkenBg
import me.paco.datecalculator.ui.components.NeumorphicSwitch
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import me.paco.datecalculator.ui.components.SettingsOverlayDialog
import me.paco.datecalculator.ui.components.SolarLunarSwitch
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

    var birthCalendarType by remember { mutableIntStateOf(0) } // 0 = 公历出生, 1 = 农历出生
    var birthDate by remember { mutableStateOf(uiState.selectedBirthDate) }
    var targetDate by remember { mutableStateOf(LocalDate.now()) }

    var showBirthDatePicker by remember { mutableStateOf(false) }
    var showTargetDatePicker by remember { mutableStateOf(false) }

    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // 农历出生日期选择状态
    val initialLunar = remember { LunarCalendarUtils.solarToLunar(birthDate) }
    var birthLunarYearInput by remember { mutableStateOf(initialLunar.year.toString()) }
    var birthLunarMonth by remember { mutableIntStateOf(initialLunar.month) }
    var birthLunarDay by remember { mutableIntStateOf(initialLunar.day) }
    var birthIsLeapMonth by remember { mutableStateOf(initialLunar.isLeapMonth) }

    val effectiveBirthDate: LocalDate = if (birthCalendarType == 0) {
        birthDate
    } else {
        val lYear = birthLunarYearInput.toIntOrNull() ?: initialLunar.year
        LunarCalendarUtils.lunarToSolar(lYear, birthLunarMonth, birthLunarDay, birthIsLeapMonth) ?: birthDate
    }

    val ageResult = remember(effectiveBirthDate, targetDate) {
        LunarCalendarUtils.calculateExactAge(effectiveBirthDate, targetDate)
    }

    if (showBirthDatePicker) {
        DatePickerModal(
            selectedDate = birthDate,
            onDateSelected = { date ->
                birthDate = date
                viewModel.updateBirthDate(date)
                showBirthDatePicker = false
            },
            onDismiss = { showBirthDatePicker = false }
        )
    }

    if (showTargetDatePicker) {
        DatePickerModal(
            selectedDate = targetDate,
            onDateSelected = { date ->
                targetDate = date
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

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
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

            // 1. 选择出生日期面板 (支持公历 / 农历拨动开关切换)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageUtils.getString("select_birth_date", lang),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeumorphicTextPrimary
                    )

                    SolarLunarSwitch(
                        isSolar = (birthCalendarType == 0),
                        onCalendarTypeChanged = { isSolar ->
                            birthCalendarType = if (isSolar) 0 else 1
                        },
                        language = lang
                    )
                }

                if (birthCalendarType == 0) {
                    // 公历出生日期选择框
                    val inputShape = RoundedCornerShape(18.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .neumorphicInset(shape = inputShape, elevation = 4.dp)
                            .border(1.dp, NeumorphicAccent.copy(alpha = 0.20f), shape = inputShape)
                            .background(NeumorphicBg, shape = inputShape)
                            .clip(inputShape)
                            .clickable { showBirthDatePicker = true }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
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
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = "${LanguageUtils.getString("birth_date_label", lang)}: ${DateCalculatorUtils.formatDateWithWeek(birthDate, lang)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeumorphicTextPrimary
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = "Select Date",
                                tint = NeumorphicAccent.copy(alpha = 0.8f)
                            )
                        }
                    }
                } else {
                    // 农历出生日期选择面板
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neumorphicInset(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                            .background(NeumorphicBg, shape = RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("农历年份", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeumorphicAccent)

                                OutlinedTextField(
                                    value = birthLunarYearInput,
                                    onValueChange = { birthLunarYearInput = it.take(4) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.width(100.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.label_lunar_month), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeumorphicAccent)

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(NeumorphicAccent)
                                            .clickable { if (birthLunarMonth > 1) birthLunarMonth-- },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    val monthNames = listOf("正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "腊")
                                    val monthName = monthNames.getOrElse(birthLunarMonth - 1) { "$birthLunarMonth" }
                                    Text("$monthName 月", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicTextPrimary)

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(NeumorphicAccent)
                                            .clickable { if (birthLunarMonth < 12) birthLunarMonth++ },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.label_lunar_day), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeumorphicAccent)

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(NeumorphicAccent)
                                            .clickable { if (birthLunarDay > 1) birthLunarDay-- },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("-", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    val dayNames = listOf("初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十", "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十")
                                    val dayName = dayNames.getOrElse(birthLunarDay - 1) { "$birthLunarDay" }
                                    Text(dayName, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicTextPrimary)

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(NeumorphicAccent)
                                            .clickable { if (birthLunarDay < 30) birthLunarDay++ },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("是否闰月", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)

                                NeumorphicSwitch(
                                    checked = birthIsLeapMonth,
                                    onCheckedChange = { birthIsLeapMonth = it }
                                )
                            }
                        }
                    }
                }

                // 目标基准日期选择框
                Text(
                    text = LanguageUtils.getString("select_target_date", lang),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeumorphicTextPrimary
                )

                val targetInputShape = RoundedCornerShape(18.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .neumorphicInset(shape = targetInputShape, elevation = 4.dp)
                        .border(1.dp, NeumorphicAccent.copy(alpha = 0.20f), shape = targetInputShape)
                        .background(NeumorphicBg, shape = targetInputShape)
                        .clip(targetInputShape)
                        .clickable { showTargetDatePicker = true }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
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
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "${LanguageUtils.getString("target_date", lang)}: ${DateCalculatorUtils.formatDateWithWeek(targetDate, lang)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeumorphicTextPrimary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            contentDescription = "Select Date",
                            tint = NeumorphicAccent.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. 年龄计算核心结果卡片 (直接显示无动画)
            val resultCardShape = RoundedCornerShape(22.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphicExtruded(shape = resultCardShape, elevation = 6.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f), shape = resultCardShape)
                    .clip(resultCardShape)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val ageText = when (lang.getEffectiveLanguage()) {
                        AppLanguage.ENGLISH -> "${ageResult.years} yrs ${ageResult.months} mos ${ageResult.days} days"
                        AppLanguage.JAPANESE -> "${ageResult.years} 歳 ${ageResult.months} ヶ月 ${ageResult.days} 日"
                        AppLanguage.KOREAN -> "${ageResult.years} 세 ${ageResult.months} 개월 ${ageResult.days} 일"
                        AppLanguage.TRADITIONAL_CHINESE -> "${ageResult.years} 歲 ${ageResult.months} 個月 ${ageResult.days} 天"
                        else -> "${ageResult.years} 岁 ${ageResult.months} 个月 ${ageResult.days} 天"
                    }

                    Text(
                        text = LanguageUtils.getString("exact_age", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = ageText,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val zodiacSignLabel = LanguageUtils.getString("zodiac_sign", lang)
                        val localizedZodiac = LanguageUtils.getLocalizedZodiac(ageResult.zodiac, lang)
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text("$zodiacSignLabel: $localizedZodiac", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        val localizedConstellation = LanguageUtils.getLocalizedConstellation(ageResult.constellation, lang)
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text("${ageResult.constellationEmoji} $localizedConstellation", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 宫格数据解构
                    val cardBg = NeumorphicSunkenBg
                    val itemShape = RoundedCornerShape(16.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neumorphicInset(shape = itemShape, elevation = 3.dp)
                            .background(cardBg, shape = itemShape)
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(LanguageUtils.getString("total_days", lang), fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.7f))
                                    Text("${ageResult.totalDays} ${LanguageUtils.getString("days_unit", lang)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeumorphicAccent)
                                }

                                Column {
                                    Text(LanguageUtils.getString("months_unit", lang), fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.7f))
                                    Text("${ageResult.totalMonths} ${LanguageUtils.getString("months_unit", lang)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeumorphicAccent)
                                }
                            }

                            HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.1f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(LanguageUtils.getString("total_weeks", lang), fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.7f))
                                    Text("${ageResult.totalWeeks} ${LanguageUtils.getString("weeks_unit", lang)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeumorphicAccent)
                                }

                                Column {
                                    Text(LanguageUtils.getString("next_birthday_days", lang), fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.7f))
                                    Text("${ageResult.daysToNextBirthday} ${LanguageUtils.getString("days_unit", lang)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeumorphicAccent)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 复制与分享按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val shareAgeText = "${LanguageUtils.getString("exact_age", lang)}: $ageText (${LanguageUtils.getString("total_days", lang)}: ${ageResult.totalDays})"

                        NeumorphicIconButton(
                            icon = Icons.Default.ContentCopy,
                            contentDescription = "Copy Age Result",
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("AgeResult", shareAgeText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, LanguageUtils.getString("confirm", lang), Toast.LENGTH_SHORT).show()
                            }
                        )

                        NeumorphicIconButton(
                            icon = Icons.Default.Share,
                            contentDescription = "Share Age Record",
                            onClick = {
                                ShareUtils.shareText(context, shareAgeText)
                            }
                        )
                    }
                }
            }
        }
    }
}
