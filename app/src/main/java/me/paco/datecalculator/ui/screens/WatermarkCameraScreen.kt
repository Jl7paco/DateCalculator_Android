package me.paco.datecalculator.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.ui.components.WatermarkBadgeOverlay
import me.paco.datecalculator.ui.components.WatermarkBitmapDrawer
import me.paco.datecalculator.util.HapticUtils
import me.paco.datecalculator.util.LocationUtils
import me.paco.datecalculator.util.RealMapTileLoader
import me.paco.datecalculator.util.WeatherUtils
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.Executors

/**
 * 100% 1:1 还原用户参考图 1 与视频的水印相机全屏拍摄 Screen
 */
@Composable
fun WatermarkCameraScreen(
    cityName: String,
    landmarkName: String,
    lang: AppLanguage,
    onPhotoCaptured: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashEnabled by remember { mutableStateOf(false) }
    var selectedZoom by remember { mutableFloatStateOf(1.0f) }
    var isCapturing by remember { mutableStateOf(false) }

    var mapTileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val nowTimeStr = remember {
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 a h:mm", Locale.SIMPLIFIED_CHINESE))
    }

    // 提前在后台准备 Map Tile
    LaunchedEffect(cityName) {
        val coords = WeatherUtils.getCityCoordinates(cityName)
        val bmp = RealMapTileLoader.loadRealMapTile(context, coords.first, coords.second)
        if (bmp != null) {
            mapTileBitmap = bmp
        }
    }

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(lensFacing, flashEnabled) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                imageCapture.flashMode = if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("WatermarkCamera", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 1. 全屏 CameraX Preview Viewfinder
        AndroidView(
            factory = {
                previewView.apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. 需求图 1: 顶部右上角 1:1 Floating Watermark Badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, end = 16.dp, start = 16.dp)
        ) {
            WatermarkBadgeOverlay(
                cityName = cityName,
                landmarkName = landmarkName,
                dateTimeStr = nowTimeStr,
                mapTileBitmap = mapTileBitmap,
                modifier = Modifier.align(Alignment.TopEnd)
            )

            // 左侧退出 X 按钮
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.50f))
                    .clickable { onDismiss() }
                    .align(Alignment.TopStart),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 3. 图 1 视角取景中心黄色对焦方框
        Box(
            modifier = Modifier
                .size(72.dp)
                .border(1.dp, Color(0xFFFACC15).copy(alpha = 0.70f), RoundedCornerShape(4.dp))
                .align(Alignment.Center)
        )

        // 4. 需求图 1 底部按键排版区
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(bottom = 32.dp, top = 16.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 焦段与 +标志 行
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf(0.5f, 1.0f, 3.0f).forEach { zoom ->
                    val isSelected = (selectedZoom == zoom)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFFFACC15) else Color.DarkGray.copy(alpha = 0.6f))
                            .clickable { selectedZoom = zoom },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (zoom == 0.5f) "0.5" else if (zoom == 1.0f) "1.0" else "3.0",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // + 标志 黄色气泡按钮
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(Color(0xFFFACC15))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+ 标志", fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 模式 Pills [ 照片 | 视频 ]
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.DarkGray.copy(alpha = 0.6f))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text("照片", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text("视频", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 底部核心快门控制行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flash Toggle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray.copy(alpha = 0.6f))
                        .clickable { flashEnabled = !flashEnabled },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flash",
                        tint = if (flashEnabled) Color(0xFFFACC15) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 核心大粉红/白色快门按钮 (Shutter)
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val shutterScale by animateFloatAsState(
                    targetValue = if (isPressed || isCapturing) 0.88f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "ShutterScale"
                )

                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .graphicsLayer {
                            scaleX = shutterScale
                            scaleY = shutterScale
                        }
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .background(Color(0xFFFCE7F3)) // 软粉白大按钮
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            enabled = !isCapturing
                        ) {
                            HapticUtils.performCrispClick(context, view)
                            isCapturing = true

                            // 拍照并压入 1:1 Apple 地图风格防伪水印 Badge
                            imageCapture.takePicture(
                                cameraExecutor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        try {
                                            val buffer = image.planes[0].buffer
                                            val bytes = ByteArray(buffer.capacity())
                                            buffer.get(bytes)
                                            val srcBmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                            image.close()

                                            if (srcBmp != null) {
                                                // 需求 2: 绘制 1:1 右下角 Apple 地图风格 Badge 水印
                                                val watermarkedBmp = WatermarkBitmapDrawer.drawWatermarkBadgeOnBitmap(
                                                    srcBitmap = srcBmp,
                                                    cityName = cityName,
                                                    landmarkName = landmarkName,
                                                    dateTimeStr = nowTimeStr,
                                                    mapTileBitmap = mapTileBitmap
                                                )

                                                val dir = File(context.filesDir, "checkin_photos").apply { if (!exists()) mkdirs() }
                                                val photoFile = File(dir, "photo_${System.currentTimeMillis()}.jpg")
                                                FileOutputStream(photoFile).use { out ->
                                                    watermarkedBmp.compress(Bitmap.CompressFormat.JPEG, 92, out)
                                                }

                                                ContextCompat.getMainExecutor(context).execute {
                                                    isCapturing = false
                                                    onPhotoCaptured(photoFile.absolutePath)
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.e("WatermarkCamera", "Error processing photo", e)
                                            ContextCompat.getMainExecutor(context).execute { isCapturing = false }
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        Log.e("WatermarkCamera", "Photo capture failed", exception)
                                        ContextCompat.getMainExecutor(context).execute { isCapturing = false }
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                    }
                }

                // 🔄 Flip Camera Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray.copy(alpha = 0.6f))
                        .clickable {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Flip Camera",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}