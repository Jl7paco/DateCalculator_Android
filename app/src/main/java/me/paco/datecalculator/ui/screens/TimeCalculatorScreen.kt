package me.paco.datecalculator.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import me.paco.datecalculator.ui.components.NeumorphicSunkenBg
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import me.paco.datecalculator.ui.components.SettingsOverlayDialog
import me.paco.datecalculator.ui.components.neumorphicButton3D
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import me.paco.datecalculator.ui.viewmodel.DateCalculatorUiState
import me.paco.datecalculator.ui.viewmodel.DateCalculatorViewModel
import me.paco.datecalculator.util.HapticUtils
import me.paco.datecalculator.util.LanguageUtils
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/**
 * 全量支持 5 国语言动态随系统/App语言自动本地化的高精时间跨度与加减推算计算器
 */
@Composable
fun TimeCalculatorScreen(
    viewModel: DateCalculatorViewModel,
    uiState: DateCalculatorUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scrollState = rememberScrollState()
    val lang = uiState.appLanguage
    val effectiveLang = lang.getEffectiveLanguage()
    val isDark = LocalDarkTheme.current

    var expression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }

    var showHistoryDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

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

    fun appendToken(token: String) {
        expression += token
        resultText = evaluateTimeExpression(expression, lang)
    }

    fun backspace() {
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
            resultText = evaluateTimeExpression(expression, lang)
        }
    }

    fun clearAll() {
        expression = ""
        resultText = ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NeumorphicBg)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 顶栏 Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = NeumorphicAccent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = LanguageUtils.getString("tab_time_calc", lang),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeumorphicTextPrimary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NeumorphicIconButton(
                    icon = Icons.Default.History,
                    onClick = { showHistoryDialog = true },
                    contentDescription = "History"
                )
                NeumorphicIconButton(
                    icon = Icons.Default.Settings,
                    onClick = { showSettingsDialog = true },
                    contentDescription = "Settings"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. 输入与结果高保真展示卡片区 (随 App/系统语言动态实时格式化文本)
        val displayShape = RoundedCornerShape(20.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neumorphicExtruded(shape = displayShape, elevation = 5.dp)
                .background(NeumorphicBg, shape = displayShape)
                .clip(displayShape)
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

        Spacer(modifier = Modifier.height(12.dp))

        // 2. 单位图例提示 行 (动态呈现如 y=年, M=月, d=天, h=时, m=分, s=秒)
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

        Spacer(modifier = Modifier.height(16.dp))

        // 3. 键盘按键区 (单位按键随 App/系统语言设置动态实时更变，例如 年/月/天/时/分/秒)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 0: 随系统/App 语言动态呈现全量时间单位按键
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

            // Row 1: (Clear, ⌫, (, ))
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
                TimeCalcKeyBtn(label = "(", modifier = Modifier.weight(1f), onClick = { HapticUtils.performCrispClick(context, view); appendToken("(") })
                TimeCalcKeyBtn(label = ")", modifier = Modifier.weight(1f), onClick = { HapticUtils.performCrispClick(context, view); appendToken(")") })
            }

            // Row 2: (7, 8, 9, ÷)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TimeCalcKeyBtn(label = "7", modifier = Modifier.weight(1f), onClick = { HapticUtils.performCrispClick(context, view); appendToken("7") })
                TimeCalcKeyBtn(label = "8", modifier = Modifier.weight(1f), onClick = { HapticUtils.performCrispClick(context, view); appendToken("8") })
                TimeCalcKeyBtn(label = "9", modifier = Modifier.weight(1f), onClick = { HapticUtils.performCrispClick(context, view); appendToken("9") })
                TimeCalcKeyBtn(label = "÷", isAccentColor = true, modifier = Modifier.weight(1f), onClick = { HapticUtils.performCrispClick(context, view); appendToken("÷") })
            }

            // Row 3: (4, 5, 6, ×)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TimeCalcKeyBtn(label = "4", modifier = Modifier.weight(1f), onClick = { HapticUtils.performCrispClick(context, view); appendToken("4") })
                TimeCalcKeyBtn(label = "5", modifier = Modifier.weight(1f), onClick = { HapticUtils.performCrispClick(context, view); appendToken("5") })
                TimeCalcKeyBtn(label = "6", modifier = Modifier.weight(1f), onClick = { HapticUtils.performCrispClick(context, view); appendToken("6") })
                TimeCalcKeyBtn(label = "×", isAccentColor = true, modifier = Modifier.weight(1f), onClick = { HapticUtils.performCrispClick(context, view); appendToken("×") })
            }

            // Row 4: (1, 2, 3, -)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TimeCalcKeyBtn(label = "1", modifier = Modifier.weight(1f), onClick = { HapticUtils.performCrispClick(context, view); appendToken("1") })
                TimeCalcKeyBtn(label = "2", modifier = Modifier.weight(1f), onClick = { HapticUtils.performCrispClick(context, view); appendToken("2") })
                TimeCalcKeyBtn(label = "3", modifier = Modifier.weight(1f), onClick = { HapticUtils.performCrispClick(context, view); appendToken("3") })
                TimeCalcKeyBtn(label = "-", isAccentColor = true, modifier = Modifier.weight(1f), onClick = { HapticUtils.performCrispClick(context, view); appendToken("-") })
            }

            // Row 5: (0, ., =, +)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TimeCalcKeyBtn(label = "0", modifier = Modifier.weight(1f), onClick = { HapticUtils.performCrispClick(context, view); appendToken("0") })
                TimeCalcKeyBtn(label = ".", modifier = Modifier.weight(1f), onClick = { HapticUtils.performCrispClick(context, view); appendToken(".") })
                TimeCalcKeyBtn(
                    label = "=",
                    isAccentColor = false,
                    textColor = Color.White,
                    btnBgColor = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        HapticUtils.performCrispClick(context, view)
                        if (expression.isNotBlank()) {
                            val evalResult = evaluateTimeExpression(expression, lang)
                            if (evalResult.isNotBlank() && evalResult != "Err") {
                                viewModel.saveToHistory("时间计算器", LanguageUtils.formatTimeExpressionForDisplay(expression, lang), evalResult)
                            }
                        }
                    }
                )
                TimeCalcKeyBtn(label = "+", isAccentColor = true, modifier = Modifier.weight(1f), onClick = { HapticUtils.performCrispClick(context, view); appendToken("+") })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val btnShape = RoundedCornerShape(14.dp)

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "TimeKeyScale"
    )

    val finalBg = btnBgColor ?: (if (isAccentColor) NeumorphicAccent else NeumorphicBg)
    val finalTxtColor = textColor ?: (if (isAccentColor) Color.White else NeumorphicTextPrimary)

    Box(
        modifier = modifier
            .height(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .neumorphicExtruded(shape = btnShape, elevation = 3.dp)
            .background(finalBg, shape = btnShape)
            .clip(btnShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = finalTxtColor
        )
    }
}

