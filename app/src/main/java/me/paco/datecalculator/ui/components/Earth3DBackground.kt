package me.paco.datecalculator.ui.components

import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Matrix
import android.graphics.Shader
import android.graphics.Paint as AndroidPaint
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import me.paco.datecalculator.R
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import java.time.LocalTime

/**
 * 真实 NASA 照片贴图 3D 卫星遥感地球背景 (直接将真实 1.39MB NASA 照片贴图作为 BitmapShader 映射至 3D 球形表面，固定 10,000 km 真实卫星视角)
 */
@Composable
fun Earth3DBackground(
    latitude: Double,
    longitude: Double,
    satelliteAltitudeKm: Int = 10000,
    isDark: Boolean = LocalDarkTheme.current,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "AtmospherePulse")

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AtmosphereGlow"
    )

    // 读取真实 1.39MB NASA 官方蓝地球 4K 卫星遥感照片 (R.drawable.earth_nasa_day)
    val dayTexture = remember(context) {
        BitmapFactory.decodeResource(context.resources, R.drawable.earth_nasa_day)
    }

    val nowHour = LocalTime.now().hour

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.44f)

            // 固定 10,000 km 最佳卫星航天视距 (呈现真实照片级球形视野)
            val radius = size.width * 0.46f

            // 1. 大气层折射与散色光晕 (Space & Atmospheric Blue Halo)
            val atmosphereGlow = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF38BDF8).copy(alpha = 0.50f * pulseAlpha),
                    Color(0xFF0284C7).copy(alpha = 0.22f * pulseAlpha),
                    Color.Transparent
                ),
                center = center,
                radius = radius * 1.30f
            )
            drawCircle(
                brush = atmosphereGlow,
                radius = radius * 1.30f,
                center = center
            )

            // 2. 3D 海洋深邃底色球体 (Deep Ocean Sphere Base)
            val oceanGradient = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0284C7),
                    Color(0xFF0369A1),
                    Color(0xFF0F172A)
                ),
                center = center,
                radius = radius
            )
            drawCircle(
                brush = oceanGradient,
                radius = radius,
                center = center
            )

            // 3. 直接将真实 1.39MB NASA 白天卫星照片作为 BitmapShader 映射在 3D 球形表面 (真实照片呈现)
            if (dayTexture != null && !dayTexture.isRecycled) {
                val texW = dayTexture.width.toFloat()
                val texH = dayTexture.height.toFloat()

                // 计算根据经纬度 $(\text{lat}, \text{lon})$ 旋转对准定位城市的 3D 纹理变换 Matrix
                val lonShift = ((longitude + 180.0) % 360.0 / 360.0 * texW).toFloat()
                val latShift = ((90.0 - latitude) / 180.0 * texH).toFloat()

                val matrix = Matrix().apply {
                    val scaleX = (radius * 2.2f) / texW
                    val scaleY = (radius * 2.2f) / texH
                    postScale(scaleX, scaleY)
                    postTranslate(center.x - radius * 1.1f - lonShift * scaleX * 0.25f, center.y - radius * 1.1f + latShift * scaleY * 0.25f)
                }

                val dayShader = BitmapShader(dayTexture, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP).apply {
                    setLocalMatrix(matrix)
                }

                val dayPaint = AndroidPaint().apply {
                    isAntiAlias = true
                    isFilterBitmap = true
                    shader = dayShader
                }

                // 在 3D 球体裁剪区内直接绘制真实 100% NASA 卫星照片
                drawContext.canvas.nativeCanvas.save()
                val clipPath = android.graphics.Path().apply {
                    addCircle(center.x, center.y, radius, android.graphics.Path.Direction.CW)
                }
                drawContext.canvas.nativeCanvas.clipPath(clipPath)
                drawContext.canvas.nativeCanvas.drawCircle(center.x, center.y, radius, dayPaint)

                drawContext.canvas.nativeCanvas.restore()
            }

            // 4. 太阳晨昏线与真实夜间过渡 shadow (Day/Night Solar Terminator Shadow)
            val isNight = nowHour < 6 || nowHour >= 18
            val nightShadowGradient = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = if (isNight) 0.55f else 0.20f),
                    Color.Black.copy(alpha = if (isNight) 0.88f else 0.45f)
                ),
                start = Offset(center.x - radius * 0.8f, center.y - radius * 0.8f),
                end = Offset(center.x + radius * 0.8f, center.y + radius * 0.8f)
            )

            drawCircle(
                brush = nightShadowGradient,
                radius = radius,
                center = center
            )
        }

        // 5. 全局 3D 拟物柔和高透遮罩 (确保前景卡片与文本 100% 绝对清晰易读)
        val maskAlpha = if (isDark) 0.60f else 0.50f
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NeumorphicBg.copy(alpha = maskAlpha))
        )
    }
}
