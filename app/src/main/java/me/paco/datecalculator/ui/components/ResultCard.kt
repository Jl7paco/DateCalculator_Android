package me.paco.datecalculator.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.paco.datecalculator.R
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.DateMode
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.StageSegmentResult
import me.paco.datecalculator.data.WeekendRule
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LunarCalendarUtils
import me.paco.datecalculator.util.ShareUtils
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

@Composable
fun ResultCard(
    visible: Boolean,
    title: String = "计算结果日期",
    baseDate: LocalDate = LocalDate.now(),
    resultDate: LocalDate,
    calculationType: CalculationType = CalculationType.ADD,
    daysInput: String = "15",
    dateMode: DateMode = DateMode.WORKDAY,
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

    val (constName, constEmoji) = LunarCalendarUtils.getConstellationInfo(resultDate)
    val fortune = LunarCalendarUtils.getDailyFortune(resultDate, constName)

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
    val shareText = "$titleDisplay: $dateStr ($descStr) | 星座: $constEmoji $constName | 运势: ${fortune.summary}"

    val cardShape24 = RoundedCornerShape(24.dp)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(150)) + expandVertically(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = tween(150))
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .neumorphicExtruded(shape = cardShape24, elevation = 6.dp)
                .background(NeumorphicBg, shape = cardShape24)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), shape = cardShape24)
                .border(1.2.dp, NeumorphicAccent.copy(alpha = 0.35f), shape = cardShape24)
                .clip(cardShape24)
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
                        tint = NeumorphicAccent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = titleDisplay,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeumorphicAccent
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = dateStr,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeumorphicTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                    SuggestionChip(
                        onClick = {},
                        shape = CircleShape,
                        label = {
                            Text(
                                text = "$constEmoji $constName",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = descStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = NeumorphicTextPrimary.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "✨ 运势: ${fortune.summary}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = NeumorphicTextPrimary.copy(alpha = 0.75f)
                )

                // 直接将单段时间轴合并到计算结果卡片内部，不提供 CSV 导出
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(10.dp))

                val unitLabel = if (dateMode == DateMode.WORKDAY) "工作日" else "自然日"
                val rawDays = daysInput.toLongOrNull() ?: 0L
                val singleSegment = StageSegmentResult(
                    stageIndex = 0,
                    remark = "单段推算",
                    type = calculationType,
                    daysCount = rawDays,
                    startDate = baseDate,
                    endDate = resultDate,
                    totalCalendarDays = abs(ChronoUnit.DAYS.between(baseDate, resultDate)),
                    restDaysCount = 0L
                )

                TimelineDiagram(
                    baseDate = baseDate,
                    finalDate = resultDate,
                    segments = listOf(singleSegment),
                    modeLabel = unitLabel,
                    regionLabel = "${holidayRegion.flagEmoji} ${holidayRegion.nativeName}",
                    showOuterCard = false,
                    showExportButton = false
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 右下角部署纯图标格式的复制与分享按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeumorphicIconButton(
                        icon = Icons.Default.Share,
                        contentDescription = "分享结果",
                        onClick = {
                            ShareUtils.shareText(context, shareText)
                        }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    NeumorphicIconButton(
                        icon = Icons.Default.ContentCopy,
                        contentDescription = "复制结果",
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
}

/**
 * 部署在右下角的纯图标新拟物按键
 */
@Composable
fun NeumorphicIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "IconBtnScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
            .background(NeumorphicBg, shape = CircleShape)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = NeumorphicAccent,
            modifier = Modifier.size(18.dp)
        )
    }
}
