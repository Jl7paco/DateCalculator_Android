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

// 凹陷按下沉降底色 (显著加深呈现 3D 沉降凹槽感)
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
 * 凸起悬浮 3D 新拟物效果 (Extruded Neumorphism)
 */
@Composable
fun Modifier.neumorphicExtruded(
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = 6.dp
): Modifier {
    val isDark = isSystemInDarkTheme()
    val lightShadowColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.White
    val darkShadowColor = if (isDark) Color.Black.copy(alpha = 0.55f) else Color(0xFFC8D1DC).copy(alpha = 0.65f)

    return this.drawBehind {
        val shadowRadius = elevation.toPx()
        val shapeOutline = shape.createOutline(size, layoutDirection, this)

        // 右下角柔和深色阴影
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(
                        shadowRadius,
                        shadowRadius * 0.6f,
                        shadowRadius * 0.6f,
                        darkShadowColor.toArgb()
                    )
                }
            }
            canvas.drawOutline(shapeOutline, paint)
        }

        // 左上角纯白高光阴影
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(
                        shadowRadius,
                        -shadowRadius * 0.6f,
                        -shadowRadius * 0.6f,
                        lightShadowColor.toArgb()
                    )
                }
            }
            canvas.drawOutline(shapeOutline, paint)
        }
    }
}

/**
 * 凹陷凹槽 3D 新拟物效果 (强烈震撼版 True Sunken / Inset Neumorphism)
 * 采用内部暗影与内侧高光结合，展现绝对清晰的 3D 凹陷沉降效果
 */
@Composable
fun Modifier.neumorphicInset(
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = 6.dp
): Modifier {
    val isDark = isSystemInDarkTheme()
    val lightShadowColor = if (isDark) Color.White.copy(alpha = 0.20f) else Color.White
    val darkShadowColor = if (isDark) Color.Black.copy(alpha = 0.85f) else Color(0xFF8896A8)

    return this.drawBehind {
        val shadowRadius = elevation.toPx()
        val shapeOutline = shape.createOutline(size, layoutDirection, this)

        drawIntoCanvas { canvas ->
            canvas.save()

            val path = Path().apply {
                addOutline(shapeOutline)
            }
            // 裁切画布，确保光影严格渲染在控件边界内部
            canvas.clipPath(path)

            // 1. 左上内侧深色强烈沉降阴影
            val darkPaint = Paint().apply {
                asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(
                        shadowRadius * 1.5f,
                        shadowRadius * 0.8f,
                        shadowRadius * 0.8f,
                        darkShadowColor.toArgb()
                    )
                }
            }
            canvas.drawPath(path, darkPaint)

            // 2. 右下内侧亮白反射光影
            val lightPaint = Paint().apply {
                asFrameworkPaint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(
                        shadowRadius * 1.5f,
                        -shadowRadius * 0.8f,
                        -shadowRadius * 0.8f,
                        lightShadowColor.toArgb()
                    )
                }
            }
            canvas.drawPath(path, lightPaint)

            canvas.restore()
        }
    }
}
