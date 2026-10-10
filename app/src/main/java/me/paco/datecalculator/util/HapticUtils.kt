package me.paco.datecalculator.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * 专为 3D 新拟物按键打造的强效双重物理机械触感反馈工具 (兼容三星 One UI / 全系 Android 机型)
 */
object HapticUtils {

    /**
     * 触发强效、清脆按压触感震动 (兼容三星等强行拦截震动的系统)
     */
    fun performCrispClick(context: Context, view: View? = null) {
        try {
            // 1. 优先触发系统级的清脆 View Haptic (三星 One UI 100% 响应)
            view?.performHapticFeedback(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    HapticFeedbackConstants.CONFIRM
                } else {
                    HapticFeedbackConstants.KEYBOARD_TAP
                },
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )

            // 2. 强效硬件级 Vibrator 35ms 满幅震动补强
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(35L, 255))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(35L)
                }
            }
        } catch (_: Exception) {
            // 防御性捕获
        }
    }
}
