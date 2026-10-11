package me.paco.datecalculator.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View

/**
 * 清脆、精准、微度有感的物理微脉冲触感反馈工具 (Micro Crisp Haptic Tap)
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // 10毫秒短脉冲 + 85精细中低振幅：100% 能够明显微感触达，且彻底告别重感与低频“呜呜”轰鸣
                    vibrator.vibrate(VibrationEffect.createOneShot(10L, 85))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(10L)
                }
            }
        } catch (_: Exception) {
            // 防御性捕获
        }
    }
}
