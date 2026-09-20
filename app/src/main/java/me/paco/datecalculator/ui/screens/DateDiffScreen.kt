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
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
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
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicChip
import me.paco.datecalculator.ui.components.NeumorphicCopyButton
import me.paco.datecalculator.ui.components.NeumorphicCustomPopup
import me.paco.datecalculator.ui.components.NeumorphicSegmentedRow
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import me.paco.datecalculator.ui.components.QuickDateChips
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.viewmodel.CustomEventItem
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LunarCalendarUtils
import me.paco.datecalculator.util.NotificationUtils
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

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
    var eventToDelete by remember { mutableStateOf<CustomEventItem?>(null) }

    var customEventName by remember { mutableStateOf("") }
    var customEventIcon by remember { mutableStateOf("📌") }
    var customEventDate by remember { mutableStateOf(LocalDate.now().plusDays(30)) }
    var showCustomDatePicker by remember { mutableStateOf(false) }

    var currentTargetEventName by remember { mutableStateOf("") }

    // 0 = 阳历目标日期, 1 = 农历目标日期
    var targetCalendarType by remember { mutableIntStateOf(0) }

    // 农历输入状态
    var targetLunarYear by remember { mutableIntStateOf(LocalDate.now().year) }
    var targetLunarMonth by remember { mutableIntStateOf(8) }
    var targetLunarDay by remember { mutableIntStateOf(15) }
    var targetIsLeapMonth by remember { mutableStateOf(false) }

    var showDiffResult by remember { mutableStateOf(false) }

    // 计算真实的结束阳历日期
    val effectiveEndDate: LocalDate = if (targetCalendarType == 0) {
        uiState.endDate
    } else {
        LunarCalendarUtils.lunarToSolar(targetLunarYear, targetLunarMonth, targetLunarDay, targetIsLeapMonth)
            ?: uiState.endDate
    }

    val reminderMessage = if (currentTargetEventName.isNotBlank()) {
        "$currentTargetEventName ($effectiveEndDate)"
    } else {
        "倒计时提醒 ($effectiveEndDate)"
    }

    // Android 13+ 通知权限运行时询问请求 Launcher
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

    // 结果展开后自动平滑下滑至结果卡片完全可见区域
    LaunchedEffect(showDiffResult, uiState.endDate) {
        if (showDiffResult) {
            delay(220)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    if (showPickerForStart) {
        DatePickerModal(
            selectedDate = uiState.baseDate,
            onDateSelected = {
                viewModel.updateBaseDate(it)
                showDiffResult = false
            },
            onDismiss = { showPickerForStart = false }
        )
    }

    if (showPickerForEnd) {
        DatePickerModal(
            selectedDate = uiState.endDate,
            onDateSelected = {
                viewModel.updateEndDate(it)
                showDiffResult = false
            },
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

    // 提醒方式选择弹窗
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

                    // 选项 1: App 弹出通知提醒
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

                    // 选项 2: 跳转系统闹铃
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

                    // 选项 3: 跳转系统日历
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

    // 删除自定义特定日期确认弹窗
    eventToDelete?.let { event ->
        AlertDialog(
            onDismissRequest = { eventToDelete = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = NeumorphicBg,
            title = { Text("确认删除特定日期", fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary) },
            text = { Text("是否确定要删除自定义特定日期 '${event.name}'？", color = NeumorphicTextPrimary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCustomEvent(event)
                    eventToDelete = null
                    Toast.makeText(context, "已删除特定日期", Toast.LENGTH_SHORT).show()
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

    // 自定义倒计时事件添加弹窗
    if (showAddCustomDialog) {
        AlertDialog(
            onDismissRequest = { showAddCustomDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = NeumorphicBg,
            title = { Text(stringResource(R.string.label_custom_event_dialog_title), fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary) },
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
                        showDiffResult = true

                        // 添加自定义倒计时事件并自动生成历史记录
                        val natDays = DateCalculatorUtils.naturalDaysBetween(uiState.baseDate, customEventDate)
                        val workDays = DateCalculatorUtils.workdaysBetween(
                            uiState.baseDate, customEventDate, uiState.weekendRule, uiState.enableChineseHolidays, uiState.holidayRegion, uiState.isCurrentWeekBigWeek, uiState.disableChinaShiftWorkdays
                        )
                        val title = "$fullName: 相差 ${abs(natDays)} 天 (${abs(workDays)} 工作日)"
                        val detail = "${uiState.baseDate}  ➔  ${customEventDate}"
                        val regionTag = "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.nativeName}"

                        viewModel.saveToHistory(
                            category = "日期倒计时",
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

    val totalNaturalDays = DateCalculatorUtils.naturalDaysBetween(uiState.baseDate, effectiveEndDate)
    val totalWorkdays = DateCalculatorUtils.workdaysBetween(
        uiState.baseDate, effectiveEndDate, uiState.weekendRule, uiState.enableChineseHolidays, uiState.holidayRegion, uiState.isCurrentWeekBigWeek, uiState.disableChinaShiftWorkdays
    )
    val weekendDays = totalNaturalDays - totalWorkdays
    val periodStr = DateCalculatorUtils.formatPeriod(uiState.baseDate, effectiveEndDate)
    val totalWeeks = totalNaturalDays / 7.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.label_date_diff_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeumorphicTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1. 起始日期 Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.label_base_date_default), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 纯净无弹跳/绝对全精简的起始日期 Card
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
                showDiffResult = false
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. 目标日期功能区
        Text(stringResource(R.string.label_custom_target_date), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        NeumorphicSegmentedRow(
            items = listOf(stringResource(R.string.label_solar), stringResource(R.string.label_lunar)),
            selectedIndex = targetCalendarType,
            onIndexSelected = {
                targetCalendarType = it
                showDiffResult = false
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (targetCalendarType == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .neumorphicExtruded(shape = RoundedCornerShape(18.dp), elevation = 5.dp)
                            .background(NeumorphicBg, shape = RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { showPickerForEnd = true }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = NeumorphicAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            val dateFormatted = DateCalculatorUtils.formatDate(uiState.endDate)
                            Text(text = stringResource(R.string.label_base_date_fmt, dateFormatted), fontWeight = FontWeight.ExtraBold, color = NeumorphicTextPrimary, fontSize = 16.sp)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. 公历年份
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(58.dp)
                                .neumorphicInset(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                                .border(1.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = RoundedCornerShape(16.dp))
                                .background(NeumorphicBg, shape = RoundedCornerShape(16.dp))
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
                                    value = targetLunarYear.toString(),
                                    onValueChange = {
                                        targetLunarYear = it.toIntOrNull() ?: targetLunarYear
                                        showDiffResult = false
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // 2. 农历月份选择
                        var monthExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .neumorphicInset(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                                    .border(1.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = RoundedCornerShape(16.dp))
                                    .background(NeumorphicBg, shape = RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
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
                                        Text(text = LunarCalendarUtils.getLunarMonthName(targetLunarMonth), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
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
                                            .background(
                                                if (isCurrent) NeumorphicAccent.copy(alpha = 0.12f) else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                targetLunarMonth = m
                                                monthExpanded = false
                                                showDiffResult = false
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

                        // 3. 农历日期选择
                        var dayExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .neumorphicInset(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                                    .border(1.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = RoundedCornerShape(16.dp))
                                    .background(NeumorphicBg, shape = RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
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
                                        Text(text = LunarCalendarUtils.getLunarDayName(targetLunarDay), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
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
                                            .background(
                                                if (isCurrent) NeumorphicAccent.copy(alpha = 0.12f) else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                targetLunarDay = d
                                                dayExpanded = false
                                                showDiffResult = false
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
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // 蓝色凸起等于号按键
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
                        showDiffResult = true
                        val title = "相差 ${abs(totalNaturalDays)} 天 (${abs(totalWorkdays)} 工作日)"
                        val detail = "${uiState.baseDate}  ➔  ${effectiveEndDate}"
                        val regionTag = "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.nativeName}"
                        viewModel.saveToHistory(
                            category = "日期倒计时",
                            title = title,
                            detail = detail,
                            regionTag = regionTag,
                            resultDate = effectiveEndDate,
                            resultDays = totalNaturalDays
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("=", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. 常用倒计时功能区
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.label_preset_countdown),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = NeumorphicTextPrimary
            )
        }

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
            HolidayRegion.TAIWAN -> listOf(
                "🎆 开国纪念日" to calculateNextSolarDate(base, 1, 1),
                "🧧 春节" to calculateNextLunarDate(base, 1, 1),
                "🕊️ 228和平纪念日" to calculateNextSolarDate(base, 2, 28),
                "🧸 儿童节" to calculateNextSolarDate(base, 4, 4),
                "🌿 清明节" to calculateNextSolarDate(base, 4, 5),
                "🛠️ 劳动节" to calculateNextSolarDate(base, 5, 1),
                "🎏 端午节" to calculateNextLunarDate(base, 5, 5),
                "🥮 中秋节" to calculateNextLunarDate(base, 8, 15),
                "🇹🇼 国庆日" to calculateNextSolarDate(base, 10, 10)
            )
            HolidayRegion.HONG_KONG -> listOf(
                "🎆 元旦" to calculateNextSolarDate(base, 1, 1),
                "🧧 农历新年" to calculateNextLunarDate(base, 1, 1),
                "✝️ 耶稣受难节" to calculateNextSolarDate(base, 4, 3),
                "🌿 清明节" to calculateNextSolarDate(base, 4, 4),
                "🛠️ 劳动节" to calculateNextSolarDate(base, 5, 1),
                "☸️ 佛诞" to calculateNextLunarDate(base, 4, 8),
                "🎏 端午节" to calculateNextLunarDate(base, 5, 5),
                "🇭🇰 特区成立纪念日" to calculateNextSolarDate(base, 7, 1),
                "🥮 中秋节" to calculateNextLunarDate(base, 8, 15),
                "🇨🇳 国庆节" to calculateNextSolarDate(base, 10, 1),
                "🏔️ 重阳节" to calculateNextLunarDate(base, 9, 9),
                "🎄 圣诞节" to calculateNextSolarDate(base, 12, 25)
            )
            HolidayRegion.MACAO -> listOf(
                "🎆 元旦" to calculateNextSolarDate(base, 1, 1),
                "🧧 农历新年" to calculateNextLunarDate(base, 1, 1),
                "✝️ 耶稣受难节" to calculateNextSolarDate(base, 4, 3),
                "🌿 清明节" to calculateNextSolarDate(base, 4, 4),
                "🛠️ 劳动节" to calculateNextSolarDate(base, 5, 1),
                "☸️ 佛诞" to calculateNextLunarDate(base, 4, 8),
                "🎏 端午节" to calculateNextLunarDate(base, 5, 5),
                "🥮 中秋节" to calculateNextLunarDate(base, 8, 15),
                "🇨🇳 国庆节" to calculateNextSolarDate(base, 10, 1),
                "🏔️ 重阳节" to calculateNextLunarDate(base, 9, 9),
                "🇲🇴 特区成立纪念日" to calculateNextSolarDate(base, 12, 20),
                "🎄 圣诞节" to calculateNextSolarDate(base, 12, 25)
            )
            HolidayRegion.SINGAPORE -> listOf(
                "🎆 元旦" to calculateNextSolarDate(base, 1, 1),
                "🧧 农历新年" to calculateNextLunarDate(base, 1, 1),
                "✝️ 耶稣受难节" to calculateNextSolarDate(base, 4, 3),
                "☪️ 开斋节" to calculateNextSolarDate(base, 3, 31),
                "🛠️ 劳动节" to calculateNextSolarDate(base, 5, 1),
                "☸️ 卫塞节" to calculateNextSolarDate(base, 5, 12),
                "☪️ 哈芝节" to calculateNextSolarDate(base, 6, 7),
                "🇸🇬 国庆日" to calculateNextSolarDate(base, 8, 9),
                "🪔 屠妖节" to calculateNextSolarDate(base, 10, 20),
                "🎄 圣诞节" to calculateNextSolarDate(base, 12, 25)
            )
            HolidayRegion.MALAYSIA -> listOf(
                "🎆 元旦" to calculateNextSolarDate(base, 1, 1),
                "🧧 农历新年" to calculateNextLunarDate(base, 1, 1),
                "☪️ 开斋节" to calculateNextSolarDate(base, 3, 31),
                "🛠️ 劳动节" to calculateNextSolarDate(base, 5, 1),
                "☸️ 卫塞节" to calculateNextSolarDate(base, 5, 12),
                "👑 最高元首诞辰" to calculateNextSolarDate(base, 6, 2),
                "☪️ 哈芝节" to calculateNextSolarDate(base, 6, 7),
                "🇲🇾 独立日" to calculateNextSolarDate(base, 8, 31),
                "🇲🇾 马来西亚日" to calculateNextSolarDate(base, 9, 16),
                "🪔 屠妖节" to calculateNextSolarDate(base, 10, 20),
                "🎄 圣诞节" to calculateNextSolarDate(base, 12, 25)
            )
            HolidayRegion.VIETNAM -> listOf(
                "🎆 阳历新年" to calculateNextSolarDate(base, 1, 1),
                "🧧 越南春节" to calculateNextLunarDate(base, 1, 1),
                "👑 雄王祭祖日" to calculateNextLunarDate(base, 3, 10),
                "🇻🇳 南方解放日" to calculateNextSolarDate(base, 4, 30),
                "🛠️ 国际劳动节" to calculateNextSolarDate(base, 5, 1),
                "🇻🇳 国庆节" to calculateNextSolarDate(base, 9, 2)
            )
            HolidayRegion.JAPAN -> listOf(
                "🎆 元日" to calculateNextSolarDate(base, 1, 1),
                "🌸 成人の日" to calculateNextSolarDate(base, 1, 12),
                "🌸 建国記念の日" to calculateNextSolarDate(base, 2, 11),
                "🌸 天皇誕生日" to calculateNextSolarDate(base, 2, 23),
                "🌿 昭和の日" to calculateNextSolarDate(base, 4, 29),
                "🎏 憲法記念日" to calculateNextSolarDate(base, 5, 3),
                "🌿 みどりの日" to calculateNextSolarDate(base, 5, 4),
                "🎏 こどもの日" to calculateNextSolarDate(base, 5, 5),
                "🌊 海の日" to calculateNextSolarDate(base, 7, 20),
                "⛰️ 山の日" to calculateNextSolarDate(base, 8, 11),
                "🍁 敬老の日" to calculateNextSolarDate(base, 9, 21),
                "🍁 秋分の日" to calculateNextSolarDate(base, 9, 23),
                "🏃 스포츠の日" to calculateNextSolarDate(base, 10, 12),
                "🎨 文化の日" to calculateNextSolarDate(base, 11, 3),
                "🍂 勤労感謝の日" to calculateNextSolarDate(base, 11, 23)
            )
            HolidayRegion.SOUTH_KOREA -> listOf(
                "🎆 신정 (元旦)" to calculateNextSolarDate(base, 1, 1),
                "🧧 설날 (春节)" to calculateNextLunarDate(base, 1, 1),
                "🇰🇷 삼일절 (三一节)" to calculateNextSolarDate(base, 3, 1),
                "🧸 어린이날 (儿童节)" to calculateNextSolarDate(base, 5, 5),
                "☸️ 부처님 오신 날" to calculateNextLunarDate(base, 4, 8),
                "🌾 현충일 (显忠日)" to calculateNextSolarDate(base, 6, 6),
                "🇰🇷 광복절 (光复节)" to calculateNextSolarDate(base, 8, 15),
                "🥮 추석 (秋夕/中秋)" to calculateNextLunarDate(base, 8, 15),
                "🇰🇷 개천절 (开天节)" to calculateNextSolarDate(base, 10, 3),
                "🇰🇷 한글날 (韩文节)" to calculateNextSolarDate(base, 10, 9),
                "🎄 성탄절 (圣诞节)" to calculateNextSolarDate(base, 12, 25)
            )
            HolidayRegion.UNITED_KINGDOM -> listOf(
                "🎆 New Year's Day" to calculateNextSolarDate(base, 1, 1),
                "✝️ Good Friday" to calculateNextSolarDate(base, 4, 3),
                "✝️ Easter Monday" to calculateNextSolarDate(base, 4, 6),
                "🇬🇧 Early May Bank Holiday" to calculateNextSolarDate(base, 5, 4),
                "🇬🇧 Spring Bank Holiday" to calculateNextSolarDate(base, 5, 25),
                "🇬🇧 Summer Bank Holiday" to calculateNextSolarDate(base, 8, 31),
                "🎄 Christmas Day" to calculateNextSolarDate(base, 12, 25),
                "🎁 Boxing Day" to calculateNextSolarDate(base, 12, 26)
            )
            HolidayRegion.GERMANY -> listOf(
                "🎆 Neujahr" to calculateNextSolarDate(base, 1, 1),
                "✝️ Karfreitag" to calculateNextSolarDate(base, 4, 3),
                "✝️ Ostermontag" to calculateNextSolarDate(base, 4, 6),
                "🛠️ Tag der Arbeit" to calculateNextSolarDate(base, 5, 1),
                "✝️ Christi Himmelfahrt" to calculateNextSolarDate(base, 5, 14),
                "✝️ Pfingstmontag" to calculateNextSolarDate(base, 5, 25),
                "🇩🇪 Tag der Deutschen Einheit" to calculateNextSolarDate(base, 10, 3),
                "🎄 1. Weihnachtstag" to calculateNextSolarDate(base, 12, 25),
                "🎄 2. Weihnachtstag" to calculateNextSolarDate(base, 12, 26)
            )
            HolidayRegion.FRANCE -> listOf(
                "🎆 Jour de l'An" to calculateNextSolarDate(base, 1, 1),
                "✝️ Lundi de Pâques" to calculateNextSolarDate(base, 4, 6),
                "🛠️ Fête du Travail" to calculateNextSolarDate(base, 5, 1),
                "🎖️ Victoire 1945" to calculateNextSolarDate(base, 5, 8),
                "✝️ Ascension" to calculateNextSolarDate(base, 5, 14),
                "🇫🇷 Fête Nationale" to calculateNextSolarDate(base, 7, 14),
                "✝️ Assomption" to calculateNextSolarDate(base, 8, 15),
                "✝️ Toussaint" to calculateNextSolarDate(base, 11, 1),
                "🎖️ Armistice 1918" to calculateNextSolarDate(base, 11, 11),
                "🎄 Noël" to calculateNextSolarDate(base, 12, 25)
            )
            HolidayRegion.ITALY -> listOf(
                "🎆 Capodanno" to calculateNextSolarDate(base, 1, 1),
                "👑 Epifania" to calculateNextSolarDate(base, 1, 6),
                "✝️ Lunedì dell'Angelo" to calculateNextSolarDate(base, 4, 6),
                "🇮🇹 Festa della Liberazione" to calculateNextSolarDate(base, 4, 25),
                "🛠️ Festa del Lavoro" to calculateNextSolarDate(base, 5, 1),
                "🇮🇹 Festa della Repubblica" to calculateNextSolarDate(base, 6, 2),
                "☀️ Ferragosto" to calculateNextSolarDate(base, 8, 15),
                "✝️ Ognissanti" to calculateNextSolarDate(base, 11, 1),
                "🎄 Natale" to calculateNextSolarDate(base, 12, 25),
                "🎁 Santo Stefano" to calculateNextSolarDate(base, 12, 26)
            )
            HolidayRegion.INDIA -> listOf(
                "🎆 New Year's Day" to calculateNextSolarDate(base, 1, 1),
                "🇮🇳 Republic Day" to calculateNextSolarDate(base, 1, 26),
                "🎨 Holi" to calculateNextSolarDate(base, 3, 4),
                "✝️ Good Friday" to calculateNextSolarDate(base, 4, 3),
                "☪️ Eid al-Fitr" to calculateNextSolarDate(base, 3, 20),
                "🇮🇳 Independence Day" to calculateNextSolarDate(base, 8, 15),
                "🪔 Diwali" to calculateNextSolarDate(base, 11, 8),
                "🇮🇳 Gandhi Jayanti" to calculateNextSolarDate(base, 10, 2),
                "🎄 Christmas" to calculateNextSolarDate(base, 12, 25)
            )
            HolidayRegion.INDONESIA -> listOf(
                "🎆 Tahun Baru Masehi" to calculateNextSolarDate(base, 1, 1),
                "🧧 Tahun Baru Imlek" to calculateNextLunarDate(base, 1, 1),
                "🇮🇩 Nyepi" to calculateNextSolarDate(base, 3, 19),
                "✝️ Wafat Isa Almasih" to calculateNextSolarDate(base, 4, 3),
                "🛠️ Hari Buruh" to calculateNextSolarDate(base, 5, 1),
                "☸️ Hari Waisak" to calculateNextSolarDate(base, 5, 31),
                "🇮🇩 Hari Lahir Pancasila" to calculateNextSolarDate(base, 6, 1),
                "☪️ Idul Fitri" to calculateNextSolarDate(base, 3, 20),
                "🇮🇩 Hari Kemerdekaan RI" to calculateNextSolarDate(base, 8, 17),
                "🎄 Hari Natal" to calculateNextSolarDate(base, 12, 25)
            )
            HolidayRegion.UNITED_STATES -> listOf(
                "🎆 New Year's Day" to calculateNextSolarDate(base, 1, 1),
                "🕊️ MLK Day" to calculateNextSolarDate(base, 1, 19),
                "🇺🇸 Presidents' Day" to calculateNextSolarDate(base, 2, 16),
                "🎖️ Memorial Day" to calculateNextSolarDate(base, 5, 25),
                "🕊️ Juneteenth Day" to calculateNextSolarDate(base, 6, 19),
                "🇺🇸 Independence Day" to calculateNextSolarDate(base, 7, 4),
                "🛠️ Labor Day" to calculateNextSolarDate(base, 9, 7),
                "🌎 Columbus Day" to calculateNextSolarDate(base, 10, 12),
                "🎖️ Veterans Day" to calculateNextSolarDate(base, 11, 11),
                "🎃 Thanksgiving" to calculateNextSolarDate(base, 11, 26),
                "🎄 Christmas" to calculateNextSolarDate(base, 12, 25)
            )
            HolidayRegion.THAILAND -> listOf(
                "🎆 元旦" to calculateNextSolarDate(base, 1, 1),
                "☸️ 万佛节" to calculateNextSolarDate(base, 2, 26),
                "👑 扎克里王朝纪念日" to calculateNextSolarDate(base, 4, 6),
                "💦 宋干节 (泼水节)" to calculateNextSolarDate(base, 4, 13),
                "🛠️ 劳动节" to calculateNextSolarDate(base, 5, 1),
                "👑 泰王诞辰" to calculateNextSolarDate(base, 7, 28),
                "👑 母亲节" to calculateNextSolarDate(base, 8, 12),
                "👑 父亲节" to calculateNextSolarDate(base, 12, 5),
                "📜 宪法日" to calculateNextSolarDate(base, 12, 10)
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
                val isSelected = (targetCalendarType == 0 && uiState.endDate == targetDate)
                NeumorphicChip(
                    text = label,
                    selected = isSelected,
                    onClick = {
                        targetCalendarType = 0
                        viewModel.updateEndDate(targetDate)
                        currentTargetEventName = label
                        showDiffResult = true

                        // 预设节日芯片点击后自动生成历史记录
                        val natDays = DateCalculatorUtils.naturalDaysBetween(uiState.baseDate, targetDate)
                        val workDays = DateCalculatorUtils.workdaysBetween(
                            uiState.baseDate, targetDate, uiState.weekendRule, uiState.enableChineseHolidays, uiState.holidayRegion, uiState.isCurrentWeekBigWeek, uiState.disableChinaShiftWorkdays
                        )
                        val title = "$label: 相差 ${abs(natDays)} 天 (${abs(workDays)} 工作日)"
                        val detail = "${uiState.baseDate}  ➔  ${targetDate}"
                        val regionTag = "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.nativeName}"

                        viewModel.saveToHistory(
                            category = "日期倒计时",
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
                val isSelected = (targetCalendarType == 0 && uiState.endDate == effectiveTargetDate)
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
                                showDiffResult = true

                                // 自定义倒计时芯片点击后自动生成历史记录
                                val natDays = DateCalculatorUtils.naturalDaysBetween(uiState.baseDate, effectiveTargetDate)
                                val workDays = DateCalculatorUtils.workdaysBetween(
                                    uiState.baseDate, effectiveTargetDate, uiState.weekendRule, uiState.enableChineseHolidays, uiState.holidayRegion, uiState.isCurrentWeekBigWeek, uiState.disableChinaShiftWorkdays
                                )
                                val title = "${customEvent.name}: 相差 ${abs(natDays)} 天 (${abs(workDays)} 工作日)"
                                val detail = "${uiState.baseDate}  ➔  ${effectiveTargetDate}"
                                val regionTag = "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.nativeName}"

                                viewModel.saveToHistory(
                                    category = "日期倒计时",
                                    title = title,
                                    detail = detail,
                                    regionTag = regionTag,
                                    resultDate = effectiveTargetDate,
                                    resultDays = natDays
                                )
                            },
                            onLongClick = {
                                eventToDelete = customEvent
                            }
                        )
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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

            // "+ 新增倒计时" 胶囊按键放置在常用倒计时列表的最后一个位置
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
                Text(stringResource(R.string.label_add_custom_countdown), fontSize = 13.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 结果卡片展开
        AnimatedVisibility(
            visible = showDiffResult,
            enter = fadeIn(animationSpec = tween(150)) + expandVertically(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphicExtruded(shape = RoundedCornerShape(24.dp), elevation = 6.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f), shape = RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.label_result_diff_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val statusLabel = when {
                        totalNaturalDays > 0 -> stringResource(R.string.label_status_future, totalNaturalDays)
                        totalNaturalDays < 0 -> stringResource(R.string.label_status_past, abs(totalNaturalDays))
                        else -> stringResource(R.string.label_status_same)
                    }

                    SuggestionChip(
                        onClick = {},
                        shape = CircleShape,
                        label = { Text(statusLabel, fontWeight = FontWeight.Bold) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.label_days_diff), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text(
                                text = "${abs(totalNaturalDays)} ${stringResource(R.string.label_days)}",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.label_workdays_diff), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text(
                                text = "${abs(totalWorkdays)} ${stringResource(R.string.label_days)}",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(stringResource(R.string.label_period_len, periodStr), color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 12.sp)
                        Text(stringResource(R.string.label_weeks_len, String.format("%.1f", abs(totalWeeks))), color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 12.sp)
                        Text(stringResource(R.string.label_holiday_std, "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.nativeName}"), color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 12.sp)
                        Text(stringResource(R.string.label_rest_days_len, abs(weekendDays).toString()), color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 倒计时设置提醒按键
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
                            .background(NeumorphicBg, shape = CircleShape)
                            .clip(CircleShape)
                            .clickable { showReminderDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Alarm, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.padding(end = 6.dp))
                            Text(stringResource(R.string.label_set_reminder), color = NeumorphicTextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    NeumorphicCopyButton(
                        text = stringResource(R.string.label_copy_comparison),
                        onClick = {
                            val clipText = "Base ${uiState.baseDate} ➔ Target ${effectiveEndDate}: $totalNaturalDays days ($totalWorkdays workdays)"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("DateDiff", clipText))
                            Toast.makeText(context, context.getString(R.string.toast_copied), Toast.LENGTH_SHORT).show()
                        }
                    )
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
