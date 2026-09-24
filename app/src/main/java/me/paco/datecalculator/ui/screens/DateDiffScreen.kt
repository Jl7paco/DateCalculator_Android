package me.paco.datecalculator.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.paco.datecalculator.R
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.ui.components.DatePickerModal
import me.paco.datecalculator.ui.components.HistoryOverlayDialog
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicChip
import me.paco.datecalculator.ui.components.NeumorphicCustomPopup
import me.paco.datecalculator.ui.components.NeumorphicIconButton
import me.paco.datecalculator.ui.components.NeumorphicSunkenBg
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import me.paco.datecalculator.ui.components.SettingsOverlayDialog
import me.paco.datecalculator.ui.components.SolarLunarSwitch
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.viewmodel.CustomEventItem
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.ui.viewmodel.EventRepeatMode
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LanguageUtils
import me.paco.datecalculator.util.LunarCalendarUtils
import me.paco.datecalculator.util.NotificationUtils
import me.paco.datecalculator.util.ShareUtils
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs

data class CountdownCardData(
    val id: Long,
    val eventName: String,
    val targetDate: LocalDate,
    val baseDate: LocalDate
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun DateDiffScreen(
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    var showPickerForStart by remember { mutableStateOf(false) }
    var showPickerForEnd by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Start Date Type: 0 = 公历, 1 = 农历
    var startCalendarType by remember { mutableIntStateOf(0) }

    // Target Date Type: 0 = 公历, 1 = 农历
    var targetCalendarType by remember { mutableIntStateOf(0) }

    // 农历输入状态
    val todayLunar = remember { LunarCalendarUtils.solarToLunar(LocalDate.now()) }

    var startLunarYearInput by remember { mutableStateOf(todayLunar.year.toString()) }
    var startLunarMonth by remember { mutableIntStateOf(todayLunar.month) }
    var startLunarDay by remember { mutableIntStateOf(todayLunar.day) }
    var startIsLeapMonth by remember { mutableStateOf(false) }

    var targetLunarYearInput by remember { mutableStateOf(todayLunar.year.toString()) }
    var targetLunarMonth by remember { mutableIntStateOf(todayLunar.month) }
    var targetLunarDay by remember { mutableIntStateOf(todayLunar.day) }
    var targetIsLeapMonth by remember { mutableStateOf(false) }

    var currentTargetEventName by remember { mutableStateOf("") }
    var showReminderDialog by remember { mutableStateOf(false) }

    // 多结果卡片推栈数据结构 (未固定的卡片顶替当前结果，固定的卡片不被顶掉，新卡片排在最前面)
    var resultCardList by remember { mutableStateOf<List<CountdownCardData>>(emptyList()) }

    fun addResultCard(eventName: String, targetDate: LocalDate) {
        val name = eventName.ifBlank { "目标日期" }
        val newCard = CountdownCardData(
            id = System.currentTimeMillis() + resultCardList.size,
            eventName = name,
            targetDate = targetDate,
            baseDate = uiState.baseDate
        )

        val firstCard = resultCardList.firstOrNull()
        val isFirstCardPinned = firstCard != null && (
            uiState.customEvents.any { (it.name == firstCard.eventName || it.name.contains(firstCard.eventName)) && it.isPinned } ||
            uiState.pinnedPresetHolidays.contains(firstCard.eventName)
        )

        resultCardList = if (firstCard == null) {
            listOf(newCard)
        } else if (!isFirstCardPinned) {
            // 未固定的时候：点击新的结果会顶替掉当前结果
            listOf(newCard) + resultCardList.drop(1).filterNot { it.eventName == name }
        } else {
            // 固定后：当前结果不会被顶掉，新的结果卡片排在旧的前面 (最顶部)
            listOf(newCard) + resultCardList.filterNot { it.eventName == name }
        }
    }

    val targetLunarYear = targetLunarYearInput.toIntOrNull() ?: todayLunar.year
    val effectiveEndDate: LocalDate = if (targetCalendarType == 0) {
        uiState.endDate
    } else {
        LunarCalendarUtils.lunarToSolar(targetLunarYear, targetLunarMonth, targetLunarDay, targetIsLeapMonth)
            ?: uiState.endDate
    }

    val reminderMessage = "距离 ${currentTargetEventName.ifBlank { "目标日期" }} ($effectiveEndDate) 还有"

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationUtils.showCountdownNotification(
                context = context,
                title = "🎉 倒计时提醒已设置",
                message = reminderMessage
            )
            Toast.makeText(context, "已成功发送系统通知提醒！", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "需要通知权限以发送应用倒计时弹出提醒", Toast.LENGTH_SHORT).show()
        }
    }

    if (showPickerForStart) {
        DatePickerModal(
            selectedDate = uiState.baseDate,
            onDateSelected = { date ->
                viewModel.updateBaseDate(date)
                showPickerForStart = false
            },
            onDismiss = { showPickerForStart = false }
        )
    }

    if (showPickerForEnd) {
        DatePickerModal(
            selectedDate = uiState.endDate,
            onDateSelected = { date ->
                viewModel.updateEndDate(date)
                showPickerForEnd = false
            },
            onDismiss = { showPickerForEnd = false }
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

    // 添加自定义倒数日 Dialog
    var showAddCustomDialog by remember { mutableStateOf(false) }
    var customNameInput by remember { mutableStateOf("") }
    var customDateInput by remember { mutableStateOf(LocalDate.now().plusDays(10)) }
    var showCustomDatePicker by remember { mutableStateOf(false) }
    var selectedEmoji by remember { mutableStateOf("🎂") }
    var selectedRepeatMode by remember { mutableStateOf(EventRepeatMode.NONE) }
    var eventToDelete by remember { mutableStateOf<CustomEventItem?>(null) }

    if (showCustomDatePicker) {
        DatePickerModal(
            selectedDate = customDateInput,
            onDateSelected = { d ->
                customDateInput = d
                showCustomDatePicker = false
            },
            onDismiss = { showCustomDatePicker = false }
        )
    }

    if (showAddCustomDialog) {
        val dialogBg = if (isDark) MaterialTheme.colorScheme.surface else NeumorphicBg
        AlertDialog(
            onDismissRequest = { showAddCustomDialog = false },
            title = { Text("添加自定义倒数日", fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("选择图标 Emoji", fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                    val emojiOptions = listOf("🎂", "🎉", "✈️", "💍", "🎓", "🎮", "🚗", "🏠", "📚", "❤️", "🏆", "📌")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        emojiOptions.forEach { emoji ->
                            val isSelected = selectedEmoji == emoji
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) NeumorphicAccent else Color.Transparent)
                                    .clickable { selectedEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 18.sp)
                            }
                        }
                    }

                    Text("倒数日名称", fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 3.dp)
                            .background(NeumorphicBg, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = customNameInput,
                            onValueChange = { customNameInput = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Text("目标日期", fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 3.dp)
                            .background(NeumorphicBg, shape = RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showCustomDatePicker = true }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(DateCalculatorUtils.formatDate(customDateInput), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                    }

                    Text("重复周期", fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        EventRepeatMode.entries.forEach { mode ->
                            val isSel = selectedRepeatMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) NeumorphicAccent else NeumorphicSunkenBg)
                                    .clickable { selectedRepeatMode = mode },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(mode.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else NeumorphicTextPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (customNameInput.isNotBlank()) {
                            viewModel.addCustomEvent(
                                name = customNameInput,
                                targetDate = customDateInput,
                                iconEmoji = selectedEmoji,
                                repeatMode = selectedRepeatMode
                            )
                            val fullName = "$selectedEmoji $customNameInput"
                            viewModel.updateEndDate(customDateInput)
                            currentTargetEventName = fullName
                            addResultCard(fullName, customDateInput)

                            showAddCustomDialog = false
                            customNameInput = ""
                        } else {
                            Toast.makeText(context, "请输入倒数日名称", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("添加保存", fontWeight = FontWeight.Bold, color = NeumorphicAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomDialog = false }) {
                    Text("取消", color = NeumorphicTextPrimary.copy(alpha = 0.7f))
                }
            },
            containerColor = dialogBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (eventToDelete != null) {
        val targetEvent = eventToDelete!!
        AlertDialog(
            onDismissRequest = { eventToDelete = null },
            title = { Text("确认删除倒数日？", fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary) },
            text = { Text("确定要删除“${targetEvent.name}”吗？此操作无法撤销。", fontSize = 13.sp, color = NeumorphicTextPrimary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomEvent(targetEvent)
                        eventToDelete = null
                        Toast.makeText(context, "已删除倒数日", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("确认删除", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { eventToDelete = null }) {
                    Text("取消", color = NeumorphicTextPrimary)
                }
            },
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else NeumorphicBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

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
                        imageVector = Icons.Default.EventRepeat,
                        contentDescription = null,
                        tint = NeumorphicAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LanguageUtils.getString("tab_countdown", uiState.appLanguage),
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

            // 置顶 / 固定倒数日展示区 (记忆保存至 SharedPreferences)
            val pinnedEvents = uiState.customEvents.filter { it.isPinned }
            val cardColorPalette = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                Color(0xFFD1FAE5), // 翡翠绿
                Color(0xFFFFEDD5), // 晚霞橘
                Color(0xFFF3E8FF), // 极光紫
                Color(0xFFFCE7F3)  // 樱花粉
            )

            if (pinnedEvents.isNotEmpty()) {
                Text(LanguageUtils.getString("fixed_countdown", uiState.appLanguage), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NeumorphicAccent)
                Spacer(modifier = Modifier.height(6.dp))

                pinnedEvents.forEachIndexed { pIdx, pinned ->
                    val upcoming = pinned.getNextUpcomingDate(uiState.baseDate)
                    val diffDays = DateCalculatorUtils.naturalDaysBetween(uiState.baseDate, upcoming)
                    val cardBgColor = cardColorPalette[pIdx % cardColorPalette.size]

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .neumorphicExtruded(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                            .background(NeumorphicBg, shape = RoundedCornerShape(16.dp))
                            .background(cardBgColor, shape = RoundedCornerShape(16.dp))
                            .border(1.dp, NeumorphicAccent.copy(alpha = 0.3f), shape = RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                targetCalendarType = 0
                                viewModel.updateEndDate(upcoming)
                                currentTargetEventName = pinned.name
                                addResultCard(pinned.name, upcoming)
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.PushPin, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(pinned.name, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = NeumorphicTextPrimary)
                                    Text("目标: $upcoming", fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.6f))
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("还有 ", fontSize = 12.sp, color = NeumorphicTextPrimary)
                                Text("${abs(diffDays)}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent)
                                Text(" 天", fontSize = 12.sp, color = NeumorphicTextPrimary)

                                Spacer(modifier = Modifier.width(8.dp))

                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "取消固定并删除",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { viewModel.togglePinCustomEvent(pinned, context) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // 1. 起始日期 Header 与同行公历/农历开关 (同行优雅呈现)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("选择起始日期", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)

                SolarLunarSwitch(
                    isSolar = (startCalendarType == 0),
                    onCalendarTypeChanged = { isSolar ->
                        startCalendarType = if (isSolar) 0 else 1
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (startCalendarType == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicExtruded(shape = RoundedCornerShape(16.dp), elevation = 5.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f), shape = RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showPickerForStart = true }
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
                                    text = "选择起始日期 (公历)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val dateFormattedWithWeek = DateCalculatorUtils.formatDateWithWeek(uiState.baseDate)
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
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            Text("起始年份 (农历)", fontSize = 11.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                            BasicTextField(
                                value = startLunarYearInput,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || (newValue.length <= 4 && newValue.all { it.isDigit() })) {
                                        startLunarYearInput = newValue
                                        val y = newValue.toIntOrNull()
                                        if (y != null) {
                                            val solar = LunarCalendarUtils.lunarToSolar(y, startLunarMonth, startLunarDay, startIsLeapMonth)
                                            if (solar != null) viewModel.updateBaseDate(solar)
                                        }
                                    }
                                },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary),
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
                                    Text("月份", fontSize = 11.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                                    Text(LunarCalendarUtils.getLunarMonthName(startLunarMonth), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                                }
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = NeumorphicAccent)
                            }
                        }

                        NeumorphicCustomPopup(
                            expanded = monthExpanded,
                            onDismissRequest = { monthExpanded = false }
                        ) {
                            (1..12).forEach { m ->
                                val isCurrent = (startLunarMonth == m)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isCurrent) NeumorphicAccent.copy(alpha = 0.12f) else Color.Transparent)
                                        .clickable {
                                            startLunarMonth = m
                                            monthExpanded = false
                                            val y = startLunarYearInput.toIntOrNull() ?: todayLunar.year
                                            val solar = LunarCalendarUtils.lunarToSolar(y, m, startLunarDay, startIsLeapMonth)
                                            if (solar != null) viewModel.updateBaseDate(solar)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text(LunarCalendarUtils.getLunarMonthName(m), fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold, color = if (isCurrent) NeumorphicAccent else NeumorphicTextPrimary, fontSize = 14.sp)
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
                                    Text("日期", fontSize = 11.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                                    Text(LunarCalendarUtils.getLunarDayName(startLunarDay), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                                }
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = NeumorphicAccent)
                            }
                        }

                        NeumorphicCustomPopup(
                            expanded = dayExpanded,
                            onDismissRequest = { dayExpanded = false }
                        ) {
                            (1..30).forEach { d ->
                                val isCurrent = (startLunarDay == d)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isCurrent) NeumorphicAccent.copy(alpha = 0.12f) else Color.Transparent)
                                        .clickable {
                                            startLunarDay = d
                                            dayExpanded = false
                                            val y = startLunarYearInput.toIntOrNull() ?: todayLunar.year
                                            val solar = LunarCalendarUtils.lunarToSolar(y, startLunarMonth, d, startIsLeapMonth)
                                            if (solar != null) viewModel.updateBaseDate(solar)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text(LunarCalendarUtils.getLunarDayName(d), fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold, color = if (isCurrent) NeumorphicAccent else NeumorphicTextPrimary, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 四个快捷按键
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val today = LocalDate.now()
                val lang = uiState.appLanguage

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .neumorphicExtruded(shape = RoundedCornerShape(12.dp), elevation = 4.dp)
                        .background(if (uiState.baseDate == today) NeumorphicAccent else NeumorphicBg, shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.updateBaseDate(today) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(LanguageUtils.getString("today", lang), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (uiState.baseDate == today) Color.White else NeumorphicTextPrimary)
                }

                val yesterday = today.minusDays(1)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .neumorphicExtruded(shape = RoundedCornerShape(12.dp), elevation = 4.dp)
                        .background(if (uiState.baseDate == yesterday) NeumorphicAccent else NeumorphicBg, shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.updateBaseDate(yesterday) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(LanguageUtils.getString("yesterday", lang), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (uiState.baseDate == yesterday) Color.White else NeumorphicTextPrimary)
                }

                val plusOneWeek = uiState.baseDate.plusWeeks(1)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .neumorphicExtruded(shape = RoundedCornerShape(12.dp), elevation = 4.dp)
                        .background(NeumorphicBg, shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.updateBaseDate(plusOneWeek) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(LanguageUtils.getString("plus_1w", lang), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NeumorphicTextPrimary)
                }

                val minusOneWeek = uiState.baseDate.minusWeeks(1)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .neumorphicExtruded(shape = RoundedCornerShape(12.dp), elevation = 4.dp)
                        .background(NeumorphicBg, shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.updateBaseDate(minusOneWeek) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(LanguageUtils.getString("minus_1w", lang), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NeumorphicTextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val lang = uiState.appLanguage

            // 2. 目标日期 Header 与同行公历/农历开关 (同行优雅呈现)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(LanguageUtils.getString("select_target_date", lang), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)

                SolarLunarSwitch(
                    isSolar = (targetCalendarType == 0),
                    onCalendarTypeChanged = { isSolar ->
                        targetCalendarType = if (isSolar) 0 else 1
                    },
                    language = lang
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (targetCalendarType == 0) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .neumorphicInset(shape = RoundedCornerShape(18.dp), elevation = 4.dp)
                            .border(1.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = RoundedCornerShape(18.dp))
                            .background(NeumorphicBg, shape = RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { showPickerForEnd = true }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = NeumorphicAccent,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "${LanguageUtils.getString("target_date", lang)}: ${DateCalculatorUtils.formatDate(uiState.endDate, lang)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeumorphicTextPrimary
                            )
                        }
                    }
                } else {
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
                                Text("年份", fontSize = 11.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                                BasicTextField(
                                    value = targetLunarYearInput,
                                    onValueChange = { newValue ->
                                        if (newValue.isEmpty() || (newValue.length <= 4 && newValue.all { it.isDigit() })) {
                                            targetLunarYearInput = newValue
                                        }
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary),
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
                                        Text("月份", fontSize = 11.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                                        Text(LunarCalendarUtils.getLunarMonthName(targetLunarMonth), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                                    }
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = NeumorphicAccent)
                                }
                            }

                            NeumorphicCustomPopup(
                                expanded = monthExpanded,
                                onDismissRequest = { monthExpanded = false }
                            ) {
                                (1..12).forEach { m ->
                                    val isCurrent = (targetLunarMonth == m)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isCurrent) NeumorphicAccent.copy(alpha = 0.12f) else Color.Transparent)
                                            .clickable {
                                                targetLunarMonth = m
                                                monthExpanded = false
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Text(LunarCalendarUtils.getLunarMonthName(m), fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold, color = if (isCurrent) NeumorphicAccent else NeumorphicTextPrimary, fontSize = 14.sp)
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
                                        Text("日期", fontSize = 11.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                                        Text(LunarCalendarUtils.getLunarDayName(targetLunarDay), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                                    }
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = NeumorphicAccent)
                                }
                            }

                            NeumorphicCustomPopup(
                                expanded = dayExpanded,
                                onDismissRequest = { dayExpanded = false }
                            ) {
                                (1..30).forEach { d ->
                                    val isCurrent = (targetLunarDay == d)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isCurrent) NeumorphicAccent.copy(alpha = 0.12f) else Color.Transparent)
                                            .clickable {
                                                targetLunarDay = d
                                                dayExpanded = false
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Text(LunarCalendarUtils.getLunarDayName(d), fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold, color = if (isCurrent) NeumorphicAccent else NeumorphicTextPrimary, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(58.dp)
                        .neumorphicExtruded(shape = RoundedCornerShape(18.dp), elevation = 5.dp)
                        .background(Color(0xFFEF4444), shape = RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .clickable {
                            val targetName = if (currentTargetEventName.isNotBlank()) currentTargetEventName else "目标日期"
                            addResultCard(targetName, effectiveEndDate)

                            val natDays = DateCalculatorUtils.naturalDaysBetween(uiState.baseDate, effectiveEndDate)
                            val workDays = DateCalculatorUtils.workdaysBetween(
                                uiState.baseDate, effectiveEndDate, uiState.weekendRule, uiState.enableChineseHolidays, uiState.holidayRegion, uiState.isCurrentWeekBigWeek, uiState.disableChinaShiftWorkdays
                            )
                            val title = "$targetName: 相差 ${abs(natDays)} 天 (${abs(workDays)} 工作日)"
                            val detail = "${uiState.baseDate}  ➔  ${effectiveEndDate}"
                            val regionTag = "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.nativeName}"

                            viewModel.saveToHistory(
                                category = "倒数日",
                                title = title,
                                detail = detail,
                                regionTag = regionTag,
                                resultDate = effectiveEndDate,
                                resultDays = natDays
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("=", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. 常用倒数日功能区 (快捷按键固定后只用 📌 图标标注，不另外生成按钮)
            Text(
                text = LanguageUtils.getString("common_countdown", uiState.appLanguage),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = NeumorphicTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            val base = uiState.baseDate
            val rawPresets = when (uiState.holidayRegion) {
                HolidayRegion.CHINA -> listOf(
                    stringResource(R.string.preset_national_day) to calculateNextSolarDate(base, 10, 1),
                    stringResource(R.string.preset_new_year) to calculateNextSolarDate(base, 1, 1),
                    stringResource(R.string.preset_spring_festival) to calculateNextLunarDate(base, 1, 1),
                    "🌿 清明节" to calculateNextSolarDate(base, 4, 4),
                    "🛠️ 五一劳动节" to calculateNextSolarDate(base, 5, 1),
                    stringResource(R.string.preset_dragon_boat) to calculateNextLunarDate(base, 5, 5),
                    stringResource(R.string.preset_gaokao) to calculateNextSolarDate(base, 6, 7),
                    "📚 中考" to calculateNextSolarDate(base, 6, 21),
                    stringResource(R.string.preset_mid_autumn) to calculateNextLunarDate(base, 8, 15)
                )
                else -> listOf(
                    stringResource(R.string.preset_new_year) to calculateNextSolarDate(base, 1, 1),
                    stringResource(R.string.preset_national_day) to calculateNextSolarDate(base, 10, 1)
                )
            }

            val presetCountdowns = rawPresets.filterNot { (label, _) ->
                uiState.disabledPresetHolidays.contains(label)
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetCountdowns.forEach { (label, targetDate) ->
                    val isPinned = uiState.pinnedPresetHolidays.contains(label)
                    val isSelected = resultCardList.firstOrNull()?.eventName == label
                    val localizedLabel = LanguageUtils.getLocalizedHolidayName(label, uiState.appLanguage)
                    val displayLabel = if (isPinned) "📌 $localizedLabel" else localizedLabel

                    Box(
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {
                                    targetCalendarType = 0
                                    viewModel.updateEndDate(targetDate)
                                    currentTargetEventName = label
                                    addResultCard(label, targetDate)

                                    val natDays = DateCalculatorUtils.naturalDaysBetween(uiState.baseDate, targetDate)
                                    val workDays = DateCalculatorUtils.workdaysBetween(
                                        uiState.baseDate, targetDate, uiState.weekendRule, uiState.enableChineseHolidays, uiState.holidayRegion, uiState.isCurrentWeekBigWeek, uiState.disableChinaShiftWorkdays
                                    )
                                    val title = "$label: 相差 ${abs(natDays)} 天 (${abs(workDays)} 工作日)"
                                    val detail = "${uiState.baseDate}  ➔  ${targetDate}"
                                    val regionTag = "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.nativeName}"

                                    viewModel.saveToHistory(
                                        category = "倒数日",
                                        title = title,
                                        detail = detail,
                                        regionTag = regionTag,
                                        resultDate = targetDate,
                                        resultDays = natDays
                                    )
                                },
                                onLongClick = {
                                    viewModel.togglePinPresetHoliday(label, context)
                                    Toast.makeText(context, if (isPinned) "已取消固定 $label" else "已固定 $label", Toast.LENGTH_SHORT).show()
                                }
                            )
                    ) {
                        NeumorphicChip(
                            text = displayLabel,
                            selected = isSelected,
                            onClick = {
                                targetCalendarType = 0
                                viewModel.updateEndDate(targetDate)
                                currentTargetEventName = label
                                addResultCard(label, targetDate)
                            },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                uiState.customEvents.forEach { customEvent ->
                    val effectiveTargetDate = customEvent.getNextUpcomingDate(uiState.baseDate)
                    val isSelected = resultCardList.firstOrNull()?.eventName == customEvent.name
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.92f else 1.0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "ChipPressScale"
                    )

                    val chipModifier = if (isSelected) {
                        Modifier
                            .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
                            .background(NeumorphicAccent, shape = CircleShape)
                    } else {
                        Modifier
                            .neumorphicExtruded(shape = CircleShape, elevation = 5.dp)
                            .background(NeumorphicBg, shape = CircleShape)
                    }

                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .then(chipModifier)
                            .clip(CircleShape)
                            .combinedClickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = {
                                    targetCalendarType = 0
                                    viewModel.updateEndDate(effectiveTargetDate)
                                    currentTargetEventName = customEvent.name
                                    addResultCard(customEvent.name, effectiveTargetDate)

                                    val natDays = DateCalculatorUtils.naturalDaysBetween(uiState.baseDate, effectiveTargetDate)
                                    val workDays = DateCalculatorUtils.workdaysBetween(
                                        uiState.baseDate, effectiveTargetDate, uiState.weekendRule, uiState.enableChineseHolidays, uiState.holidayRegion, uiState.isCurrentWeekBigWeek, uiState.disableChinaShiftWorkdays
                                    )
                                    val title = "${customEvent.name}: 相差 ${abs(natDays)} 天 (${abs(workDays)} 工作日)"
                                    val detail = "${uiState.baseDate}  ➔  ${effectiveTargetDate}"
                                    val regionTag = "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.nativeName}"

                                    viewModel.saveToHistory(
                                        category = "倒数日",
                                        title = title,
                                        detail = detail,
                                        regionTag = regionTag,
                                        resultDate = effectiveTargetDate,
                                        resultDays = natDays
                                    )
                                },
                                onLongClick = {
                                    viewModel.togglePinCustomEvent(customEvent, context)
                                    Toast.makeText(context, if (customEvent.isPinned) "已取消固定" else "已固定倒数日", Toast.LENGTH_SHORT).show()
                                }
                            )
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (customEvent.isPinned) {
                                Icon(imageVector = Icons.Default.PushPin, contentDescription = "已固定", tint = if (isSelected) Color.White else NeumorphicAccent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            Text(
                                text = customEvent.name,
                                color = if (isSelected) Color.White else NeumorphicTextPrimary,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "删除",
                                tint = if (isSelected) Color.White.copy(alpha = 0.8f) else NeumorphicTextPrimary.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { eventToDelete = customEvent }
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
                        .background(NeumorphicBg, shape = CircleShape)
                        .clip(CircleShape)
                        .clickable { showAddCustomDialog = true }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("自定义", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NeumorphicAccent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. 多卡片推栈倒计时结果展示层 (精简二合一卡片，高亮双胶囊放同一行，取消下部列表)
            if (resultCardList.isNotEmpty()) {
                val lang = uiState.appLanguage
                val prefix = LanguageUtils.getString("days_until_prefix", lang)
                val suffix = LanguageUtils.getString("days_until_suffix", lang)

                resultCardList.forEachIndexed { cardIdx, cardData ->
                    val natDays = DateCalculatorUtils.naturalDaysBetween(cardData.baseDate, cardData.targetDate)
                    val workDays = DateCalculatorUtils.workdaysBetween(
                        cardData.baseDate, cardData.targetDate, uiState.weekendRule, uiState.enableChineseHolidays, uiState.holidayRegion, uiState.isCurrentWeekBigWeek, uiState.disableChinaShiftWorkdays
                    )
                    val cardIsPinned = uiState.customEvents.any { (it.name == cardData.eventName || it.name.contains(cardData.eventName)) && it.isPinned } ||
                                       uiState.pinnedPresetHolidays.contains(cardData.eventName)

                    val cardBgColor = cardColorPalette[cardIdx % cardColorPalette.size]
                    val resultCardShape = RoundedCornerShape(22.dp)

                    val titleText = if (prefix.isNotBlank()) {
                        if (suffix.isNotBlank()) "$prefix ${cardData.eventName} $suffix" else "$prefix ${cardData.eventName}"
                    } else {
                        "${cardData.eventName} $suffix"
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .neumorphicExtruded(shape = resultCardShape, elevation = 6.dp)
                            .background(NeumorphicBg, shape = resultCardShape)
                            .background(cardBgColor, shape = resultCardShape)
                            .border(1.2.dp, NeumorphicAccent.copy(alpha = 0.35f), shape = resultCardShape)
                            .clip(resultCardShape)
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = titleText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeumorphicAccent
                                )

                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "关闭卡片",
                                    tint = NeumorphicTextPrimary.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable {
                                            resultCardList = resultCardList.filterNot { it.id == cardData.id }
                                        }
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "${abs(natDays)}",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeumorphicTextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = LanguageUtils.getString("days_unit", lang),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeumorphicAccent,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 左右并列双胶囊 (同一行显示：相差自然日 与 相差工作日)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .neumorphicInset(shape = CircleShape, elevation = 2.dp)
                                        .background(NeumorphicSunkenBg, shape = CircleShape)
                                        .clip(CircleShape)
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${LanguageUtils.getString("diff_natural", lang)}: ", fontSize = 11.5.sp, color = NeumorphicTextPrimary.copy(alpha = 0.75f))
                                        Text("${abs(natDays)}${LanguageUtils.getString("days_unit", lang)}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicTextPrimary)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .neumorphicInset(shape = CircleShape, elevation = 2.dp)
                                        .background(NeumorphicSunkenBg, shape = CircleShape)
                                        .clip(CircleShape)
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${LanguageUtils.getString("diff_workday", lang)}: ", fontSize = 11.5.sp, color = NeumorphicAccent)
                                        Text("${abs(workDays)}${LanguageUtils.getString("days_unit", lang)}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // 右下角圆形纯图标按钮组 (提醒、固定、分享、复制)
                            val cardClipText = "$titleText: ${abs(natDays)}${LanguageUtils.getString("days_unit", lang)} (${abs(workDays)} ${LanguageUtils.getString("workday", lang)})"

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NeumorphicIconButton(
                                    icon = Icons.Default.Alarm,
                                    contentDescription = "提醒",
                                    onClick = {
                                        currentTargetEventName = cardData.eventName
                                        showReminderDialog = true
                                    }
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                NeumorphicIconButton(
                                    icon = Icons.Default.PushPin,
                                    contentDescription = if (cardIsPinned) "取消固定" else "固定倒数日",
                                    onClick = {
                                        viewModel.togglePinPresetHoliday(cardData.eventName, context)
                                        Toast.makeText(context, if (cardIsPinned) "已取消固定" else "已固定倒数日", Toast.LENGTH_SHORT).show()
                                    }
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                NeumorphicIconButton(
                                    icon = Icons.Default.Share,
                                    contentDescription = "分享",
                                    onClick = { ShareUtils.shareText(context, cardClipText) }
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                NeumorphicIconButton(
                                    icon = Icons.Default.ContentCopy,
                                    contentDescription = "复制",
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("DateDiff", cardClipText))
                                        Toast.makeText(context, context.getString(R.string.toast_copied), Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun calculateNextSolarDate(baseDate: LocalDate, month: Int, day: Int): LocalDate {
    var target = LocalDate.of(baseDate.year, month, day)
    if (target.isBefore(baseDate)) {
        target = LocalDate.of(baseDate.year + 1, month, day)
    }
    return target
}

private fun calculateNextLunarDate(baseDate: LocalDate, lunarMonth: Int, lunarDay: Int): LocalDate {
    var solar = LunarCalendarUtils.lunarToSolar(baseDate.year, lunarMonth, lunarDay)
    if (solar == null || solar.isBefore(baseDate)) {
        solar = LunarCalendarUtils.lunarToSolar(baseDate.year + 1, lunarMonth, lunarDay)
            ?: baseDate.plusDays(30)
    }
    return solar
}
