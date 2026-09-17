package me.paco.datecalculator.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 按照要求完全取消所有阴影 (纯平极简风)
 */
fun Modifier.paperShadow(
    elevation: Dp = 0.dp,
    shape: Shape = RoundedCornerShape(12.dp)
): Modifier = this
