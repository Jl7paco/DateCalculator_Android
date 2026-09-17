package me.paco.datecalculator.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.paco.datecalculator.R
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.WeekendRule
import me.paco.datecalculator.util.DateCalculatorUtils
import java.time.LocalDate

@Composable
fun ResultCard(
    visible: Boolean,
    title: String = "计算结果日期",
    resultDate: LocalDate,
    weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
    enableHolidays: Boolean = true,
    holidayRegion: HolidayRegion = HolidayRegion.CHINA,
    isCurrentWeekBigWeek: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateStr = DateCalculatorUtils.formatDate(resultDate)
    val shortDateStr = resultDate.format(DateCalculatorUtils.SHORT_DATE_FORMATTER)
    val descStr = DateCalculatorUtils.getDateDescription(resultDate)
    val isWork = DateCalculatorUtils.isWorkday(resultDate, weekendRule, enableHolidays, holidayRegion, isCurrentWeekBigWeek)

    val titleDisplay = when (title) {
        "工作日计算结果日期" -> stringResource(R.string.label_result_title_workday)
        "自然日计算结果日期" -> stringResource(R.string.label_result_title_natural)
        else -> title
    }

    val chipDisplay = if (isWork) {
        stringResource(R.string.label_workday_chip)
    } else {
        stringResource(R.string.label_weekend_chip)
    }

    val copyToast = stringResource(R.string.toast_copied)

    // 新拟物风格计算结果卡片
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(150)) + expandVertically(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = tween(150))
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .neumorphicExtruded(shape = RoundedCornerShape(24.dp), elevation = 6.dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f), shape = RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = titleDisplay,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = dateStr,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                SuggestionChip(
                    onClick = {},
                    shape = CircleShape,
                    label = {
                        Text(
                            text = chipDisplay,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = descStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 完全对齐原素材图效的新拟物复制按钮 (无硬描边，柔和自然 3D 光影)
                NeumorphicCopyButton(
                    text = stringResource(R.string.label_copy_result),
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("DateResult", "$shortDateStr ($dateStr)")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, copyToast, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

/**
 * 专为浅蓝卡片背景设计的新拟物复制胶囊按键 (完全贴合原素材图效，绝无硬线条描边)
 */
@Composable
fun NeumorphicCopyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val shadowDark = if (isDark) Color.Black.copy(alpha = 0.6f) else Color(0xFF7A8DA8).copy(alpha = 0.50f)
    val shadowLight = if (isDark) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.45f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .drawBehind {
                val shadowRadius = 4.dp.toPx()
                val shapeOutline = CircleShape.createOutline(size, layoutDirection, this)

                // 1. 右下自然蓝灰柔暗影
                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        asFrameworkPaint().apply {
                            isAntiAlias = true
                            color = android.graphics.Color.TRANSPARENT
                            setShadowLayer(
                                shadowRadius,
                                shadowRadius * 0.5f,
                                shadowRadius * 0.5f,
                                shadowDark.toArgb()
                            )
                        }
                    }
                    canvas.drawOutline(shapeOutline, paint)
                }

                // 2. 左上自然柔和白高光
                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        asFrameworkPaint().apply {
                            isAntiAlias = true
                            color = android.graphics.Color.TRANSPARENT
                            setShadowLayer(
                                shadowRadius * 0.8f,
                                -shadowRadius * 0.4f,
                                -shadowRadius * 0.4f,
                                shadowLight.toArgb()
                            )
                        }
                    }
                    canvas.drawOutline(shapeOutline, paint)
                }
            }
            .background(NeumorphicBg, shape = CircleShape)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                tint = NeumorphicAccent,
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(
                text = text,
                color = NeumorphicTextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
