package me.paco.datecalculator.ui.components

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

/**
 * 紧凑型胶囊切页开关 (尺寸缩小 30% 以上，支持同行右侧布局)
 */
@Composable
fun NeumorphicCapsuleSwitch(
    option1Text: String,
    option2Text: String,
    isOption1Selected: Boolean,
    onOptionChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 30.dp
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
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
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
                    .neumorphicExtruded(shape = trackShape, elevation = 2.dp)
                    .background(NeumorphicAccent, shape = trackShape)
            )
            if (transitionOffset < 1f) {
                Spacer(modifier = Modifier.weight(1f - transitionOffset))
            }
        }

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
                    fontSize = 11.sp,
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
                    fontSize = 11.sp,
                    fontWeight = if (!isOption1Selected) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (!isOption1Selected) Color.White else NeumorphicTextPrimary.copy(alpha = 0.65f)
                )
            }
        }
    }
}

/**
 * 公历 / 农历 同行胶囊拨动开关 (精细缩小尺寸，可直接置于标题/日期同行)
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
        modifier = modifier.width(110.dp),
        height = 30.dp
    )
}

/**
 * 工作日 / 自然日双标签滑动胶囊拨动开关 (精细缩小尺寸)
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
        modifier = modifier.width(130.dp),
        height = 30.dp
    )
}

/**
 * 新拟物分段切页器 (支持草图胶囊滑动样式，精细缩小 30% 尺寸)
 */
@Composable
fun NeumorphicSegmentedRow(
    items: List<String>,
    selectedIndex: Int,
    onIndexSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 32.dp
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
                .padding(vertical = 1.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
                        .background(NeumorphicAccent, shape = CircleShape)
                } else {
                    Modifier
                        .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
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
                        fontSize = 11.5.sp,
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
    height: Dp = 34.dp
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
            .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
            .background(NeumorphicAccent, shape = CircleShape)
    } else {
        Modifier
            .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
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
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.5.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (selected) Color.White else NeumorphicTextPrimary
        )
    }
}

@Composable
fun NeumorphicCustomPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 180.dp,
    height: Dp = 260.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    if (expanded) {
        Popup(
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true)
        ) {
            Box(
                modifier = modifier
                    .width(width)
                    .height(height)
                    .neumorphicExtruded(shape = RoundedCornerShape(16.dp), elevation = 8.dp)
                    .background(NeumorphicBg, shape = RoundedCornerShape(16.dp))
                    .border(1.2.dp, NeumorphicAccent.copy(alpha = 0.25f), shape = RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    content()
                }
            }
        }
    }
}
