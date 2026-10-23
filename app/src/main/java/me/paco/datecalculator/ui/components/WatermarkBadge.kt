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
 * 100% 1:1 还原用户参考图 1 & 图 2 中的 iOS / Apple 地图风格高端水印 Badge 浮层
 */
@Composable
fun WatermarkBadgeOverlay(
    cityName: String,
    landmarkName: String,
    dateTimeStr: String,
    mapTileBitmap: Bitmap? = null,
    modifier: Modifier = Modifier
) {
    val badgeShape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .clip(badgeShape)
            .background(Color(0xD81E293B)) // 深色高透软玻璃质感
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
                    .size(68.dp)
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

                // 中心黄色大图钉 📍
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(24.dp)
                )

                // 左下角小标签: "地图"
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
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
                modifier = Modifier.width(180.dp)
            ) {
                Text(
                    text = cityName.ifBlank { "深圳市, 中国" },
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = landmarkName.ifBlank { "东海国际中心" },
                    fontSize = 12.5.sp,
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
                    fontSize = 10.5.sp,
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
 * 绘制带有 1:1 Apple 地图风格 Badge 水印的照片 Bitmap
 */
object WatermarkBitmapDrawer {

    fun drawWatermarkBadgeOnBitmap(
        srcBitmap: Bitmap,
        cityName: String,
        landmarkName: String,
        dateTimeStr: String,
        mapTileBitmap: Bitmap? = null
    ): Bitmap {
        val width = srcBitmap.width
        val height = srcBitmap.height
        val mutableBitmap = srcBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        val scale = (width / 1080f).coerceIn(0.8f, 2.5f)
        val badgeW = (320f * scale).coerceAtMost(width * 0.75f)
        val badgeH = (110f * scale).coerceAtMost(height * 0.25f)
        val margin = 32f * scale

        val right = width - margin
        val left = right - badgeW
        val bottom = height - margin
        val top = bottom - badgeH

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(215, 30, 41, 59)
        }

        val rect = RectF(left, top, right, bottom)
        canvas.drawRoundRect(rect, 24f * scale, 24f * scale, bgPaint)

        val mapMargin = 12f * scale
        val mapSize = badgeH - (mapMargin * 2)
        val mapRect = RectF(left + mapMargin, top + mapMargin, left + mapMargin + mapSize, top + mapMargin + mapSize)

        if (mapTileBitmap != null) {
            canvas.drawBitmap(mapTileBitmap, null, mapRect, null)
        } else {
            val mapBgPaint = Paint().apply { color = android.graphics.Color.parseColor("#0F172A") }
            canvas.drawRoundRect(mapRect, 14f * scale, 14f * scale, mapBgPaint)
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 15f * scale
            isFakeBoldText = true
        }

        val textX = left + mapMargin + mapSize + (12f * scale)
        var textY = top + mapMargin + (22f * scale)

        val cleanCity = if (cityName.isBlank()) "深圳市, 中国" else cityName
        canvas.drawText(cleanCity, textX, textY, textPaint)

        textY += (20f * scale)
        textPaint.textSize = 13.5f * scale
        val cleanLandmark = if (landmarkName.isBlank()) "东海国际中心" else landmarkName
        canvas.drawText(cleanLandmark, textX, textY, textPaint)

        textY += (20f * scale)
        textPaint.textSize = 11.5f * scale
        textPaint.color = android.graphics.Color.parseColor("#CBD5E1")
        val cleanTime = if (dateTimeStr.isBlank()) LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 a h:mm", Locale.SIMPLIFIED_CHINESE)) else dateTimeStr
        canvas.drawText("🕐 $cleanTime", textX, textY, textPaint)

        return mutableBitmap
    }
}