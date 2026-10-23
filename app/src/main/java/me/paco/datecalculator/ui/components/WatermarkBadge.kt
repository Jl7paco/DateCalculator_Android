package me.paco.datecalculator.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.paco.datecalculator.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 需求 1: 预览界面与拍出成品的水印样式、布局、色彩与文字排版 100% 绝对一致 (Apple 地图风格 Badge)
 */
@Composable
fun WatermarkBadgeOverlay(
    cityName: String,
    landmarkName: String,
    dateTimeStr: String,
    mapTileBitmap: Bitmap? = null,
    scaleFactor: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    val badgeShape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scaleFactor.coerceIn(0.5f, 1.0f)
                scaleY = scaleFactor.coerceIn(0.5f, 1.0f)
            }
            .clip(badgeShape)
            .background(Color(0xD81E293B))
            .border(1.dp, Color.White.copy(alpha = 0.20f), badgeShape)
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            // 1. 左侧地图缩略图方块
            val mapBoxShape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(mapBoxShape)
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                if (mapTileBitmap != null) {
                    Image(
                        bitmap = mapTileBitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.bg_open_street_map),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(24.dp)
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "地图",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // 2. 右侧地理名称与时刻排版
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.width(175.dp)
            ) {
                Text(
                    text = cityName.ifBlank { "深圳市, 中国" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = landmarkName.ifBlank { "车公庙" },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.92f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                val formattedTime = if (dateTimeStr.isNotBlank()) {
                    dateTimeStr
                } else {
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 a h:mm", Locale.SIMPLIFIED_CHINESE))
                }

                Text(
                    text = "🕐 $formattedTime",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 需求 1: 在照片 Bitmap 上绘制与预览界面 100% 视觉像素级一致的水印 Badge
 */
object WatermarkBitmapDrawer {

    fun drawWatermarkBadgeOnBitmap(
        srcBitmap: Bitmap,
        cityName: String,
        landmarkName: String,
        dateTimeStr: String,
        mapTileBitmap: Bitmap? = null,
        userScaleFactor: Float = 1.0f
    ): Bitmap {
        val width = srcBitmap.width
        val height = srcBitmap.height
        val mutableBitmap = srcBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        // 需求 3: 水印默认大小设为原面积的 80% (宽度约为画面宽度的 54%)
        val badgeW = (width * 0.54f * userScaleFactor.coerceIn(0.5f, 1.0f)).coerceAtMost(width * 0.82f)
        val badgeH = badgeW * 0.33f
        val margin = (width * 0.03f).coerceAtLeast(24f)

        val right = width - margin
        val left = right - badgeW
        val bottom = height - margin
        val top = bottom - badgeH

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(216, 30, 41, 59)
        }

        val rect = RectF(left, top, right, bottom)
        val cornerRadius = badgeH * 0.18f
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)

        val mapMargin = badgeH * 0.10f
        val mapSize = badgeH - (mapMargin * 2)
        val mapRect = RectF(left + mapMargin, top + mapMargin, left + mapMargin + mapSize, top + mapMargin + mapSize)

        if (mapTileBitmap != null) {
            canvas.drawBitmap(mapTileBitmap, null, mapRect, null)
        } else {
            val mapBgPaint = Paint().apply { color = android.graphics.Color.parseColor("#0F172A") }
            canvas.drawRoundRect(mapRect, cornerRadius * 0.6f, cornerRadius * 0.6f, mapBgPaint)
        }

        // 需求 2: 照片水印文案字号与水印 Badge 总体尺寸 (badgeH) 按 1:1 比例等比动态缩放
        val textX = left + mapMargin + mapSize + (badgeH * 0.08f)
        var textY = top + mapMargin + (badgeH * 0.22f)

        val cityPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = badgeH * 0.18f
            isFakeBoldText = true
        }
        val cleanCity = if (cityName.isBlank()) "深圳市, 中国" else cityName
        canvas.drawText(cleanCity, textX, textY, cityPaint)

        textY += (badgeH * 0.22f)
        val landmarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = badgeH * 0.16f
            isFakeBoldText = true
        }
        val cleanLandmark = if (landmarkName.isBlank()) "车公庙" else landmarkName
        canvas.drawText(cleanLandmark, textX, textY, landmarkPaint)

        textY += (badgeH * 0.22f)
        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#CBD5E1")
            textSize = badgeH * 0.14f
            isFakeBoldText = false
        }
        val cleanTime = if (dateTimeStr.isBlank()) LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 a h:mm", Locale.SIMPLIFIED_CHINESE)) else dateTimeStr
        canvas.drawText("🕐 $cleanTime", textX, textY, timePaint)

        return mutableBitmap
    }
}