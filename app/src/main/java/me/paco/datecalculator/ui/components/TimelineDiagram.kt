package me.paco.datecalculator.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.RegionalHolidays
import me.paco.datecalculator.data.StageSegmentResult
import me.paco.datecalculator.data.TimelineBlockType
import me.paco.datecalculator.data.WeekendRule
import me.paco.datecalculator.data.decomposeChronologicalBlocks
import me.paco.datecalculator.util.CsvExportUtils
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.HapticUtils
import me.paco.datecalculator.util.LanguageUtils
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.roundToInt

data class DayTimelineDetail(
    val date: LocalDate,
    val type: TimelineBlockType
)

/**
 * 1:1 还原参考图的高质感多段/单段时序安排示意图 (按压/滑动触发气泡，松手瞬间隐藏，无缝无分割线进度条)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimelineDiagram(
    segments: List<StageSegmentResult>,
    finalDate: LocalDate,
    baseDate: LocalDate,
    planTitle: String = "",
    isEmbedded: Boolean = false,
    weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
    enableHolidays: Boolean = true,
    holidayRegion: HolidayRegion = HolidayRegion.CHINA,
    isCurrentWeekBigWeek: Boolean = true,
    disableChinaShiftWorkdays: Boolean = false,
    language: AppLanguage = AppLanguage.SIMPLIFIED_CHINESE,
    modifier: Modifier = Modifier
) {
    // 校验：若未输入任何有效天数 (segments 为空或总天数为 0)，严禁渲染多段结果卡片
    val hasValidDays = segments.any { it.daysCount > 0L }
    if (segments.isEmpty() || !hasValidDays) return

    val context = LocalContext.current
    val cardShape = RoundedCornerShape(22.dp)
    val effectiveLang = language.getEffectiveLanguage()

    // 过滤出有有效天数的段落进行展示
    val validSegments = remember(segments) {
        segments.filter { it.daysCount > 0L }
    }

    // 全量时序分解块 (全传参作为 remember key，确保切换双休规则或节假日设置时 100% 实时重算)
    val allBlocks = remember(baseDate, finalDate, weekendRule, enableHolidays, holidayRegion, isCurrentWeekBigWeek, disableChinaShiftWorkdays) {
        decomposeChronologicalBlocks(
            startDate = baseDate,
            endDate = finalDate,
            weekendRule = weekendRule,
            enableHolidays = enableHolidays,
            holidayRegion = holidayRegion,
            isCurrentWeekBigWeek = isCurrentWeekBigWeek,
            disableChinaShiftWorkdays = disableChinaShiftWorkdays
        )
    }

    // 生成每日粒度的完整序列列表用于按压/滑动气泡交互
    val dayList = remember(baseDate, finalDate, weekendRule, enableHolidays, holidayRegion, isCurrentWeekBigWeek, disableChinaShiftWorkdays) {
        if (baseDate == finalDate) emptyList() else {
            val isForward = baseDate.isBefore(finalDate)
            val start = if (isForward) baseDate else finalDate
            val end = if (isForward) finalDate else baseDate

            val list = mutableListOf<DayTimelineDetail>()
            var curr = start
            while (!curr.isAfter(end)) {
                val isShift = enableHolidays && !disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(curr, holidayRegion)
                val isStat = enableHolidays && RegionalHolidays.isStatutoryHoliday(curr, holidayRegion)
                val isWeekend = weekendRule.isWeekend(curr, isCurrentWeekBigWeek)

                val t = when {
                    isShift -> TimelineBlockType.SHIFT_WORKDAY
                    isStat -> TimelineBlockType.STATUTORY_HOLIDAY
                    isWeekend -> TimelineBlockType.WEEKEND
                    else -> TimelineBlockType.WORKDAY
                }
                list.add(DayTimelineDetail(curr, t))
                curr = curr.plusDays(1)
            }
            list
        }
    }

    val totalDurationDays = remember(allBlocks) {
        allBlocks.sumOf { it.daysCount }.coerceAtLeast(1)
    }

    val totalWorkdays = remember(allBlocks) {
        allBlocks.filter { it.type == TimelineBlockType.WORKDAY || it.type == TimelineBlockType.SHIFT_WORKDAY }.sumOf { it.daysCount }
    }

    val totalWeekends = remember(allBlocks) {
        allBlocks.filter { it.type == TimelineBlockType.WEEKEND }.sumOf { it.daysCount }
    }

    val totalStatutoryHolidays = remember(allBlocks) {
        allBlocks.filter { it.type == TimelineBlockType.STATUTORY_HOLIDAY }.sumOf { it.daysCount }
    }

    // 气泡状态
    var activeHoverDay by remember { mutableStateOf<DayTimelineDetail?>(null) }
    var activeHoverSource by remember { mutableStateOf<String?>(null) }
    var barWidthPx by remember { mutableFloatStateOf(1f) }
    var touchFraction by remember { mutableFloatStateOf(0.5f) }
    var lastHoverIndex by remember { mutableIntStateOf(-1) }

    // 落指防抖与死区阈值状态 (彻底消除落指瞬间的物理微抖动)
    var initialTouchX by remember { mutableFloatStateOf(-1f) }
    var isDraggingThresholdMet by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val bubbleWidthDp = 270.dp
    val edgeMarginDp = 12.dp

    // 在屏幕中线两侧所能移动的最大安全像素位移 (严格计算气泡半宽与边缘边距，确保气泡绝对不会穿出左右屏幕边界)
    val maxAllowedOffsetPx = if (barWidthPx > 0f) {
        val bubbleHalfWidthPx = with(density) { (bubbleWidthDp / 2).toPx() }
        val edgeMarginPx = with(density) { edgeMarginDp.toPx() }
        ((barWidthPx / 2f) - bubbleHalfWidthPx - edgeMarginPx).coerceAtLeast(0f)
    } else 0f

    val rawTargetXOffsetPx = if (activeHoverDay != null && barWidthPx > 0f) {
        (touchFraction - 0.5f) * barWidthPx
    } else 0f

    val clampedTargetXOffsetPx = rawTargetXOffsetPx.coerceIn(-maxAllowedOffsetPx, maxAllowedOffsetPx).roundToInt()

    val animOffsetXPx = remember { Animatable(0f) }

    LaunchedEffect(clampedTargetXOffsetPx, activeHoverDay, isDraggingThresholdMet) {
        val target = clampedTargetXOffsetPx.toFloat()
        if (activeHoverDay != null) {
            if (!isDraggingThresholdMet) {
                // 首次落指：直接 snapTo 瞬移到落指位置，绝不从屏幕中间缓慢动画滑过去！
                animOffsetXPx.snapTo(target)
            } else {
                // 明确左右拖拽时：平滑跟随
                animOffsetXPx.animateTo(
                    targetValue = target,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
        }
    }

    // 高对比色彩映射（确保双休黄与节假日红绝对鲜明突出）
    val weekendColor = Color(0xFFF59E0B) // 高亮黄/橙
    val holidayColor = Color(0xFFEF4444) // 高亮红

    val containerModifier = if (isEmbedded) {
        modifier.fillMaxWidth()
    } else {
        modifier
            .fillMaxWidth()
            .neumorphicExtruded(shape = cardShape, elevation = 5.dp)
            .background(NeumorphicBg, shape = cardShape)
            .padding(16.dp)
    }

    Box(modifier = containerModifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. 标头行：图标 + 标题 + 右侧 3D 导出 CSV 按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = NeumorphicAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = planTitle.ifBlank { LanguageUtils.getString("timeline_title", language) },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeumorphicAccent
                    )
                }

                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .neumorphicExtruded(shape = CircleShape, elevation = 2.dp)
                        .background(NeumorphicBg, shape = CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            val exportTitle = planTitle.ifBlank { LanguageUtils.getString("timeline_title", language) }
                            CsvExportUtils.exportMultiStageCsv(context, exportTitle, baseDate, finalDate, validSegments)
                        }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = NeumorphicAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = LanguageUtils.getString("export_csv", language),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeumorphicAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. 总历时与类型汇总行
            val workdayLabel = when (effectiveLang) {
                AppLanguage.ENGLISH -> "Workday"
                AppLanguage.JAPANESE -> "稼働日"
                AppLanguage.KOREAN -> "근무일"
                AppLanguage.TRADITIONAL_CHINESE -> "工作日"
                else -> "工作日"
            }

            val weekendLabel = when (effectiveLang) {
                AppLanguage.ENGLISH -> "Weekend"
                AppLanguage.JAPANESE -> "週末"
                AppLanguage.KOREAN -> "주말"
                AppLanguage.TRADITIONAL_CHINESE -> "週末雙休"
                else -> "周末双休"
            }

            val holidayLabel = when (effectiveLang) {
                AppLanguage.ENGLISH -> "Holidays"
                AppLanguage.JAPANESE -> "祝日"
                AppLanguage.KOREAN -> "공휴일"
                AppLanguage.TRADITIONAL_CHINESE -> "法定節假日"
                else -> "法定节假日"
            }

            val durationLabel = when (effectiveLang) {
                AppLanguage.ENGLISH -> "Total"
                AppLanguage.JAPANESE -> "総所要"
                AppLanguage.KOREAN -> "총 기간"
                AppLanguage.TRADITIONAL_CHINESE -> "總歷時"
                else -> "总历时"
            }

            val daysUnit = LanguageUtils.getString("days_unit", language)

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 1. 工作日 (主题色)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeumorphicAccent)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$workdayLabel: $totalWorkdays $daysUnit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeumorphicAccent
                    )
                }

                // 2. 黄色的周末双休图例 (#F59E0B)
                if (totalWeekends > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(weekendColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$weekendLabel: $totalWeekends $daysUnit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                    }
                }

                // 3. 红色的法定节假日图例 (#EF4444)
                if (totalStatutoryHolidays > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(holidayColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$holidayLabel: $totalStatutoryHolidays $daysUnit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = holidayColor
                        )
                    }
                }

                // 4. 总历时
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$durationLabel: $totalDurationDays $daysUnit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeumorphicTextPrimary.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. 全局总时序进度条 (固定高度 18dp，气泡向上 -38dp 浮层展示，布局尺寸 100% 绝对恒定，下方子阶段轴 UI 绝不下滑移位)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
            ) {
                // A. 全局总时序进度条 (自然居于布局中，按压时位置静止不变)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { coords ->
                                barWidthPx = coords.size.width.toFloat()
                            }
                            .pointerInput(dayList) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull() ?: continue

                                        if (change.pressed) {
                                            change.consume()
                                            val touchX = change.position.x
                                            if (dayList.isNotEmpty() && barWidthPx > 0f) {
                                                if (initialTouchX < 0f) {
                                                    // 首次落指：锁定落指起点位置，绝不因触摸微抖动而发生位移
                                                    initialTouchX = touchX
                                                    isDraggingThresholdMet = false
                                                    touchFraction = (touchX / barWidthPx).coerceIn(0f, 1f)
                                                } else {
                                                    val dragDistancePx = abs(touchX - initialTouchX)
                                                    // 超过 14dp 后才判定为有意识的左右滑移
                                                    if (dragDistancePx > with(density) { 14.dp.toPx() }) {
                                                        isDraggingThresholdMet = true
                                                    }
                                                    if (isDraggingThresholdMet) {
                                                        touchFraction = (touchX / barWidthPx).coerceIn(0f, 1f)
                                                    }
                                                }

                                                val idx = (touchFraction * dayList.size).toInt().coerceIn(dayList.indices)
                                                if (idx != lastHoverIndex || activeHoverSource != "MAIN") {
                                                    lastHoverIndex = idx
                                                    activeHoverSource = "MAIN"
                                                    activeHoverDay = dayList[idx]
                                                    HapticUtils.performCrispClick(context)
                                                }
                                            }
                                        } else {
                                            // 手指松开/离开瞬间隐退气泡并重置落指坐标
                                            activeHoverSource = null
                                            activeHoverDay = null
                                            lastHoverIndex = -1
                                            initialTouchX = -1f
                                            isDraggingThresholdMet = false
                                        }
                                    }
                                }
                            }
                            .neumorphicInset(shape = CircleShape, elevation = 2.dp)
                            .background(NeumorphicSunkenBg, shape = CircleShape)
                            .clip(CircleShape)
                    ) {
                        if (allBlocks.isNotEmpty()) {
                            allBlocks.forEach { block ->
                                val weight = (block.daysCount.toFloat() / totalDurationDays).coerceAtLeast(0.01f)
                                val blockColor = when (block.type) {
                                    TimelineBlockType.WORKDAY, TimelineBlockType.SHIFT_WORKDAY -> NeumorphicAccent
                                    TimelineBlockType.STATUTORY_HOLIDAY -> holidayColor
                                    TimelineBlockType.WEEKEND -> weekendColor
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(weight)
                                        .fillMaxHeight()
                                        .background(blockColor)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(NeumorphicAccent)
                            )
                        }
                    }

                    // B. 触控按压点高亮框 (主轴被按压时显示高亮瞄准框)
                    if (activeHoverSource == "MAIN" && activeHoverDay != null && barWidthPx > 0f) {
                        val density = LocalDensity.current
                        val indicatorWidthDp = 16.dp
                        val indicatorWidthPx = with(density) { indicatorWidthDp.toPx() }
                        val offsetX = ((barWidthPx * touchFraction) - (indicatorWidthPx / 2f)).coerceIn(0f, barWidthPx - indicatorWidthPx)
                        Box(
                            modifier = Modifier
                                .offset(x = with(density) { offsetX.toDp() })
                                .width(indicatorWidthDp)
                                .fillMaxHeight()
                                .border(2.dp, Color(0xFFF59E0B), shape = RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.35f), shape = RoundedCornerShape(4.dp))
                        )
                    }
                }

                // C. 指向气泡浮层 (主轴被按压时悬浮在主轴正上方)
                if (activeHoverSource == "MAIN" && activeHoverDay != null) {
                    val density = LocalDensity.current
                    val bubbleOffsetPxY = with(density) { (-48).dp.roundToPx() }

                    Popup(
                        alignment = Alignment.TopCenter,
                        offset = IntOffset(animOffsetXPx.value.roundToInt(), bubbleOffsetPxY),
                        properties = PopupProperties(
                            focusable = false,
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false,
                            clippingEnabled = false
                        )
                    ) {
                        val dayDetail = activeHoverDay!!
                        val dateFormatted = DateCalculatorUtils.formatDateWithWeek(dayDetail.date, language)
                        val (typeText, typeColor) = when (dayDetail.type) {
                            TimelineBlockType.WORKDAY, TimelineBlockType.SHIFT_WORKDAY -> "$workdayLabel 💼" to NeumorphicAccent
                            TimelineBlockType.STATUTORY_HOLIDAY -> "$holidayLabel 🌸" to holidayColor
                            TimelineBlockType.WEEKEND -> "$weekendLabel 🏖️" to Color(0xFFD97706)
                        }

                        // 完整悬浮指向气泡卡片 (固定 270.dp 尺寸，气泡随触摸位置左右流畅平移)
                        Box(
                            modifier = Modifier
                                .width(270.dp)
                                .height(38.dp)
                                .neumorphicExtruded(shape = RoundedCornerShape(12.dp), elevation = 6.dp)
                                .background(NeumorphicBg, shape = RoundedCornerShape(12.dp))
                                .border(1.2.dp, NeumorphicAccent.copy(alpha = 0.35f), shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "📍 $dateFormatted",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeumorphicTextPrimary,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• $typeText",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = typeColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 判断是否仅单段且需隐藏重复详情
            val hideDuplicateDetail = isEmbedded || validSegments.size == 1

            if (!hideDuplicateDetail) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = NeumorphicTextPrimary.copy(alpha = 0.12f))
                Spacer(modifier = Modifier.height(12.dp))

                // 4. 多段子项目详细清单
                validSegments.forEachIndexed { idx, seg ->
                    val isAdd = (seg.type == CalculationType.ADD)
                    val typeTag = if (isAdd) LanguageUtils.getString("stage_add_label", language) else LanguageUtils.getString("stage_sub_label", language)
                    val signSymbol = if (isAdd) "+" else "-"

                    val segTotalDays = abs(ChronoUnit.DAYS.between(seg.startDate, seg.endDate)).coerceAtLeast(1L)
                    
                    val segBlocks = remember(seg.startDate, seg.endDate, weekendRule, enableHolidays, holidayRegion, isCurrentWeekBigWeek, disableChinaShiftWorkdays) {
                        decomposeChronologicalBlocks(
                            startDate = seg.startDate,
                            endDate = seg.endDate,
                            weekendRule = weekendRule,
                            enableHolidays = enableHolidays,
                            holidayRegion = holidayRegion,
                            isCurrentWeekBigWeek = isCurrentWeekBigWeek,
                            disableChinaShiftWorkdays = disableChinaShiftWorkdays
                        )
                    }

                    val (_, segStat, segWeek) = DateCalculatorUtils.calculateBreakdown(
                        startDate = seg.startDate,
                        endDate = seg.endDate,
                        weekendRule = weekendRule,
                        enableHolidays = enableHolidays,
                        holidayRegion = holidayRegion,
                        isCurrentWeekBigWeek = isCurrentWeekBigWeek,
                        disableChinaShiftWorkdays = disableChinaShiftWorkdays
                    )
                    val segRestTotal = segStat + segWeek

                    val titleText = "${seg.remark} ($typeTag)"

                    val restText = if (segRestTotal > 0) {
                        when (effectiveLang) {
                            AppLanguage.ENGLISH -> " (incl. $segRestTotal days rest)"
                            AppLanguage.JAPANESE -> " (休日${segRestTotal}日含む)"
                            AppLanguage.KOREAN -> " (휴일 ${segRestTotal}일 포함)"
                            AppLanguage.TRADITIONAL_CHINESE -> " (含 $segRestTotal 天休假)"
                            else -> " (含 $segRestTotal 天休假)"
                        }
                    } else ""

                    val segDayList = remember(seg.startDate, seg.endDate, weekendRule, enableHolidays, holidayRegion, isCurrentWeekBigWeek, disableChinaShiftWorkdays) {
                        if (seg.startDate == seg.endDate) emptyList() else {
                            val isForward = seg.startDate.isBefore(seg.endDate)
                            val start = if (isForward) seg.startDate else seg.endDate
                            val end = if (isForward) seg.endDate else seg.startDate

                            val list = mutableListOf<DayTimelineDetail>()
                            var curr = start
                            while (!curr.isAfter(end)) {
                                val isShift = enableHolidays && !disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(curr, holidayRegion)
                                val isStat = enableHolidays && RegionalHolidays.isStatutoryHoliday(curr, holidayRegion)
                                val isWeekend = weekendRule.isWeekend(curr, isCurrentWeekBigWeek)

                                val t = when {
                                    isShift -> TimelineBlockType.SHIFT_WORKDAY
                                    isStat -> TimelineBlockType.STATUTORY_HOLIDAY
                                    isWeekend -> TimelineBlockType.WEEKEND
                                    else -> TimelineBlockType.WORKDAY
                                }
                                list.add(DayTimelineDetail(curr, t))
                                curr = curr.plusDays(1)
                            }
                            list
                        }
                    }

                    var subBarWidthPx by remember { mutableFloatStateOf(1f) }
                    var subTouchFraction by remember { mutableFloatStateOf(0.5f) }

                    // 精确计算子轴气泡与高亮选区的 1:1 坐标对齐 (计算当前选中天数的视觉几何中心点，确保气泡与高亮框 100% 精确对齐)
                    val activeSubDayIndex = if (segDayList.isNotEmpty()) {
                        (subTouchFraction * segDayList.size).toInt().coerceIn(segDayList.indices)
                    } else 0

                    val subDayCenterFraction = if (segDayList.isNotEmpty()) {
                        (activeSubDayIndex + 0.5f) / segDayList.size.toFloat()
                    } else 0.5f

                    val subBarLeftOffsetDp = 30.dp
                    val bubbleHalfWidthDp = 135.dp
                    val edgeMarginDpSub = 12.dp

                    val subBarCenterXDp = subBarLeftOffsetDp + with(density) { (subBarWidthPx / 2f).toDp() }
                    val fingerXDp = subBarLeftOffsetDp + with(density) { (subDayCenterFraction * subBarWidthPx).toDp() }

                    val outerCardWidthDp = with(density) { barWidthPx.toDp() } + 32.dp
                    val minBubbleCenterX = edgeMarginDpSub + bubbleHalfWidthDp
                    val maxBubbleCenterX = (outerCardWidthDp - edgeMarginDpSub - bubbleHalfWidthDp).coerceAtLeast(minBubbleCenterX)

                    val clampedBubbleCenterX = fingerXDp.coerceIn(minBubbleCenterX, maxBubbleCenterX)
                    val subTargetXOffsetDp = clampedBubbleCenterX - subBarCenterXDp
                    val clampedSubTargetXOffsetPx = with(density) { subTargetXOffsetDp.toPx() }.roundToInt()

                    val subAnimOffsetXPx = remember { Animatable(0f) }

                    LaunchedEffect(clampedSubTargetXOffsetPx, activeHoverSource) {
                        val target = clampedSubTargetXOffsetPx.toFloat()
                        if (activeHoverSource == "SUB_$idx" && activeHoverDay != null) {
                            subAnimOffsetXPx.snapTo(target)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(52.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isAdd) NeumorphicAccent else holidayColor)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = titleText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeumorphicAccent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                Text(
                                    text = "$signSymbol${seg.daysCount} $workdayLabel (共 $segTotalDays 日)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeumorphicTextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(5.dp))

                            // 子项专属时序比例条 (支持全功能触控/滑动悬浮气泡交互)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .onGloballyPositioned { coords ->
                                            subBarWidthPx = coords.size.width.toFloat()
                                        }
                                        .pointerInput(segDayList) {
                                            awaitPointerEventScope {
                                                while (true) {
                                                    val event = awaitPointerEvent()
                                                    val change = event.changes.firstOrNull() ?: continue

                                                    if (change.pressed) {
                                                        change.consume()
                                                        val touchX = change.position.x
                                                        if (segDayList.isNotEmpty() && subBarWidthPx > 0f) {
                                                            subTouchFraction = (touchX / subBarWidthPx).coerceIn(0f, 1f)

                                                            val subIdx = (subTouchFraction * segDayList.size).toInt().coerceIn(segDayList.indices)
                                                            if (subIdx != lastHoverIndex || activeHoverSource != "SUB_$idx") {
                                                                lastHoverIndex = subIdx
                                                                activeHoverSource = "SUB_$idx"
                                                                activeHoverDay = segDayList[subIdx]
                                                                HapticUtils.performCrispClick(context)
                                                            }
                                                        }
                                                    } else {
                                                        activeHoverSource = null
                                                        activeHoverDay = null
                                                        lastHoverIndex = -1
                                                    }
                                                }
                                            }
                                        }
                                        .neumorphicInset(shape = CircleShape, elevation = 1.5.dp)
                                        .background(NeumorphicSunkenBg, shape = CircleShape)
                                        .clip(CircleShape)
                                ) {
                                    if (segBlocks.isNotEmpty()) {
                                        segBlocks.forEach { block ->
                                            val weight = (block.daysCount.toFloat() / segTotalDays).coerceAtLeast(0.01f)
                                            val blockColor = when (block.type) {
                                                TimelineBlockType.WORKDAY, TimelineBlockType.SHIFT_WORKDAY -> NeumorphicAccent
                                                TimelineBlockType.STATUTORY_HOLIDAY -> holidayColor
                                                TimelineBlockType.WEEKEND -> weekendColor
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .weight(weight)
                                                    .fillMaxHeight()
                                                    .background(blockColor)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(NeumorphicAccent)
                                        )
                                    }
                                }

                                // 触控按压点高亮框 (在子项进度条按压位置显示高亮瞄准框)
                                if (activeHoverSource == "SUB_$idx" && activeHoverDay != null && subBarWidthPx > 0f) {
                                    val indicatorWidthDp = 12.dp
                                    val indicatorWidthPx = with(density) { indicatorWidthDp.toPx() }
                                    val offsetX = ((subBarWidthPx * subDayCenterFraction) - (indicatorWidthPx / 2f)).coerceIn(0f, subBarWidthPx - indicatorWidthPx)
                                    Box(
                                        modifier = Modifier
                                            .offset(x = with(density) { offsetX.toDp() })
                                            .width(indicatorWidthDp)
                                            .fillMaxHeight()
                                            .border(1.5.dp, Color(0xFFF59E0B), shape = RoundedCornerShape(3.dp))
                                            .background(Color.White.copy(alpha = 0.35f), shape = RoundedCornerShape(3.dp))
                                    )
                                }

                                // 子轴专属指向气泡 (精准吸附在子轴正上方，随触摸位置平移)
                                if (activeHoverSource == "SUB_$idx" && activeHoverDay != null) {
                                    val bubbleOffsetPxY = with(density) { (-44).dp.roundToPx() }

                                    Popup(
                                        alignment = Alignment.TopCenter,
                                        offset = IntOffset(subAnimOffsetXPx.value.roundToInt(), bubbleOffsetPxY),
                                        properties = PopupProperties(
                                            focusable = false,
                                            dismissOnBackPress = false,
                                            dismissOnClickOutside = false,
                                            clippingEnabled = false
                                        )
                                    ) {
                                        val dayDetail = activeHoverDay!!
                                        val dateFormatted = DateCalculatorUtils.formatDateWithWeek(dayDetail.date, language)
                                        val (typeText, typeColor) = when (dayDetail.type) {
                                            TimelineBlockType.WORKDAY, TimelineBlockType.SHIFT_WORKDAY -> "$workdayLabel 💼" to NeumorphicAccent
                                            TimelineBlockType.STATUTORY_HOLIDAY -> "$holidayLabel 🌸" to holidayColor
                                            TimelineBlockType.WEEKEND -> "$weekendLabel 🏖️" to Color(0xFFD97706)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .width(270.dp)
                                                .height(38.dp)
                                                .neumorphicExtruded(shape = RoundedCornerShape(12.dp), elevation = 6.dp)
                                                .background(NeumorphicBg, shape = RoundedCornerShape(12.dp))
                                                .border(1.2.dp, NeumorphicAccent.copy(alpha = 0.35f), shape = RoundedCornerShape(12.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "📍 $dateFormatted",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NeumorphicTextPrimary,
                                                    maxLines = 1
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "• $typeText",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = typeColor,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(5.dp))

                            Text(
                                text = "${DateCalculatorUtils.formatDate(seg.startDate, language)}  ➔  ${DateCalculatorUtils.formatDate(seg.endDate, language)}$restText",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = NeumorphicTextPrimary.copy(alpha = 0.65f)
                            )
                        }
                    }

                    if (idx < validSegments.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
