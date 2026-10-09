package me.paco.datecalculator.ui.components

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
import me.paco.datecalculator.ui.theme.LocalDarkTheme

// 明亮浅色底色与深色背景底色
val NeumorphicBg @Composable get() = if (LocalDarkTheme.current) {
    Color(0xFF1B232A)
} else {
    Color(0xFFF0F4F8)
}

// 凹陷按下沉降底色
val NeumorphicSunkenBg @Composable get() = if (LocalDarkTheme.current) {
    Color(0xFF13191E)
} else {
    Color(0xFFDCE2E9)
}

val NeumorphicAccent @Composable get() = MaterialTheme.colorScheme.primary

val NeumorphicTextPrimary @Composable get() = if (LocalDarkTheme.current) {
    Color(0xFFF8FAFC)
} else {
    Color(0xFF1E293B)
}

/**
 * 3D 浮雕悬浮新拟物效果 (浅色模式下专门强化高对比双重透视发光与冷灰沉降阴影；深色模式保持优异体验)
 */
@Composable
fun Modifier.neumorphicExtruded(
    shape: Shape = RoundedCornerShape(22.dp),
    elevation: Dp = 6.dp
): Modifier {
    val isDark = LocalDarkTheme.current

    // 浅色模式专属：强化 100% 纯白顶边定向高光与板岩蓝灰底部 3D 悬浮立面投影
    val lightShadowColor = if (isDark) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.98f)
    val darkShadowColor = if (isDark) Color.Black.copy(alpha = 0.70f) else Color(0xFF8C9BAE).copy(alpha = 0.78f)

    return this.drawBehind {
        val shadowRadius = elevation.toPx()
        val shapeOutline = shape.createOutline(size, layoutDirection, this)

        // 1. 右下自然投影暗影 (浅色模式下加大偏移，营造强烈的 3D 浮雕立体感)
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(
                        shadowRadius * if (isDark) 1.25f else 1.45f,
                        shadowRadius * if (isDark) 0.35f else 0.75f,
                        shadowRadius * if (isDark) 0.35f else 0.75f,
                        darkShadowColor.toArgb()
                    )
                }
            }
            canvas.drawOutline(shapeOutline, paint)
        }

        // 2. 左上透视定向高光 (浅色模式下强反光，浮雕边缘清晰爆表)
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(
                        shadowRadius * if (isDark) 1.0f else 1.25f,
                        -shadowRadius * if (isDark) 0.30f else 0.70f,
                        -shadowRadius * if (isDark) 0.30f else 0.70f,
                        lightShadowColor.toArgb()
                    )
                }
            }
            canvas.drawOutline(shapeOutline, paint)
        }
    }
}

/**
 * 3D 沉降凹槽刻痕效果 (浅色模式下强化深刻痕沉降)
 */
@Composable
fun Modifier.neumorphicInset(
    shape: Shape = RoundedCornerShape(14.dp),
    elevation: Dp = 5.dp
): Modifier {
    val isDark = LocalDarkTheme.current
    val lightShadowColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.90f)
    val darkShadowColor = if (isDark) Color.Black.copy(alpha = 0.65f) else Color(0xFF818CF8).copy(alpha = 0.45f)

    return this.drawBehind {
        val shadowRadius = elevation.toPx()
        val shapeOutline = shape.createOutline(size, layoutDirection, this)

        drawIntoCanvas { canvas ->
            canvas.save()

            val path = Path().apply {
                addOutline(shapeOutline)
            }
            canvas.clipPath(path)

            // 1. 左上内侧深刻痕沉降暗影
            val darkPaint = Paint().apply {
                asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(
                        shadowRadius * 1.3f,
                        shadowRadius * 0.75f,
                        shadowRadius * 0.75f,
                        darkShadowColor.toArgb()
                    )
                }
            }
            canvas.drawPath(path, darkPaint)

            // 2. 右下内侧透视反光高光
            val lightPaint = Paint().apply {
                asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(
                        shadowRadius * 1.3f,
                        -shadowRadius * 0.75f,
                        -shadowRadius * 0.75f,
                        lightShadowColor.toArgb()
                    )
                }
            }
            canvas.drawPath(path, lightPaint)

            canvas.restore()
        }
    }
}
