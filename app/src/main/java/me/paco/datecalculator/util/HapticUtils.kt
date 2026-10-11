package me.paco.datecalculator.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View

/**
 * 极其轻盈、清脆、无低频余震的微型触感反馈工具 (Micro Crisp Haptic Tick)
 */
object HapticUtils {

    fun performCrispClick(context: Context, view: View? = null) {
        if (!PreferenceUtils.isHapticFeedbackEnabled(context)) return

        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // API 30+: 使用硬件原生的 Composition PRIMITIVE_TICK，0.35f 超轻按压点触，彻底告别低频轰鸣
                    val composition = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.35f)
                    vibrator.vibrate(composition.compose())
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // API 29: 使用原生 EFFECT_TICK（轻触）
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // API 26+: 超轻 5ms 脉冲 + 35 低振幅
                    vibrator.vibrate(VibrationEffect.createOneShot(5L, 35))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(5L)
                }
            }
        } catch (_: Exception) {
            // 防御性捕获
        }
    }
}
