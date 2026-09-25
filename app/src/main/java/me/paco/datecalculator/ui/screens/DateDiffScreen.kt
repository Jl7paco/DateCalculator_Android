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
import me.paco.datecalculator.ui.theme.LocalDarkTheme
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
import me.paco.datecalculator.data.AppLanguage
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
    val isDark = LocalDarkTheme.current
    val lang = uiState.appLanguage

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

    // 多卡片推栈倒计时结果数据结构
    var resultCardList by remember { mutableStateOf<List<CountdownCardData>>(emptyList()) }

    fun addResultCard(eventName: String, targetDate: LocalDate) {
        val name = eventName.ifBlank { LanguageUtils.getString("target_date", lang) }
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
            listOf(newCard) + resultCardList.drop(1).filterNot { it.eventName == name }
        } else {
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

    val reminderMessage = "${LanguageUtils.getString("days_until_prefix", lang)} ${currentTargetEventName.ifBlank { LanguageUtils.getString("target_date", lang) }} ($effectiveEndDate) ${LanguageUtils.getString("days_until_suffix", lang)}"

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationUtils.showCountdownNotification(
                context = context,
                title = "🎉 Reminder",
                message = reminderMessage
            )
            Toast.makeText(context, "Notification Set!", Toast.LENGTH_SHORT).show()
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
            title = { Text(LanguageUtils.getString("add_countdown", lang), fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Emoji Icon", fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                    val emojiOptions = listOf("🎂", "🎉", "✈️", "💍", "🎓", "🎮", "🚗", "🏠", "📚", "❤️", "🏆", "📌")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        emojiOptions.forEach { emoji ->
                            val isSelected = selectedEmoji == emoji
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) NeumorphicAccent else Color.Transparent)
                                    .clickable { selectedEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 18.sp)
                            }
                        }
                    }

                    Text(LanguageUtils.getString("anniversary_name", lang), fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
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

                    Text(LanguageUtils.getString("target_date", lang), fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        EventRepeatMode.entries.forEach { mode ->
                            val isSel = selectedRepeatMode == mode
                            val modeLabel = when (mode) {
                                EventRepeatMode.NONE -> "None"
                                EventRepeatMode.WEEKLY -> "Weekly"
                                EventRepeatMode.MONTHLY -> "Monthly"
                                EventRepeatMode.YEARLY -> "Yearly"
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) NeumorphicAccent else NeumorphicSunkenBg)
                                    .clickable { selectedRepeatMode = mode },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(modeLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else NeumorphicTextPrimary)
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
                        }
                    }
                ) {
                    Text(LanguageUtils.getString("confirm", lang), fontWeight = FontWeight.Bold, color = NeumorphicAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomDialog = false }) {
                    Text(LanguageUtils.getString("cancel", lang), color = NeumorphicTextPrimary.copy(alpha = 0.7f))
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
            title = { Text(LanguageUtils.getString("confirm_delete_anniversary", lang), fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary) },
            text = { Text("${targetEvent.name}", fontSize = 13.sp, color = NeumorphicTextPrimary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomEvent(targetEvent)
                        eventToDelete = null
                    }
                ) {
                    Text(LanguageUtils.getString("confirm_delete_btn", lang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { eventToDelete = null }) {
                    Text(LanguageUtils.getString("cancel", lang), color = NeumorphicTextPrimary)
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
                        text = LanguageUtils.getString("tab_countdown", lang),
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

            // 置顶 / 固定倒数日展示区
            val pinnedEvents = uiState.customEvents.filter { it.isPinned }
            val cardColorPalette = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                Color(0xFFD1FAE5),
                Color(0xFFFFEDD5),
                Color(0xFFF3E8FF),
                Color(0xFFFCE7F3)
            )

            if (pinnedEvents.isNotEmpty()) {
                Text(LanguageUtils.getString("fixed_countdown", lang), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NeumorphicAccent)
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
                                    Text("Target: $upcoming", fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.6f))
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${abs(diffDays)} ${LanguageUtils.getString("days_unit", lang)}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent)

                                Spacer(modifier = Modifier.width(8.dp))

                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Unpin",
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

            // 1. 起始日期 Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(LanguageUtils.getString("select_start_date", lang), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)

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
                                    text = LanguageUtils.getString("base_date", lang),
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
                            contentDescription = "Select Date",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
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

            // 2. 目标日期 Header
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
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                            text = "${LanguageUtils.getString("target_date", lang)}: ${DateCalculatorUtils.formatDate(uiState.endDate)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeumorphicTextPrimary
                        )
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
                            val targetName = if (currentTargetEventName.isNotBlank()) currentTargetEventName else LanguageUtils.getString("target_date", lang)
                            addResultCard(targetName, effectiveEndDate)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("=", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. 常用倒数日功能区
            Text(
                text = LanguageUtils.getString("common_countdown", lang),
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
                    val localizedName = LanguageUtils.getLocalizedHolidayName(label, lang)
                    val displayLabel = if (isPinned) "📌 $localizedName" else localizedName

                    Box(
                        modifier = Modifier.clickable {
                            targetCalendarType = 0
                            viewModel.updateEndDate(targetDate)
                            currentTargetEventName = localizedName
                            addResultCard(localizedName, targetDate)
                        }
                    ) {
                        NeumorphicChip(
                            text = displayLabel,
                            selected = isSelected,
                            onClick = {
                                targetCalendarType = 0
                                viewModel.updateEndDate(targetDate)
                                currentTargetEventName = localizedName
                                addResultCard(localizedName, targetDate)
                            },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
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
                        Text(LanguageUtils.getString("custom_btn", lang), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NeumorphicAccent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. 多卡片推栈倒计时结果展示层
            if (resultCardList.isNotEmpty()) {
                resultCardList.forEachIndexed { cardIdx, cardData ->
                    val natDays = DateCalculatorUtils.naturalDaysBetween(cardData.baseDate, cardData.targetDate)
                    val workDays = DateCalculatorUtils.workdaysBetween(
                        cardData.baseDate, cardData.targetDate, uiState.weekendRule, uiState.enableChineseHolidays, uiState.holidayRegion, uiState.isCurrentWeekBigWeek, uiState.disableChinaShiftWorkdays
                    )
                    val isPast = cardData.targetDate.isBefore(cardData.baseDate)

                    val resultCardShape = RoundedCornerShape(22.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.EventRepeat, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cardData.eventName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable {
                                            resultCardList = resultCardList.filterNot { it.id == cardData.id }
                                        }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "${abs(natDays)} ${LanguageUtils.getString("days_unit", lang)}",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            SuggestionChip(
                                onClick = {},
                                shape = CircleShape,
                                label = {
                                    Text(
                                        text = if (isPast) "Passed ${abs(natDays)} days" else "≈ ${abs(workDays)} ${LanguageUtils.getString("workday", lang)}",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${LanguageUtils.getString("base_date", lang)}: ${cardData.baseDate}  ➔  ${LanguageUtils.getString("target_date", lang)}: ${cardData.targetDate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
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
