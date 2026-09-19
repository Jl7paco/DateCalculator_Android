package me.paco.datecalculator.ui.components

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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
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
 * 新拟物分段切页器
 */
@Composable
fun NeumorphicSegmentedRow(
    items: List<String>,
    selectedIndex: Int,
    onIndexSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 50.dp
) {
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
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (isSelected) Color.White else NeumorphicTextPrimary
                )
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
