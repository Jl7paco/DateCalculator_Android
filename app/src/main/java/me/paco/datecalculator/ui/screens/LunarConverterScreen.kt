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
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.paco.datecalculator.R
import me.paco.datecalculator.ui.components.DatePickerModal
import me.paco.datecalculator.ui.components.FireworksAnimation
import me.paco.datecalculator.ui.components.HistoryOverlayDialog
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicCustomPopup
import me.paco.datecalculator.ui.components.NeumorphicIconButton
import me.paco.datecalculator.ui.components.NeumorphicSegmentedRow
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import me.paco.datecalculator.ui.components.QuickDateChips
import me.paco.datecalculator.ui.components.SettingsOverlayDialog
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.DateCalculatorUtils
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
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isDark = isSystemInDarkTheme()

    // Mode: 0 = 阳历转农历, 1 = 农历转阳历
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
                    fontWeight = FontWeight.Bold,
                    color = NeumorphicTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = cardScale.value
                            scaleY = cardAlpha.value
                        }
                        .clickable { showSolarPicker = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
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
                                    text = "选择公历日期",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val dateFormattedWithWeek = DateCalculatorUtils.formatDateWithWeek(solarDate)
                                Text(
                                    text = dateFormattedWithWeek,
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
                    }
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
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeumorphicTextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // 节气、节日与星座芯片
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SuggestionChip(
                                    onClick = {},
                                    shape = CircleShape,
                                    label = { Text("$solarConstEmoji $solarConstName", fontWeight = FontWeight.Bold) }
                                )

                                if (lunarResult.solarTerm.isNotEmpty()) {
                                    val termIcon = LunarCalendarUtils.getSolarTermIcon(lunarResult.solarTerm)
                                    SuggestionChip(
                                        onClick = {},
                                        shape = CircleShape,
                                        label = { Text("$termIcon ${lunarResult.solarTerm}", fontWeight = FontWeight.Bold) }
                                    )
                                }

                                if (lunarResult.festival.isNotEmpty()) {
                                    SuggestionChip(
                                        onClick = { viewModel.triggerFireworks() },
                                        shape = CircleShape,
                                        label = { Text(stringResource(R.string.label_traditional_festival, lunarResult.festival), fontWeight = FontWeight.Bold) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "✨ 运势: ${solarFortune.summary}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = NeumorphicTextPrimary.copy(alpha = 0.75f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            val almanacBoxShape = RoundedCornerShape(18.dp)

                            val yiBadgeBg = Color(0xFF10B981)
                            val yiBadgeText = Color.White
                            val yiChipBg = if (isDark) Color(0xFF065F46).copy(alpha = 0.7f) else Color(0xFFD1FAE5)
                            val yiChipBorder = Color(0xFF10B981).copy(alpha = 0.45f)
                            val yiChipText = if (isDark) Color(0xFF34D399) else Color(0xFF047857)

                            val jiBadgeBg = Color(0xFF64748B)
                            val jiBadgeText = Color.White
                            val jiChipBg = if (isDark) Color(0xFF334155).copy(alpha = 0.7f) else Color(0xFFF1F5F9)
                            val jiChipBorder = Color(0xFF94A3B8).copy(alpha = 0.35f)
                            val jiChipText = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .neumorphicInset(shape = almanacBoxShape, elevation = 3.dp)
                                    .border(1.dp, NeumorphicAccent.copy(alpha = 0.15f), shape = almanacBoxShape)
                                    .background(
                                        if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        else Color(0xFFE8EEF5),
                                        shape = almanacBoxShape
                                    )
                                    .padding(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(yiBadgeBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("宜", color = yiBadgeText, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        FlowRow(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            almanacYiJi.yiList.forEach { yiItem ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(yiChipBg)
                                                        .border(0.5.dp, yiChipBorder, shape = RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        text = yiItem,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = yiChipText
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.1f))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(jiBadgeBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("忌", color = jiBadgeText, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        FlowRow(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            almanacYiJi.jiList.forEach { jiItem ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(jiChipBg)
                                                        .border(0.5.dp, jiChipBorder, shape = RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        text = jiItem,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = jiChipText
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val yiText = almanacYiJi.yiList.joinToString("·")
                            val jiText = almanacYiJi.jiList.joinToString("·")
                            val clipText = "Solar ${solarDate} ➔ ${lunarResult.getFullDescription()} | 宜: $yiText | 忌: $jiText"

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NeumorphicIconButton(
                                    icon = Icons.Default.Share,
                                    contentDescription = "分享农历转换结果",
                                    onClick = {
                                        ShareUtils.shareText(context, clipText)
                                    }
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                NeumorphicIconButton(
                                    icon = Icons.Default.ContentCopy,
                                    contentDescription = "复制农历转换结果",
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("LunarDate", clipText))
                                        Toast.makeText(context, context.getString(R.string.toast_copied), Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }

            } else {
                // ================= 农历转公历 =================
                Text(
                    text = "参考公历日期选择",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeumorphicTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = cardScale.value
                            scaleY = cardAlpha.value
                        }
                        .clickable { showLunarRefPicker = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
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
                                    text = "快速对齐公历基准日",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val dateFormattedWithWeek = DateCalculatorUtils.formatDateWithWeek(lunarRefSolarDate)
                                Text(
                                    text = dateFormattedWithWeek,
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

                Text(
                    text = stringResource(R.string.label_select_lunar_date),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeumorphicTextPrimary
                )

                val parsedYear = lunarYearInput.toIntOrNull()

                Spacer(modifier = Modifier.height(4.dp))
                val hintText = if (parsedYear != null && parsedYear in 1900..2100) {
                    val ganZhiZodiacStr = LunarCalendarUtils.getYearGanZhiAndZodiac(parsedYear)
                    stringResource(R.string.label_gregorian_year_hint, ganZhiZodiacStr)
                } else {
                    "请输入公历年份 (例如: 2026)"
                }

                Text(
                    text = hintText,
                    style = MaterialTheme.typography.bodySmall,
                    color = NeumorphicTextPrimary.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .neumorphicInset(shape = RoundedCornerShape(18.dp), elevation = 4.dp)
                                .border(1.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = RoundedCornerShape(18.dp))
                                .background(NeumorphicBg, shape = RoundedCornerShape(18.dp))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.label_gregorian_year),
                                    fontSize = 11.sp,
                                    color = NeumorphicAccent,
                                    fontWeight = FontWeight.Bold
                                )
                                BasicTextField(
                                    value = lunarYearInput,
                                    onValueChange = { newValue ->
                                        if (newValue.isEmpty() || (newValue.length <= 4 && newValue.all { it.isDigit() })) {
                                            lunarYearInput = newValue
                                            showLunarResult = false
                                        }
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeumorphicTextPrimary
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        var monthExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .neumorphicInset(shape = RoundedCornerShape(18.dp), elevation = 4.dp)
                                    .border(1.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = RoundedCornerShape(18.dp))
                                    .background(NeumorphicBg, shape = RoundedCornerShape(18.dp))
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { monthExpanded = true }
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = stringResource(R.string.label_lunar_month), fontSize = 11.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                                        Text(text = LunarCalendarUtils.getLunarMonthName(lunarMonth), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                                    }
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = NeumorphicAccent)
                                }
                            }

                            NeumorphicCustomPopup(
                                expanded = monthExpanded,
                                onDismissRequest = { monthExpanded = false }
                            ) {
                                (1..12).forEach { m ->
                                    val isCurrent = (lunarMonth == m)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isCurrent) NeumorphicAccent.copy(alpha = 0.12f) else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                lunarMonth = m
                                                monthExpanded = false
                                                showLunarResult = false
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = LunarCalendarUtils.getLunarMonthName(m),
                                            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (isCurrent) NeumorphicAccent else NeumorphicTextPrimary,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        var dayExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .neumorphicInset(shape = RoundedCornerShape(18.dp), elevation = 4.dp)
                                    .border(1.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = RoundedCornerShape(18.dp))
                                    .background(NeumorphicBg, shape = RoundedCornerShape(18.dp))
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { dayExpanded = true }
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = stringResource(R.string.label_lunar_day), fontSize = 11.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                                        Text(text = LunarCalendarUtils.getLunarDayName(lunarDay), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                                    }
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = NeumorphicAccent)
                                }
                            }

                            NeumorphicCustomPopup(
                                expanded = dayExpanded,
                                onDismissRequest = { dayExpanded = false }
                            ) {
                                (1..30).forEach { d ->
                                    val isCurrent = (lunarDay == d)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isCurrent) NeumorphicAccent.copy(alpha = 0.12f) else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                lunarDay = d
                                                dayExpanded = false
                                                showLunarResult = false
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = LunarCalendarUtils.getLunarDayName(d),
                                            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (isCurrent) NeumorphicAccent else NeumorphicTextPrimary,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    val convertedSolarDate = if (parsedYear != null) {
                        LunarCalendarUtils.lunarToSolar(parsedYear, lunarMonth, lunarDay, isLeapMonth)
                    } else null

                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(58.dp)
                            .neumorphicExtruded(shape = RoundedCornerShape(18.dp), elevation = 5.dp)
                            .background(NeumorphicAccent, shape = RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                if (parsedYear == null) {
                                    Toast.makeText(context, "请先输入公历年份", Toast.LENGTH_SHORT).show()
                                } else {
                                    showLunarResult = true
                                    if (convertedSolarDate != null) {
                                        val solarStr = DateCalculatorUtils.formatDate(convertedSolarDate)
                                        val convertAlmanac = LunarCalendarUtils.getAlmanacYiJi(convertedSolarDate)
                                        val yiStr = convertAlmanac.yiList.joinToString("·")
                                        val jiStr = convertAlmanac.jiList.joinToString("·")

                                        viewModel.saveToHistory(
                                            category = "农历公历",
                                            title = solarStr,
                                            detail = "农历 ${parsedYear}年${LunarCalendarUtils.getLunarMonthName(lunarMonth)}${LunarCalendarUtils.getLunarDayName(lunarDay)} ➔ 公历 ${solarStr} | 宜: $yiStr | 忌: $jiStr"
                                        )
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("=", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }

                val currentLeapMonth = if (parsedYear != null) LunarCalendarUtils.getLeapMonth(parsedYear) else 0
                if (currentLeapMonth == lunarMonth && currentLeapMonth > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isLeapMonth,
                            onCheckedChange = {
                                isLeapMonth = it
                                showLunarResult = false
                            }
                        )
                        Text(stringResource(R.string.label_leap_month_check, LunarCalendarUtils.getLunarMonthName(lunarMonth)), color = NeumorphicTextPrimary)
                    }
                } else {
                    isLeapMonth = false
                }

                Spacer(modifier = Modifier.height(14.dp))

                val convertedSolarDate = if (parsedYear != null) {
                    LunarCalendarUtils.lunarToSolar(parsedYear, lunarMonth, lunarDay, isLeapMonth)
                } else null

                val cardShape24 = RoundedCornerShape(24.dp)

                AnimatedVisibility(
                    visible = showLunarResult && parsedYear != null,
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
                                text = stringResource(R.string.label_solar_result),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeumorphicAccent
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (convertedSolarDate != null && parsedYear != null) {
                                val solarStr = DateCalculatorUtils.formatDate(convertedSolarDate)
                                val descStr = DateCalculatorUtils.getDateDescription(convertedSolarDate)
                                val festivalStr = LunarCalendarUtils.solarToLunar(convertedSolarDate).festival
                                val convertAlmanac = LunarCalendarUtils.getAlmanacYiJi(convertedSolarDate)
                                val convertLunar = LunarCalendarUtils.solarToLunar(convertedSolarDate)
                                val (convertConstName, convertConstEmoji) = LunarCalendarUtils.getConstellationInfo(convertedSolarDate)
                                val convertFortune = LunarCalendarUtils.getDailyFortune(convertedSolarDate, convertConstName)

                                Text(
                                    text = solarStr,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeumorphicTextPrimary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "${convertLunar.ganZhiYear} (${convertLunar.zodiac}) 年",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeumorphicTextPrimary.copy(alpha = 0.9f)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // 节气、节日与星座芯片 (农历转公历同样完整展示)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    SuggestionChip(
                                        onClick = {},
                                        shape = CircleShape,
                                        label = { Text("$convertConstEmoji $convertConstName", fontWeight = FontWeight.Bold) }
                                    )

                                    if (convertLunar.solarTerm.isNotEmpty()) {
                                        val termIcon = LunarCalendarUtils.getSolarTermIcon(convertLunar.solarTerm)
                                        SuggestionChip(
                                            onClick = {},
                                            shape = CircleShape,
                                            label = { Text("$termIcon ${convertLunar.solarTerm}", fontWeight = FontWeight.Bold) }
                                        )
                                    }

                                    if (festivalStr.isNotEmpty()) {
                                        SuggestionChip(
                                            onClick = { viewModel.triggerFireworks() },
                                            shape = CircleShape,
                                            label = { Text(stringResource(R.string.label_traditional_festival, festivalStr), fontWeight = FontWeight.Bold) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "✨ 运势: ${convertFortune.summary}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = NeumorphicTextPrimary.copy(alpha = 0.75f)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = descStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeumorphicTextPrimary.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val almanacBoxShape = RoundedCornerShape(18.dp)

                                val convertYiBadgeBg = Color(0xFF10B981)
                                val convertYiBadgeText = Color.White
                                val convertYiChipBg = if (isDark) Color(0xFF065F46).copy(alpha = 0.7f) else Color(0xFFD1FAE5)
                                val convertYiChipBorder = Color(0xFF10B981).copy(alpha = 0.45f)
                                val convertYiChipText = if (isDark) Color(0xFF34D399) else Color(0xFF047857)

                                val convertJiBadgeBg = Color(0xFF64748B)
                                val convertJiBadgeText = Color.White
                                val convertJiChipBg = if (isDark) Color(0xFF334155).copy(alpha = 0.7f) else Color(0xFFF1F5F9)
                                val convertJiChipBorder = Color(0xFF94A3B8).copy(alpha = 0.35f)
                                val convertJiChipText = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .neumorphicInset(shape = almanacBoxShape, elevation = 3.dp)
                                        .border(1.dp, NeumorphicAccent.copy(alpha = 0.15f), shape = almanacBoxShape)
                                        .background(
                                            if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            else Color(0xFFE8EEF5),
                                            shape = almanacBoxShape
                                        )
                                        .padding(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(convertYiBadgeBg),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("宜", color = convertYiBadgeText, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            FlowRow(
                                                modifier = Modifier.weight(1f),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                convertAlmanac.yiList.forEach { yiItem ->
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(convertYiChipBg)
                                                            .border(0.5.dp, convertYiChipBorder, shape = RoundedCornerShape(8.dp))
                                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                                    ) {
                                                        Text(
                                                            text = yiItem,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = convertYiChipText
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.1f))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(convertJiBadgeBg),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("忌", color = convertJiBadgeText, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            FlowRow(
                                                modifier = Modifier.weight(1f),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                convertAlmanac.jiList.forEach { jiItem ->
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(convertJiChipBg)
                                                            .border(0.5.dp, convertJiChipBorder, shape = RoundedCornerShape(8.dp))
                                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                                    ) {
                                                        Text(
                                                            text = jiItem,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = convertJiChipText
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                val yiText = convertAlmanac.yiList.joinToString("·")
                                val jiText = convertAlmanac.jiList.joinToString("·")
                                val clipText = "Lunar ${parsedYear}/${lunarMonth}/${lunarDay} ➔ Solar: $solarStr | 宜: $yiText | 忌: $jiText"

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    NeumorphicIconButton(
                                        icon = Icons.Default.Share,
                                        contentDescription = "分享公历转换结果",
                                        onClick = {
                                            ShareUtils.shareText(context, clipText)
                                        }
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    NeumorphicIconButton(
                                        icon = Icons.Default.ContentCopy,
                                        contentDescription = "复制公历转换结果",
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("SolarDate", clipText))
                                            Toast.makeText(context, context.getString(R.string.toast_copied), Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            } else {
                                Text("Invalid date range (1900-2100)", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        FireworksAnimation(
            trigger = uiState.fireworksTrigger,
            onAnimationFinished = { viewModel.resetFireworks() }
        )
    }
}
