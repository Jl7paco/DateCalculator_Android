package me.paco.datecalculator.ui.components

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3D 真实球面经纬度投影卫星视角地球背景 (包含大陆板块球面投影、经纬网、太阳晨昏线与万家灯火)
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

    // 当前时间推算太阳直射经度 (模拟真实太阳移动与晨昏线)
    val nowHour = LocalTime.now().hour
    val sunLonDeg = (12 - nowHour) * 15.0

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.42f)

            // 视距高度缩放：1,000 km (放大近地 2.4x) ➔ 25,000 km (太空全景 0.8x)
            val baseRadius = size.width * 0.40f
            val altitudeFactor = (25000f - satelliteAltitudeKm.coerceIn(1000, 25000)) / 15000f
            val radius = (baseRadius * (0.85f + altitudeFactor * 1.15f)).coerceAtLeast(100f)

            // 1. 大气层折射与散色光晕 (Atmospheric Halo)
            val atmosphereGlow = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF38BDF8).copy(alpha = 0.45f * pulseAlpha),
                    Color(0xFF0284C7).copy(alpha = 0.18f * pulseAlpha),
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

            // 2. 3D 海洋深邃球体底色 (3D Ocean Globe Base)
            val oceanGradient = if (isDark) {
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1E293B), Color(0xFF0284C7).copy(alpha = 0.90f), Color(0xFF0F172A)),
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

            // 3. 3D 球面三维投影变换函数 (3D Spherical Camera Projection)
            val centerLatRad = latitude * PI / 180.0
            val centerLonRad = longitude * PI / 180.0

            fun project3D(latDeg: Double, lonDeg: Double): Triple<Float, Float, Boolean> {
                val phi = latDeg * PI / 180.0
                val deltaLambda = (lonDeg - longitude) * PI / 180.0

                val x = cos(phi) * sin(deltaLambda)
                val y = cos(centerLatRad) * sin(phi) - sin(centerLatRad) * cos(phi) * cos(deltaLambda)
                val z = sin(centerLatRad) * sin(phi) + cos(centerLatRad) * cos(phi) * cos(deltaLambda)

                val px = (center.x + radius * x).toFloat()
                val py = (center.y - radius * y).toFloat()
                val isVisible = z > 0.0 // z > 0 表示在球体正面朝向镜头区域

                return Triple(px, py, isVisible)
            }

            // 4. 绘制 3D 经纬度线 (3D Meridian & Parallel Lines)
            for (latStep in -60..60 step 30) {
                val path = Path()
                var firstPoint = true
                for (lonStep in -180..180 step 10) {
                    val (px, py, vis) = project3D(latStep.toDouble(), lonStep.toDouble())
                    if (vis) {
                        if (firstPoint) {
                            path.moveTo(px, py)
                            firstPoint = false
                        } else {
                            path.lineTo(px, py)
                        }
                    } else {
                        firstPoint = true
                    }
                }
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.12f),
                    style = Stroke(width = 1f)
                )
            }

            // 5. 真实 3D 大陆板块数据 (Eurasia, Africa, Australia, Americas)
            val eurasiaPolygon = listOf(
                Pair(65.0, 170.0), Pair(60.0, 100.0), Pair(55.0, 37.0), Pair(50.0, 14.0),
                Pair(36.0, -5.0), Pair(30.0, 31.0), Pair(28.0, 77.0), Pair(10.0, 106.0),
                Pair(1.0, 104.0), Pair(22.0, 114.0), Pair(31.0, 121.0), Pair(40.0, 116.0),
                Pair(35.0, 140.0)
            )

            val africaPolygon = listOf(
                Pair(36.0, -5.0), Pair(30.0, 31.0), Pair(12.0, 43.0), Pair(-34.0, 18.0),
                Pair(6.0, 12.0), Pair(14.0, -17.0)
            )

            val australiaPolygon = listOf(
                Pair(-12.0, 130.0), Pair(-25.0, 153.0), Pair(-38.0, 145.0), Pair(-32.0, 115.0), Pair(-20.0, 118.0)
            )

            val americasPolygon = listOf(
                Pair(60.0, -140.0), Pair(45.0, -124.0), Pair(32.0, -117.0), Pair(25.0, -80.0),
                Pair(45.0, -63.0), Pair(10.0, -75.0), Pair(-15.0, -40.0), Pair(-55.0, -68.0), Pair(-10.0, -78.0)
            )

            val landColor = if (isDark) Color(0xFF10B981).copy(alpha = 0.65f) else Color(0xFF34D399).copy(alpha = 0.85f)

            listOf(eurasiaPolygon, africaPolygon, australiaPolygon, americasPolygon).forEach { poly ->
                val landPath = Path()
                var hasDrawn = false
                poly.forEachIndexed { idx, (pLat, pLon) ->
                    val (px, py, vis) = project3D(pLat, pLon)
                    if (vis) {
                        if (!hasDrawn) {
                            landPath.moveTo(px, py)
                            hasDrawn = true
                        } else {
                            landPath.lineTo(px, py)
                        }
                    }
                }
                if (hasDrawn) {
                    landPath.close()
                    drawPath(landPath, color = landColor)
                }
            }

            // 6. 太阳晨昏线与夜间暗影 (Day/Night Solar Terminator)
            val nightShadowGradient = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.35f),
                    Color.Black.copy(alpha = 0.82f)
                ),
                start = Offset(center.x - radius * 0.8f, center.y - radius * 0.8f),
                end = Offset(center.x + radius * 0.8f, center.y + radius * 0.8f)
            )

            drawCircle(
                brush = nightShadowGradient,
                radius = radius,
                center = center
            )

            // 7. 城市万家灯火金光 (Urban Night Lights)
            val majorCities = listOf(
                Pair(22.5, 114.0), // 深圳 / 珠三角
                Pair(31.2, 121.5), // 上海 / 长三角
                Pair(39.9, 116.4), // 北京 / 京津冀
                Pair(35.7, 139.7), // 东京
                Pair(37.5, 127.0), // 首尔
                Pair(1.3, 103.8),  // 新加坡
                Pair(51.5, -0.1),  // 伦敦
                Pair(40.7, -74.0)  // 纽约
            )

            majorCities.forEach { (cLat, cLon) ->
                val (px, py, vis) = project3D(cLat, cLon)
                if (vis) {
                    // 判断该城市是否处于夜间侧 (远离太阳直射角度)
                    val deltaSunLon = (cLon - sunLonDeg + 540) % 360 - 180
                    val isNightSide = deltaSunLon < -40 || deltaSunLon > 40

                    if (isNightSide) {
                        drawCircle(
                            color = Color(0xFFFCD34D).copy(alpha = pulseAlpha * 0.95f),
                            radius = 3.5f,
                            center = Offset(px, py)
                        )
                        drawCircle(
                            color = Color(0xFFF59E0B).copy(alpha = pulseAlpha * 0.45f),
                            radius = 9f,
                            center = Offset(px, py)
                        )
                    }
                }
            }

            // 8. 绘制当前 GPS 定位城市精确定位锚点 (GPS Location Pin Badge)
            val (userPx, userPy, userVis) = project3D(latitude, longitude)
            if (userVis) {
                drawCircle(
                    color = Color(0xFFEF4444),
                    radius = 5.5f,
                    center = Offset(userPx, userPy)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5f,
                    center = Offset(userPx, userPy)
                )
                drawCircle(
                    color = Color(0xFFEF4444).copy(alpha = pulseAlpha * 0.7f),
                    radius = 15f,
                    center = Offset(userPx, userPy),
                    style = Stroke(width = 2f)
                )
            }
        }

        // 9. 全局 3D 拟物柔和高透遮罩 (确保前景卡片与文本 100% 绝对清晰易读)
        val maskAlpha = if (isDark) 0.60f else 0.50f
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NeumorphicBg.copy(alpha = maskAlpha))
        )
    }
}
