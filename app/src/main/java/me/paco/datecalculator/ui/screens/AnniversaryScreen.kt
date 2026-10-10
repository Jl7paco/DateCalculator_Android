package me.paco.datecalculator.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import me.paco.datecalculator.R
import me.paco.datecalculator.data.AnniversaryItem
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
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LanguageUtils
import me.paco.datecalculator.util.LocationUtils
import me.paco.datecalculator.util.ShareUtils
import me.paco.datecalculator.util.WeatherUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun AnniversaryScreen(
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isDark = LocalDarkTheme.current
    val lang = uiState.appLanguage
    val effectiveLang = lang.getEffectiveLanguage()

    var showAddDialog by remember { mutableStateOf(false) }
    var showCheckInDialog by remember { mutableStateOf(false) }

    var titleInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf(LocalDate.now()) }
    var remarkInput by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    var selectedEmoji by remember { mutableStateOf("❤️") }
    var linkToCountdown by remember { mutableStateOf(true) }

    var itemToDelete by remember { mutableStateOf<AnniversaryItem?>(null) }
    var selectedDetailItem by remember { mutableStateOf<AnniversaryItem?>(null) }

    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val bounceAnim = remember { Animatable(1.0f) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            viewModel.fetchCurrentGpsLocation(context)
        }
    }

    if (showDatePicker) {
        DatePickerModal(
            selectedDate = dateInput,
            onDateSelected = { date ->
                dateInput = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
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

    // 新增纪念日 Dialog
    if (showAddDialog) {
        val dialogBg = if (isDark) MaterialTheme.colorScheme.surface else NeumorphicBg
        val remarkLabel = when (effectiveLang) {
            AppLanguage.ENGLISH -> "Notes & Wishes"
            AppLanguage.JAPANESE -> "一言メモ"
            AppLanguage.KOREAN -> "메모"
            AppLanguage.TRADITIONAL_CHINESE -> "備註與想說的話"
            else -> "备注与想说的话"
        }
        val syncLabel = when (effectiveLang) {
            AppLanguage.ENGLISH -> "Pin to Custom Countdowns"
            AppLanguage.JAPANESE -> "カウントダウンにピン留め固定"
            AppLanguage.KOREAN -> "디데이에 고정"
            AppLanguage.TRADITIONAL_CHINESE -> "聯動固定到自定義紀念日與倒數日"
            else -> "联动固定到自定义纪念日与倒数日"
        }

        val titlePlaceholder = when (effectiveLang) {
            AppLanguage.ENGLISH -> "e.g. Wedding Anniversary / Birthday"
            AppLanguage.JAPANESE -> "例: 結婚記念日 / 誕生日"
            AppLanguage.KOREAN -> "예: 결혼 기념일 / 생일"
            AppLanguage.TRADITIONAL_CHINESE -> "如: 戀愛紀念日 / 生日"
            else -> "例如: 恋爱纪念日 / 结婚纪念日 / 生日"
        }

        val remarkPlaceholder = when (effectiveLang) {
            AppLanguage.ENGLISH -> "Write a sweet note..."
            AppLanguage.JAPANESE -> "一言メモを記入..."
            AppLanguage.KOREAN -> "기록할 메모 입력..."
            AppLanguage.TRADITIONAL_CHINESE -> "寫點值得記錄的小美好..."
            else -> "写点值得记录的小美好..."
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(LanguageUtils.getString("anniversary_dialog_title", lang), fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("选择图标 Emoji", fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                    val emojiOptions = listOf("❤️", "💍", "🎂", "🎉", "✈️", "🎓", "🎮", "🚗", "🏠", "📚", "🏆", "📌")
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

                    // 1. 纪念日名称输入框 (红圈 3D 凹槽 + 灰色提示文字)
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
                        if (titleInput.isEmpty()) {
                            Text(
                                text = titlePlaceholder,
                                fontSize = 13.sp,
                                color = NeumorphicTextPrimary.copy(alpha = 0.40f)
                            )
                        }
                        BasicTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 2. 纪念日日期选择框 (黄圈 3D 凹槽 + 样式与年龄计算页 100% 对齐)
                    Text(LanguageUtils.getString("anniversary_date", lang), fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                    val inputShape14 = RoundedCornerShape(14.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .neumorphicInset(shape = inputShape14, elevation = 3.dp)
                            .border(1.dp, NeumorphicAccent.copy(alpha = 0.20f), shape = inputShape14)
                            .background(NeumorphicBg, shape = inputShape14)
                            .clip(inputShape14)
                            .clickable { showDatePicker = true }
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
                                    text = DateCalculatorUtils.formatDateWithWeek(dateInput, lang),
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

                    // 3. 备注与想说的话输入框 (红圈 3D 凹槽 + 灰色提示文字)
                    Text(remarkLabel, fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
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
                        if (remarkInput.isEmpty()) {
                            Text(
                                text = remarkPlaceholder,
                                fontSize = 12.5.sp,
                                color = NeumorphicTextPrimary.copy(alpha = 0.40f)
                            )
                        }
                        BasicTextField(
                            value = remarkInput,
                            onValueChange = { remarkInput = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NeumorphicTextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 4. 是否联动固定 Switch (样式与系统设置页 100% 对齐)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(syncLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                        NeumorphicSwitch(
                            checked = linkToCountdown,
                            onCheckedChange = { linkToCountdown = it }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (titleInput.isNotBlank()) {
                            val newItem = AnniversaryItem(
                                title = titleInput,
                                date = dateInput,
                                iconEmoji = selectedEmoji,
                                remark = remarkInput,
                                isPinned = linkToCountdown
                            )
                            viewModel.addAnniversary(newItem, context)

                            val fullName = "$selectedEmoji $titleInput"
                            viewModel.addCustomEvent(
                                name = fullName,
                                targetDate = dateInput,
                                iconEmoji = selectedEmoji,
                                isPinned = linkToCountdown,
                                context = context
                            )

                            showAddDialog = false
                            titleInput = ""
                            remarkInput = ""
                        }
                    }
                ) {
                    Text(LanguageUtils.getString("save_anniversary", lang), fontWeight = FontWeight.Bold, color = NeumorphicAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(LanguageUtils.getString("cancel", lang), color = NeumorphicTextPrimary.copy(alpha = 0.7f))
                }
            },
            containerColor = dialogBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // 精准打卡 Dialog
    if (showCheckInDialog) {
        val dialogBg = if (isDark) MaterialTheme.colorScheme.surface else NeumorphicBg
        val currentCity = uiState.currentCityName
        val coords = WeatherUtils.getCityCoordinates(currentCity)
        val nowTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        AlertDialog(
            onDismissRequest = { showCheckInDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(LanguageUtils.getString("check_in_dialog_title", lang), fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeumorphicSunkenBg)
                            .padding(10.dp)
                    ) {
                        Column {
                            val localizedCity = LanguageUtils.getLocalizedCityName(currentCity, lang)
                            Text("定位地点: $localizedCity", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                            Text("GPS 经纬度: (${coords.first}, ${coords.second})", fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.7f))
                            Text("打卡时刻: $nowTimeStr", fontSize = 11.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("打卡纪念事件名称", fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                    val checkInTitlePlaceholder = when (effectiveLang) {
                        AppLanguage.ENGLISH -> "e.g. Arrived in Paris / First Concert"
                        AppLanguage.JAPANESE -> "例: パリ到着 / 初コンサート"
                        AppLanguage.KOREAN -> "예: 파리 도착 / 첫 콘서트"
                        AppLanguage.TRADITIONAL_CHINESE -> "如: 抵達巴黎 / 首次看演唱會"
                        else -> "例如: 抵达深圳 / 打卡地标 / 首次看演唱会"
                    }

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
                        if (titleInput.isEmpty()) {
                            Text(
                                text = checkInTitlePlaceholder,
                                fontSize = 13.sp,
                                color = NeumorphicTextPrimary.copy(alpha = 0.40f)
                            )
                        }
                        BasicTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (titleInput.isNotBlank()) {
                            val newItem = AnniversaryItem(
                                title = titleInput,
                                date = LocalDate.now(),
                                iconEmoji = "📍",
                                isCheckIn = true,
                                locationName = currentCity,
                                latitude = coords.first,
                                longitude = coords.second,
                                checkInTimeStr = nowTimeStr,
                                isPinned = true
                            )
                            viewModel.addAnniversary(newItem, context)

                            val fullName = "📍 $titleInput"
                            viewModel.addCustomEvent(
                                name = fullName,
                                targetDate = LocalDate.now(),
                                iconEmoji = "📍",
                                isPinned = true,
                                context = context
                            )

                            showCheckInDialog = false
                            titleInput = ""
                        }
                    }
                ) {
                    Text("立即打卡保存", fontWeight = FontWeight.Bold, color = NeumorphicAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckInDialog = false }) {
                    Text(LanguageUtils.getString("cancel", lang), color = NeumorphicTextPrimary.copy(alpha = 0.7f))
                }
            },
            containerColor = dialogBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (itemToDelete != null) {
        val targetItem = itemToDelete!!
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(LanguageUtils.getString("confirm_delete_anniversary", lang), fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary) },
            text = { Text("${targetItem.iconEmoji} ${targetItem.title}", fontSize = 13.sp, color = NeumorphicTextPrimary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAnniversary(targetItem.id, context)
                        itemToDelete = null
                    }
                ) {
                    Text(LanguageUtils.getString("confirm_delete_btn", lang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(LanguageUtils.getString("cancel", lang), color = NeumorphicTextPrimary)
                }
            },
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else NeumorphicBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    val list = uiState.anniversaryList

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
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = NeumorphicAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${LanguageUtils.getString("tab_anniversary", lang)} (${list.size})",
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

            // 新增纪念日与打卡两个 3D 高亮按键
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val btnShape = RoundedCornerShape(16.dp)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .neumorphicExtruded(shape = btnShape, elevation = 4.dp)
                        .background(Color(0xFFEC4899), shape = btnShape)
                        .clip(btnShape)
                        .clickable {
                            dateInput = LocalDate.now()
                            titleInput = ""
                            remarkInput = ""
                            showCheckInDialog = true

                            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                            if (!hasFine && !hasCoarse) {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(LanguageUtils.getString("check_in", lang), fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 13.5.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .neumorphicExtruded(shape = btnShape, elevation = 4.dp)
                        .background(Color(0xFF0D9488), shape = btnShape)
                        .clip(btnShape)
                        .clickable {
                            dateInput = LocalDate.now()
                            titleInput = ""
                            remarkInput = ""
                            showAddDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(LanguageUtils.getString("add_anniversary", lang), fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 13.5.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 纪念日列表展示
            if (list.isEmpty()) {
                val emptyCardShape = RoundedCornerShape(22.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .neumorphicExtruded(shape = emptyCardShape, elevation = 4.dp)
                        .background(NeumorphicBg, shape = emptyCardShape)
                        .clip(emptyCardShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(LanguageUtils.getString("no_anniversary_record", lang), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(LanguageUtils.getString("add_anniversary_hint", lang), fontSize = 12.sp, color = NeumorphicTextPrimary.copy(alpha = 0.6f))
                    }
                }
            } else {
                val today = LocalDate.now()

                list.forEach { item ->
                    val totalDaysPassed = DateCalculatorUtils.naturalDaysBetween(item.date, today)
                    val nextUpcoming = item.getNextUpcomingDate(today)
                    val daysRemaining = DateCalculatorUtils.naturalDaysBetween(today, nextUpcoming)

                    val cardShape = RoundedCornerShape(22.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .neumorphicExtruded(shape = cardShape, elevation = 5.dp)
                            .background(NeumorphicBg, shape = cardShape)
                            .border(1.dp, NeumorphicAccent.copy(alpha = 0.15f), shape = cardShape)
                            .clip(cardShape)
                            .combinedClickable(
                                onClick = { selectedDetailItem = item },
                                onLongClick = { itemToDelete = item }
                            )
                            .padding(14.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.iconEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(item.title, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = NeumorphicTextPrimary)
                                        Text("${LanguageUtils.getString("base_date", lang)}: ${DateCalculatorUtils.formatDate(item.date, lang)}", fontSize = 11.5.sp, color = NeumorphicTextPrimary.copy(alpha = 0.65f))
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                                            .background(if (item.isPinned) NeumorphicAccent else NeumorphicBg, shape = CircleShape)
                                            .clip(CircleShape)
                                            .clickable { viewModel.togglePinAnniversary(item.id, context) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PushPin,
                                            contentDescription = "Pin",
                                            tint = if (item.isPinned) Color.White else NeumorphicAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable { itemToDelete = item }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(LanguageUtils.getString("days_passed", lang), fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.6f))
                                    Text("${abs(totalDaysPassed)} ${LanguageUtils.getString("days_unit", lang)}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(LanguageUtils.getString("next_anniversary_remains", lang), fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.6f))
                                    Text("${abs(daysRemaining)} ${LanguageUtils.getString("days_unit", lang)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                }
                            }

                            if (item.isCheckIn) {
                                Spacer(modifier = Modifier.height(6.dp))
                                SuggestionChip(
                                    onClick = {},
                                    shape = CircleShape,
                                    label = {
                                        Text(
                                            text = "📍 ${item.locationName} (${item.checkInTimeStr})",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