private data class ParsedDateParts(
    val year: Int,
    val month: Int,
    val day: Int
)

private fun tryParseDate(str: String): ParsedDateParts? {
    val regex = Regex("(\\d{4})y\\s*(\\d{1,2})M\\s*(\\d{1,2})d")
    val match = regex.find(str)
    if (match != null) {
        val y = match.groupValues[1].toIntOrNull() ?: return null
        val m = match.groupValues[2].toIntOrNull() ?: return null
        val d = match.groupValues[3].toIntOrNull() ?: return null
        return ParsedDateParts(y, m, d)
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

/**
 * 随系统/App语言设置多国语言本地化推算输出 (例如 2年 8月 29天 5小时 30分 12秒)
 */
private fun formatSecondsToHumanReadable(seconds: Double, lang: AppLanguage): String {
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

private fun evaluateTimeExpression(expression: String, lang: AppLanguage): String {
    try {
        val cleanExpr = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace(" ", "")
            .trim()

        if (cleanExpr.isBlank()) return ""

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

                val uY = LanguageUtils.getTimeUnitLabel("y", lang)
                val uM = LanguageUtils.getTimeUnitLabel("M", lang)
                val uD = LanguageUtils.getTimeUnitLabel("d", lang)

                return "${resultLocalDate.year}$uY ${resultLocalDate.monthValue}$uM ${resultLocalDate.dayOfMonth}$uD"
            }
        }

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

        return formatSecondsToHumanReadable(currentVal, lang)
    } catch (_: Exception) {
        return "Err"
    }
}
