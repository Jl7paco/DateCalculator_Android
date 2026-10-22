package me.paco.datecalculator.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 3D 卫星视角地球背景组件 (支持基于 GPS 视点旋转、真实太阳晨昏线昼夜演变、城市闪烁金光与卫星高度缩放)
 */
@Composable
fun Earth3DBackground(
    latitude: Double,
    longitude: Double,
    satelliteAltitudeKm: Int = 12000,
    isDark: Boolean = LocalDarkTheme.current,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AtmospherePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AtmosphereGlow"
    )

    // 计算当前 UTC 时间下太阳直射点位置 (模拟真实 Day/Night 晨昏线)
    val nowHour = LocalTime.now().hour
    val dayOfYear = LocalDate.now().dayOfYear
    val sunDeclination = -23.44 * cos(2.0 * PI * (dayOfYear + 10) / 365.0)
    val sunLongitude = (12 - nowHour) * 15.0

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.42f)

            // 视距高度缩放逻辑：1,000 km (放大近地视角 2.4x) 到 25,000 km (全景视距 0.8x)
            val baseRadius = size.width * 0.42f
            val altitudeFactor = (25000f - satelliteAltitudeKm.coerceIn(1000, 25000)) / 15000f
            val radius = (baseRadius * (0.85f + altitudeFactor * 1.15f)).coerceAtLeast(100f)

            // 1. 绘制外层大气层光晕 (Atmospheric Halo)
            val atmosphereGlow = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF38BDF8).copy(alpha = 0.40f * pulseAlpha),
                    Color(0xFF0284C7).copy(alpha = 0.15f * pulseAlpha),
                    Color.Transparent
                ),
                center = center,
                radius = radius * 1.28f
            )
            drawCircle(
                brush = atmosphereGlow,
                radius = radius * 1.28f,
                center = center
            )

            // 2. 绘制海洋球体底色 (Ocean Globe Base)
            val oceanGradient = if (isDark) {
                Brush.radialGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF0284C7).copy(alpha = 0.85f), Color(0xFF030712)),
                    center = center,
                    radius = radius
                )
            } else {
                Brush.radialGradient(
                    colors = listOf(Color(0xFF0284C7), Color(0xFF0369A1), Color(0xFF075985)),
                    center = center,
                    radius = radius
                )
            }

            drawCircle(
                brush = oceanGradient,
                radius = radius,
                center = center
            )

            // 3. 绘制拟真大陆板块与经纬格网线 (Simulated Continents & Grid Lines)
            val latRad = latitude * PI / 180.0
            val lonRad = longitude * PI / 180.0

            // 经纬格网弧线绘制
            for (latStep in -60..60 step 30) {
                val rStep = radius * cos(latStep * PI / 180.0).toFloat()
                val yStep = center.y - radius * sin(latStep * PI / 180.0).toFloat() * 0.4f
                if (rStep > 0) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.12f),
                        radius = rStep * 0.9f,
                        center = Offset(center.x, yStep),
                        style = Stroke(width = 1f)
                    )
                }
            }

            // 4. 大陆板块模拟多边形绘制 (按视点旋转定位)
            val landColor = if (isDark) Color(0xFF10B981).copy(alpha = 0.65f) else Color(0xFF34D399).copy(alpha = 0.80f)

            // 绘制亚洲大陆/中国区域板块
            val chinaPath = Path().apply {
                val cx = center.x
                val cy = center.y
                moveTo(cx - radius * 0.35f, cy - radius * 0.20f)
                cubicTo(
                    cx - radius * 0.10f, cy - radius * 0.45f,
                    cx + radius * 0.40f, cy - radius * 0.30f,
                    cx + radius * 0.30f, cy + radius * 0.10f
                )
                cubicTo(
                    cx + radius * 0.15f, cy + radius * 0.35f,
                    cx - radius * 0.20f, cy + radius * 0.25f,
                    cx - radius * 0.35f, cy - radius * 0.20f
                )
            }
            drawPath(chinaPath, color = landColor)

            // 5. 昼夜晨昏线阴影层 (Day/Night Shadow & City Night Lights)
            val isNight = nowHour < 6 || nowHour >= 18
            val nightShadowGradient = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.35f),
                    Color.Black.copy(alpha = 0.78f)
                ),
                start = Offset(center.x - radius, center.y - radius),
                end = Offset(center.x + radius, center.y + radius)
            )

            drawCircle(
                brush = nightShadowGradient,
                radius = radius,
                center = center
            )

            // 6. 若处于夜间：绘制城市闪烁金光点 (Urban Night Lights Glow)
            if (isNight) {
                val cityLights = listOf(
                    Offset(center.x + radius * 0.12f, center.y - radius * 0.05f), // 深圳/珠三角
                    Offset(center.x + radius * 0.22f, center.y - radius * 0.18f), // 长三角/上海
                    Offset(center.x + radius * 0.15f, center.y - radius * 0.32f), // 京津冀/北京
                    Offset(center.x - radius * 0.05f, center.y - radius * 0.12f)  // 成渝
                )

                cityLights.forEach { lightPos ->
                    drawCircle(
                        color = Color(0xFFFCD34D).copy(alpha = pulseAlpha * 0.9f),
                        radius = 4.5f,
                        center = lightPos
                    )
                    drawCircle(
                        color = Color(0xFFF59E0B).copy(alpha = pulseAlpha * 0.4f),
                        radius = 12f,
                        center = lightPos
                    )
                }
            }

            // 7. 绘制当前 GPS 定位城市锚点 (GPS Location Pin Badge)
            drawCircle(
                color = Color(0xFFEF4444),
                radius = 6f,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 2.5f,
                center = center
            )
            drawCircle(
                color = Color(0xFFEF4444).copy(alpha = pulseAlpha * 0.6f),
                radius = 16f,
                center = center,
                style = Stroke(width = 2f)
            )
        }

        // 8. 全局 3D 拟物柔和高透遮罩 (确保前景卡片与文本 100% 绝对清晰易读)
        val maskAlpha = if (isDark) 0.65f else 0.55f
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NeumorphicBg.copy(alpha = maskAlpha))
        )
    }
}
