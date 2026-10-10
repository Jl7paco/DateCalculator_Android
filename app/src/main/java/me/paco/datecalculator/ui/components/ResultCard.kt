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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.DateMode
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.StageSegmentResult
import me.paco.datecalculator.data.WeekendRule
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.LanguageUtils
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
    appLanguage: AppLanguage = AppLanguage.SIMPLIFIED_CHINESE,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateStr = DateCalculatorUtils.formatDate(resultDate, appLanguage)
    val descStr = DateCalculatorUtils.formatDateWithWeek(resultDate, appLanguage)
    val isWork = DateCalculatorUtils.isWorkday(resultDate, weekendRule, enableHolidays, holidayRegion, isCurrentWeekBigWeek)

    val titleDisplay = if (dateMode == DateMode.WORKDAY) LanguageUtils.getString("result_title_workday", appLanguage) else LanguageUtils.getString("result_title_natural", appLanguage)
    val chipDisplay = if (isWork) LanguageUtils.getString("workday_chip", appLanguage) else LanguageUtils.getString("weekend_chip", appLanguage)

    val copyToast = stringResource(R.string.toast_copied)
    val shareText = "$titleDisplay: $dateStr ($descStr)"

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
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = titleDisplay,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeumorphicAccent
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = dateStr,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeumorphicTextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = descStr,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeumorphicTextPrimary.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                val (constName, constEmoji) = LunarCalendarUtils.getConstellationInfo(resultDate)
                val fortune = LunarCalendarUtils.getDailyFortune(resultDate, constName, appLanguage)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val localizedConstellation = LanguageUtils.getLocalizedConstellation(constName, appLanguage)
                    SuggestionChip(
                        onClick = {},
                        shape = CircleShape,
                        label = { Text("$constEmoji $localizedConstellation", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )

                    SuggestionChip(
                        onClick = {},
                        shape = CircleShape,
                        label = { Text(chipDisplay, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "✨ ${fortune.summary}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = NeumorphicTextPrimary.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(12.dp))

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
                    planTitle = "",
                    language = appLanguage
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeumorphicIconButton(
                        icon = Icons.Default.Share,
                        contentDescription = "Share",
                        size = 36.dp,
                        onClick = {
                            ShareUtils.shareText(context, shareText)
                        }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    NeumorphicIconButton(
                        icon = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        size = 36.dp,
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("ResultDate", shareText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, copyToast, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}
