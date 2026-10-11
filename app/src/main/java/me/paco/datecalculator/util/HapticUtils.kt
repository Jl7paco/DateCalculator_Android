package me.paco.datecalculator.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View

/**
 * 统一清脆短促的微型物理按压触感震动 (12ms Uniform Crisp Short Vibration)
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
                    // 统一 12ms 短促清脆点击触感，杜绝首次长震动
                    vibrator.vibrate(VibrationEffect.createOneShot(12L, 160))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(12L)
                }
            }
        } catch (_: Exception) {
            // 防御性捕获
        }
    }
}
