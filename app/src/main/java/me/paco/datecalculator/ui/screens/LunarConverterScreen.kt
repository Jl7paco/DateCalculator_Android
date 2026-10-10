package me.paco.datecalculator.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.paco.datecalculator.R
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.ui.components.DatePickerModal
import me.paco.datecalculator.ui.components.HistoryOverlayDialog
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicIconButton
import me.paco.datecalculator.ui.components.NeumorphicSegmentedRow
import me.paco.datecalculator.ui.components.QuickDateChips
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LunarConverterScreen(
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Convert Mode: 0 = 公历转农历, 1 = 农历转公历
    var convertMode by remember { mutableIntStateOf(0) }

    // State for Solar -> Lunar
    var solarDate by remember { mutableStateOf(LocalDate.now()) }
    var showSolarPicker by remember { mutableStateOf(false) }

    // State for Lunar -> Solar
    val todayLunar = remember { LunarCalendarUtils.solarToLunar(LocalDate.now()) }
    var lunarRefSolarDate by remember { mutableStateOf(LocalDate.now()) }
    var showLunarRefPicker by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    var lunarYearInput by remember { mutableStateOf(todayLunar.year.toString()) }
    var lunarMonth by remember { mutableIntStateOf(todayLunar.month) }
    var lunarDay by remember { mutableIntStateOf(todayLunar.day) }
    var isLeapMonth by remember { mutableStateOf(todayLunar.isLeapMonth) }

    var showLunarResult by remember { mutableStateOf(true) }

    val cardScale = remember { Animatable(1.0f) }
    val cardAlpha = remember { Animatable(1.0f) }
    var triggerBounce by remember { mutableIntStateOf(0) }

    LaunchedEffect(triggerBounce) {
        if (triggerBounce > 0) {
            launch {
                cardScale.animateTo(1.04f, animationSpec = tween(100))
                cardScale.animateTo(1.00f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            }
            launch {
                cardAlpha.snapTo(0.4f)
                cardAlpha.animateTo(1.0f, animationSpec = tween(250))
            }
        }
    }

    val lang = uiState.appLanguage

    if (showSolarPicker) {
        DatePickerModal(
            selectedDate = solarDate,
            onDateSelected = { date ->
                solarDate = date
                showLunarResult = true
                val lunarResult = LunarCalendarUtils.solarToLunar(date)
                val almanacYiJi = LunarCalendarUtils.getAlmanacYiJi(date)
                val yiStr = almanacYiJi.yiList.joinToString("·")
                val jiStr = almanacYiJi.jiList.joinToString("·")

                viewModel.saveToHistory(
                    category = "农历公历",
                    title = "${lunarResult.lunarMonthName}${lunarResult.lunarDayName}",
                    detail = "公历 ${date} ➔ ${lunarResult.getFullDescription()} | 宜: $yiStr | 忌: $jiStr"
                )
            },
            onDismiss = { showSolarPicker = false }
        )
    }

    if (showLunarRefPicker) {
        DatePickerModal(
            selectedDate = lunarRefSolarDate,
            onDateSelected = { date ->
                lunarRefSolarDate = date
                val l = LunarCalendarUtils.solarToLunar(date)
                lunarYearInput = l.year.toString()
                lunarMonth = l.month
                lunarDay = l.day
                isLeapMonth = l.isLeapMonth
                showLunarResult = true
                showLunarRefPicker = false
            },
            onDismiss = { showLunarRefPicker = false }
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
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = NeumorphicAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.label_lunar_conv_title),
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

            Spacer(modifier = Modifier.height(10.dp))

            NeumorphicSegmentedRow(
                items = listOf(stringResource(R.string.label_solar_to_lunar), stringResource(R.string.label_lunar_to_solar)),
                selectedIndex = convertMode,
                onIndexSelected = {
                    convertMode = it
                    showLunarResult = true
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (convertMode == 0) {
                // ================= 公历转农历 =================
                Text(
                    text = stringResource(R.string.label_select_solar_date),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeumorphicTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                val inputShape = RoundedCornerShape(18.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .neumorphicInset(shape = inputShape, elevation = 4.dp)
                        .border(1.dp, NeumorphicAccent.copy(alpha = 0.20f), shape = inputShape)
                        .background(NeumorphicBg, shape = inputShape)
                        .clip(inputShape)
                        .clickable { showSolarPicker = true }
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
                                text = "${LanguageUtils.getString("base_date", lang)}: ${DateCalculatorUtils.formatDateWithWeek(solarDate, lang)}",
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

                Spacer(modifier = Modifier.height(8.dp))

                QuickDateChips(
                    selectedDate = solarDate,
                    onSelectDate = { date ->
                        solarDate = date
                        showLunarResult = true
                        triggerBounce++
                        val lunarResult = LunarCalendarUtils.solarToLunar(date)
                        val almanacYiJi = LunarCalendarUtils.getAlmanacYiJi(date)
                        val yiStr = almanacYiJi.yiList.joinToString("·")
                        val jiStr = almanacYiJi.jiList.joinToString("·")

                        viewModel.saveToHistory(
                            category = "农历公历",
                            title = "${lunarResult.lunarMonthName}${lunarResult.lunarDayName}",
                            detail = "公历 ${date} ➔ ${lunarResult.getFullDescription()} | 宜: $yiStr | 忌: $jiStr"
                        )
                    },
                    language = lang
                )

                Spacer(modifier = Modifier.height(14.dp))

                val lunarResult = LunarCalendarUtils.solarToLunar(solarDate)
                val almanacYiJi = LunarCalendarUtils.getAlmanacYiJi(solarDate)
                val (solarConstName, solarConstEmoji) = LunarCalendarUtils.getConstellationInfo(solarDate)
                val solarFortune = LunarCalendarUtils.getDailyFortune(solarDate, solarConstName)

                val cardShape24 = RoundedCornerShape(24.dp)

                AnimatedVisibility(
                    visible = showLunarResult,
                    enter = fadeIn(animationSpec = tween(150)) + expandVertically(animationSpec = tween(150)),
                    exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = tween(150))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neumorphicExtruded(shape = cardShape24, elevation = 6.dp)
                            .background(NeumorphicBg, shape = cardShape24)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), shape = cardShape24)
                            .border(1.2.dp, NeumorphicAccent.copy(alpha = 0.35f), shape = cardShape24)
                            .clip(cardShape24)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.label_lunar_result),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeumorphicAccent
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            val leapTag = if (lunarResult.isLeapMonth) "闰" else ""
                            Text(
                                text = "$leapTag${lunarResult.lunarMonthName}${lunarResult.lunarDayName}",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeumorphicTextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${lunarResult.ganZhiYear} (${lunarResult.zodiac}) 年",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeumorphicTextPrimary.copy(alpha = 0.85f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val localizedConstellation = LanguageUtils.getLocalizedConstellation(solarConstName, lang)
                                SuggestionChip(
                                    onClick = {},
                                    shape = CircleShape,
                                    label = { Text(localizedConstellation, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                                )

                                if (lunarResult.solarTerm.isNotEmpty()) {
                                    val termIcon = LunarCalendarUtils.getSolarTermIcon(lunarResult.solarTerm)
                                    SuggestionChip(
                                        onClick = {},
                                        shape = CircleShape,
                                        label = { Text("$termIcon ${lunarResult.solarTerm}", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "✨ 运势: ${solarFortune.summary}",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeumorphicTextPrimary.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(10.dp))

                            val isDark = LocalDarkTheme.current
                            val yiChipBg = if (isDark) Color(0xFF065F46).copy(alpha = 0.55f) else Color(0xFFD1FAE5)
                            val yiChipBorder = if (isDark) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFF10B981).copy(alpha = 0.35f)
                            val yiChipText = if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857)

                            val jiChipBg = if (isDark) Color(0xFF334155).copy(alpha = 0.65f) else Color(0xFFF1F5F9)
                            val jiChipBorder = if (isDark) Color(0xFF94A3B8).copy(alpha = 0.5f) else Color(0xFF94A3B8).copy(alpha = 0.35f)
                            val jiChipText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)

                            // 老黄历宜忌
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("宜", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                FlowRow(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    almanacYiJi.yiList.forEach { yiItem ->
                                        val translatedYi = LanguageUtils.getLocalizedAlmanacItem(yiItem, lang)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(yiChipBg)
                                                .border(0.5.dp, yiChipBorder, shape = RoundedCornerShape(6.dp))
                                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                        ) {
                                            Text(translatedYi, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = yiChipText)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF64748B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("忌", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                FlowRow(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    almanacYiJi.jiList.forEach { jiItem ->
                                        val translatedJi = LanguageUtils.getLocalizedAlmanacItem(jiItem, lang)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(jiChipBg)
                                                .border(0.5.dp, jiChipBorder, shape = RoundedCornerShape(6.dp))
                                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                        ) {
                                            Text(translatedJi, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = jiChipText)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val resultShareText = "公历 $solarDate ➔ 农历 ${lunarResult.getFullDescription()}"
                                val copyToastText = stringResource(R.string.toast_copied)

                                NeumorphicIconButton(
                                    icon = Icons.Default.Share,
                                    contentDescription = "分享农历转换结果",
                                    onClick = {
                                        ShareUtils.shareText(context, resultShareText)
                                    }
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                NeumorphicIconButton(
                                    icon = Icons.Default.ContentCopy,
                                    contentDescription = "复制农历转换结果",
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("LunarResult", resultShareText)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, copyToastText, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // ================= 农历转公历 =================
                Text(
                    text = stringResource(R.string.label_select_lunar_date),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeumorphicTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

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
                                value = lunarYearInput,
                                onValueChange = { lunarYearInput = it.take(4) },
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
                                        .clickable { if (lunarMonth > 1) lunarMonth-- },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("-", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                val monthNames = listOf("正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "腊")
                                val monthName = monthNames.getOrElse(lunarMonth - 1) { "${lunarMonth}" }
                                Text("$monthName 月", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicTextPrimary)

                                Spacer(modifier = Modifier.width(12.dp))

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(NeumorphicAccent)
                                        .clickable { if (lunarMonth < 12) lunarMonth++ },
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
                                        .clickable { if (lunarDay > 1) lunarDay-- },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("-", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                val dayNames = listOf("初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十", "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十")
                                val dayName = dayNames.getOrElse(lunarDay - 1) { "${lunarDay}" }
                                Text(dayName, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicTextPrimary)

                                Spacer(modifier = Modifier.width(12.dp))

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(NeumorphicAccent)
                                        .clickable { if (lunarDay < 30) lunarDay++ },
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

                            Switch(
                                checked = isLeapMonth,
                                onCheckedChange = { isLeapMonth = it }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val year = lunarYearInput.toIntOrNull() ?: todayLunar.year
                val convertedSolarDate = LunarCalendarUtils.lunarToSolar(year, lunarMonth, lunarDay, isLeapMonth)

                if (convertedSolarDate != null) {
                    val cardShape24 = RoundedCornerShape(24.dp)
                    val (lunarConstName, lunarConstEmoji) = LunarCalendarUtils.getConstellationInfo(convertedSolarDate)
                    val lunarFortune = LunarCalendarUtils.getDailyFortune(convertedSolarDate, lunarConstName)
                    val convertedAlmanac = LunarCalendarUtils.getAlmanacYiJi(convertedSolarDate)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neumorphicExtruded(shape = cardShape24, elevation = 6.dp)
                            .background(NeumorphicBg, shape = cardShape24)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), shape = cardShape24)
                            .border(1.2.dp, NeumorphicAccent.copy(alpha = 0.35f), shape = cardShape24)
                            .clip(cardShape24)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.label_solar_result),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeumorphicAccent
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = DateCalculatorUtils.formatDate(convertedSolarDate, lang),
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeumorphicTextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = DateCalculatorUtils.formatDateWithWeek(convertedSolarDate, lang),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeumorphicTextPrimary.copy(alpha = 0.85f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val localizedConstellation = LanguageUtils.getLocalizedConstellation(lunarConstName, lang)
                            SuggestionChip(
                                onClick = {},
                                shape = CircleShape,
                                label = { Text(localizedConstellation, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "✨ 运势: ${lunarFortune.summary}",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeumorphicTextPrimary.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(10.dp))

                            val isDark = LocalDarkTheme.current
                            val yiChipBg = if (isDark) Color(0xFF065F46).copy(alpha = 0.55f) else Color(0xFFD1FAE5)
                            val yiChipBorder = if (isDark) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFF10B981).copy(alpha = 0.35f)
                            val yiChipText = if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857)

                            val jiChipBg = if (isDark) Color(0xFF334155).copy(alpha = 0.65f) else Color(0xFFF1F5F9)
                            val jiChipBorder = if (isDark) Color(0xFF94A3B8).copy(alpha = 0.5f) else Color(0xFF94A3B8).copy(alpha = 0.35f)
                            val jiChipText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)

                            // 老黄历宜忌
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("宜", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                FlowRow(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    convertedAlmanac.yiList.forEach { yiItem ->
                                        val translatedYi = LanguageUtils.getLocalizedAlmanacItem(yiItem, lang)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(yiChipBg)
                                                .border(0.5.dp, yiChipBorder, shape = RoundedCornerShape(6.dp))
                                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                        ) {
                                            Text(translatedYi, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = yiChipText)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF64748B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("忌", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                FlowRow(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    convertedAlmanac.jiList.forEach { jiItem ->
                                        val translatedJi = LanguageUtils.getLocalizedAlmanacItem(jiItem, lang)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(jiChipBg)
                                                .border(0.5.dp, jiChipBorder, shape = RoundedCornerShape(6.dp))
                                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                        ) {
                                            Text(translatedJi, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = jiChipText)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val resultShareText = "农历 ${year}年$lunarMonth 月$lunarDay 日 ➔ 公历 $convertedSolarDate"
                                val copyToastText = stringResource(R.string.toast_copied)

                                NeumorphicIconButton(
                                    icon = Icons.Default.Share,
                                    contentDescription = "分享公历转换结果",
                                    onClick = {
                                        ShareUtils.shareText(context, resultShareText)
                                    }
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                NeumorphicIconButton(
                                    icon = Icons.Default.ContentCopy,
                                    contentDescription = "复制公历转换结果",
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("SolarResult", resultShareText)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, copyToastText, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
