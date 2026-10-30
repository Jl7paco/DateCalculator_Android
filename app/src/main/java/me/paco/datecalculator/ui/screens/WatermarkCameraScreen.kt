package me.paco.datecalculator.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
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
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.ui.components.NeumorphicAccent
import me.paco.datecalculator.ui.components.NeumorphicBg
import me.paco.datecalculator.ui.components.NeumorphicSunkenBg
import me.paco.datecalculator.ui.components.NeumorphicTextPrimary
import me.paco.datecalculator.ui.components.WatermarkBadgeOverlay
import me.paco.datecalculator.ui.components.WatermarkBitmapDrawer
import me.paco.datecalculator.ui.components.neumorphicExtruded
import me.paco.datecalculator.ui.components.neumorphicInset
import me.paco.datecalculator.ui.theme.LocalDarkTheme
import me.paco.datecalculator.util.DateCalculatorUtils
import me.paco.datecalculator.util.HapticUtils
import me.paco.datecalculator.util.RealMapTileLoader
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.abs

enum class CameraMode { PHOTO, VIDEO }

/**
 * 原生专业相机架构的水印相机 Screen：
 * 1. Window Frame 强锁竖屏 Portrait ($1080 \times 1920$)，绝不随设备旋转倒乱 UI。
 * 2. 4:3 摄像头取景框 ($1080 \times 1440$) 永久垂直居中。
 * 3. 顶部放大倍率胶囊 ($0.6x, 1x, 2x, 3x, 5x, 10x$) 100% 预设 0.5x..10x，永久高亮显示不消失。
 * 4. 底部快门/闪光/翻转/模式 Pills 保持在底部控制区，仅图标与文字根据设备陀螺仪旋转 $0^\circ, 90^\circ, 270^\circ$ 保持人眼水平直观可读。
 * 5. 水印 Badge 100% 1:1 严格对齐手绘示意图 (草图 1, 2, 3)。
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
    val scope = rememberCoroutineScope()

    var cameraMode by remember { mutableStateOf(CameraMode.PHOTO) }
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashEnabled by remember { mutableStateOf(false) }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        cameraMode = CameraMode.VIDEO
    }

    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var minZoomRatio by remember { mutableFloatStateOf(0.5f) }
    var maxZoomRatio by remember { mutableFloatStateOf(10.0f) }
    var currentZoom by remember { mutableFloatStateOf(1.0f) }

    var watermarkScale by remember { mutableFloatStateOf(1.0f) }

    var isCapturing by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableLongStateOf(0L) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }

    var pendingPhotoPath by remember { mutableStateOf<String?>(null) }
    var pendingPhotoBmp by remember { mutableStateOf<Bitmap?>(null) }

    var liveLat by remember { mutableFloatStateOf(latitude.toFloat()) }
    var liveLon by remember { mutableFloatStateOf(longitude.toFloat()) }
    var liveCity by remember { mutableStateOf(cityName) }
    var liveLandmark by remember { mutableStateOf(landmarkName) }

    var mapTileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val nowTimeStr = remember {
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 a h:mm", Locale.SIMPLIFIED_CHINESE))
    }

    LaunchedEffect(latitude, longitude) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val bmp = RealMapTileLoader.loadRealMapTile(context, latitude, longitude)
            if (bmp != null) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    mapTileBitmap = bmp
                }
            }
        }
    }

    var deviceRotationDeg by remember { mutableIntStateOf(0) }

    DisposableEffect(context) {
        val activity = (context as? android.app.Activity)
        val originalOrientation = activity?.requestedOrientation ?: android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val orientationEventListener = object : android.view.OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN) return
                val newRot = when {
                    orientation in 65..115 -> 270
                    orientation in 155..205 -> 180
                    orientation in 245..295 -> 90
                    orientation in 0..35 || orientation in 325..360 -> 0
                    else -> deviceRotationDeg
                }
                if (newRot != deviceRotationDeg) {
                    Log.d("WatermarkCamera", "Gyroscope orientation changed: $orientation -> deviceRotationDeg: $newRot")
                    deviceRotationDeg = newRot
                }
            }
        }
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable()
        }
        onDispose {
            orientationEventListener.disable()
            activity?.requestedOrientation = originalOrientation
        }
    }

    val animatedRotation by animateFloatAsState(
        targetValue = deviceRotationDeg.toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "DeviceRotation"
    )

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    val recorder = remember { Recorder.Builder().build() }
    val videoCapture = remember { VideoCapture.withOutput(recorder) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(lensFacing, cameraMode, flashEnabled) {
        currentZoom = 1.0f
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .build()
                    .also {
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
                camera.cameraControl.setZoomRatio(1.0f)

                camera.cameraInfo.zoomState.observe(lifecycleOwner) { state ->
                    if (state != null) {
                        minZoomRatio = if (lensFacing == CameraSelector.LENS_FACING_BACK && state.minZoomRatio >= 1.0f) 0.5f else state.minZoomRatio
                        maxZoomRatio = if (lensFacing == CameraSelector.LENS_FACING_BACK && state.maxZoomRatio < 5.0f) 10.0f else state.maxZoomRatio
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

    // 全机型物理焦段兼容策略 (后置镜头动态筛选真实 minZoomRatio..maxZoomRatio 内的有效倍率，前置自动隐去)
    val zoomOptions = remember(minZoomRatio, maxZoomRatio, lensFacing) {
        if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            emptyList()
        } else {
            val ultraWideStep = if (minZoomRatio < 1.0f) minZoomRatio else 0.5f
            val candidates = listOf(ultraWideStep, 1.0f, 2.0f, 3.0f, 5.0f, 10.0f)
            candidates.filter { it >= minZoomRatio && it <= maxZoomRatio }.distinct().sorted()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // 1. 严格 3:4 摄像头取景框容器 (垂直居中于全屏)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.aspectRatio(3f / 4f)
                ) {
                    // 相机 Preview
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

                    // 100% 1:1 数学校准最新手绘示意图 (草图 1, 2, 3)：全量限制在 4:3 摄像头取景框内部！
                    val targetRotationZ = when (deviceRotationDeg) {
                        270 -> -90f  // 前摄靠左 (草图 2)：逆时针 90° 旋转，使文字在人眼视角 100% 正向水平可读
                        90 -> 90f    // 前摄靠右 (草图 3)：顺时针 90° 旋转，使文字在人眼视角 100% 正向水平可读
                        180 -> 180f  // 倒持竖屏
                        else -> 0f   // 正向竖屏 (草图 1)
                    }

                    val animatedBadgeRotation by animateFloatAsState(
                        targetValue = targetRotationZ,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "BadgeRotation"
                    )

                    val badgeAlignment = when (deviceRotationDeg) {
                        90 -> Alignment.TopStart   // 前摄靠右 (草图 3：人眼视角右上角 = 手机 TopStart)
                        else -> Alignment.TopEnd   // 竖屏 (草图 1) & 前摄靠左 (草图 2)
                    }

                    val badgePadding = when (deviceRotationDeg) {
                        90 -> Modifier.padding(top = 12.dp, start = 12.dp)
                        else -> Modifier.padding(top = 12.dp, end = 12.dp)
                    }

                    val badgeWidthDp = 220.dp * watermarkScale
                    val badgeHeightDp = 72.dp * watermarkScale
                    val halfDiff = (badgeWidthDp - badgeHeightDp) / 2f

                    val shiftX = when (deviceRotationDeg) {
                        270 -> -halfDiff  // 前摄靠左 (草图 2)：-74dp 左拉
                        90 -> halfDiff    // 前摄靠右 (草图 3)：+74dp 右推
                        else -> 0.dp
                    }

                    val shiftY = when (deviceRotationDeg) {
                        270 -> halfDiff   // 前摄靠左 (草图 2)：+74dp 下压
                        90 -> -halfDiff   // 前摄靠右 (草图 3)：-74dp 上拉
                        else -> 0.dp
                    }

                    val animatedShiftX by animateDpAsState(
                        targetValue = shiftX,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "WatermarkShiftX"
                    )
                    val animatedShiftY by animateDpAsState(
                        targetValue = shiftY,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "WatermarkShiftY"
                    )

                    Box(
                        modifier = Modifier
                            .align(badgeAlignment)
                            .then(badgePadding)
                    ) {
                        WatermarkBadgeOverlay(
                            cityName = liveCity,
                            landmarkName = liveLandmark,
                            dateTimeStr = nowTimeStr,
                            mapTileBitmap = mapTileBitmap,
                            scaleFactor = watermarkScale,
                            lang = lang,
                            modifier = Modifier
                                .offset(x = animatedShiftX, y = animatedShiftY)
                                .graphicsLayer {
                                    rotationZ = animatedBadgeRotation
                                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                                }
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, _, zoom, _ ->
                                        watermarkScale = (watermarkScale * zoom).coerceIn(0.5f, 1.0f)
                                    }
                                }
                        )
                    }
                }
            }

            // 2. 顶栏控制器 (退出 X 按钮 + 放大倍率胶囊 0.6x 1x 2x 3x 5x 10x 100% 永久高亮显示)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧退出 X 按钮
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.50f))
                        .clickable {
                            HapticUtils.performCrispClick(context, view)
                            scope.launch(Dispatchers.Default) {
                                try { activeRecording?.stop() } catch (_: Exception) {}
                            }
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp).graphicsLayer { rotationZ = animatedRotation }
                    )
                }

                if (zoomOptions.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.50f))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            zoomOptions.forEach { z ->
                                val isSelected = abs(currentZoom - z) < 0.15f
                                val labelText = if (z < 1.0f) {
                                    String.format(Locale.US, "%.1fx", z)
                                } else if (z % 1.0f == 0.0f) {
                                    "${z.toInt()}x"
                                } else {
                                    String.format(Locale.US, "%.1fx", z)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(width = 36.dp, height = 28.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) NeumorphicAccent else Color.Transparent)
                                        .clickable {
                                            boundCamera?.cameraControl?.setZoomRatio(z)
                                            currentZoom = z
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = labelText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.graphicsLayer { rotationZ = animatedRotation }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 多语言动态翻译模式切换 Pills [ PHOTO | VIDEO ]
            val photoModePillTxt = when (lang.getEffectiveLanguage()) {
                AppLanguage.ENGLISH -> "PHOTO"
                AppLanguage.TRADITIONAL_CHINESE -> "相片"
                AppLanguage.JAPANESE -> "写真"
                AppLanguage.KOREAN -> "사진"
                else -> "照片"
            }

            val videoModePillTxt = when (lang.getEffectiveLanguage()) {
                AppLanguage.ENGLISH -> "VIDEO"
                AppLanguage.TRADITIONAL_CHINESE -> "影片"
                AppLanguage.JAPANESE -> "動画"
                AppLanguage.KOREAN -> "비디오"
                else -> "视频"
            }

            // 3. 底部完整控制区 [ ⚡闪光灯 | 🔴快门 | 🔄翻转镜头 ] 与下方 [照片 | 视频] Pills
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 85.dp, top = 8.dp, start = 16.dp, end = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Row 1: 快门与手势控制行 (Flash | Shutter | Flip)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Flash Button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
                                .background(NeumorphicBg, shape = CircleShape)
                                .clip(CircleShape)
                                .clickable { flashEnabled = !flashEnabled },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Flash",
                                tint = if (flashEnabled) NeumorphicAccent else NeumorphicTextPrimary,
                                modifier = Modifier.size(16.dp).graphicsLayer { rotationZ = animatedRotation }
                            )
                        }

                        // 快门按钮
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val shutterScale by animateFloatAsState(
                            targetValue = if (isPressed || isCapturing) 0.88f else 1.0f,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label = "ShutterScale"
                        )

                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .graphicsLayer {
                                    scaleX = shutterScale
                                    scaleY = shutterScale
                                }
                                .neumorphicExtruded(shape = CircleShape, elevation = 5.dp)
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

                                        imageCapture.takePicture(
                                            cameraExecutor,
                                            object : ImageCapture.OnImageCapturedCallback() {
                                                override fun onCaptureSuccess(image: ImageProxy) {
                                                    val rotationDeg = image.imageInfo.rotationDegrees
                                                    val buffer = image.planes[0].buffer
                                                    val bytes = ByteArray(buffer.capacity())
                                                    buffer.get(bytes)
                                                    val rawBmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                                    image.close()

                                                    scope.launch(Dispatchers.Default) {
                                                        if (rawBmp != null) {
                                                            val srcBmp = if (rotationDeg != 0) {
                                                                val matrix = Matrix().apply { postRotate(rotationDeg.toFloat()) }
                                                                Bitmap.createBitmap(rawBmp, 0, 0, rawBmp.width, rawBmp.height, matrix, true)
                                                            } else rawBmp

                                                            val watermarkedBmp = WatermarkBitmapDrawer.drawWatermarkBadgeOnBitmap(
                                                                srcBitmap = srcBmp,
                                                                cityName = liveCity,
                                                                landmarkName = liveLandmark,
                                                                dateTimeStr = nowTimeStr,
                                                                mapTileBitmap = mapTileBitmap,
                                                                userScaleFactor = watermarkScale
                                                            )

                                                            val dir = File(context.filesDir, "checkin_photos").apply { if (!exists()) mkdirs() }
                                                            val photoFile = File(dir, "photo_${System.currentTimeMillis()}.jpg")
                                                            FileOutputStream(photoFile).use { out ->
                                                                watermarkedBmp.compress(Bitmap.CompressFormat.JPEG, 92, out)
                                                            }

                                                            withContext(Dispatchers.Main) {
                                                                isCapturing = false
                                                                pendingPhotoPath = photoFile.absolutePath
                                                                pendingPhotoBmp = watermarkedBmp
                                                            }
                                                        }
                                                    }
                                                }

                                                override fun onError(exception: ImageCaptureException) {
                                                    Log.e("WatermarkCamera", "Photo capture failed", exception)
                                                    scope.launch(Dispatchers.Main) { isCapturing = false }
                                                }
                                            }
                                        )
                                    } else {
                                        if (isRecording) {
                                            activeRecording?.stop()
                                            isRecording = false
                                        } else {
                                            val dir = File(context.filesDir, "checkin_videos").apply { if (!exists()) mkdirs() }
                                            val videoFile = File(dir, "video_${System.currentTimeMillis()}.mp4")
                                            val outputOptions = FileOutputOptions.Builder(videoFile).build()

                                            val hasAudioPerm = ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                            var pendingRecording = videoCapture.output.prepareRecording(context, outputOptions)
                                            if (hasAudioPerm) {
                                                @Suppress("MissingPermission")
                                                pendingRecording = pendingRecording.withAudioEnabled()
                                            }

                                            val recording = pendingRecording.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                                                when (recordEvent) {
                                                    is VideoRecordEvent.Start -> {
                                                        isRecording = true
                                                    }
                                                    is VideoRecordEvent.Finalize -> {
                                                        isRecording = false
                                                        scope.launch(Dispatchers.Default) {
                                                            val videoBmp = DateCalculatorUtils.getVideoThumbnailBitmap(videoFile.absolutePath)
                                                            val watermarkedBmp = if (videoBmp != null) {
                                                                WatermarkBitmapDrawer.drawWatermarkBadgeOnBitmap(
                                                                    srcBitmap = videoBmp,
                                                                    cityName = liveCity,
                                                                    landmarkName = liveLandmark,
                                                                    dateTimeStr = nowTimeStr,
                                                                    mapTileBitmap = mapTileBitmap,
                                                                    userScaleFactor = watermarkScale
                                                                )
                                                            } else null

                                                            withContext(Dispatchers.Main) {
                                                                pendingPhotoPath = videoFile.absolutePath
                                                                pendingPhotoBmp = watermarkedBmp
                                                            }
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
                                    .size(if (isRecording) 20.dp else 44.dp)
                                    .clip(if (isRecording) RoundedCornerShape(6.dp) else CircleShape)
                                    .background(if (cameraMode == CameraMode.VIDEO) Color.Red else NeumorphicAccent)
                            )
                        }

                        // 翻转按钮
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .neumorphicExtruded(shape = CircleShape, elevation = 3.dp)
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
                                modifier = Modifier.size(16.dp).graphicsLayer { rotationZ = animatedRotation }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: 拨杆开关放置于快门下方绿色框位置（文字绝对固定，不跟随横竖屏旋转）
                    val modeSwitchShape = RoundedCornerShape(14.dp)
                    Box(
                        modifier = Modifier
                            .neumorphicInset(shape = modeSwitchShape, elevation = 2.dp)
                            .background(NeumorphicSunkenBg, shape = modeSwitchShape)
                            .clip(modeSwitchShape)
                            .padding(horizontal = 3.dp, vertical = 2.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (cameraMode == CameraMode.PHOTO) NeumorphicAccent else Color.Transparent)
                                    .clickable {
                                        if (!isRecording && !isCapturing) cameraMode = CameraMode.PHOTO
                                    }
                                    .padding(horizontal = 14.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = photoModePillTxt,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (cameraMode == CameraMode.PHOTO) Color.White else NeumorphicTextPrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (cameraMode == CameraMode.VIDEO) Color.Red else Color.Transparent)
                                    .clickable {
                                        if (!isRecording && !isCapturing) {
                                            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                                audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                            } else {
                                                cameraMode = CameraMode.VIDEO
                                            }
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = videoModePillTxt,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (cameraMode == CameraMode.VIDEO) Color.White else NeumorphicTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 拍完照/录完视频后的全屏 [↺ 重拍] 与 [✔ 满意/使用] 预览确认弹层
            if (pendingPhotoPath != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    if (pendingPhotoPath?.endsWith(".mp4") == true) {
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    setVideoPath(pendingPhotoPath)
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = true
                                        start()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (pendingPhotoBmp != null) {
                        Image(
                            bitmap = pendingPhotoBmp!!.asImageBitmap(),
                            contentDescription = "Preview",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.85f))
                            .padding(bottom = 36.dp, top = 20.dp, start = 48.dp, end = 48.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.DarkGray.copy(alpha = 0.85f))
                                .clickable {
                                    HapticUtils.performCrispClick(context, view)
                                    try { File(pendingPhotoPath!!).delete() } catch (_: Exception) {}
                                    pendingPhotoPath = null
                                    pendingPhotoBmp = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retake",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(NeumorphicAccent)
                                .clickable {
                                    HapticUtils.performCrispClick(context, view)
                                    val path = pendingPhotoPath
                                    if (path != null) {
                                        pendingPhotoPath = null
                                        pendingPhotoBmp = null
                                        onPhotoCaptured(path)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Confirm",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
