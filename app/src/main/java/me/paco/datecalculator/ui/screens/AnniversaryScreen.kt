package me.paco.datecalculator.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import me.paco.datecalculator.ui.theme.LocalDarkTheme
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.paco.datecalculator.data.AnniversaryItem
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
import me.paco.datecalculator.util.LocationUtils
import me.paco.datecalculator.util.ShareUtils
import me.paco.datecalculator.util.WeatherUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun AnniversaryScreen(
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isDark = LocalDarkTheme.current

    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // 常用添加纪念日 Dialog 状态
    var showAddDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedEmoji by remember { mutableStateOf("❤️") }
    var remarkInput by remember { mutableStateOf("") }
    var linkToCountdown by remember { mutableStateOf(true) } // 是否自动联动与固定到倒数日

    // 📍 纪念日打卡 Dialog 状态
    var showCheckInDialog by remember { mutableStateOf(false) }
    var checkInTitleInput by remember { mutableStateOf("") }
    var checkInNoteInput by remember { mutableStateOf("") }
    var checkInEmoji by remember { mutableStateOf("📍") }

    var itemToDelete by remember { mutableStateOf<AnniversaryItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadAnniversaries(context)
    }

    if (showDatePicker) {
        DatePickerModal(
            selectedDate = dateInput,
            onDateSelected = { d ->
                dateInput = d
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

    // 常规添加纪念日 Dialog (支持图标选择，参考自定义倒数日界面)
    if (showAddDialog) {
        val dialogBg = if (isDark) MaterialTheme.colorScheme.surface else NeumorphicBg
        val lang = uiState.appLanguage
        val selectEmojiLabel = when (lang) {
            AppLanguage.ENGLISH -> "Select Emoji"
            AppLanguage.JAPANESE -> "アイコン選択"
            AppLanguage.KOREAN -> "아이콘 선택"
            AppLanguage.TRADITIONAL_CHINESE -> "選擇圖標 Emoji"
            else -> "选择图标 Emoji"
        }
        val remarkLabel = when (lang) {
            AppLanguage.ENGLISH -> "Notes & Remarks"
            AppLanguage.JAPANESE -> "メモ"
            AppLanguage.KOREAN -> "메모"
            AppLanguage.TRADITIONAL_CHINESE -> "備註與想說的話"
            else -> "备注与想说的话"
        }
        val syncLabel = when (lang) {
            AppLanguage.ENGLISH -> "Pin & Sync to Countdown"
            AppLanguage.JAPANESE -> "カウントダウンに固定同期"
            AppLanguage.KOREAN -> "디데이에 고정 동기화"
            AppLanguage.TRADITIONAL_CHINESE -> "聯動固定到自定義紀念日與倒數日"
            else -> "联动固定到自定义纪念日与倒数日"
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(LanguageUtils.getString("anniversary_dialog_title", lang), fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(selectEmojiLabel, fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
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

                    Text(LanguageUtils.getString("anniversary_name", lang), fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 3.dp)
                            .background(NeumorphicBg, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Text(LanguageUtils.getString("anniversary_date", lang), fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 3.dp)
                            .background(NeumorphicBg, shape = RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(DateCalculatorUtils.formatDate(dateInput, lang), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                    }

                    Text(remarkLabel, fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 3.dp)
                            .background(NeumorphicBg, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = remarkInput,
                            onValueChange = { remarkInput = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NeumorphicTextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(syncLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                        Switch(
                            checked = linkToCountdown,
                            onCheckedChange = { linkToCountdown = it },
                            modifier = Modifier.scale(0.8f)
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
                                iconEmoji = selectedEmoji
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

    // 📍 精准打卡 Dialog (更名：“打卡”，自动保存并联动至自定义纪念日/倒数日)
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
                    Text("精准打卡", fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
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
                            Text("📍 定位地点: $currentCity", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                            Text("🌐 GPS 经纬度: (${coords.first}, ${coords.second})", fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.7f))
                            Text("⏰ 打卡时刻: $nowTimeStr", fontSize = 11.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("打卡主题名称", fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 3.dp)
                            .background(NeumorphicBg, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (checkInTitleInput.isEmpty()) {
                            Text("如: 恋爱打卡 / 毕业旅行留念", fontSize = 12.sp, color = NeumorphicTextPrimary.copy(alpha = 0.5f))
                        }
                        BasicTextField(
                            value = checkInTitleInput,
                            onValueChange = { checkInTitleInput = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Text("记录当时做了什么或去了哪里", fontSize = 12.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 3.dp)
                            .background(NeumorphicBg, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (checkInNoteInput.isEmpty()) {
                            Text("例如: 在深圳湾公园看海，天气晴朗...", fontSize = 12.sp, color = NeumorphicTextPrimary.copy(alpha = 0.5f))
                        }
                        BasicTextField(
                            value = checkInNoteInput,
                            onValueChange = { checkInNoteInput = it },
                            textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = NeumorphicTextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val title = checkInTitleInput.ifBlank { "打卡" }
                        val checkInItem = AnniversaryItem(
                            title = title,
                            date = LocalDate.now(),
                            iconEmoji = checkInEmoji,
                            isCheckIn = true,
                            locationName = currentCity,
                            latitude = coords.first,
                            longitude = coords.second,
                            checkInTimeStr = nowTimeStr,
                            remark = checkInNoteInput
                        )

                        viewModel.addAnniversary(checkInItem, context)

                        // 联动到自定义纪念日与倒数日快捷按钮
                        viewModel.addCustomEvent(
                            name = "$checkInEmoji $title",
                            targetDate = LocalDate.now(),
                            iconEmoji = checkInEmoji
                        )

                        Toast.makeText(context, "🎉 打卡成功！已自动联动至自定义纪念日", Toast.LENGTH_SHORT).show()
                        showCheckInDialog = false
                        checkInTitleInput = ""
                        checkInNoteInput = ""
                    }
                ) {
                    Text("立即打卡", fontWeight = FontWeight.ExtraBold, color = NeumorphicAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckInDialog = false }) {
                    Text("取消", color = NeumorphicTextPrimary.copy(alpha = 0.7f))
                }
            },
            containerColor = dialogBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (itemToDelete != null) {
        val target = itemToDelete!!
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("确认删除纪念日？", fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary) },
            text = { Text("确定要删除“${target.title}”卡片吗？该页面所有历史卡片均在列表保存。", fontSize = 13.sp, color = NeumorphicTextPrimary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAnniversary(target.id, context)
                        itemToDelete = null
                        Toast.makeText(context, "已删除纪念日卡片", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("确认删除", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
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
            // 顶栏 (36dp 高度, 15sp 标题，无运势无历史同步，纯粹展示纪念日)
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
                        tint = Color(0xFFEC4899),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${LanguageUtils.getString("tab_anniversary", uiState.appLanguage)} (${uiState.anniversaryList.size})",
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

            // 两个高光按键：📍 打卡 与 ➕ 添加纪念日
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .neumorphicExtruded(shape = RoundedCornerShape(14.dp), elevation = 4.dp)
                        .background(Color(0xFFEC4899), shape = RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showCheckInDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(LanguageUtils.getString("check_in", uiState.appLanguage), fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 13.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .neumorphicExtruded(shape = RoundedCornerShape(14.dp), elevation = 4.dp)
                        .background(NeumorphicAccent, shape = RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showAddDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(LanguageUtils.getString("add_anniversary", uiState.appLanguage), fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 全量纪念日与打卡结果展示列表
            if (uiState.anniversaryList.isEmpty()) {
                val emptyCardShape = RoundedCornerShape(20.dp)
                val lang = uiState.appLanguage
                val emptyTitle = when (lang) {
                    AppLanguage.ENGLISH -> "❤️ No Anniversaries Saved"
                    AppLanguage.JAPANESE -> "❤️ 保存された記念日はありません"
                    AppLanguage.KOREAN -> "❤️ 저장된 기념일이 없습니다"
                    AppLanguage.TRADITIONAL_CHINESE -> "❤️ 暫無記錄的重要紀念日"
                    else -> "❤️ 暂无记录的重要纪念日"
                }
                val emptyDesc = when (lang) {
                    AppLanguage.ENGLISH -> "Tap '+ Add Anniversary' or '📍 Check-in' above to save moments"
                    AppLanguage.JAPANESE -> "上の「記念日追加」または「📍 チェックイン」をタップして保存"
                    AppLanguage.KOREAN -> "상단의 '+ 기념일 추가' 또는 '📍 위치 체크인'을 누르세요"
                    AppLanguage.TRADITIONAL_CHINESE -> "點擊上方“新增紀念日”或“📍 打卡”保存美好時刻"
                    else -> "点击上方“添加纪念日”或“📍 打卡”保存美好时刻"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 6.dp)
                        .neumorphicExtruded(shape = emptyCardShape, elevation = 5.dp)
                        .background(NeumorphicBg, shape = emptyCardShape)
                        .border(1.2.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = emptyCardShape)
                        .clip(emptyCardShape)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(emptyTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(emptyDesc, fontSize = 12.sp, color = NeumorphicTextPrimary.copy(alpha = 0.6f))
                    }
                }
            } else {
                uiState.anniversaryList.forEach { item ->
                    val elapsedDays = DateCalculatorUtils.naturalDaysBetween(item.date, LocalDate.now())
                    val cardShape = RoundedCornerShape(20.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .neumorphicExtruded(shape = cardShape, elevation = 5.dp)
                            .background(NeumorphicBg, shape = cardShape)
                            .border(1.2.dp, if (item.isCheckIn) Color(0xFFEC4899).copy(alpha = 0.35f) else NeumorphicAccent.copy(alpha = 0.25f), shape = cardShape)
                            .clip(cardShape)
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
                                        Text("日期: ${item.date}", fontSize = 11.sp, color = NeumorphicTextPrimary.copy(alpha = 0.6f))
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (item.isCheckIn) {
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(Color(0xFFEC4899).copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text("📍打卡精选", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEC4899))
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }

                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "删除卡片",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable { itemToDelete = item }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 天数数字大字展示
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = if (elapsedDays >= 0) "已陪伴过去" else "距离即将到来",
                                        fontSize = 11.sp,
                                        color = NeumorphicTextPrimary.copy(alpha = 0.65f)
                                    )
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "${abs(elapsedDays)}",
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (item.isCheckIn) Color(0xFFEC4899) else NeumorphicAccent
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("天", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                                    }
                                }

                                val upcoming = item.getNextUpcomingDate(LocalDate.now())
                                val nextDays = DateCalculatorUtils.naturalDaysBetween(LocalDate.now(), upcoming)
                                SuggestionChip(
                                    onClick = {},
                                    shape = CircleShape,
                                    label = { Text("下个周年还剩 $nextDays 天", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                            }

                            // 打卡专用元数据展示 (地点 + 经纬度 + 打卡时间 + 备注)
                            if (item.isCheckIn || item.locationName.isNotEmpty() || item.remark.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(8.dp))

                                if (item.locationName.isNotEmpty()) {
                                    val coordsText = if (item.latitude != null && item.longitude != null) " (${item.latitude}, ${item.longitude})" else ""
                                    Text("📍 地点: ${item.locationName}$coordsText", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary)
                                }

                                if (item.checkInTimeStr.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("⏰ 打卡时刻: ${item.checkInTimeStr}", fontSize = 11.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                                }

                                if (item.remark.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("📝 记录: ${item.remark}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = NeumorphicTextPrimary.copy(alpha = 0.85f))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 复制与分享按钮
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NeumorphicIconButton(
                                    icon = Icons.Default.Share,
                                    contentDescription = "分享纪念日",
                                    onClick = {
                                        val shareText = "【重要纪念日分享】\n${item.iconEmoji} ${item.title}\n起点日期: ${item.date}\n已陪伴过去: ${abs(elapsedDays)} 天\n${if (item.locationName.isNotEmpty()) "📍 打卡地点: ${item.locationName}\n" else ""}${if (item.remark.isNotEmpty()) "📝 记录: ${item.remark}" else ""}"
                                        ShareUtils.shareText(context, "纪念日卡片", shareText)
                                    }
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                NeumorphicIconButton(
                                    icon = Icons.Default.ContentCopy,
                                    contentDescription = "复制纪念日信息",
                                    onClick = {
                                        val copyText = "${item.iconEmoji} ${item.title}: ${item.date} (已过去 ${abs(elapsedDays)} 天) ${if (item.remark.isNotEmpty()) "| ${item.remark}" else ""}"
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("AnniversaryItem", copyText))
                                        Toast.makeText(context, "纪念日信息已复制！", Toast.LENGTH_SHORT).show()
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
