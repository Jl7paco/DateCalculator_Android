package me.paco.datecalculator.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.CalendarContract
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
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
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
import me.paco.datecalculator.ui.components.QuickDateChips
import me.paco.datecalculator.ui.components.SettingsOverlayDialog
import me.paco.datecalculator.ui.components.SolarLunarSwitch
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.viewmodel.CustomEventItem
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LunarCalendarUtils
import me.paco.datecalculator.util.NotificationUtils
import me.paco.datecalculator.util.ShareUtils
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

data class CountdownCardData(
    val id: Long = System.currentTimeMillis(),
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
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var showPickerForStart by remember { mutableStateOf(false) }
    var showPickerForEnd by remember { mutableStateOf(false) }
    var showAddCustomDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var eventToDelete by remember { mutableStateOf<CustomEventItem?>(null) }

    var customEventName by remember { mutableStateOf("") }
    var customEventIcon by remember { mutableStateOf("📌") }
    var customEventDate by remember { mutableStateOf(LocalDate.now().plusDays(30)) }
    var showCustomDatePicker by remember { mutableStateOf(false) }

    var currentTargetEventName by remember { mutableStateOf("") }

    var targetCalendarType by remember { mutableIntStateOf(0) } // 0=公历, 1=农历

    var targetLunarYear by remember { mutableIntStateOf(LocalDate.now().year) }
    var targetLunarMonth by remember { mutableIntStateOf(8) }
    var targetLunarDay by remember { mutableIntStateOf(15) }
    var targetIsLeapMonth by remember { mutableStateOf(false) }

    // 多结果卡片推栈数据结构 (点击新的倒数日时在顶部追加，原卡片向下排)
    var resultCardList by remember { mutableStateOf<List<CountdownCardData>>(emptyList()) }

    fun addResultCard(eventName: String, targetDate: LocalDate) {
        val name = eventName.ifBlank { "目标日期" }
        val newCard = CountdownCardData(
            id = System.currentTimeMillis() + resultCardList.size,
            eventName = name,
            targetDate = targetDate,
            baseDate = uiState.baseDate
        )
        // 过滤掉完全重复项，然后插入在最顶部
        resultCardList = listOf(newCard) + resultCardList.filterNot { it.eventName == name && it.targetDate == targetDate }
    }

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

    LaunchedEffect(resultCardList.size) {
        if (resultCardList.isNotEmpty()) {
            delay(220)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    if (showPickerForStart) {
        DatePickerModal(
            selectedDate = uiState.baseDate,
            onDateSelected = { viewModel.updateBaseDate(it) },
            onDismiss = { showPickerForStart = false }
        )
    }

    if (showPickerForEnd) {
        DatePickerModal(
            selectedDate = uiState.endDate,
            onDateSelected = { viewModel.updateEndDate(it) },
            onDismiss = { showPickerForEnd = false }
        )
    }

    if (showCustomDatePicker) {
        DatePickerModal(
            selectedDate = customEventDate,
            onDateSelected = { customEventDate = it },
            onDismiss = { showCustomDatePicker = false }
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

    if (showReminderDialog) {
        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = NeumorphicBg,
            title = { Text("选择提醒方式", fontWeight = FontWeight.ExtraBold, color = NeumorphicTextPrimary) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("提醒名称: $reminderMessage", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeumorphicAccent)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .neumorphicExtruded(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                            .background(NeumorphicBg, shape = RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                showReminderDialog = false
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (!hasPermission) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        NotificationUtils.showCountdownNotification(
                                            context = context,
                                            title = "🎉 倒计时提醒已设置",
                                            message = reminderMessage
                                        )
                                        Toast.makeText(context, "已成功发送系统通知提醒", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    NotificationUtils.showCountdownNotification(
                                        context = context,
                                        title = "🎉 倒计时提醒已设置",
                                        message = reminderMessage
                                    )
                                    Toast.makeText(context, "已成功发送系统通知提醒", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = NeumorphicAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("🔔 App 弹出通知提醒", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = NeumorphicTextPrimary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .neumorphicExtruded(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                            .background(NeumorphicBg, shape = RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                showReminderDialog = false
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("AlarmName", reminderMessage))

                                try {
                                    val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                                        putExtra(AlarmClock.EXTRA_MESSAGE, reminderMessage)
                                        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                                    }
                                    context.startActivity(alarmIntent)
                                } catch (_: Exception) {
                                    try {
                                        val showAlarmsIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                                        context.startActivity(showAlarmsIntent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "请在系统时钟中添加闹钟", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Alarm, contentDescription = null, tint = NeumorphicAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("⏰ 跳转系统闹铃设置界面", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = NeumorphicTextPrimary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .neumorphicExtruded(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                            .background(NeumorphicBg, shape = RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                showReminderDialog = false
                                try {
                                    val calendarIntent = Intent(Intent.ACTION_INSERT).apply {
                                        data = CalendarContract.Events.CONTENT_URI
                                        putExtra(CalendarContract.Events.TITLE, reminderMessage)
                                        putExtra(
                                            CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                            effectiveEndDate.atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                        )
                                        putExtra(
                                            CalendarContract.EXTRA_EVENT_END_TIME,
                                            effectiveEndDate.atTime(10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                        )
                                    }
                                    context.startActivity(calendarIntent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "请在系统日历中新建日程", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.EventNote, contentDescription = null, tint = NeumorphicAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("📅 跳转系统日历新建日程", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = NeumorphicTextPrimary)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showReminderDialog = false }) {
                    Text("取消", color = NeumorphicTextPrimary)
                }
            }
        )
    }

    eventToDelete?.let { event ->
        AlertDialog(
            onDismissRequest = { eventToDelete = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = NeumorphicBg,
            title = { Text("确认删除倒数日", fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary) },
            text = { Text("是否确定要删除倒数日 '${event.name}'？", color = NeumorphicTextPrimary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCustomEvent(event)
                    eventToDelete = null
                    Toast.makeText(context, "已删除倒数日", Toast.LENGTH_SHORT).show()
                }) {
                    Text("删除", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { eventToDelete = null }) {
                    Text("取消", color = NeumorphicTextPrimary)
                }
            }
        )
    }

    if (showAddCustomDialog) {
        AlertDialog(
            onDismissRequest = { showAddCustomDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = NeumorphicBg,
            title = { Text("新建固定倒数日", fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("选择分类图标:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)

                    val icons = listOf("📌", "🎂", "💍", "❤️", "🚀", "✈️", "🎓", "🏠", "💰", "🎁", "⚽", "🎮")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        icons.forEach { icon ->
                            val isSelected = (customEventIcon == icon)
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .then(
                                        if (isSelected) {
                                            Modifier
                                                .neumorphicInset(shape = CircleShape)
                                                .background(NeumorphicAccent.copy(alpha = 0.2f), shape = CircleShape)
                                        } else {
                                            Modifier
                                                .neumorphicExtruded(shape = CircleShape)
                                                .background(NeumorphicBg, shape = CircleShape)
                                        }
                                    )
                                    .clip(CircleShape)
                                    .clickable { customEventIcon = icon },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(icon, fontSize = 18.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("给重要的日子起个名字，记录期待的时刻:", fontSize = 13.sp, color = NeumorphicTextPrimary)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .neumorphicInset(shape = RoundedCornerShape(12.dp))
                            .background(NeumorphicBg, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = customEventName,
                            onValueChange = { customEventName = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .neumorphicExtruded(shape = RoundedCornerShape(12.dp))
                            .background(NeumorphicBg, shape = RoundedCornerShape(12.dp))
                            .clickable { showCustomDatePicker = true }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text("目标日期: ${DateCalculatorUtils.formatDate(customEventDate)}", fontWeight = FontWeight.Bold, color = NeumorphicAccent)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (customEventName.isNotBlank()) {
                        val fullName = "$customEventIcon $customEventName"
                        viewModel.addCustomEvent(fullName, customEventDate, customEventIcon)
                        viewModel.updateEndDate(customEventDate)
                        currentTargetEventName = fullName
                        targetCalendarType = 0
                        addResultCard(fullName, customEventDate)

                        val natDays = DateCalculatorUtils.naturalDaysBetween(uiState.baseDate, customEventDate)
                        val workDays = DateCalculatorUtils.workdaysBetween(
                            uiState.baseDate, customEventDate, uiState.weekendRule, uiState.enableChineseHolidays, uiState.holidayRegion, uiState.isCurrentWeekBigWeek, uiState.disableChinaShiftWorkdays
                        )
                        val title = "$fullName: 相差 ${abs(natDays)} 天 (${abs(workDays)} 工作日)"
                        val detail = "${uiState.baseDate}  ➔  ${customEventDate}"
                        val regionTag = "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.nativeName}"

                        viewModel.saveToHistory(
                            category = "倒数日",
                            title = title,
                            detail = detail,
                            regionTag = regionTag,
                            resultDate = customEventDate,
                            resultDays = natDays
                        )

                        customEventName = ""
                    }
                    showAddCustomDialog = false
                }) {
                    Text("添加并查看结果", fontWeight = FontWeight.Bold, color = NeumorphicAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomDialog = false }) {
                    Text("取消", color = NeumorphicTextPrimary)
                }
            }
        )
    }

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
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = NeumorphicAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "倒数日",
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

        // 置顶 / 固定倒数日展示区 (多彩异色卡片)
        val pinnedEvents = uiState.customEvents.filter { it.isPinned }
        val cardColorPalette = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            Color(0xFFD1FAE5), // 翡翠绿
            Color(0xFFFFEDD5), // 晚霞橘
            Color(0xFFF3E8FF), // 极光紫
            Color(0xFFFCE7F3)  // 樱花粉
        )

        if (pinnedEvents.isNotEmpty()) {
            Text("固定倒数日", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NeumorphicAccent)
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
                                    .clickable { viewModel.togglePinCustomEvent(pinned) }
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.label_base_date_default), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
        }

        Spacer(modifier = Modifier.height(8.dp))

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
                            text = "选择起始日期",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeumorphicAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val dateFormattedWithWeek = DateCalculatorUtils.formatDateWithWeek(uiState.baseDate)
                        Text(
                            text = dateFormattedWithWeek,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeumorphicTextPrimary
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.EditCalendar,
                    contentDescription = "选择日期",
                    tint = NeumorphicAccent.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        QuickDateChips(
            selectedDate = uiState.baseDate,
            onSelectDate = {
                viewModel.updateBaseDate(it)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. 目标日期功能区 (目标日期与公历/农历拨动开关做在同一行，符合草图设计)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .neumorphicExtruded(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                    .background(NeumorphicBg, shape = RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showPickerForEnd = true }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = NeumorphicAccent)
                    Spacer(modifier = Modifier.width(6.dp))
                    val dateFormatted = DateCalculatorUtils.formatDate(uiState.endDate)
                    Text(text = "目标: $dateFormatted", fontWeight = FontWeight.ExtraBold, color = NeumorphicTextPrimary, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 公历 / 农历 同行胶囊拨动开关 (遵照手绘草图)
            SolarLunarSwitch(
                isSolar = (targetCalendarType == 0),
                onCalendarTypeChanged = { isSolar ->
                    targetCalendarType = if (isSolar) 0 else 1
                },
                modifier = Modifier.width(120.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(52.dp)
                    .neumorphicExtruded(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                    .background(Color(0xFFEF4444), shape = RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        val targetName = currentTargetEventName.ifBlank { "目标日期" }
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

        // 3. 常用倒数日功能区
        Text(
            text = "常用倒数日",
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
                val isSelected = resultCardList.firstOrNull()?.eventName == label
                NeumorphicChip(
                    text = label,
                    selected = isSelected,
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
                    modifier = Modifier.padding(vertical = 2.dp)
                )
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
                        .height(40.dp)
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
                                viewModel.togglePinCustomEvent(customEvent)
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
                            fontSize = 13.sp
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
                    .height(40.dp)
                    .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
                    .background(NeumorphicBg, shape = CircleShape)
                    .clip(CircleShape)
                    .clickable { showAddCustomDialog = true }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("+ 新增倒数日", fontSize = 13.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. 多结果卡片推栈渲染 (点击新的倒数日时在顶部追加新卡片，原来的卡片往下排，每张卡片多彩异色)
        val cardShape20 = RoundedCornerShape(20.dp)

        resultCardList.forEachIndexed { cardIdx, cardData ->
            val totalNat = DateCalculatorUtils.naturalDaysBetween(cardData.baseDate, cardData.targetDate)
            val totalWork = DateCalculatorUtils.workdaysBetween(
                cardData.baseDate, cardData.targetDate, uiState.weekendRule, uiState.enableChineseHolidays, uiState.holidayRegion, uiState.isCurrentWeekBigWeek, uiState.disableChinaShiftWorkdays
            )
            val weekDaysCount = totalNat - totalWork
            val pStr = DateCalculatorUtils.formatPeriod(cardData.baseDate, cardData.targetDate)
            val wWeeks = totalNat / 7.0

            val cardBgColor = cardColorPalette[cardIdx % cardColorPalette.size]
            val cardIsPinned = uiState.customEvents.any { it.name == cardData.eventName && it.isPinned }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .neumorphicExtruded(shape = cardShape20, elevation = 5.dp)
                    .background(NeumorphicBg, shape = cardShape20)
                    .background(cardBgColor, shape = cardShape20)
                    .border(1.2.dp, NeumorphicAccent.copy(alpha = 0.35f), shape = cardShape20)
                    .clip(cardShape20)
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 卡片顶部：关闭按键与标题
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "距离 ${cardData.eventName} 还有",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeumorphicAccent
                        )

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭结果卡片",
                            tint = NeumorphicTextPrimary.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    resultCardList = resultCardList.filterNot { it.id == cardData.id }
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 醒目的大字天数 (取消椭圆形框)
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${abs(totalNat)}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeumorphicTextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "天",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeumorphicAccent,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 同一行显示：相差自然日 9天，相差工作日 6天 (如草图“小字放同一行”标注，取消下方多余列表)
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
                                Text("相差自然日: ", fontSize = 11.5.sp, color = NeumorphicTextPrimary.copy(alpha = 0.75f))
                                Text("${abs(totalNat)}天", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicTextPrimary)
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
                                Text("相差工作日: ", fontSize = 11.5.sp, color = NeumorphicAccent)
                                Text("${abs(totalWork)}天", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // 右下角圆形纯图标按钮组 (提醒、固定、分享、复制)
                    val cardClipText = "距离 ${cardData.eventName} 还有 ${abs(totalNat)} 天 (${abs(totalWork)} 工作日) | 起始 ${cardData.baseDate} ➔ 目标 ${cardData.targetDate}"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. 提醒 Icon Button
                        NeumorphicIconButton(
                            icon = Icons.Default.Alarm,
                            contentDescription = "添加系统提醒",
                            onClick = {
                                currentTargetEventName = cardData.eventName
                                showReminderDialog = true
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // 2. 固定/置顶 Icon Button (📌)
                        NeumorphicIconButton(
                            icon = Icons.Default.PushPin,
                            contentDescription = if (cardIsPinned) "取消固定" else "固定倒数日",
                            onClick = {
                                val existing = uiState.customEvents.find { it.name == cardData.eventName }
                                if (existing != null) {
                                    viewModel.togglePinCustomEvent(existing)
                                } else {
                                    viewModel.addCustomEvent(cardData.eventName, cardData.targetDate, "📌")
                                    val added = viewModel.uiState.value.customEvents.find { it.name == cardData.eventName }
                                    added?.let { viewModel.togglePinCustomEvent(it) }
                                }
                                Toast.makeText(context, if (cardIsPinned) "已取消固定" else "已固定倒数日", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // 3. 分享 Icon Button (📤)
                        NeumorphicIconButton(
                            icon = Icons.Default.Share,
                            contentDescription = "分享倒数日",
                            onClick = { ShareUtils.shareText(context, cardClipText) }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // 4. 复制 Icon Button (📋)
                        NeumorphicIconButton(
                            icon = Icons.Default.ContentCopy,
                            contentDescription = "复制倒数日结果",
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

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun calculateNextSolarDate(baseDate: LocalDate, month: Int, day: Int): LocalDate {
    val thisYearDate = LocalDate.of(baseDate.year, month, day)
    return if (!baseDate.isAfter(thisYearDate)) {
        thisYearDate
    } else {
        LocalDate.of(baseDate.year + 1, month, day)
    }
}

private fun calculateNextLunarDate(baseDate: LocalDate, lunarMonth: Int, lunarDay: Int): LocalDate {
    val thisYearSolar = LunarCalendarUtils.lunarToSolar(baseDate.year, lunarMonth, lunarDay)
    return if (thisYearSolar != null && !baseDate.isAfter(thisYearSolar)) {
        thisYearSolar
    } else {
        LunarCalendarUtils.lunarToSolar(baseDate.year + 1, lunarMonth, lunarDay) ?: baseDate
    }
}
