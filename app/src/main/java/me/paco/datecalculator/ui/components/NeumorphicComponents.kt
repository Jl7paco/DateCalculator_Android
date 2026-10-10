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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import me.paco.datecalculator.util.LanguageUtils

/**
 * 图标按钮专属 3D 浮雕立体渲染修饰符
 */
@Composable
fun Modifier.neumorphicButton3D(
    elevation: Dp = 3.5.dp
): Modifier {
    val isDark = LocalDarkTheme.current
    val lightShadowColor = if (isDark) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.55f)
    val darkShadowColor = if (isDark) Color.Black.copy(alpha = 0.65f) else Color(0xFF94A3B8).copy(alpha = 0.65f)

    return this.drawBehind {
        val shadowRadius = elevation.toPx()
        val shapeOutline = CircleShape.createOutline(size, layoutDirection, this)

        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(
                        shadowRadius * 1.1f,
                        shadowRadius * 0.5f,
                        shadowRadius * 0.5f,
                        darkShadowColor.toArgb()
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
                        shadowRadius * 0.9f,
                        -shadowRadius * 0.45f,
                        -shadowRadius * 0.45f,
                        lightShadowColor.toArgb()
                    )
                }
            }
            canvas.drawOutline(shapeOutline, paint)
        }
    }
}

/**
 * 具有按压 3D 弹簧弹性缩放与微调动画的新拟物图标按钮
 */
@Composable
fun NeumorphicIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = 36.dp,
    tint: Color = NeumorphicAccent
) {
    val isDark = LocalDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.82f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "IconButtonScale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isPressed) -12f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "IconButtonRotation"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            }
            .neumorphicButton3D(elevation = 3.5.dp)
            .background(NeumorphicBg, shape = CircleShape)
            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.80f), CircleShape)
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
            tint = tint,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

/**
 * 3D 新拟物圆润拨动开关 (消除左上角白色阴影溢出，颜色跟随全局设置 NeumorphicAccent)
 */
@Composable
fun NeumorphicSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalDarkTheme.current
    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "SwitchThumbOffset"
    )

    val trackShape = CircleShape

    Box(
        modifier = modifier
            .width(52.dp)
            .height(28.dp)
            .neumorphicInset(shape = trackShape, elevation = 2.dp)
            .background(if (checked) NeumorphicAccent else NeumorphicSunkenBg, shape = trackShape)
            .clip(trackShape)
            .clickable { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (thumbOffset > 0f) {
                Spacer(modifier = Modifier.weight(thumbOffset))
            }

            // 白色圆钮：采用向下微影沉降 DropShadow，彻底杜绝左上角白色发光晕染溢出 Track 边缘
            val thumbShadowColor = if (isDark) Color.Black.copy(alpha = 0.60f) else Color(0xFF64748B).copy(alpha = 0.30f)
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .drawBehind {
                        val shapeOutline = CircleShape.createOutline(size, layoutDirection, this)
                        drawIntoCanvas { canvas ->
                            val paint = Paint().apply {
                                asFrameworkPaint().apply {
                                    isAntiAlias = true
                                    color = android.graphics.Color.TRANSPARENT
                                    setShadowLayer(
                                        4.dp.toPx(),
                                        0f,
                                        1.5.dp.toPx(),
                                        thumbShadowColor.toArgb()
                                    )
                                }
                            }
                            canvas.drawOutline(shapeOutline, paint)
                        }
                    }
                    .clip(CircleShape)
                    .background(Color.White)
            )

            if (thumbOffset < 1f) {
                Spacer(modifier = Modifier.weight(1f - thumbOffset))
            }
        }
    }
}

/**
 * 紧凑型胶囊切页开关
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
                    color = if (isOption1Selected) Color.White else NeumorphicTextPrimary.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
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
                    color = if (!isOption1Selected) Color.White else NeumorphicTextPrimary.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 公历 / 农历 同行胶囊拨动开关
 */
@Composable
fun SolarLunarSwitch(
    isSolar: Boolean,
    onCalendarTypeChanged: (Boolean) -> Unit,
    language: AppLanguage = AppLanguage.SIMPLIFIED_CHINESE,
    modifier: Modifier = Modifier
) {
    val switchWidth = if (language.isChineseLocale) 110.dp else 125.dp
    NeumorphicCapsuleSwitch(
        option1Text = LanguageUtils.getString("solar", language),
        option2Text = LanguageUtils.getString("lunar", language),
        isOption1Selected = isSolar,
        onOptionChanged = onCalendarTypeChanged,
        modifier = modifier.width(switchWidth),
        height = 30.dp
    )
}

/**
 * 工作日 / 自然日双标签滑动胶囊拨动开关
 */
@Composable
fun WorkdayNaturalSwitch(
    isWorkday: Boolean,
    onWorkdayChanged: (Boolean) -> Unit,
    language: AppLanguage = AppLanguage.SIMPLIFIED_CHINESE,
    modifier: Modifier = Modifier
) {
    val switchWidth = if (language.isChineseLocale) 125.dp else 150.dp
    NeumorphicCapsuleSwitch(
        option1Text = LanguageUtils.getString("workday", language),
        option2Text = LanguageUtils.getString("natural_day", language),
        isOption1Selected = isWorkday,
        onOptionChanged = onWorkdayChanged,
        modifier = modifier.width(switchWidth),
        height = 30.dp
    )
}

/**
 * 模式三向切页器
 */
@Composable
fun NeumorphicSegmentedRow(
    items: List<String>,
    selectedIndex: Int,
    onIndexSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 36.dp
) {
    val containerShape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(NeumorphicSunkenBg, shape = containerShape)
            .clip(containerShape)
            .padding(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, title ->
                val isSelected = selectedIndex == index
                val itemShape = RoundedCornerShape(10.dp)

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.93f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "SegmentPressScale"
                )

                val itemModifier = if (isSelected) {
                    Modifier
                        .neumorphicExtruded(shape = itemShape, elevation = 3.dp)
                        .background(NeumorphicAccent, shape = itemShape)
                } else {
                    Modifier.background(Color.Transparent)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .then(itemModifier)
                        .clip(itemShape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onIndexSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
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
