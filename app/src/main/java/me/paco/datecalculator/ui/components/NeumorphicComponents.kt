package me.paco.datecalculator.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

/**
 * 严格遵照手绘草图设计的通用双选项平滑滑动胶囊拨动开关 (Capsule Track + 内层 Highlight Pill + 双向并列 Label)
 */
@Composable
fun NeumorphicCapsuleSwitch(
    option1Text: String,
    option2Text: String,
    isOption1Selected: Boolean,
    onOptionChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 38.dp
) {
    val transitionOffset by animateFloatAsState(
        targetValue = if (isOption1Selected) 0f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "CapsuleSwitchOffset"
    )

    val trackShape = CircleShape

    Box(
        modifier = modifier
            .height(height)
            .background(NeumorphicSunkenBg, shape = trackShape)
            .clip(trackShape)
            .clickable { onOptionChanged(!isOption1Selected) }
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // 动态平滑滑动的内层悬浮高亮 Capsule 实体滑块 (对应手绘草图中的内圈 Pill)
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (transitionOffset > 0f) {
                Spacer(modifier = Modifier.weight(transitionOffset))
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .neumorphicExtruded(shape = trackShape, elevation = 3.dp)
                    .background(NeumorphicAccent, shape = trackShape)
            )
            if (transitionOffset < 1f) {
                Spacer(modifier = Modifier.weight(1f - transitionOffset))
            }
        }

        // 左右并列显示的两个 Option Label (高亮选中文字)
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onOptionChanged(true) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option1Text,
                    fontSize = 12.sp,
                    fontWeight = if (isOption1Selected) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (isOption1Selected) Color.White else NeumorphicTextPrimary.copy(alpha = 0.65f)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onOptionChanged(false) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option2Text,
                    fontSize = 12.sp,
                    fontWeight = if (!isOption1Selected) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (!isOption1Selected) Color.White else NeumorphicTextPrimary.copy(alpha = 0.65f)
                )
            }
        }
    }
}

/**
 * 公历 / 农历 同行胶囊拨动开关 (遵照草图设计)
 */
@Composable
fun SolarLunarSwitch(
    isSolar: Boolean,
    onCalendarTypeChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    NeumorphicCapsuleSwitch(
        option1Text = "公历",
        option2Text = "农历",
        isOption1Selected = isSolar,
        onOptionChanged = onCalendarTypeChanged,
        modifier = modifier
    )
}

/**
 * 工作日 / 自然日双标签滑动胶囊拨动开关 (遵照草图设计)
 */
@Composable
fun WorkdayNaturalSwitch(
    isWorkday: Boolean,
    onWorkdayChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    NeumorphicCapsuleSwitch(
        option1Text = "工作日",
        option2Text = "自然日",
        isOption1Selected = isWorkday,
        onOptionChanged = onWorkdayChanged,
        modifier = modifier
    )
}

/**
 * 新拟物分段切页器 (支持草图胶囊滑动样式)
 */
@Composable
fun NeumorphicSegmentedRow(
    items: List<String>,
    selectedIndex: Int,
    onIndexSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 42.dp
) {
    if (items.size == 2) {
        NeumorphicCapsuleSwitch(
            option1Text = items[0],
            option2Text = items[1],
            isOption1Selected = (selectedIndex == 0),
            onOptionChanged = { isOpt1 -> onIndexSelected(if (isOpt1) 0 else 1) },
            modifier = modifier,
            height = height
        )
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items.forEachIndexed { index, title ->
                val isSelected = selectedIndex == index

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.93f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "SegmentPressScale"
                )

                val segModifier = if (isSelected) {
                    Modifier
                        .neumorphicExtruded(shape = CircleShape, elevation = 5.dp)
                        .background(NeumorphicAccent, shape = CircleShape)
                } else {
                    Modifier
                        .neumorphicExtruded(shape = CircleShape, elevation = 5.dp)
                        .background(NeumorphicBg, shape = CircleShape)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .then(segModifier)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onIndexSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (isSelected) Color.White else NeumorphicTextPrimary
                    )
                }
            }
        }
    }
}

/**
 * 新拟物单选框
 */
@Composable
fun NeumorphicRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val boxModifier = if (selected) {
        Modifier
            .neumorphicInset(shape = CircleShape, elevation = 4.dp)
            .background(NeumorphicSunkenBg, shape = CircleShape)
    } else {
        Modifier
            .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
            .background(NeumorphicBg, shape = CircleShape)
    }

    Box(
        modifier = modifier
            .size(24.dp)
            .then(boxModifier)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(NeumorphicAccent)
            )
        }
    }
}

/**
 * 新拟物全圆角胶囊 Chip 按钮
 */
@Composable
fun NeumorphicChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 40.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "ChipPressScale"
    )

    val chipModifier = if (selected) {
        Modifier
            .neumorphicExtruded(shape = CircleShape, elevation = 5.dp)
            .background(NeumorphicAccent, shape = CircleShape)
    } else {
        Modifier
            .neumorphicExtruded(shape = CircleShape, elevation = 5.dp)
            .background(NeumorphicBg, shape = CircleShape)
    }

    Box(
        modifier = modifier
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(chipModifier)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else NeumorphicTextPrimary,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

/**
 * 专为浅蓝卡片背景设计的新拟物复制胶囊按键 (柔和自然 3D 光影，附带 3D 弹簧触控感)
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

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val btnScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "NeumorphicCopyBtnScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .graphicsLayer {
                scaleX = btnScale
                scaleY = btnScale
            }
            .drawBehind {
                val shadowRadius = 4.dp.toPx()
                val shapeOutline = CircleShape.createOutline(size, layoutDirection, this)

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
            .border(1.dp, NeumorphicAccent.copy(alpha = 0.15f), shape = CircleShape)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
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

/**
 * 自定义纯物理 Popup 下拉菜单 (支持自定义宽度 width 与高度 height，绝对零外框阴影，100% 呈现 18.dp 平滑 R 角与新拟物光影)
 */
@Composable
fun NeumorphicCustomPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 110.dp,
    height: Dp = 260.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    if (expanded) {
        Popup(
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = modifier
                    .width(width)
                    .height(height)
                    .padding(10.dp)
                    .neumorphicExtruded(shape = RoundedCornerShape(18.dp), elevation = 8.dp)
                    .background(NeumorphicBg, shape = RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 6.dp, horizontal = 4.dp)
                ) {
                    content()
                }
            }
        }
    }
}
