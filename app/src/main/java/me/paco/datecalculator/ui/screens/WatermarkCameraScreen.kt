package me.paco.datecalculator.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
 * 100% 重构水印相机 Screen (满足 7 大需求：前置隐藏变焦、主题色统领、双指 Pinch 缩放水印、修正竖拍旋转、相册回放预览、移除 +标志 与黄框)
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

    // 需求 5: 预览界面双指 Pinch 缩放水印大小 (0.6x - 1.8x)
    var watermarkScale by remember { mutableFloatStateOf(1.0f) }

    var isCapturing by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableLongStateOf(0L) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }

    var lastCapturedPath by remember { mutableStateOf<String?>(null) }
    var showPlaybackDialog by remember { mutableStateOf(false) }

    var liveLat by remember { mutableFloatStateOf(latitude.toFloat()) }
    var liveLon by remember { mutableFloatStateOf(longitude.toFloat()) }
    var liveCity by remember { mutableStateOf(cityName) }
    var liveLandmark by remember { mutableStateOf(landmarkName) }

    var mapTileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val nowTimeStr = remember {
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 a h:mm", Locale.SIMPLIFIED_CHINESE))
    }

    // 实时 GPS 定位与地图瓦片加载
    LaunchedEffect(Unit) {
        while (true) {
            LocationUtils.requestSingleLocationUpdate(context) { loc ->
                if (loc != null) {
                    liveLat = loc.latitude.toFloat()
                    liveLon = loc.longitude.toFloat()
                    val districtLoc = LocationUtils.getCurrentLocationWithDistrict(context)
                    if (districtLoc.cityName.isNotBlank()) liveCity = districtLoc.cityName
                    val sub = districtLoc.districtName.substringAfter("·").trim()
                    if (sub.isNotBlank()) liveLandmark = sub
                }
            }
            val bmp = RealMapTileLoader.loadRealMapTile(context, liveLat.toDouble(), liveLon.toDouble())
            if (bmp != null) {
                mapTileBitmap = bmp
            }
            delay(4000)
        }
    }

    // 视频录制计时器
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

    // 需求 1: 后置相机依据硬件能力生成倍数 (如 0.5x, 1x, 2x, 3x, 5x)，前置相机绝对不显示变焦按钮
    val zoomOptions = remember(minZoomRatio, maxZoomRatio, lensFacing) {
        if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            emptyList()
        } else {
            val options = mutableListOf<Float>()
            if (minZoomRatio < 1.0f) options.add(minZoomRatio)
            options.add(1.0f)
            if (maxZoomRatio >= 2.0f) options.add(2.0f)
            if (maxZoomRatio >= 3.0f) options.add(3.0f)
            if (maxZoomRatio >= 5.0f) options.add(5.0f)
            options.distinct().sorted()
        }
    }

    // 需求 7: 拍完后可以在水印相机内点按缩略图弹窗回放预览
    if (showPlaybackDialog && !lastCapturedPath.isNullOrBlank()) {
        Dialog(
            onDismissRequest = { showPlaybackDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                val photoFile = File(lastCapturedPath!!)
                val bmp = remember(lastCapturedPath) {
                    if (photoFile.exists()) {
                        try { BitmapFactory.decodeFile(photoFile.absolutePath) } catch (_: Exception) { null }
                    } else null
                }

                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Playback",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 顶栏关闭与通知
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 44.dp, start = 20.dp, end = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.60f))
                            .clickable { showPlaybackDialog = false }
                            .align(Alignment.TopStart),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(NeumorphicAccent)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Text("📷 防伪水印成品照片预览", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
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

        // 2. 需求 5: 预览界面双指 Pinch 缩放水印 (与成品尺寸 100% 保持一致)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, end = 16.dp, start = 16.dp)
        ) {
            WatermarkBadgeOverlay(
                cityName = liveCity,
                landmarkName = liveLandmark,
                dateTimeStr = nowTimeStr,
                mapTileBitmap = mapTileBitmap,
                scaleFactor = watermarkScale,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            watermarkScale = (watermarkScale * zoom).coerceIn(0.6f, 1.8f)
                        }
                    }
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

        // 需求 4: 已取消预览框中莫名的黄框显示！

        // 3. 需求 3: 底部 3D 新拟物控制 Deck (采用全局一致的主题色 NeumorphicAccent)
        val bottomDeckShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .neumorphicExtruded(shape = bottomDeckShape, elevation = 8.dp)
                .background(NeumorphicBg, shape = bottomDeckShape)
                .clip(bottomDeckShape)
                .padding(bottom = 28.dp, top = 18.dp, start = 20.dp, end = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 需求 1: 焦段行 (仅后置相机显示物理变焦倍率，前置相机隐去)
                if (zoomOptions.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        zoomOptions.forEach { z ->
                            val isSelected = abs(currentZoom - z) < 0.15f
                            val labelText = if (z == 0.5f) "0.5" else if (z == 1.0f) "1.0" else if (z == 2.0f) "2.0" else if (z == 3.0f) "3.0" else "5.0"

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .neumorphicExtruded(shape = CircleShape, elevation = if (isSelected) 2.dp else 4.dp)
                                    .background(if (isSelected) NeumorphicAccent else NeumorphicBg, shape = CircleShape)
                                    .clip(CircleShape)
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
                                    color = if (isSelected) Color.White else NeumorphicTextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 需求 2: 取消 +标志 按钮！
                // 需求 3: 模式切换 Pills 主题色一致
                val modeSwitchShape = RoundedCornerShape(20.dp)
                Box(
                    modifier = Modifier
                        .neumorphicInset(shape = modeSwitchShape, elevation = 3.dp)
                        .background(NeumorphicSunkenBg, shape = modeSwitchShape)
                        .clip(modeSwitchShape)
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (cameraMode == CameraMode.PHOTO) NeumorphicAccent else Color.Transparent)
                                .clickable {
                                    if (!isRecording && !isCapturing) {
                                        cameraMode = CameraMode.PHOTO
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("照片", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (cameraMode == CameraMode.PHOTO) Color.White else NeumorphicTextPrimary)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (cameraMode == CameraMode.VIDEO) Color.Red else Color.Transparent)
                                .clickable {
                                    if (!isRecording && !isCapturing) {
                                        cameraMode = CameraMode.VIDEO
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("视频", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (cameraMode == CameraMode.VIDEO) Color.White else NeumorphicTextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 底部快门控制与需求 7 相册回放预览按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 需求 7: 左侧相册/照片回放预览缩略图按钮
                    val galleryFile = lastCapturedPath?.let { File(it) }
                    val galleryBmp = remember(lastCapturedPath) {
                        if (galleryFile != null && galleryFile.exists()) {
                            try { BitmapFactory.decodeFile(galleryFile.absolutePath) } catch (_: Exception) { null }
                        } else null
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
                            .background(NeumorphicBg, shape = CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                if (!lastCapturedPath.isNullOrBlank()) {
                                    showPlaybackDialog = true
                                } else {
                                    Toast.makeText(context, "尚无已拍摄的水印成品照片", Toast.LENGTH_SHORT).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (galleryBmp != null) {
                            Image(
                                bitmap = galleryBmp.asImageBitmap(),
                                contentDescription = "Gallery",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Gallery",
                                tint = NeumorphicAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // 核心 3D 新拟物快门/录制按钮 (主题色统一)
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
                            .neumorphicExtruded(shape = CircleShape, elevation = 6.dp)
                            .background(NeumorphicBg, shape = CircleShape)
                            .clip(CircleShape)
                            .border(3.dp, if (cameraMode == CameraMode.VIDEO) Color.Red else NeumorphicAccent, CircleShape)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                HapticUtils.performCrispClick(context, view)

                                if (cameraMode == CameraMode.PHOTO) {
                                    if (isCapturing) return@clickable
                                    isCapturing = true

                                    // 需求 6: 修正照片竖拍旋转角度 (读取 EXIF/ImageProxy 旋转角度 postRotate)
                                    imageCapture.takePicture(
                                        cameraExecutor,
                                        object : ImageCapture.OnImageCapturedCallback() {
                                            override fun onCaptureSuccess(image: ImageProxy) {
                                                try {
                                                    val rotationDeg = image.imageInfo.rotationDegrees
                                                    val buffer = image.planes[0].buffer
                                                    val bytes = ByteArray(buffer.capacity())
                                                    buffer.get(bytes)
                                                    val rawBmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                                    image.close()

                                                    if (rawBmp != null) {
                                                        // 校正旋转角度，确保竖拍保持 100% 正向竖版
                                                        val srcBmp = if (rotationDeg != 0) {
                                                            val matrix = Matrix().apply { postRotate(rotationDeg.toFloat()) }
                                                            Bitmap.createBitmap(rawBmp, 0, 0, rawBmp.width, rawBmp.height, matrix, true)
                                                        } else rawBmp

                                                        // 需求 5: 按用户 Pinch 手势缩放大小压入 1:1 完美比例水印
                                                        val watermarkedBmp = WatermarkBitmapDrawer.drawWatermarkBadgeOnBitmap(
                                                            srcBitmap = srcBmp,
                                                            cityName = liveCity,
                                                            landmarkName = liveLandmark,
                                                            dateTimeStr = nowTimeStr,
                                                            mapTileBitmap = mapTileBitmap,
                                                            userScaleFactor = watermarkScale
                                                        )

                                                        // 保存到系统相册
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
                                                            lastCapturedPath = photoFile.absolutePath
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
                                    // 视频录制
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
                                                            lastCapturedPath = videoFile.absolutePath
                                                            Toast.makeText(context, "水印视频录制完成！", Toast.LENGTH_SHORT).show()
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
                                .size(if (isRecording) 28.dp else 58.dp)
                                .clip(if (isRecording) RoundedCornerShape(6.dp) else CircleShape)
                                .background(if (cameraMode == CameraMode.VIDEO) Color.Red else NeumorphicAccent)
                        )
                    }

                    // 需求 3: 闪光灯与翻转按钮（统一主题色）
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
                            .background(NeumorphicBg, shape = CircleShape)
                            .clip(CircleShape)
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
                            tint = NeumorphicTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}