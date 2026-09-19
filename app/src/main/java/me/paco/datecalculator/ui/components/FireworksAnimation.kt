package me.paco.datecalculator.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val radius: Float
)

@Composable
fun FireworksAnimation(
    trigger: Int,
    modifier: Modifier = Modifier,
    onAnimationFinished: () -> Unit = {}
) {
    if (trigger <= 0) return

    val progress = remember(trigger) { Animatable(0f) }

    val particles = remember(trigger) {
        val colors = listOf(
            Color(0xFFFF2A6D),
            Color(0xFFFFC600),
            Color(0xFF05D54B),
            Color(0xFF00E5FF),
            Color(0xFF9D00FF),
            Color(0xFFFF5E00)
        )
        List(60) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 800f + 200f
            Particle(
                x = 0f,
                y = 0f,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed,
                color = colors.random(),
                radius = Random.nextFloat() * 8f + 6f
            )
        }
    }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1200, easing = LinearEasing)
            )
            onAnimationFinished() // 重置 Trigger，防止二次进入页面时重复播放
        }
    }

    if (progress.value < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 3f)
            val t = progress.value
            val currentAlpha = (1f - t).coerceIn(0f, 1f)

            particles.forEach { p ->
                val px = center.x + p.vx * t
                val py = center.y + p.vy * t + 300f * t * t // 模拟重力下坠
                drawCircle(
                    color = p.color.copy(alpha = currentAlpha),
                    radius = p.radius * (1f - t * 0.5f),
                    center = Offset(px, py)
                )
            }
        }
    }
}
