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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

/**
 * 新拟物分段切页器 (无论选中还是未选中，每个段位均有清晰凸起 3D 按键图框)
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
                // 选中态：亮蓝色高亮 3D 按键
                Modifier
                    .neumorphicExtruded(shape = CircleShape, elevation = 5.dp)
                    .background(NeumorphicAccent, shape = CircleShape)
            } else {
                // 未选中态：清晰 3D 悬浮凸起按键 (绝不隐藏图框)
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
 * 新拟物单选框 (参照附图 3D 圆形 RadioButton：未选中凸起，选中凹陷 + 蓝色点)
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
 * 新拟物全圆角胶囊 Chip 按钮 (未选中 3D 凸起，选中亮蓝色高亮，图框 100% 显现)
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
        // 选中态：亮蓝色高亮胶囊
        Modifier
            .neumorphicExtruded(shape = CircleShape, elevation = 5.dp)
            .background(NeumorphicAccent, shape = CircleShape)
    } else {
        // 未选中态：3D 凸起按键
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
