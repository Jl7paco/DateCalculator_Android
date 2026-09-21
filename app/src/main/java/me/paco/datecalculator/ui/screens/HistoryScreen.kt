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
import androidx.compose.material3.HorizontalDivider
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
                        text = stringResource(R.string.label_clear_dialog_title),
                        fontWeight = FontWeight.ExtraBold,
                        color = NeumorphicTextPrimary,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Text(
                    text = stringResource(R.string.label_clear_dialog_msg),
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
                        text = stringResource(R.string.label_clear_confirm),
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
                        text = stringResource(R.string.label_cancel),
                        color = NeumorphicTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        )
    }

    // 多段排期历史记录详情弹窗 (宽度扩展至 92% 全屏宽展现，极为舒展)
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = NeumorphicAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(item.title, fontWeight = FontWeight.ExtraBold, color = NeumorphicTextPrimary, fontSize = 16.sp)
                    }

                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                            .background(NeumorphicBg, shape = CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                val (multiFinalDate, multiSegments) = viewModel.calculateMultiStageTimeline()
                                val unitLabel = if (uiState.dateMode == DateMode.WORKDAY) "工作日" else "自然日"
                                val regionLabel = "${uiState.holidayRegion.flagEmoji} ${uiState.holidayRegion.nativeName}"
                                CsvExporter.exportStagesToCsv(
                                    context = context,
                                    baseDate = uiState.baseDate,
                                    finalDate = multiFinalDate,
                                    segments = multiSegments,
                                    modeLabel = unitLabel,
                                    regionLabel = regionLabel
                                )
                            }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("导出 CSV", fontSize = 11.sp, color = NeumorphicAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 3.dp)
                            .background(NeumorphicSunkenBg, shape = RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("排期明细概览", fontWeight = FontWeight.Bold, color = NeumorphicAccent, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(item.detail, fontSize = 13.sp, color = NeumorphicTextPrimary, lineHeight = 18.sp)
                        }
                    }

                    val (multiFinalDate, multiSegments) = viewModel.calculateMultiStageTimeline()
                    val unitLabel = if (uiState.dateMode == DateMode.WORKDAY) "工作日" else "自然日"

                    // 直接渲染图表，不加第二层带边框卡片
                    TimelineDiagram(
                        baseDate = uiState.baseDate,
                        finalDate = multiFinalDate,
                        segments = multiSegments,
                        modeLabel = unitLabel,
                        regionLabel = item.regionTag,
                        showOuterCard = false,
                        showExportButton = false
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
                    Text("关闭详情", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        )
    }

    // 修改历史记录名称弹窗
    itemToEditTitle?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToEditTitle = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = NeumorphicBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = NeumorphicAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("修改历史记录名称", fontWeight = FontWeight.ExtraBold, color = NeumorphicTextPrimary, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("请输入新的历史记录名称:", fontSize = 13.sp, color = NeumorphicTextPrimary)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .neumorphicInset(shape = RoundedCornerShape(12.dp))
                            .border(1.5.dp, NeumorphicAccent.copy(alpha = 0.4f), shape = RoundedCornerShape(12.dp))
                            .background(NeumorphicSunkenBg, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = editedTitleText,
                            onValueChange = { editedTitleText = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NeumorphicTextPrimary),
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
                                viewModel.updateHistoryItemTitle(item.id, editedTitleText.trim())
                                Toast.makeText(context, "已重命名历史记录名称", Toast.LENGTH_SHORT).show()
                            }
                            itemToEditTitle = null
                        }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("保存名称", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                    Text("取消", color = NeumorphicTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // 对齐规则设置页面的 36dp 统一小标题高度与样式
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = NeumorphicAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.label_history_count_fmt, uiState.historyList.size),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeumorphicTextPrimary
                )
            }

            if (uiState.historyList.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                        .background(NeumorphicBg, shape = CircleShape)
                        .clip(CircleShape)
                        .clickable { showClearDialog = true }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.label_clear), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.historyList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = NeumorphicTextPrimary.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.label_empty_history), color = NeumorphicTextPrimary.copy(alpha = 0.6f), fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(uiState.historyList, key = { it.id }) { item ->
                    val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                    val timeStr = sdf.format(Date(item.timestamp))
                    val cardShape = RoundedCornerShape(18.dp)

                    // 改回原版优雅的标准新拟物凸起卡片 (不加额外描边线)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .neumorphicExtruded(shape = cardShape, elevation = 5.dp)
                            .background(NeumorphicBg, shape = cardShape)
                            .clip(cardShape)
                            .clickable {
                                if (item.category == "多段加减") {
                                    itemToViewDetail = item
                                }
                            }
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. 分类 Pill 胶囊标签
                                val categoryBg = when (item.category) {
                                    "日期计算" -> NeumorphicAccent
                                    "日期倒计时" -> Color(0xFF8B5CF6)
                                    else -> Color(0xFF10B981)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(categoryBg)
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.category,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (item.category == "多段加减") {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "点击查看详情",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = NeumorphicTextPrimary.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                // 右侧编辑、复制与删除按键
                                val buttonShape = RoundedCornerShape(10.dp)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // ✏️ 重命名历史记录名称按键
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .neumorphicExtruded(shape = buttonShape, elevation = 3.dp)
                                            .background(NeumorphicBg, shape = buttonShape)
                                            .clip(buttonShape)
                                            .clickable {
                                                itemToEditTitle = item
                                                editedTitleText = item.title
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "重命名记录", tint = NeumorphicAccent, modifier = Modifier.size(16.dp))
                                    }

                                    // 📋 复制记录
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .neumorphicExtruded(shape = buttonShape, elevation = 3.dp)
                                            .background(NeumorphicBg, shape = buttonShape)
                                            .clip(buttonShape)
                                            .clickable {
                                                val clipText = "[${item.category}] ${item.title} (${item.detail})"
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("HistoryItem", clipText))
                                                Toast.makeText(context, copiedToast, Toast.LENGTH_SHORT).show()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "复制记录", tint = NeumorphicAccent, modifier = Modifier.size(16.dp))
                                    }

                                    // 🗑️ 删除记录
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .neumorphicExtruded(shape = buttonShape, elevation = 3.dp)
                                            .background(NeumorphicBg, shape = buttonShape)
                                            .clip(buttonShape)
                                            .clickable {
                                                viewModel.deleteHistoryItem(item)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "删除记录", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 2. 突出展示核心计算结果或自定义方案大字
                            Text(
                                text = item.title,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = NeumorphicTextPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // 3. 结构化计算过程
                            Text(
                                text = item.detail,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 13.sp,
                                color = NeumorphicTextPrimary.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // 4. 底部地区标识与记录生成时间
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.regionTag,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeumorphicAccent
                                )
                                Text(
                                    text = timeStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = NeumorphicTextPrimary.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
