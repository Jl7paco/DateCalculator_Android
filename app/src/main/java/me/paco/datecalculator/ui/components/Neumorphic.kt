package me.paco.datecalculator.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// 明亮冰雪白底色 (还原附图)，Accent 动态跟进整机系统主题色
val NeumorphicBg @Composable get() = if (isSystemInDarkTheme()) {
    MaterialTheme.colorScheme.surface
} else {
    Color(0xFFF2F5FA)
}

// 凹陷按下沉降底色
val NeumorphicSunkenBg @Composable get() = if (isSystemInDarkTheme()) {
    MaterialTheme.colorScheme.surfaceVariant
} else {
    Color(0xFFDDE3EC)
}

val NeumorphicAccent @Composable get() = MaterialTheme.colorScheme.primary

val NeumorphicTextPrimary @Composable get() = if (isSystemInDarkTheme()) {
    MaterialTheme.colorScheme.onSurface
} else {
    Color(0xFF2D3748)
}

/**
 * 凸起悬浮 3D 新拟物效果 (四角平滑弥散高光与暗影，彻底消除右上角光影断层)
 */
@Composable
fun Modifier.neumorphicExtruded(
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = 4.dp
): Modifier {
    val isDark = isSystemInDarkTheme()
    val lightShadowColor = if (isDark) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.55f)
    val darkShadowColor = if (isDark) Color.Black.copy(alpha = 0.50f) else Color(0xFFB0BDCC).copy(alpha = 0.45f)

    return this.drawBehind {
        val shadowRadius = elevation.toPx()
        val shapeOutline = shape.createOutline(size, layoutDirection, this)

        // 1. 柔和全向底边自然暗影 (增加平滑弥散半径，消除右上角断层)
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(
                        shadowRadius * 1.25f,
                        shadowRadius * 0.35f,
                        shadowRadius * 0.35f,
                        darkShadowColor.toArgb()
                    )
                }
            }
            canvas.drawOutline(shapeOutline, paint)
        }

        // 2. 柔和全向顶边自然高光 (4 角全覆盖，无干瘪挂边)
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(
                        shadowRadius * 1.0f,
                        -shadowRadius * 0.30f,
                        -shadowRadius * 0.30f,
                        lightShadowColor.toArgb()
                    )
                }
            }
            canvas.drawOutline(shapeOutline, paint)
        }
    }
}

/**
 * 凹陷凹槽 3D 新拟物效果 (去除硬描边，完全贴合原素材图效)
 */
@Composable
fun Modifier.neumorphicInset(
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = 4.dp
): Modifier {
    val isDark = isSystemInDarkTheme()
    val lightShadowColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.50f)
    val darkShadowColor = if (isDark) Color.Black.copy(alpha = 0.65f) else Color(0xFFA2B0C2).copy(alpha = 0.55f)

    return this.drawBehind {
        val shadowRadius = elevation.toPx()
        val shapeOutline = shape.createOutline(size, layoutDirection, this)

        drawIntoCanvas { canvas ->
            canvas.save()

            val path = Path().apply {
                addOutline(shapeOutline)
            }
            canvas.clipPath(path)

            // 1. 左上内侧自然沉降暗影
            val darkPaint = Paint().apply {
                asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(
                        shadowRadius * 1.1f,
                        shadowRadius * 0.6f,
                        shadowRadius * 0.6f,
                        darkShadowColor.toArgb()
                    )
                }
            }
            canvas.drawPath(path, darkPaint)

            // 2. 右下内侧柔和反射高光
            val lightPaint = Paint().apply {
                asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(
                        shadowRadius * 1.1f,
                        -shadowRadius * 0.6f,
                        -shadowRadius * 0.6f,
                        lightShadowColor.toArgb()
                    )
                }
            }
            canvas.drawPath(path, lightPaint)

            canvas.restore()
        }
    }
}
