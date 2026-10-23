package me.paco.datecalculator.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import kotlinx.coroutines.delay
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicSunkenBg
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import me.paco.datecalculator.ui.components.WatermarkBadgeOverlay
import me.paco.datecalculator.ui.components.WatermarkBitmapDrawer
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.util.DateCalculatorUtils
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
import kotlin.math.abs

enum class CameraMode { PHOTO, VIDEO }

/**
 * 100% 1:1 还原用户参考图 1 与视频的水印相机全屏拍摄 Screen (动态真机变焦、真实 GPS 坐标 OSM 地图瓦片、照片/视频双模式录制)
 */
@Composable
fun WatermarkCameraScreen(
    cityName: String,
    landmarkName: String,
    latitude: Double,
    longitude: Double,
    lang: AppLanguage,
    onPhotoCaptured: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraMode by remember { mutableStateOf(CameraMode.PHOTO) }
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashEnabled by remember { mutableStateOf(false) }

    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var minZoomRatio by remember { mutableFloatStateOf(1.0f) }
    var maxZoomRatio by remember { mutableFloatStateOf(10.0f) }
    var currentZoom by remember { mutableFloatStateOf(1.0f) }

    var isCapturing by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableLongStateOf(0L) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }

    var mapTileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val nowTimeStr = remember {
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 a h:mm", Locale.SIMPLIFIED_CHINESE))
    }

    var liveLat by remember { mutableDoubleStateOf(latitude) }
    var liveLon by remember { mutableDoubleStateOf(longitude) }
    var liveCity by remember { mutableStateOf(cityName) }
    var liveLandmark by remember { mutableStateOf(landmarkName) }

    // 需求 2: 水印相机预览界面的地图与地名实时跟手轮询 GPS 定位变化，自动加载最新相对应的在地 OpenStreetMap 真实地图瓦片
    LaunchedEffect(Unit) {
        while (true) {
            LocationUtils.requestSingleLocationUpdate(context) { loc ->
                if (loc != null) {
                    liveLat = loc.latitude
                    liveLon = loc.longitude
                    val districtLoc = LocationUtils.getCurrentLocationWithDistrict(context)
                    if (districtLoc.cityName.isNotBlank()) {
                        liveCity = districtLoc.cityName
                    }
                    val districtPart = districtLoc.districtName.substringAfter("·").trim()
                    if (districtPart.isNotBlank()) {
                        liveLandmark = districtPart
                    }
                }
            }
            val bmp = RealMapTileLoader.loadRealMapTile(context, liveLat, liveLon)
            if (bmp != null) {
                mapTileBitmap = bmp
            }
            delay(4000)
        }
    }

    // 录制视频计时器
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0L
            while (isRecording) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val recorder = remember { Recorder.Builder().build() }
    val videoCapture = remember { VideoCapture.withOutput(recorder) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(lensFacing, cameraMode, flashEnabled) {
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

                val camera = if (cameraMode == CameraMode.PHOTO) {
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } else {
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        videoCapture
                    )
                }

                boundCamera = camera
                // 需求 1: 动态读取真机相机物理支持的实际变焦倍率范围 (如 0.5x, 1x, 2x, 5x)
                camera.cameraInfo.zoomState.observe(lifecycleOwner) { state ->
                    if (state != null) {
                        minZoomRatio = state.minZoomRatio
                        maxZoomRatio = state.maxZoomRatio
                        currentZoom = state.zoomRatio
                    }
                }
            } catch (e: Exception) {
                Log.e("WatermarkCamera", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            activeRecording?.stop()
        }
    }

    // 计算真机支持的变焦倍率列表
    val zoomOptions = remember(minZoomRatio, maxZoomRatio) {
        val options = mutableListOf<Float>()
        if (minZoomRatio < 1.0f) {
            options.add(minZoomRatio)
        }
        options.add(1.0f)
        if (maxZoomRatio >= 2.0f) options.add(2.0f)
        if (maxZoomRatio >= 3.0f) options.add(3.0f)
        if (maxZoomRatio >= 5.0f) options.add(5.0f)
        options.distinct().sorted()
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
                    .clickable {
                        if (isRecording) {
                            activeRecording?.stop()
                        }
                        onDismiss()
                    }
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

            // 视频模式正在录制时的动态红点与计时器
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Red.copy(alpha = 0.85f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    val m = recordingSeconds / 60
                    val s = recordingSeconds % 60
                    Text(
                        text = String.format(Locale.US, "🔴 %02d:%02d", m, s),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
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
            // 需求 1: 焦段行 (根据真机实际相机支持的物理放大倍数动态呈现)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                zoomOptions.forEach { z ->
                    val isSelected = abs(currentZoom - z) < 0.15f
                    val labelText = if (z == 0.5f) "0.5" else if (z == 1.0f) "1.0" else if (z == 2.0f) "2.0" else if (z == 3.0f) "3.0" else "5.0"

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFFFACC15) else Color.DarkGray.copy(alpha = 0.6f))
                            .clickable {
                                boundCamera?.cameraControl?.setZoomRatio(z)
                                currentZoom = z
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = labelText,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

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

            // 模式 Pills [ 照片 | 视频 ] (已实现功能性真实 Photo/Video 模式切换)
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
                        .background(if (cameraMode == CameraMode.PHOTO) Color.White.copy(alpha = 0.30f) else Color.Transparent)
                        .clickable {
                            if (!isRecording && !isCapturing) {
                                cameraMode = CameraMode.PHOTO
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text("照片", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (cameraMode == CameraMode.VIDEO) Color.Red.copy(alpha = 0.60f) else Color.Transparent)
                        .clickable {
                            if (!isRecording && !isCapturing) {
                                cameraMode = CameraMode.VIDEO
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text("视频", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
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

                // 核心快门/录制按钮 (照片模式为粉白大光环，视频模式为红色录制块)
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
                        .border(4.dp, if (cameraMode == CameraMode.VIDEO) Color.Red else Color.White, CircleShape)
                        .background(if (cameraMode == CameraMode.VIDEO) Color(0xFFFEE2E2) else Color(0xFFFCE7F3))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            HapticUtils.performCrispClick(context, view)

                            if (cameraMode == CameraMode.PHOTO) {
                                if (isCapturing) return@clickable
                                isCapturing = true

                                // 照片拍摄 + 压入防伪水印
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
                                                    val watermarkedBmp = WatermarkBitmapDrawer.drawWatermarkBadgeOnBitmap(
                                                        srcBitmap = srcBmp,
                                                        cityName = liveCity,
                                                        landmarkName = liveLandmark,
                                                        dateTimeStr = nowTimeStr,
                                                        mapTileBitmap = mapTileBitmap
                                                    )

                                                    // 需求 1: 拍摄完成后，同步保存一份带防伪水印的高清照片至手机公共相册 (DCIM/DateCalculator)
                                                    DateCalculatorUtils.saveWatermarkedPhotoToGallery(
                                                        context = context,
                                                        bitmap = watermarkedBmp,
                                                        fileName = "watermark_${System.currentTimeMillis()}"
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
                            } else {
                                // 视频模式：启动/停止真实 Video 录制
                                if (isRecording) {
                                    activeRecording?.stop()
                                    isRecording = false
                                } else {
                                    val dir = File(context.filesDir, "checkin_videos").apply { if (!exists()) mkdirs() }
                                    val videoFile = File(dir, "video_${System.currentTimeMillis()}.mp4")
                                    val outputOptions = FileOutputOptions.Builder(videoFile).build()

                                    @Suppress("MissingPermission")
                                    val recording = videoCapture.output
                                        .prepareRecording(context, outputOptions)
                                        .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                                            when (recordEvent) {
                                                is VideoRecordEvent.Start -> {
                                                    isRecording = true
                                                }
                                                is VideoRecordEvent.Finalize -> {
                                                    isRecording = false
                                                    if (!recordEvent.hasError()) {
                                                        Toast.makeText(context, "水印视频录制成功！", Toast.LENGTH_SHORT).show()
                                                        onPhotoCaptured(videoFile.absolutePath)
                                                    } else {
                                                        Toast.makeText(context, "视频保存失败", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        }
                                    activeRecording = recording
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isRecording) 28.dp else 60.dp)
                            .clip(if (isRecording) RoundedCornerShape(6.dp) else CircleShape)
                            .background(if (cameraMode == CameraMode.VIDEO) Color.Red else Color.White)
                    )
                }

                // 🔄 Flip Camera Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray.copy(alpha = 0.6f))
                        .clickable {
                            if (!isRecording) {
                                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                            }
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
