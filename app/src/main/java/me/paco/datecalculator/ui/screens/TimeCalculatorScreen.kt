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
import kotlin.math.abs

/**
 * 100% 原汁原味还原截图按键位置、移除二层卡片底盒的高级新拟物 UI 风格时间计算器 Screen
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

    // 时间与日期表达式极速计算引擎
    fun evaluateExpression() {
        if (expression.isBlank()) {
            resultText = ""
            return
        }
        val evaluated = TimeExpressionEvaluator.evaluate(expression)
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

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(14.dp)
        ) {
            if (showTopHeader) {
                // 顶栏 (36dp 高度, 15sp 标题)
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

            // 1. 表达式与结果巨幕展示卡片 (匹配截图阴影边框样式)
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

                    Text(
                        text = expression.ifBlank { "0" },
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

                    Text(
                        text = if (resultText.isNotBlank()) "=$resultText" else "=0s",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = NeumorphicAccent,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. 需求 1: 单位图例提示 Chip 行 (含 M = 月, y = 年, d = 天数, h = 小时, m = 分钟, s = 秒)
            val unitHints = when (effectiveLang) {
                AppLanguage.ENGLISH -> listOf("y=Y", "M=M", "d=D", "h=H", "m=M", "s=S")
                AppLanguage.TRADITIONAL_CHINESE -> listOf("y=年", "M=月", "d=日", "h=時", "m=分", "s=秒")
                AppLanguage.JAPANESE -> listOf("y=年", "M=月", "d=日", "h=時", "m=分", "s=秒")
                AppLanguage.KOREAN -> listOf("y=년", "M=월", "d=일", "h=시", "m=분", "s=초")
                else -> listOf("y=年", "M=月", "d=日", "h=时", "m=分", "s=秒")
            }

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
                // Row 0: 需求 1 - 加入 "M = 月" 的全量时间与日期单位按键组 (y, M, d, h, m, s)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TimeCalcKeyBtn(label = "y", onClick = { HapticUtils.performCrispClick(context, view); appendToken("y") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = "M", onClick = { HapticUtils.performCrispClick(context, view); appendToken("M") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = "d", onClick = { HapticUtils.performCrispClick(context, view); appendToken("d") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = "h", onClick = { HapticUtils.performCrispClick(context, view); appendToken("h") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = "m", onClick = { HapticUtils.performCrispClick(context, view); appendToken("m") }, modifier = Modifier.weight(1f))
                    TimeCalcKeyBtn(label = "s", onClick = { HapticUtils.performCrispClick(context, view); appendToken("s") }, modifier = Modifier.weight(1f))
                }

                // Row 1: 需求：括号 `(` 与 `)` 100% 放在同一行 (Clear, ⌫, (, ))
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
                    TimeCalcKeyBtn(label = "=", isAccentColor = true, onClick = {
                        HapticUtils.performCrispClick(context, view)
                        evaluateExpression()
                    }, modifier = Modifier.weight(1f))
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
    textColor: Color? = null
) {
    val keyShape = RoundedCornerShape(14.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "TimeKeyScale"
    )

    val bg = if (isAccentColor) NeumorphicAccent else NeumorphicBg
    val txtColor = textColor ?: if (isAccentColor) Color.White else NeumorphicTextPrimary

    Box(
        modifier = modifier
            .height(52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .neumorphicExtruded(shape = keyShape, elevation = if (isPressed) 2.dp else 4.dp)
            .background(bg, shape = keyShape)
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
            color = txtColor
        )
    }
}

/**
 * 智能时间与日期计算推算引擎 (支持 999d -> 2y 8M 29d 自动规范换算, 及 2026y 12M 9d + 20d -> 2026y 12M 29d 日期加减)
 */
object TimeExpressionEvaluator {

    private data class ParsedDateToken(
        val year: Int,
        val month: Int,
        val day: Int
    )

