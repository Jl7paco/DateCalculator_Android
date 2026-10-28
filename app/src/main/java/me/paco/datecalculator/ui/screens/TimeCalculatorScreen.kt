package me.paco.datecalculator.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.ui.components.HistoryOverlayDialog
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicIconButton
import me.paco.datecalculator.ui.components.NeumorphicIconHeaderBadge
import me.paco.datecalculator.ui.components.NeumorphicSunkenBg
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import me.paco.datecalculator.ui.components.SettingsOverlayDialog
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.HapticUtils
import me.paco.datecalculator.util.LanguageUtils
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

/**
 * 100% 原汁原味还原截图按键位置、支持 Shunting-yard 逆波兰算法与友好错误提示的高级新拟物 UI 风格时间计算器 Screen
 */
@Composable
fun TimeCalculatorScreen(
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState,
    modifier: Modifier = Modifier,
    showTopHeader: Boolean = true
) {
    val context = LocalContext.current
    val view = LocalView.current
    val lang = uiState.appLanguage
    val effectiveLang = lang.getEffectiveLanguage()

    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    var expression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    fun evaluateExpression() {
        if (expression.isBlank()) {
            resultText = ""
            return
        }
        val evaluated = TimeExpressionEvaluator.evaluate(expression, lang)
        resultText = evaluated
    }

    fun appendToken(token: String) {
        expression += token
        evaluateExpression()
    }

    fun clearAll() {
        expression = ""
        resultText = ""
    }

    fun backspace() {
        if (expression.isNotEmpty()) {
            expression = expression.substring(0, expression.length - 1)
            evaluateExpression()
        }
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

    Box(modifier = modifier.fillMaxSize().background(NeumorphicBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(14.dp)
        ) {
            if (showTopHeader) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NeumorphicIconHeaderBadge(
                            icon = Icons.Default.MoreTime,
                            size = 28.dp,
                            iconSize = 14.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val titleText = when (effectiveLang) {
                            AppLanguage.ENGLISH -> "Time Calculator"
                            AppLanguage.TRADITIONAL_CHINESE -> "時間計算器"
                            AppLanguage.JAPANESE -> "時間計算機"
                            AppLanguage.KOREAN -> "시간 계산기"
                            else -> "时间计算器"
                        }
                        Text(
                            text = titleText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeumorphicTextPrimary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NeumorphicIconButton(
                            icon = Icons.Default.History,
                            onClick = { showHistoryDialog = true },
                            contentDescription = LanguageUtils.getString("history_title", lang),
                            size = 32.dp
                        )

                        NeumorphicIconButton(
                            icon = Icons.Default.Settings,
                            onClick = { showSettingsDialog = true },
                            contentDescription = LanguageUtils.getString("settings_title", lang),
                            size = 32.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // 1. 表达式与结果展示卡片
            val displayCardShape = RoundedCornerShape(18.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphicInset(shape = displayCardShape, elevation = 4.dp)
                    .border(1.2.dp, NeumorphicAccent.copy(alpha = 0.30f), shape = displayCardShape)
                    .background(NeumorphicSunkenBg, shape = displayCardShape)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = when (effectiveLang) {
                            AppLanguage.ENGLISH -> "Expression"
                            AppLanguage.TRADITIONAL_CHINESE -> "表達式"
                            AppLanguage.JAPANESE -> "数式"
                            AppLanguage.KOREAN -> "수식"
                            else -> "表达式"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeumorphicTextPrimary.copy(alpha = 0.65f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val formattedDisplayExpr = LanguageUtils.formatTimeExpressionForDisplay(expression, lang)

                    Text(
                        text = formattedDisplayExpr.ifBlank { "0" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeumorphicTextPrimary,
                        textAlign = TextAlign.End,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    HorizontalDivider(
                        color = NeumorphicTextPrimary.copy(alpha = 0.15f),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val zeroUnit = LanguageUtils.getTimeUnitLabel("s", lang)
                    if (resultText.startsWith("Err")) {
                        Text(
                            text = resultText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = if (resultText.isNotBlank()) "=$resultText" else "=0$zeroUnit",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = NeumorphicAccent,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. 单位图例提示 行 (y=年, M=月, d=日, h=时, m=分, s=秒)
            val unitY = LanguageUtils.getTimeUnitLabel("y", lang)
            val unitM = LanguageUtils.getTimeUnitLabel("M", lang)
            val unitD = LanguageUtils.getTimeUnitLabel("d", lang)
            val unitH = LanguageUtils.getTimeUnitLabel("h", lang)
            val unitMin = LanguageUtils.getTimeUnitLabel("m", lang)
            val unitS = LanguageUtils.getTimeUnitLabel("s", lang)

            val unitHints = listOf("y=$unitY", "M=$unitM", "d=$unitD", "h=$unitH", "m=$unitMin", "s=$unitS")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                unitHints.forEach { hint ->
                    Box(
                        modifier = Modifier
                            .neumorphicInset(shape = RoundedCornerShape(8.dp), elevation = 2.dp)
                            .background(NeumorphicSunkenBg, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = hint,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = NeumorphicTextPrimary.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 键盘按键区
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Row 0: 需求 1 - 动态多语言时间单位按键组 (年, 月, 天, 时, 分, 秒 / Y, M, D, H, Min, S)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TimeCalcKeyBtn(label = unitY, onClick = { HapticUtils.performCrispClick(context, view); appendToken("y") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = unitM, onClick = { HapticUtils.performCrispClick(context, view); appendToken("M") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = unitD, onClick = { HapticUtils.performCrispClick(context, view); appendToken("d") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = unitH, onClick = { HapticUtils.performCrispClick(context, view); appendToken("h") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = unitMin, onClick = { HapticUtils.performCrispClick(context, view); appendToken("m") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = unitS, onClick = { HapticUtils.performCrispClick(context, view); appendToken("s") }, modifier = Modifier.weight(1f))
                }

                // Row 1: 括号 `(` 与 `)` 100% 同行呈现 (Clear, ⌫, (, ))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TimeCalcKeyBtn(
                        label = "Clear",
                        isAccentColor = false,
                        textColor = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            HapticUtils.performCrispClick(context, view)
                            clearAll()
                        }
                    )
                    TimeCalcKeyBtn(
                        label = "⌫",
                        isAccentColor = false,
                        textColor = NeumorphicTextPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            HapticUtils.performCrispClick(context, view)
                            backspace()
                        }
                    )
                    TimeCalcKeyBtn(label = "(", onClick = { HapticUtils.performCrispClick(context, view); appendToken("(") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = ")", onClick = { HapticUtils.performCrispClick(context, view); appendToken(")") }, modifier = Modifier.weight(1f))
                }

                // Row 2: 7, 8, 9, ÷
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TimeCalcKeyBtn(label = "7", onClick = { HapticUtils.performCrispClick(context, view); appendToken("7") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = "8", onClick = { HapticUtils.performCrispClick(context, view); appendToken("8") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = "9", onClick = { HapticUtils.performCrispClick(context, view); appendToken("9") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = "÷", isAccentColor = true, onClick = { HapticUtils.performCrispClick(context, view); appendToken(" ÷ ") }, modifier = Modifier.weight(1f))
                }

                // Row 3: 4, 5, 6, ×
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TimeCalcKeyBtn(label = "4", onClick = { HapticUtils.performCrispClick(context, view); appendToken("4") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = "5", onClick = { HapticUtils.performCrispClick(context, view); appendToken("5") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = "6", onClick = { HapticUtils.performCrispClick(context, view); appendToken("6") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = "×", isAccentColor = true, onClick = { HapticUtils.performCrispClick(context, view); appendToken(" × ") }, modifier = Modifier.weight(1f))
                }

                // Row 4: 1, 2, 3, -
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TimeCalcKeyBtn(label = "1", onClick = { HapticUtils.performCrispClick(context, view); appendToken("1") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = "2", onClick = { HapticUtils.performCrispClick(context, view); appendToken("2") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = "3", onClick = { HapticUtils.performCrispClick(context, view); appendToken("3") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = "-", isAccentColor = true, onClick = { HapticUtils.performCrispClick(context, view); appendToken(" - ") }, modifier = Modifier.weight(1f))
                }

                // Row 5: 0, ., =, +
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TimeCalcKeyBtn(label = "0", onClick = { HapticUtils.performCrispClick(context, view); appendToken("0") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = ".", onClick = { HapticUtils.performCrispClick(context, view); appendToken(".") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(
                        label = "=",
                        isAccentColor = false,
                        textColor = Color.White,
                        btnBgColor = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            HapticUtils.performCrispClick(context, view)
                            if (expression.isNotBlank()) {
                                evaluateExpression()
                                if (resultText.isNotBlank() && !resultText.startsWith("Err")) {
                                    viewModel.saveToHistory("时间计算器", LanguageUtils.formatTimeExpressionForDisplay(expression, lang), resultText)
                                }
                            }
                        }
                    )
                    TimeCalcKeyBtn(label = "+", isAccentColor = true, onClick = { HapticUtils.performCrispClick(context, view); appendToken(" + ") }, modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TimeCalcKeyBtn(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAccentColor: Boolean = false,
    textColor: Color? = null,
    btnBgColor: Color? = null
) {
    val keyShape = RoundedCornerShape(14.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "TimeKeyScale"
    )

    val finalBg = btnBgColor ?: (if (isAccentColor) NeumorphicAccent else NeumorphicBg)
    val finalTxtColor = textColor ?: (if (isAccentColor) Color.White else NeumorphicTextPrimary)

    Box(
        modifier = modifier
            .height(52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .neumorphicExtruded(shape = keyShape, elevation = if (isPressed) 2.dp else 4.dp)
            .background(finalBg, shape = keyShape)
            .clip(keyShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = finalTxtColor
        )
    }
}

private class CalcException(val msgZh: String, val msgEn: String) : Exception(msgZh)

/**
 * 智能时间与日期计算推算引擎 (支持 Shunting-yard 逆波兰算法、日期加减推算、日期查距与友情错误提示)
 */
object TimeExpressionEvaluator {

    private sealed class TimeCalcValue {
        data class Scalar(val value: Double) : TimeCalcValue()
        data class DurationSec(val seconds: Double) : TimeCalcValue()
        data class CalendarDate(val date: LocalDate) : TimeCalcValue()
    }

    private fun parseTokenValue(token: String): TimeCalcValue {
        val clean = token.trim()

        val dateRegex = Regex("^(\\d{4})[y年]\\s*(\\d{1,2})[M月]\\s*(\\d{1,2})[d日天]$")
        val dateMatch = dateRegex.find(clean)
        if (dateMatch != null) {
            val y = dateMatch.groupValues[1].toInt()
            val m = dateMatch.groupValues[2].toInt()
            val d = dateMatch.groupValues[3].toInt()
            try {
                return TimeCalcValue.CalendarDate(LocalDate.of(y, m, d))
            } catch (_: Exception) {
                throw CalcException("日期格式无效 ($y-$m-$d)", "Invalid date ($y-$m-$d)")
            }
        }

        val unitRegex = Regex("(\\d+(?:\\.\\d+)?)\\s*([yMdhms])")
        val matches = unitRegex.findAll(clean)
        var totalSec = 0.0
        var foundUnit = false

        for (m in matches) {
            foundUnit = true
            val num = m.groupValues[1].toDoubleOrNull() ?: 0.0
            val unit = m.groupValues[2]
            totalSec += when (unit) {
                "y" -> num * 365.0 * 86400.0
                "M" -> num * 30.0 * 86400.0
                "d" -> num * 86400.0
                "h" -> num * 3600.0
                "m" -> num * 60.0
                "s" -> num
                else -> 0.0
            }
        }

        if (foundUnit) {
            return TimeCalcValue.DurationSec(totalSec)
        }

        val num = clean.toDoubleOrNull()
        if (num != null) {
            return TimeCalcValue.Scalar(num)
        }

        throw CalcException("无法识别的词条: $clean", "Unrecognized token: $clean")
    }

    private fun applyOp(op: String, a: TimeCalcValue, b: TimeCalcValue): TimeCalcValue {
        return when (op) {
            "+" -> when {
                a is TimeCalcValue.DurationSec && b is TimeCalcValue.DurationSec -> TimeCalcValue.DurationSec(a.seconds + b.seconds)
                a is TimeCalcValue.CalendarDate && b is TimeCalcValue.DurationSec -> {
                    val days = (b.seconds / 86400.0).toLong()
                    TimeCalcValue.CalendarDate(a.date.plusDays(days))
                }
                a is TimeCalcValue.DurationSec && b is TimeCalcValue.CalendarDate -> {
                    val days = (a.seconds / 86400.0).toLong()
                    TimeCalcValue.CalendarDate(b.date.plusDays(days))
                }
                a is TimeCalcValue.Scalar && b is TimeCalcValue.Scalar -> TimeCalcValue.Scalar(a.value + b.value)
                else -> throw CalcException("不同单位类型不能直接相加", "Cannot add different value types")
            }
            "-" -> when {
                a is TimeCalcValue.DurationSec && b is TimeCalcValue.DurationSec -> TimeCalcValue.DurationSec(a.seconds - b.seconds)
                a is TimeCalcValue.CalendarDate && b is TimeCalcValue.DurationSec -> {
                    val days = (b.seconds / 86400.0).toLong()
                    TimeCalcValue.CalendarDate(a.date.minusDays(days))
                }
                a is TimeCalcValue.CalendarDate && b is TimeCalcValue.CalendarDate -> {
                    val days = ChronoUnit.DAYS.between(b.date, a.date)
                    TimeCalcValue.DurationSec(days * 86400.0)
                }
                a is TimeCalcValue.Scalar && b is TimeCalcValue.Scalar -> TimeCalcValue.Scalar(a.value - b.value)
                else -> throw CalcException("不同单位类型不能直接相减", "Cannot subtract different value types")
            }
            "*" -> when {
                a is TimeCalcValue.DurationSec && b is TimeCalcValue.Scalar -> TimeCalcValue.DurationSec(a.seconds * b.value)
                a is TimeCalcValue.Scalar && b is TimeCalcValue.DurationSec -> TimeCalcValue.DurationSec(a.value * b.seconds)
                a is TimeCalcValue.Scalar && b is TimeCalcValue.Scalar -> TimeCalcValue.Scalar(a.value * b.value)
                a is TimeCalcValue.CalendarDate || b is TimeCalcValue.CalendarDate -> throw CalcException("日期不能参与乘法运算", "Dates cannot be multiplied")
                else -> throw CalcException("时长之间不能直接相乘", "Cannot multiply duration by duration")
            }
            "/" -> when {
                b is TimeCalcValue.Scalar && b.value == 0.0 -> throw CalcException("除数不能为0", "Division by zero")
                b is TimeCalcValue.DurationSec && b.seconds == 0.0 -> throw CalcException("除数不能为0", "Division by zero")
                a is TimeCalcValue.DurationSec && b is TimeCalcValue.Scalar -> TimeCalcValue.DurationSec(a.seconds / b.value)
                a is TimeCalcValue.DurationSec && b is TimeCalcValue.DurationSec -> TimeCalcValue.Scalar(a.seconds / b.seconds)
                a is TimeCalcValue.Scalar && b is TimeCalcValue.Scalar -> TimeCalcValue.Scalar(a.value / b.value)
                a is TimeCalcValue.CalendarDate || b is TimeCalcValue.CalendarDate -> throw CalcException("日期不能参与除法运算", "Dates cannot be divided")
                else -> throw CalcException("无法执行此除法运算", "Invalid division")
            }
            else -> throw CalcException("未知运算符: $op", "Unknown operator: $op")
        }
    }

    private fun formatResult(value: TimeCalcValue, lang: AppLanguage): String {
        return when (value) {
            is TimeCalcValue.Scalar -> {
                val v = value.value
                if (v == v.toLong().toDouble()) v.toLong().toString() else String.format(Locale.US, "%.2f", v)
            }
            is TimeCalcValue.CalendarDate -> {
                val d = value.date
                val uY = LanguageUtils.getTimeUnitLabel("y", lang)
                val uM = LanguageUtils.getTimeUnitLabel("M", lang)
                val uD = LanguageUtils.getTimeUnitLabel("d", lang)
                "${d.year}$uY ${d.monthValue}$uM ${d.dayOfMonth}$uD"
            }
            is TimeCalcValue.DurationSec -> {
                formatSecondsToHumanReadable(value.seconds, lang)
            }
        }
    }

    fun formatSecondsToHumanReadable(seconds: Double, lang: AppLanguage): String {
        if (seconds < 0) {
            return "-" + formatSecondsToHumanReadable(abs(seconds), lang)
        }

        val sec = seconds.toLong()
        val totalDays = sec / 86400L
        val remSecTotal = sec % 86400L

        val years = totalDays / 365L
        val remDaysAfterYear = totalDays % 365L
        val months = remDaysAfterYear / 30L
        val remDays = remDaysAfterYear % 30L

        val hours = remSecTotal / 3600L
        val remSecHours = remSecTotal % 3600L
        val minutes = remSecHours / 60L
        val remSec = remSecHours % 60L

        val uY = LanguageUtils.getTimeUnitLabel("y", lang)
        val uM = LanguageUtils.getTimeUnitLabel("M", lang)
        val uD = LanguageUtils.getTimeUnitLabel("d", lang)
        val uH = LanguageUtils.getTimeUnitLabel("h", lang)
        val uMin = LanguageUtils.getTimeUnitLabel("m", lang)
        val uS = LanguageUtils.getTimeUnitLabel("s", lang)

        val parts = mutableListOf<String>()
        if (years > 0) parts.add("${years}$uY")
        if (months > 0) parts.add("${months}$uM")
        if (remDays > 0) parts.add("${remDays}$uD")
        if (hours > 0) parts.add("${hours}$uH")
        if (minutes > 0) parts.add("${minutes}$uMin")
        if (remSec > 0 || parts.isEmpty()) parts.add("${remSec}$uS")

        return parts.joinToString(" ")
    }

    fun evaluate(expression: String, lang: AppLanguage): String {
        try {
            val cleanExpr = expression
                .replace("×", "*")
                .replace("÷", "/")
                .replace(" ", "")
                .trim()

            if (cleanExpr.isBlank()) return ""

            val tokenRegex = Regex("(\\d{4}[y年]\\d{1,2}[M月]\\d{1,2}[d日天]|(?:\\d+(?:\\.\\d+)?)[yMdhms]|\\d+(?:\\.\\d+)?|[+\\-*/()])")
            val rawTokens = tokenRegex.findAll(cleanExpr).map { it.value }.toList()

            if (rawTokens.isEmpty()) return ""

            // Shunting-yard 逆波兰算法
            val outputQueue = mutableListOf<String>()
            val operatorStack = java.util.Stack<String>()

            fun prec(op: String): Int = when (op) {
                "*", "/" -> 2
                "+", "-" -> 1
                else -> 0
            }

            for (token in rawTokens) {
                when (token) {
                    "+", "-", "*", "/" -> {
                        while (operatorStack.isNotEmpty() && prec(operatorStack.peek()) >= prec(token)) {
                            outputQueue.add(operatorStack.pop())
                        }
                        operatorStack.push(token)
                    }
                    "(" -> operatorStack.push(token)
                    ")" -> {
                        var foundParen = false
                        while (operatorStack.isNotEmpty()) {
                            val top = operatorStack.pop()
                            if (top == "(") {
                                foundParen = true
                                break
                            } else {
                                outputQueue.add(top)
                            }
                        }
                        if (!foundParen) throw CalcException("右括号无匹配", "Unmatched closing parenthesis")
                    }
                    else -> outputQueue.add(token)
                }
            }

            while (operatorStack.isNotEmpty()) {
                val top = operatorStack.pop()
                if (top == "(" || top == ")") throw CalcException("左括号无匹配", "Unmatched opening parenthesis")
                outputQueue.add(top)
            }

            // RPN 求值
            val evalStack = java.util.Stack<TimeCalcValue>()

            for (token in outputQueue) {
                if (token in setOf("+", "-", "*", "/")) {
                    if (evalStack.size < 2) throw CalcException("运算符缺少操作数", "Operator missing operands")
                    val b = evalStack.pop()
                    val a = evalStack.pop()
                    val result = applyOp(token, a, b)
                    evalStack.push(result)
                } else {
                    evalStack.push(parseTokenValue(token))
                }
            }

            if (evalStack.size != 1) throw CalcException("表达式不完整", "Incomplete expression")

            return formatResult(evalStack.pop(), lang)
        } catch (e: CalcException) {
            val errPrefix = "Err"
            val errMsg = if (lang == AppLanguage.ENGLISH) e.msgEn else e.msgZh
            return "$errPrefix ($errMsg)"
        } catch (_: Exception) {
            return if (lang == AppLanguage.ENGLISH) "Err (Syntax error)" else "Err (语法错误)"
        }
    }
}