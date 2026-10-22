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
import androidx.compose.foundation.Image
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
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.graphicsLayer
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
import me.paco.datecalculator.ui.components.NeumorphicIconHeaderBadge
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
import me.paco.datecalculator.ui.components.QuickDateChips
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.SolidColor
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
import me.paco.datecalculator.data.RegionalHolidays
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

data class HolidayThemePattern(
    val photoResId: Int,
    val overlayMaskColor: Color,
    val accentColor: Color
)

@Composable
private fun getHolidayThemePattern(label: String, isDark: Boolean): HolidayThemePattern {
    val clean = label.replace(Regex("[^\\u4e00-\\u9fa5a-zA-Z]"), "").lowercase()
    val maskAlpha = if (isDark) 0.35f else 0.22f

    val resId = when {
        // 1. 用户 10 款指定原图超优先匹配 (Strict User Overrides)
        clean.contains("中秋") || clean.contains("추석") -> R.drawable.bg_flat_autumn_leaves
        clean.contains("元旦") || clean.contains("跨年") || clean.contains("newyear") || clean.contains("신정") || clean.contains("元日") -> R.drawable.bg_flat_new_year
        clean.contains("春") || clean.contains("年") || clean.contains("설날") || clean.contains("tết") -> R.drawable.bg_flat_spring_festival
        clean.contains("清明") -> R.drawable.bg_flat_qingming
        clean.contains("劳动") || clean.contains("五一") || clean.contains("勞動") || clean.contains("labor") || clean.contains("labour") || clean.contains("勤労") -> R.drawable.bg_flat_labor_day
        clean.contains("高考") || clean.contains("수능") -> R.drawable.bg_flat_gaokao
        clean.contains("中考") -> R.drawable.bg_flat_zhongkao
        clean.contains("国庆") || clean.contains("國慶") -> R.drawable.bg_flat_national_china
        clean.contains("independence") -> R.drawable.bg_flat_national_us
        clean.contains("特区成立") || clean.contains("特區成立") || clean.contains("香港") -> R.drawable.bg_flat_hk_skyline

        // 2. 本地 28 款 VectorDrawable SVG 矢量素材全量关联映射 (Local SVG Vector Overrides)
        clean.contains("澳门") || clean.contains("澳門") -> R.drawable.bg_macau_city
        clean.contains("端午") -> R.drawable.bg_dragon_boat
        clean.contains("复活") -> R.drawable.bg_easter
        clean.contains("佛诞") || clean.contains("佛誕") || clean.contains("卫塞") || clean.contains("衛塞") || clean.contains("水灯") -> R.drawable.bg_lotus
        clean.contains("狂欢") -> R.drawable.bg_carnival
        clean.contains("洒红") -> R.drawable.bg_holi
        clean.contains("静居") -> R.drawable.bg_temple
        clean.contains("万佛") -> R.drawable.bg_temple
        clean.contains("anzac") || clean.contains("军团") -> R.drawable.bg_poppy
        clean.contains("matariki") || clean.contains("毛利新年") -> R.drawable.bg_matariki
        clean.contains("感恩") || clean.contains("thanksgiving") -> R.drawable.bg_thanksgiving
        clean.contains("万圣") || clean.contains("萬聖") || clean.contains("halloween") -> R.drawable.bg_halloween
        clean.contains("儿童") || clean.contains("兒童") || clean.contains("어린이") -> R.drawable.bg_children
        clean.contains("圣诞") || clean.contains("聖誕") || clean.contains("christmas") -> R.drawable.bg_christmas
        clean.contains("山之日") || clean.contains("山の日") -> R.drawable.bg_mountain_day
        clean.contains("屠妖") || clean.contains("diwali") || clean.contains("deepavali") -> R.drawable.bg_diwali
        clean.contains("开斋") || clean.contains("哈芝") || clean.contains("伊斯兰新年") || clean.contains("先知") -> R.drawable.bg_eid
        clean.contains("成人の日") || clean.contains("樱花") -> R.drawable.bg_flat_sakura
        clean.contains("建国記念") || clean.contains("天皇") || clean.contains("鳥居") -> R.drawable.bg_flat_torii
        clean.contains("こどもの日") || clean.contains("鲤鱼旗") -> R.drawable.bg_flat_carp
        clean.contains("한글날") || clean.contains("광복절") || clean.contains("삼일절") -> R.drawable.bg_flat_korea_flower
        clean.contains("canada") || clean.contains("加拿大") -> R.drawable.bg_flat_canada_maple
        clean.contains("songkran") || clean.contains("泼水") -> R.drawable.bg_flat_songkran
        clean.contains("australia") || clean.contains("澳大利亚") -> R.drawable.bg_flat_australia
        clean.contains("海之日") || clean.contains("夏") || clean.contains("summer") -> R.drawable.bg_summer
        clean.contains("重阳") || clean.contains("重陽") || clean.contains("丰收") || clean.contains("退伍军人") -> R.drawable.bg_harvest
        else -> R.drawable.bg_fireworks
    }

    val accent = when {
        clean.contains("春") || clean.contains("国庆") || clean.contains("圣诞") -> Color(0xFFEF4444)
        clean.contains("清明") || clean.contains("高考") || clean.contains("劳动") -> Color(0xFF10B981)
        clean.contains("特区") || clean.contains("元旦") -> Color(0xFF3B82F6)
        clean.contains("中秋") || clean.contains("感恩") || clean.contains("万圣") -> Color(0xFFD97706)
        else -> NeumorphicAccent
    }

    return HolidayThemePattern(
        photoResId = resId,
        overlayMaskColor = accent.copy(alpha = maskAlpha),
        accentColor = accent
    )
}

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
    val effectiveLang = lang.getEffectiveLanguage()

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
        val customTitlePlaceholder = when (effectiveLang) {
            AppLanguage.ENGLISH -> "e.g. Exam / Concert / Vacation"
            AppLanguage.JAPANESE -> "例: 試験 / コンサート / 旅行"
            AppLanguage.KOREAN -> "예: 시험 / 콘서트 / 휴가"
            AppLanguage.TRADITIONAL_CHINESE -> "如: 考研倒數 / 演唱會 / 假期"
            else -> "例如: 考研倒计时 / 看演唱会 / 毕业旅行"
        }

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

                    // 1. 倒数日名称
                    Text(LanguageUtils.getString("anniversary_name", lang), fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 3.dp)
                            .border(1.dp, NeumorphicAccent.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                            .background(NeumorphicSunkenBg, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (customNameInput.isEmpty()) {
                            Text(
                                text = customTitlePlaceholder,
                                fontSize = 13.sp,
                                color = NeumorphicTextPrimary.copy(alpha = 0.40f)
                            )
                        }
                        BasicTextField(
                            value = customNameInput,
                            onValueChange = { customNameInput = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary),
                            cursorBrush = SolidColor(NeumorphicAccent),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 2. 目标日期选择框
                    Text(LanguageUtils.getString("target_date", lang), fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                    val inputShape14 = RoundedCornerShape(14.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .neumorphicInset(shape = inputShape14, elevation = 3.dp)
                            .border(1.dp, NeumorphicAccent.copy(alpha = 0.20f), shape = inputShape14)
                            .background(NeumorphicBg, shape = inputShape14)
                            .clip(inputShape14)
                            .clickable { showCustomDatePicker = true }
                            .padding(horizontal = 12.dp),
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
                                    modifier = Modifier.padding(end = 8.dp).size(18.dp)
                                )
                                Text(
                                    text = DateCalculatorUtils.formatDateWithWeek(customDateInput, lang),
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeumorphicTextPrimary
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = "Select Date",
                                tint = NeumorphicAccent.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        EventRepeatMode.entries.forEach { mode ->
                            val isSel = selectedRepeatMode == mode
                            val modeLabel = mode.getLocalizedName(lang)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) NeumorphicAccent else NeumorphicSunkenBg)
                                    .clickable { selectedRepeatMode = mode },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(modeLabel, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else NeumorphicTextPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (customNameInput.isNotBlank()) {
                            val fullName = "$selectedEmoji $customNameInput"
                            viewModel.addCustomEvent(
                                name = fullName,
                                targetDate = customDateInput,
                                iconEmoji = selectedEmoji,
                                repeatMode = selectedRepeatMode,
                                isPinned = true,
                                context = context
                            )
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
                        viewModel.deleteCustomEvent(targetEvent, context)
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
                .padding(16.dp)
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
                    NeumorphicIconHeaderBadge(
                        icon = Icons.Default.EventRepeat,
                        size = 28.dp,
                        iconSize = 14.dp
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
                    NeumorphicIconButton(
                        icon = Icons.Default.History,
                        onClick = { showHistoryDialog = true },
                        contentDescription = LanguageUtils.getString("history_title", lang),
                        size = 32.dp
                    )

                    NeumorphicIconButton(
                        icon = Icons.Default.Settings,
                        onClick = { showSettingsDialog = true },
                        contentDescription = LanguageUtils.getString("settings_title", lang),
                        size = 32.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 预计算 19 个地区全量预设常用节假日列表
            val base = uiState.baseDate
            val rawPresets = RegionalHolidays.getPresetCountdowns(uiState.holidayRegion, base)

            // 汇总全量置顶固定倒数日（包含联动同步的自定义事件与置顶的常用节假日）
            val allPinnedCards = mutableListOf<Pair<CustomEventItem?, Pair<String, LocalDate>>>()

            // 1. 自定义或同步建立的置顶事件
            uiState.customEvents.filter { it.isPinned }.forEach { item ->
                allPinnedCards.add(Pair(item, item.name to item.getNextUpcomingDate(uiState.baseDate)))
            }

            // 2. 常用节假日被置顶的事件
            rawPresets.filter { (label, _) -> uiState.pinnedPresetHolidays.contains(label) }.forEach { (label, date) ->
                val localizedName = LanguageUtils.getLocalizedHolidayName(label, lang)
                if (allPinnedCards.none { it.second.first == localizedName || it.second.first == label }) {
                    allPinnedCards.add(Pair(null, localizedName to date))
                }
            }

            val cardColorPalette = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                Color(0xFFD1FAE5),
                Color(0xFFFFEDD5),
                Color(0xFFF3E8FF),
                Color(0xFFFCE7F3)
            )

            if (allPinnedCards.isNotEmpty()) {
                Text(LanguageUtils.getString("fixed_countdown", lang), fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp, color = NeumorphicAccent)
                Spacer(modifier = Modifier.height(6.dp))

                allPinnedCards.forEachIndexed { pIdx, (customItem, pair) ->
                    val (pinnedName, upcoming) = pair
                    val diffDays = DateCalculatorUtils.naturalDaysBetween(uiState.baseDate, upcoming)
                    val cardBgColor = if (isDark) {
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                            Color(0xFF065F46).copy(alpha = 0.40f),
                            Color(0xFF7C2D12).copy(alpha = 0.40f),
                            Color(0xFF581C87).copy(alpha = 0.40f),
                            Color(0xFF831843).copy(alpha = 0.40f)
                        )[pIdx % 5]
                    } else {
                        cardColorPalette[pIdx % cardColorPalette.size]
                    }

                    val cardShape = RoundedCornerShape(22.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .neumorphicExtruded(shape = cardShape, elevation = 5.dp)
                            .background(NeumorphicBg, shape = cardShape)
                            .background(cardBgColor, shape = cardShape)
                            .border(1.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = cardShape)
                            .clip(cardShape)
                            .clickable {
                                targetCalendarType = 0
                                viewModel.updateEndDate(upcoming)
                                currentTargetEventName = pinnedName
                                addResultCard(pinnedName, upcoming)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.PushPin, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(pinnedName, fontWeight = FontWeight.ExtraBold, fontSize = 14.5.sp, color = NeumorphicTextPrimary)
                                    Text("${LanguageUtils.getString("target_date", lang)}: ${DateCalculatorUtils.formatDate(upcoming, lang)}", fontSize = 11.5.sp, color = NeumorphicTextPrimary.copy(alpha = 0.65f))
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${abs(diffDays)} ${LanguageUtils.getString("days_unit", lang)}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent)

                                Spacer(modifier = Modifier.width(10.dp))

                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Unpin",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable {
                                            if (customItem != null) {
                                                viewModel.togglePinCustomEvent(customItem, context)
                                            } else {
                                                val rawLabel = rawPresets.firstOrNull { LanguageUtils.getLocalizedHolidayName(it.first, lang) == pinnedName }?.first ?: pinnedName
                                                viewModel.togglePinPresetHoliday(rawLabel, context)
                                            }
                                        }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // 1. 起始日期 Header (高对比高清文本)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageUtils.getString("select_start_date", lang),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeumorphicTextPrimary
                )

                SolarLunarSwitch(
                    isSolar = (startCalendarType == 0),
                    onCalendarTypeChanged = { isSolar ->
                        startCalendarType = if (isSolar) 0 else 1
                    },
                    language = lang
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (startCalendarType == 0) {
                val inputShape = RoundedCornerShape(18.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .neumorphicInset(shape = inputShape, elevation = 4.dp)
                        .border(1.dp, NeumorphicAccent.copy(alpha = 0.20f), shape = inputShape)
                        .background(NeumorphicBg, shape = inputShape)
                        .clip(inputShape)
                        .clickable { showPickerForStart = true }
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
                                text = "${LanguageUtils.getString("base_date", lang)}: ${DateCalculatorUtils.formatDateWithWeek(uiState.baseDate, lang)}",
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

            Spacer(modifier = Modifier.height(8.dp))

            // 统一调取 QuickDateChips 全组件，确保 100% 视觉对齐 RoundedCornerShape(14.dp) 样式
            QuickDateChips(
                selectedDate = uiState.baseDate,
                onSelectDate = { date -> viewModel.updateBaseDate(date) },
                language = lang
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. 目标日期 Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageUtils.getString("select_target_date", lang),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeumorphicTextPrimary
                )

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
                val inputShape = RoundedCornerShape(18.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .neumorphicInset(shape = inputShape, elevation = 4.dp)
                        .border(1.dp, NeumorphicAccent.copy(alpha = 0.20f), shape = inputShape)
                        .background(NeumorphicBg, shape = inputShape)
                        .clip(inputShape)
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

                Spacer(modifier = Modifier.width(10.dp))

                val btnShape = RoundedCornerShape(18.dp)
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(58.dp)
                        .neumorphicExtruded(shape = btnShape, elevation = 5.dp)
                        .background(Color(0xFFEF4444), shape = btnShape)
                        .clip(btnShape)
                        .clickable {
                            val targetName = if (currentTargetEventName.isNotBlank()) currentTargetEventName else LanguageUtils.getString("target_date", lang)
                            addResultCard(targetName, effectiveEndDate)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("=", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. 常用倒数日功能区 (3 栏等宽矩阵对齐 + 纯文字取消 Emoji + 节日专属半透明气场背景)
            Text(
                text = LanguageUtils.getString("common_countdown", lang),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = NeumorphicTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            val presetCountdowns = rawPresets.filterNot { (label, _) ->
                uiState.disabledPresetHolidays.contains(label)
            }

            val chipShape14 = RoundedCornerShape(14.dp)
            val isDark = LocalDarkTheme.current

            // 矩阵排版：每行 3 个 100% 相同高宽按键 (3 栏等宽矩阵，取消 Emoji，带有节日专属半透明气场背景)
            val rows = presetCountdowns.chunked(3)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowItems.forEach { (label, targetDate) ->
                            val isPinned = uiState.pinnedPresetHolidays.contains(label)
                            val isSelected = resultCardList.firstOrNull()?.eventName == label

                            // 100% 彻底清洗全量 Emoji，纯粹地道文字
                            val rawLocalized = LanguageUtils.getLocalizedHolidayName(label, lang)
                            val cleanName = rawLocalized.replace(Regex("[\\x{1F300}-\\x{1F9FF}\\x{2600}-\\x{26FF}\\x{2700}-\\x{27BF}\\x{1F600}-\\x{1F64F}\\x{1F680}-\\x{1F6FF}\\x{1F1E6}-\\x{1F1FF}\\u2600-\\u27BF]"), "").trim()

                            val pattern = getHolidayThemePattern(label, isDark)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .neumorphicExtruded(shape = chipShape14, elevation = 4.dp)
                                    .background(NeumorphicBg, shape = chipShape14)
                                    .border(
                                        width = if (isSelected) 1.8.dp else 1.dp,
                                        color = if (isSelected) pattern.accentColor else pattern.accentColor.copy(alpha = 0.30f),
                                        shape = chipShape14
                                    )
                                    .clip(chipShape14)
                                    .combinedClickable(
                                        onClick = {
                                            targetCalendarType = 0
                                            viewModel.updateEndDate(targetDate)
                                            currentTargetEventName = cleanName
                                            addResultCard(cleanName, targetDate)
                                        },
                                        onLongClick = {
                                            viewModel.togglePinPresetHoliday(label, context)
                                            val msg = if (isPinned) "Unpinned" else "Pinned!"
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // 1. 无版权风险 Unsplash CC0 100% 真实照片局部贴图 (如秋分/中秋金黄秋叶、清明春绿、国庆红金)
                                Image(
                                    painter = painterResource(pattern.photoResId),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    alpha = if (isDark) 0.60f else 0.50f,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // 2. 柔和颜色遮罩，确保前景文字 100% 绝对清晰易读
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(pattern.overlayMaskColor)
                                )

                                // 3. 前景层：纯正优雅文字 (100% 彻底清洗任何 Emoji，纯粹干练)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = cleanName,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = if (lang.isChineseLocale) 12.sp else 10.5.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isPinned) {
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("📌", fontSize = 9.sp)
                                    }
                                }
                            }
                        }

                        // 若最后一行不足 3 项：补充等宽空位，保持所有卡片尺寸 100% 绝对统一！
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                // 自定义添加按键
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                        .background(NeumorphicBg, shape = CircleShape)
                        .clip(CircleShape)
                        .clickable { showAddCustomDialog = true }
                        .padding(horizontal = 14.dp),
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

            // 4. 多卡片推栈倒计时结果展示层 (纯图标固定按钮，无文字，多语言全覆盖，36sp ExtraBold 巨幕数字)
            if (resultCardList.isNotEmpty()) {
                resultCardList.forEachIndexed { cardIdx, cardData ->
                    val natDays = DateCalculatorUtils.naturalDaysBetween(cardData.baseDate, cardData.targetDate)
                    val workDays = DateCalculatorUtils.workdaysBetween(
                        cardData.baseDate, cardData.targetDate, uiState.weekendRule, uiState.enableChineseHolidays, uiState.holidayRegion, uiState.isCurrentWeekBigWeek, uiState.disableChinaShiftWorkdays
                    )
                    val isPast = cardData.targetDate.isBefore(cardData.baseDate)
                    val isCardPinned = uiState.pinnedPresetHolidays.contains(cardData.eventName) || uiState.customEvents.any { (it.name == cardData.eventName || it.name.contains(cardData.eventName)) && it.isPinned }

                    val resultCardShape = RoundedCornerShape(22.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .neumorphicExtruded(shape = resultCardShape, elevation = 6.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f), shape = resultCardShape)
                            .border(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f), shape = resultCardShape)
                            .clip(resultCardShape)
                            .padding(18.dp)
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
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                                            .background(if (isCardPinned) NeumorphicAccent else NeumorphicBg, shape = CircleShape)
                                            .clip(CircleShape)
                                            .clickable {
                                                val fullName = cardData.eventName
                                                val foundCustom = uiState.customEvents.find { it.name == fullName || it.name.contains(cardData.eventName) }
                                                if (foundCustom != null) {
                                                    viewModel.togglePinCustomEvent(foundCustom, context)
                                                } else {
                                                    viewModel.togglePinPresetHoliday(fullName, context)
                                                }
                                                val msg = if (isCardPinned) "Unpinned" else "Pinned!"
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PushPin,
                                            contentDescription = "Pin",
                                            tint = if (isCardPinned) Color.White else NeumorphicAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

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
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "${abs(natDays)} ${LanguageUtils.getString("days_unit", lang)}",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val chipLabelText = if (isPast) {
                                when (lang.getEffectiveLanguage()) {
                                    AppLanguage.SIMPLIFIED_CHINESE, AppLanguage.TRADITIONAL_CHINESE -> "${LanguageUtils.getString("days_passed", lang)} ${abs(natDays)} 天"
                                    AppLanguage.JAPANESE -> "${abs(natDays)} 日経過"
                                    AppLanguage.KOREAN -> "${abs(natDays)} 일 지나옴"
                                    else -> "Passed ${abs(natDays)} days"
                                }
                            } else {
                                "≈ ${abs(workDays)} ${LanguageUtils.getString("workday", lang)}"
                            }

                            SuggestionChip(
                                onClick = {},
                                shape = CircleShape,
                                label = {
                                    Text(
                                        text = chipLabelText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "${LanguageUtils.getString("base_date", lang)}: ${DateCalculatorUtils.formatDate(cardData.baseDate, lang)}  ➔  ${LanguageUtils.getString("target_date", lang)}: ${DateCalculatorUtils.formatDate(cardData.targetDate, lang)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
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
    var candidate = LocalDate.of(baseDate.year, month, day)
    if (candidate.isBefore(baseDate)) {
        candidate = candidate.plusYears(1)
    }
    return candidate
}

private fun calculateNextLunarDate(baseDate: LocalDate, lunarMonth: Int, lunarDay: Int): LocalDate {
    var year = baseDate.year
    var candidate = LunarCalendarUtils.lunarToSolar(year, lunarMonth, lunarDay, false) ?: baseDate
    if (candidate.isBefore(baseDate)) {
        candidate = LunarCalendarUtils.lunarToSolar(year + 1, lunarMonth, lunarDay, false) ?: baseDate.plusYears(1)
    }
    return candidate
}
