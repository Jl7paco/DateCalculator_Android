package me.paco.datecalculator.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * 专为 3D 新拟物按键打造的微细清脆物理机械触感反馈工具 (Crisp Tactile Haptic Feedback)
 */
object HapticUtils {

    /**
     * 触发如真实按钮按下的清脆按压触感震动 (20ms)
     */
    fun performCrispClick(context: Context) {
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
                    // 20ms 高幅清脆按压触感震动，确保各类 Android 手机 (包括三星 One UI) 均能清晰感觉到按钮按下的手感
                    vibrator.vibrate(VibrationEffect.createOneShot(20L, 220))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(20L)
                }
            }
        } catch (_: Exception) {
            // 防御性捕获无震动硬件或权限受限情况
        }
    }
}
