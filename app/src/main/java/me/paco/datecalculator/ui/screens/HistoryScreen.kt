package me.paco.datecalculator.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import me.paco.datecalculator.R
import me.paco.datecalculator.data.DateMode
import me.paco.datecalculator.data.HistoryItem
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicSunkenBg
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import me.paco.datecalculator.ui.components.TimelineDiagram
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.CsvExporter
import me.paco.datecalculator.util.LanguageUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lang = uiState.appLanguage

    var showClearDialog by remember { mutableStateOf(false) }
    var itemToEditTitle by remember { mutableStateOf<HistoryItem?>(null) }
    var itemToViewDetail by remember { mutableStateOf<HistoryItem?>(null) }
    var editedTitleText by remember { mutableStateOf("") }

    val historyClearedToast = stringResource(R.string.toast_history_cleared)
    val copiedToast = stringResource(R.string.toast_copied)

    // 新拟物 3D 风格清空确认二次弹窗
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = NeumorphicBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LanguageUtils.getString("clear_history", lang),
                        fontWeight = FontWeight.ExtraBold,
                        color = NeumorphicTextPrimary,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Text(
                    text = LanguageUtils.getString("clear_history_confirm", lang),
                    color = NeumorphicTextPrimary.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
                        .background(MaterialTheme.colorScheme.error, shape = CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            viewModel.clearHistory()
                            showClearDialog = false
                            Toast.makeText(context, historyClearedToast, Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = LanguageUtils.getString("confirm", lang),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                        .background(NeumorphicBg, shape = CircleShape)
                        .clip(CircleShape)
                        .clickable { showClearDialog = false }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = LanguageUtils.getString("cancel", lang),
                        color = NeumorphicTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        )
    }

    // 多段排期历史记录详情弹窗 (宽度扩展至 92% 全屏宽展现)
    itemToViewDetail?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToViewDetail = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = NeumorphicBg,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Timeline, contentDescription = null, tint = NeumorphicAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LanguageUtils.getLocalizedHistoryTitle(item.title, lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeumorphicTextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                            .background(NeumorphicBg, shape = CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                val results = viewModel.computeMultiStageSequence()
                                val base = uiState.baseDate
                                val finalDate = results.lastOrNull()?.endDate ?: base
                                val unitLabel = if (uiState.dateMode == DateMode.WORKDAY) LanguageUtils.getString("workday", lang) else LanguageUtils.getString("natural_day", lang)
                                CsvExporter.exportStagesToCsv(
                                    context = context,
                                    baseDate = base,
                                    finalDate = finalDate,
                                    segments = results,
                                    modeLabel = unitLabel,
                                    regionLabel = item.regionTag
                                )
                            }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(LanguageUtils.getString("export_csv", lang), fontSize = 11.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = LanguageUtils.getLocalizedHistoryDetail(item.detail, lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val results = viewModel.computeMultiStageSequence()
                    val base = uiState.baseDate
                    val finalDate = results.lastOrNull()?.endDate ?: base
                    val unitLabel = if (uiState.dateMode == DateMode.WORKDAY) LanguageUtils.getString("workday", lang) else LanguageUtils.getString("natural_day", lang)

                    TimelineDiagram(
                        baseDate = base,
                        finalDate = finalDate,
                        segments = results,
                        modeLabel = unitLabel,
                        regionLabel = item.regionTag,
                        showOuterCard = false,
                        showExportButton = false,
                        language = lang
                    )
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
                        .background(NeumorphicAccent, shape = CircleShape)
                        .clip(CircleShape)
                        .clickable { itemToViewDetail = null }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(LanguageUtils.getString("close_details", lang), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        )
    }

    // 重命名标题弹窗
    itemToEditTitle?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToEditTitle = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = NeumorphicBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = NeumorphicAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LanguageUtils.getString("edit_history_title", lang),
                        fontWeight = FontWeight.Bold,
                        color = NeumorphicTextPrimary,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = LanguageUtils.getString("enter_new_history_title", lang),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 3.dp)
                            .background(NeumorphicSunkenBg, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = editedTitleText,
                            onValueChange = { editedTitleText = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeumorphicTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
                        .background(NeumorphicAccent, shape = CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            if (editedTitleText.isNotBlank()) {
                                viewModel.renameHistoryItem(item.id, editedTitleText)
                                itemToEditTitle = null
                                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(LanguageUtils.getString("save_title", lang), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                        .background(NeumorphicBg, shape = CircleShape)
                        .clip(CircleShape)
                        .clickable { itemToEditTitle = null }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(LanguageUtils.getString("cancel", lang), color = NeumorphicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = NeumorphicAccent
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = LanguageUtils.getString("history_title", lang),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeumorphicTextPrimary
                )
            }

            if (uiState.historyList.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
                        .background(NeumorphicBg, shape = CircleShape)
                        .clip(CircleShape)
                        .clickable { showClearDialog = true }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "清空历史",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = LanguageUtils.getString("clear_history", lang),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp)
                    .neumorphicInset(shape = RoundedCornerShape(24.dp), elevation = 4.dp)
                    .background(NeumorphicBg, shape = RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = NeumorphicTextPrimary.copy(alpha = 0.35f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = LanguageUtils.getString("no_history", lang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeumorphicTextPrimary.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.historyList, key = { it.id }) { item ->
                    val isMultiStage = item.title.contains("多段") || item.detail.contains("多段")
                    val cardShape = RoundedCornerShape(20.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neumorphicExtruded(shape = cardShape, elevation = 5.dp)
                            .background(NeumorphicBg, shape = cardShape)
                            .border(1.dp, NeumorphicAccent.copy(alpha = 0.2f), shape = cardShape)
                            .clip(cardShape)
                            .clickable {
                                if (isMultiStage) {
                                    itemToViewDetail = item
                                }
                            }
                            .padding(14.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(NeumorphicAccent.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = LanguageUtils.getLocalizedHistoryCategory(item.category, lang),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeumorphicAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = LanguageUtils.getLocalizedHistoryTitle(item.title, lang),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = NeumorphicTextPrimary,
                                        maxLines = 1
                                    )

                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "修改标题",
                                        tint = NeumorphicTextPrimary.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .padding(start = 6.dp)
                                            .size(16.dp)
                                            .clickable {
                                                itemToEditTitle = item
                                                editedTitleText = item.title
                                            }
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formatTime(item.timestamp),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = NeumorphicTextPrimary.copy(alpha = 0.5f)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "删除记录",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable {
                                                viewModel.deleteHistoryItem(item)
                                            }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = LanguageUtils.getLocalizedHistoryDetail(item.detail, lang),
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeumorphicTextPrimary.copy(alpha = 0.85f),
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = LanguageUtils.getLocalizedRegionTag(item.regionTag, lang),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = NeumorphicAccent,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isMultiStage) {
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(NeumorphicAccent.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "📊 Diagram",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NeumorphicAccent
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "复制条目",
                                        tint = NeumorphicAccent,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("HistoryItem", "${item.title}: ${item.detail}")
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, copiedToast, Toast.LENGTH_SHORT).show()
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
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
