package me.paco.datecalculator.util

import android.content.Context
import android.content.Intent

object ShareUtils {
    fun shareText(context: Context, text: String, title: String = "分享计算结果") {
        try {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, title)
            context.startActivity(shareIntent)
        } catch (_: Exception) {
            // Fallback gracefully
        }
    }
}
