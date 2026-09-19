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
import androidx.compose.animation.core.Animatable
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
import kotlinx.coroutines.launch
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
            Toast.makeText(context, "已成功弹出本地通知提醒！", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "需要通知权限以发送应用倒计时弹出提醒", Toast.LENGTH_SHORT).show()
        }
    }

    // 基准日期切换时的 Pop Bounce 缩放与渐变高亮动效
    val cardScale = remember { Animatable(1.0f) }
    val cardAlpha = remember { Animatable(1.0f) }

    LaunchedEffect(uiState.baseDate) {
        launch {
            cardScale.animateTo(1.04f, animationSpec = tween(100))
            cardScale.animateTo(1.00f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        }
        launch {
            cardAlpha.snapTo(0.4f)
            cardAlpha.animateTo(1.0f, animationSpec = tween(250))
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

    // 提醒方式选择弹窗 (支持 App 弹出通知提醒 + 运行时权限检查)
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

                    // 选项 1: App 弹出通知提醒 (若未授权则触发主动询问)
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
                                        // 首次/未授权时，自动询问是否授权通知权限
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

                    // 可选 Emoji 图标阵列
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
                    Text("请输入自定义事件名称并选择目标日期:", fontSize = 13.sp, color = NeumorphicTextPrimary)

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
        uiState.baseDate, effectiveEndDate, uiState.weekendRule, uiState.enableChineseHolidays, uiState.holidayRegion, uiState.isCurrentWeekBigWeek
    )
    val weekendDays = totalNaturalDays - totalWorkdays
    val periodStr = DateCalculatorUtils.formatPeriod(uiState.baseDate, effectiveEndDate)
    val totalWeeks = totalNaturalDays / 7.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.label_date_diff_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NeumorphicTextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 基准日期 Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.label_base_date_default), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
                    .background(NeumorphicBg, shape = CircleShape)
                    .clip(CircleShape)
                    .clickable {
                        viewModel.setToday()
                        showDiffResult = false
                    }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.label_set_today), fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 显眼放大版新拟物基准起始日期 Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = cardScale.value
                    scaleY = cardAlpha.value
                }
                .neumorphicExtruded(shape = RoundedCornerShape(20.dp), elevation = 5.dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f), shape = RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .clickable { showPickerForStart = true }
                .padding(horizontal = 16.dp, vertical = 14.dp)
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
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        Text(
                            text = "选择起始日期",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeumorphicAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val dateFormattedWithWeek = DateCalculatorUtils.formatDateWithWeek(uiState.baseDate)
                        Text(
                            text = dateFormattedWithWeek,
                            fontSize = 18.sp,
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

        Spacer(modifier = Modifier.height(16.dp))

        // 自动响应不同国家/地区所对应的热门倒计时节日快捷项
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.label_preset_countdown),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = NeumorphicTextPrimary
            )

            Box(
                modifier = Modifier
                    .height(32.dp)
                    .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                    .background(NeumorphicBg, shape = CircleShape)
                    .clip(CircleShape)
                    .clickable { showAddCustomDialog = true }
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.label_add_custom_countdown), fontSize = 11.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val base = uiState.baseDate
        // 根据切换的地区，自动替换为对应国家的热门倒计时节日
        val presetCountdowns = when (uiState.holidayRegion) {
            HolidayRegion.CHINA -> listOf(
                stringResource(R.string.preset_national_day) to calculateNextSolarDate(base, 10, 1),
                stringResource(R.string.preset_new_year) to calculateNextSolarDate(base, 1, 1),
                stringResource(R.string.preset_spring_festival) to calculateNextLunarDate(base, 1, 1),
                stringResource(R.string.preset_mid_autumn) to calculateNextLunarDate(base, 8, 15),
                stringResource(R.string.preset_dragon_boat) to calculateNextLunarDate(base, 5, 5),
                stringResource(R.string.preset_gaokao) to calculateNextSolarDate(base, 6, 7),
                "📚 中考" to calculateNextSolarDate(base, 6, 21)
            )
            HolidayRegion.TAIWAN -> listOf(
                "🎆 元旦" to calculateNextSolarDate(base, 1, 1),
                "🧧 春节" to calculateNextLunarDate(base, 1, 1),
                "🕊️ 228和平纪念日" to calculateNextSolarDate(base, 2, 28),
                "🧸 儿童节" to calculateNextSolarDate(base, 4, 4),
                "🌿 清明节" to calculateNextSolarDate(base, 4, 5),
                "🎏 端午节" to calculateNextLunarDate(base, 5, 5),
                "🥮 中秋节" to calculateNextLunarDate(base, 8, 15),
                "🇹🇼 国庆日" to calculateNextSolarDate(base, 10, 10)
            )
            HolidayRegion.HONG_KONG -> listOf(
                "🎆 元旦" to calculateNextSolarDate(base, 1, 1),
                "🧧 农历新年" to calculateNextLunarDate(base, 1, 1),
                "🎏 端午节" to calculateNextLunarDate(base, 5, 5),
                "🇭🇰 特区成立纪念日" to calculateNextSolarDate(base, 7, 1),
                "🥮 中秋节" to calculateNextLunarDate(base, 8, 15),
                "🇨🇳 国庆节" to calculateNextSolarDate(base, 10, 1),
                "🎄 圣诞节" to calculateNextSolarDate(base, 12, 25)
            )
            HolidayRegion.MACAO -> listOf(
                "🎆 元旦" to calculateNextSolarDate(base, 1, 1),
                "🧧 农历新年" to calculateNextLunarDate(base, 1, 1),
                "🎏 端午节" to calculateNextLunarDate(base, 5, 5),
                "🥮 中秋节" to calculateNextLunarDate(base, 8, 15),
                "🇨🇳 国庆节" to calculateNextSolarDate(base, 10, 1),
                "🇲🇴 特区成立纪念日" to calculateNextSolarDate(base, 12, 20),
                "🎄 圣诞节" to calculateNextSolarDate(base, 12, 25)
            )
            HolidayRegion.JAPAN -> listOf(
                "🎆 元日" to calculateNextSolarDate(base, 1, 1),
                "🌸 成人の日" to calculateNextSolarDate(base, 1, 13),
                "🌸 建国記念の日" to calculateNextSolarDate(base, 2, 11),
                "🌸 天皇誕生日" to calculateNextSolarDate(base, 2, 23),
                "🎏 憲法記念日" to calculateNextSolarDate(base, 5, 3),
                "🎏 こどもの日" to calculateNextSolarDate(base, 5, 5),
                "🍁 敬老の日" to calculateNextSolarDate(base, 9, 15),
                "🍂 勤労感謝の日" to calculateNextSolarDate(base, 11, 23)
            )
            HolidayRegion.SOUTH_KOREA -> listOf(
                "🎆 신정 (元旦)" to calculateNextSolarDate(base, 1, 1),
                "🧧 설날 (春节)" to calculateNextLunarDate(base, 1, 1),
                "🇰🇷 삼일절 (三一节)" to calculateNextSolarDate(base, 3, 1),
                "🧸 어린이날 (儿童节)" to calculateNextSolarDate(base, 5, 5),
                "🌾 현충일 (显忠日)" to calculateNextSolarDate(base, 6, 6),
                "🇰🇷 광복절 (光复节)" to calculateNextSolarDate(base, 8, 15),
                "🥮 추석 (中秋/秋夕)" to calculateNextLunarDate(base, 8, 15),
                "🇰🇷 개천절 (开天节)" to calculateNextSolarDate(base, 10, 3)
            )
            HolidayRegion.UNITED_STATES -> listOf(
                "🎆 New Year's Day" to calculateNextSolarDate(base, 1, 1),
                "🕊️ MLK Day" to calculateNextSolarDate(base, 1, 20),
                "🇺🇸 Independence Day" to calculateNextSolarDate(base, 7, 4),
                "🛠️ Labor Day" to calculateNextSolarDate(base, 9, 1),
                "🎃 Thanksgiving" to calculateNextSolarDate(base, 11, 26),
                "🎄 Christmas" to calculateNextSolarDate(base, 12, 25)
            )
            HolidayRegion.THAILAND -> listOf(
                "🎆 วันขึ้นปีใหม่ (元旦)" to calculateNextSolarDate(base, 1, 1),
                "💦 วันสงกรานต์ (泼水节)" to calculateNextSolarDate(base, 4, 13),
                "👑 วันเฉลิมพระชนมพรรษา" to calculateNextSolarDate(base, 7, 28),
                "👑 วันแม่แห่งชาติ (母亲节)" to calculateNextSolarDate(base, 8, 12),
                "👑 วันพ่อแห่งชาติ (父亲节)" to calculateNextSolarDate(base, 12, 5)
            )
            else -> listOf(
                stringResource(R.string.preset_new_year) to calculateNextSolarDate(base, 1, 1),
                stringResource(R.string.preset_national_day) to calculateNextSolarDate(base, 10, 1)
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 系统内置按当前国家/地区自动转换的节假日快捷项
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
                    },
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            // 用户自定义特定日期 Chip (支持自选 Emoji 图标与周期推算)
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
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(12.dp))

        // 目标特定日期选框与等于号按键
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

            // 新拟物蓝色凸起等于号按键 (=)
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
                        val title = "倒计时: ${abs(totalNaturalDays)}天 (${abs(totalWorkdays)}工作日)"
                        val detail = "${uiState.baseDate} ➔ $effectiveEndDate [${uiState.holidayRegion.label}]"
                        viewModel.saveToHistory(title, detail, effectiveEndDate, totalNaturalDays)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("=", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }

        if (targetCalendarType == 0) {
            Spacer(modifier = Modifier.height(6.dp))
            QuickDateChips(
                selectedDate = uiState.endDate,
                onSelectDate = { date ->
                    viewModel.updateEndDate(date)
                    showDiffResult = true // 点击直接输出结果
                    val title = "倒计时: ${abs(DateCalculatorUtils.naturalDaysBetween(uiState.baseDate, date))}天"
                    val detail = "${uiState.baseDate} ➔ $date"
                    viewModel.saveToHistory(title, detail, date)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 未按等于号时结果整个UI不显示，按下后以 150ms 动画渐进展开
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
                    .padding(20.dp)
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

                    Spacer(modifier = Modifier.height(12.dp))

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

                    Spacer(modifier = Modifier.height(16.dp))

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

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(stringResource(R.string.label_period_len, periodStr), color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text(stringResource(R.string.label_weeks_len, String.format("%.1f", abs(totalWeeks))), color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text(stringResource(R.string.label_holiday_std, "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.nativeName}"), color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text(stringResource(R.string.label_rest_days_len, abs(weekendDays).toString()), color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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

                    Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(24.dp))
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
