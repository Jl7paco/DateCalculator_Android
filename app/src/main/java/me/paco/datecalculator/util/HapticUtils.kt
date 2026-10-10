package me.paco.datecalculator.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * 强效物理机械触感反馈工具 (强制硬件马达震动，解决部分机型彻底无震感问题)
 */
object HapticUtils {

    fun performCrispClick(context: Context, view: View? = null) {
        try {
            // 1. 尝试系统级 View Haptic
            view?.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )

            // 2. 绕过预设 Effect_Click，强制调用底层线性马达最大振幅的 OneShot
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (vibrator.hasVibrator()) {
                // 彻底抛弃 createPredefined，直接使用底层长脉冲强制触发
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // 35ms, 255 (最大振幅)
                    vibrator.vibrate(VibrationEffect.createOneShot(35L, 255))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(35L)
                }
            }
        } catch (_: Exception) {
        }
    }
}
