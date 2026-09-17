package me.paco.datecalculator.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    text = stringResource(R.string.label_history_count_fmt, uiState.historyList.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeumorphicTextPrimary
                )
            }

            if (uiState.historyList.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                        .background(NeumorphicBg)
                        .clip(CircleShape)
                        .clickable { showClearDialog = true }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.label_clear), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

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
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(stringResource(R.string.label_empty_history), color = NeumorphicTextPrimary.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.historyList, key = { it.id }) { item ->
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val timeStr = sdf.format(Date(item.timestamp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neumorphicExtruded(shape = RoundedCornerShape(18.dp), elevation = 4.dp)
                            .background(NeumorphicBg)
                            .clip(RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = NeumorphicAccent
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .neumorphicExtruded(shape = RoundedCornerShape(10.dp), elevation = 3.dp)
                                            .background(NeumorphicBg)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                val clipText = "${item.title} (${item.detail})"
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("HistoryItem", clipText))
                                                Toast.makeText(context, copiedToast, Toast.LENGTH_SHORT).show()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = NeumorphicAccent, modifier = Modifier.size(18.dp))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .neumorphicExtruded(shape = RoundedCornerShape(10.dp), elevation = 3.dp)
                                            .background(NeumorphicBg)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                viewModel.deleteHistoryItem(item)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = item.detail, style = MaterialTheme.typography.bodyMedium, color = NeumorphicTextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.bodySmall,
                                color = NeumorphicTextPrimary.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}
