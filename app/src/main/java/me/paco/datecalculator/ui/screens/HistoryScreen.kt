package me.paco.datecalculator.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import androidx.compose.ui.unit.sp
import me.paco.datecalculator.data.HistoryItem
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicIconButton
import me.paco.datecalculator.ui.components.NeumorphicSunkenBg
import me.paco.datecalculator.ui.components.neumorphicButton3D
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LanguageUtils
import me.paco.datecalculator.util.ShareUtils

@Composable
fun HistoryScreen(
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isDark = LocalDarkTheme.current
    val lang = uiState.appLanguage

    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var itemToRename by remember { mutableStateOf<HistoryItem?>(null) }
    var itemToDelete by remember { mutableStateOf<HistoryItem?>(null) }
    var itemToViewDetail by remember { mutableStateOf<HistoryItem?>(null) }
    var newTitleInput by remember { mutableStateOf("") }

    // 重命名 Dialog
    if (itemToRename != null) {
        val targetItem = itemToRename!!
        val dialogBg = if (isDark) MaterialTheme.colorScheme.surface else NeumorphicBg
        AlertDialog(
            onDismissRequest = { itemToRename = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = LanguageUtils.getString("edit_history_title", lang),
                        fontWeight = FontWeight.Bold,
                        color = NeumorphicTextPrimary,
                        fontSize = 15.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = LanguageUtils.getString("enter_new_history_title", lang),
                        fontSize = 12.sp,
                        color = NeumorphicTextPrimary.copy(alpha = 0.7f)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 3.dp)
                            .border(1.dp, NeumorphicAccent.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                            .background(NeumorphicSunkenBg, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = newTitleInput,
                            onValueChange = { newTitleInput = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeumorphicTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTitleInput.isNotBlank()) {
                            viewModel.renameHistoryItem(targetItem.id, newTitleInput.trim())
                            itemToRename = null
                        }
                    }
                ) {
                    Text(
                        text = LanguageUtils.getString("save_title", lang),
                        fontWeight = FontWeight.Bold,
                        color = NeumorphicAccent
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToRename = null }) {
                    Text(
                        text = LanguageUtils.getString("cancel", lang),
                        color = NeumorphicTextPrimary.copy(alpha = 0.7f)
                    )
                }
            },
            containerColor = dialogBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // 针对多段计算的历史记录详情查看 Dialog
    if (itemToViewDetail != null) {
        val detailItem = itemToViewDetail!!
        val dialogBg = if (isDark) MaterialTheme.colorScheme.surface else NeumorphicBg

        AlertDialog(
            onDismissRequest = { itemToViewDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = detailItem.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = NeumorphicTextPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neumorphicInset(shape = RoundedCornerShape(12.dp), elevation = 3.dp)
                            .background(NeumorphicSunkenBg, shape = RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = LanguageUtils.getLocalizedHistoryDetail(detailItem.detail, lang),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = NeumorphicTextPrimary,
                            lineHeight = 18.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NeumorphicIconButton(
                            icon = Icons.Default.Share,
                            contentDescription = "Share",
                            size = 32.dp,
                            onClick = {
                                ShareUtils.shareText(context, "${detailItem.title}\n${detailItem.detail}")
                            }
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        NeumorphicIconButton(
                            icon = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            size = 32.dp,
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("HistoryDetail", "${detailItem.title}\n${detailItem.detail}")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { itemToViewDetail = null }) {
                    Text(
                        text = LanguageUtils.getString("confirm", lang),
                        fontWeight = FontWeight.Bold,
                        color = NeumorphicAccent
                    )
                }
            },
            containerColor = dialogBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // 单条记录删除确认 Dialog
    if (itemToDelete != null) {
        val targetItem = itemToDelete!!
        val dialogBg = if (isDark) MaterialTheme.colorScheme.surface else NeumorphicBg
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "删除此条历史记录？",
                        fontWeight = FontWeight.Bold,
                        color = NeumorphicTextPrimary,
                        fontSize = 15.sp
                    )
                }
            },
            text = {
                Text(
                    text = "${targetItem.title} (${targetItem.detail})",
                    fontSize = 13.sp,
                    color = NeumorphicTextPrimary.copy(alpha = 0.85f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeHistoryItem(targetItem.id)
                        itemToDelete = null
                    }
                ) {
                    Text(
                        text = LanguageUtils.getString("confirm_delete_btn", lang),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(
                        text = LanguageUtils.getString("cancel", lang),
                        color = NeumorphicTextPrimary.copy(alpha = 0.7f)
                    )
                }
            },
            containerColor = dialogBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // 清空全部历史 Dialog
    if (showClearConfirmDialog) {
        val dialogBg = if (isDark) MaterialTheme.colorScheme.surface else NeumorphicBg
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "清空全部历史推算？",
                        fontWeight = FontWeight.Bold,
                        color = NeumorphicTextPrimary,
                        fontSize = 15.sp
                    )
                }
            },
            text = {
                Text(
                    text = "所有已保存的推算历史将被清空，此操作不可撤销。",
                    fontSize = 12.5.sp,
                    color = NeumorphicTextPrimary.copy(alpha = 0.75f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text(
                        text = LanguageUtils.getString("confirm_delete_btn", lang),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(
                        text = LanguageUtils.getString("cancel", lang),
                        color = NeumorphicTextPrimary.copy(alpha = 0.7f)
                    )
                }
            },
            containerColor = dialogBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    text = "${LanguageUtils.getString("history_title", lang)} (${uiState.historyList.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeumorphicTextPrimary
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.historyList.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .neumorphicButton3D(elevation = 3.5.dp)
                            .background(NeumorphicBg, shape = CircleShape)
                            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.80f), CircleShape)
                            .clip(CircleShape)
                            .clickable { showClearConfirmDialog = true }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = LanguageUtils.getString("clear_history", lang),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                if (onClose != null) {
                    NeumorphicIconButton(
                        icon = Icons.Default.Close,
                        onClick = onClose,
                        contentDescription = "Close",
                        size = 32.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = NeumorphicAccent.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = LanguageUtils.getString("history_empty", lang),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeumorphicTextPrimary.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            val itemShape = RoundedCornerShape(16.dp)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(
                    items = uiState.historyList,
                    key = { it.id }
                ) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neumorphicExtruded(shape = itemShape, elevation = 4.dp)
                            .background(NeumorphicBg, shape = itemShape)
                            .border(1.dp, NeumorphicAccent.copy(alpha = 0.15f), shape = itemShape)
                            .clip(itemShape)
                            .clickable {
                                if (item.detail.contains("\n") || item.detail.length > 25) {
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
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(NeumorphicAccent.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = LanguageUtils.getLocalizedHistoryCategory(item.category, lang),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeumorphicAccent
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = LanguageUtils.getLocalizedHistoryTitle(item.title, lang),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NeumorphicTextPrimary
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Rename",
                                        tint = NeumorphicAccent,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                newTitleInput = item.title
                                                itemToRename = item
                                            }
                                    )

                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = NeumorphicAccent,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                val clipText = "${item.title}: ${item.detail}"
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("HistoryRecord", clipText)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                            }
                                    )

                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                itemToDelete = item
                                            }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = LanguageUtils.getLocalizedHistoryDetail(item.detail, lang),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = NeumorphicTextPrimary.copy(alpha = 0.85f),
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = DateCalculatorUtils.formatTimestamp(item.timestamp, lang),
                                    fontSize = 11.sp,
                                    color = NeumorphicTextPrimary.copy(alpha = 0.5f)
                                )

                                Text(
                                    text = LanguageUtils.getLocalizedRegionTag(item.regionTag, lang),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeumorphicAccent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