    private fun tryParseDate(tokenStr: String): ParsedDateToken? {
        val yMatch = Regex("(\\d{4})\\s*y").find(tokenStr)?.groupValues?.get(1)?.toIntOrNull()
        val mMatch = Regex("(\\d{1,2})\\s*M").find(tokenStr)?.groupValues?.get(1)?.toIntOrNull()
        val dMatch = Regex("(\\d{1,2})\\s*d").find(tokenStr)?.groupValues?.get(1)?.toIntOrNull()

        if (yMatch != null && mMatch != null && dMatch != null) {
            return try {
                ParsedDateToken(yMatch, mMatch, dMatch)
            } catch (_: Exception) { null }
        }
        return null
    }

    private fun parseTimeToSeconds(timeStr: String): Double {
        var totalSec = 0.0
        val regex = Regex("(\\d+(?:\\.\\d+)?)\\s*([yMdhms])")
        val matches = regex.findAll(timeStr)

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

        if (!foundUnit) {
            totalSec = timeStr.trim().toDoubleOrNull() ?: 0.0
        }
        return totalSec
    }

    // 需求 1: 999 天自动规范换算成 2y 8M 29d
    fun formatSecondsToHumanReadable(seconds: Double): String {
        if (seconds < 0) {
            return "-" + formatSecondsToHumanReadable(abs(seconds))
        }

        var sec = seconds.toLong()
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

        val parts = mutableListOf<String>()
        if (years > 0) parts.add("${years}y")
        if (months > 0) parts.add("${months}M")
        if (remDays > 0) parts.add("${remDays}d")
        if (hours > 0) parts.add("${hours}h")
        if (minutes > 0) parts.add("${minutes}m")
        if (remSec > 0 || parts.isEmpty()) parts.add("${remSec}s")

        return parts.joinToString(" ")
    }

    fun evaluate(expression: String): String {
        try {
            val cleanExpr = expression
                .replace("×", "*")
                .replace("÷", "/")
                .replace(" ", "")
                .trim()

            if (cleanExpr.isBlank()) return ""

            // 需求 1: 检查是否属于日期加减推算 (例如 2026y 12M 9d + 20d)
            val dateAddSubRegex = Regex("^(\\d{4}y\\d{1,2}M\\d{1,2}d)\\s*([+\\-])\\s*(\\d+(?:\\.\\d+)?[yMdhms])$")
            val dateMatch = dateAddSubRegex.find(cleanExpr)

            if (dateMatch != null) {
                val dateStr = dateMatch.groupValues[1]
                val op = dateMatch.groupValues[2]
                val durationStr = dateMatch.groupValues[3]

                val parsedDate = tryParseDate(dateStr)
                if (parsedDate != null) {
                    val baseLocalDate = LocalDate.of(parsedDate.year, parsedDate.month, parsedDate.day)
                    val durDays = (parseTimeToSeconds(durationStr) / 86400.0).toLong()

                    val resultLocalDate = if (op == "+") {
                        baseLocalDate.plusDays(durDays)
                    } else {
                        baseLocalDate.minusDays(durDays)
                    }

                    return "${resultLocalDate.year}y ${resultLocalDate.monthValue}M ${resultLocalDate.dayOfMonth}d"
                }
            }

            // 标准时间跨度加减推算
            val tokens = cleanExpr.split(Regex("(?<=[+\\-*/()])|(?=[+\\-*/()])")).map { it.trim() }.filter { it.isNotEmpty() }
            if (tokens.isEmpty()) return ""

            var currentVal = parseTimeToSeconds(tokens[0])
            var i = 1
            while (i < tokens.size - 1) {
                val op = tokens[i]
                val nextVal = parseTimeToSeconds(tokens[i + 1])
                when (op) {
                    "+" -> currentVal += nextVal
                    "-" -> currentVal -= nextVal
                    "*" -> currentVal *= nextVal
                    "/" -> if (nextVal != 0.0) currentVal /= nextVal
                }
                i += 2
            }

            return formatSecondsToHumanReadable(currentVal)
        } catch (_: Exception) {
            return "Err"
        }
    }
}