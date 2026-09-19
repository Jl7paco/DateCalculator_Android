package me.paco.datecalculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val initialMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        yearRange = 1900..2100
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .neumorphicExtruded(shape = RoundedCornerShape(28.dp), elevation = 8.dp)
                .background(NeumorphicBg, shape = RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 新拟物定制版 DatePicker 视觉适配 (指定 1900..2100 年份范围，修复文本输入模式光标格式卡顿)
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = NeumorphicBg,
                        titleContentColor = NeumorphicTextPrimary,
                        headlineContentColor = NeumorphicAccent,
                        weekdayContentColor = NeumorphicTextPrimary,
                        subheadContentColor = NeumorphicTextPrimary,
                        yearContentColor = NeumorphicTextPrimary,
                        currentYearContentColor = NeumorphicAccent,
                        selectedYearContentColor = Color.White,
                        selectedYearContainerColor = NeumorphicAccent,
                        dayContentColor = NeumorphicTextPrimary,
                        disabledDayContentColor = NeumorphicTextPrimary.copy(alpha = 0.3f),
                        selectedDayContentColor = Color.White,
                        selectedDayContainerColor = NeumorphicAccent,
                        todayDateBorderColor = NeumorphicAccent,
                        todayContentColor = NeumorphicAccent
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 新拟物确认与取消双胶囊按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    // 取消按钮
                    Box(
                        modifier = Modifier
                            .height(42.dp)
                            .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
                            .background(NeumorphicBg, shape = CircleShape)
                            .clip(CircleShape)
                            .clickable { onDismiss() }
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("取消", color = NeumorphicTextPrimary, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // 确定按钮 (亮蓝高亮按键)
                    Box(
                        modifier = Modifier
                            .height(42.dp)
                            .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
                            .background(NeumorphicAccent, shape = CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val localDate = Instant.ofEpochMilli(millis)
                                        .atZone(ZoneId.of("UTC"))
                                        .toLocalDate()
                                    onDateSelected(localDate)
                                }
                                onDismiss()
                            }
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("确定", color = Color.White, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}
