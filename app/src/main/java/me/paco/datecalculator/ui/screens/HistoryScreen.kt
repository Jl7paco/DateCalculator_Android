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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.paco.datecalculator.R
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
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

    val historyClearedToast = stringResource(R.string.toast_history_cleared)
    val copiedToast = stringResource(R.string.toast_copied)

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.label_clear_dialog_title)) },
            text = { Text(stringResource(R.string.label_clear_dialog_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearDialog = false
                    Toast.makeText(context, historyClearedToast, Toast.LENGTH_SHORT).show()
                }) {
                    Text(stringResource(R.string.label_clear_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.label_cancel))
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
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.label_history_count_fmt, uiState.historyList.size),
                    style = MaterialTheme.typography.titleMedium,
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

                    // 卡片预留 4.dp 外边距，确保新拟物软阴影 100% 弥散散开不被 Edge 边缘裁剪
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .neumorphicExtruded(shape = cardShape, elevation = 5.dp)
                            .background(NeumorphicBg, shape = cardShape)
                            .border(1.dp, NeumorphicAccent.copy(alpha = 0.15f), shape = cardShape)
                            .clip(cardShape)
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. 分类 Pill 胶囊标签 (配以对应主题色彩)
                                val categoryBg = when (item.category) {
                                    "日期计算" -> NeumorphicAccent
                                    "日期倒计时" -> Color(0xFF8B5CF6)
                                    else -> Color(0xFF10B981)
                                }

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

                                // 右侧复制与删除 32dp 圆角按键
                                val buttonShape = RoundedCornerShape(10.dp)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

                            // 2. 突出展示核心计算结果大字 (大字加粗高亮)
                            Text(
                                text = item.title,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = NeumorphicTextPrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // 3. 结构化计算过程 (清晰箭头标识)
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
