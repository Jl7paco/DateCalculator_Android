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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
        // 顶栏 (36dp 高度, 15sp 标题, 无括号简洁名称)
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
                    text = LanguageUtils.getString("tab_age", uiState.appLanguage),
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

        // 1. 出生日期选择卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showBirthDatePicker = true },
            shape = RoundedCornerShape(16.dp),
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
                            text = LanguageUtils.getString("select_birth_date", uiState.appLanguage),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val birthFormatted = DateCalculatorUtils.formatDateWithWeek(birthDate)
                        Text(
                            text = birthFormatted,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
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

        Spacer(modifier = Modifier.height(14.dp))

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
                val lang = uiState.appLanguage
                val ageText = when (lang) {
                    AppLanguage.ENGLISH -> "${ageResult.years} yrs ${ageResult.months} mos ${ageResult.days} days"
                    AppLanguage.JAPANESE -> "${ageResult.years} 歳 ${ageResult.months} ヶ月 ${ageResult.days} 日"
                    AppLanguage.KOREAN -> "${ageResult.years} 세 ${ageResult.months} 개월 ${ageResult.days} 일"
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
                    SuggestionChip(
                        onClick = {},
                        shape = CircleShape,
                        label = { Text("$zodiacSignLabel: ${ageResult.zodiac}", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
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
                                Text("${ageResult.daysToNextBirthday} ${LanguageUtils.getString("days_unit", lang)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
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
                            val copyText = "出生日期: ${DateCalculatorUtils.formatDate(birthDate)} | 精确年龄: ${ageResult.years}岁${ageResult.months}个月${ageResult.days}天 (共 ${ageResult.totalDays} 天) | 生肖星座: ${ageResult.zodiac}年 ${ageResult.constellationEmoji}${ageResult.constellation}"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("AgeResult", copyText))
                            Toast.makeText(context, "年龄计算结果已复制", Toast.LENGTH_SHORT).show()
                        },
                        contentDescription = "复制结果",
                        modifier = Modifier.weight(1f)
                    )

                    NeumorphicIconButton(
                        icon = Icons.Default.Share,
                        onClick = {
                            val shareText = "我的年龄档案:\n出生日期: ${DateCalculatorUtils.formatDate(birthDate)}\n周岁: ${ageResult.years}岁 ${ageResult.months}个月 ${ageResult.days}天\n生肖: ${ageResult.zodiac} | 星座: ${ageResult.constellationEmoji}${ageResult.constellation}\n已陪伴这个世界: ${ageResult.totalDays} 天 (${ageResult.totalWeeks} 周)"
                            ShareUtils.shareText(context, "我的精准年龄档案", shareText)
                        },
                        contentDescription = "分享长卡",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. 星座专属每日运势 Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicExtruded(shape = RoundedCornerShape(20.dp), elevation = 5.dp)
                .background(NeumorphicBg, shape = RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${fortune.emoji} ${fortune.constellation} (${fortune.dateRange}) 专属运势", fontWeight = FontWeight.Bold, color = NeumorphicAccent, fontSize = 14.sp)
                    }

                    Row {
                        repeat(fortune.starRating) {
                            Text("⭐", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = fortune.summary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = NeumorphicTextPrimary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("幸运数字: ${fortune.luckyNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeumorphicAccent)
                    Text("幸运颜色: ${fortune.luckyColor}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeumorphicAccent)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
