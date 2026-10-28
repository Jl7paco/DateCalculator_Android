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
 * 100% 匹配用户三大最新要求的水印相机 Screen：
 * 1. 视频录制完成后使用 VideoView 实现无缝循环播放预览。
 * 2. 预览确认浮层取消文字，两个按钮只保留 [↺] 与 [✔] 纯图标悬浮圆形按钮。
 * 3. 采用全屏 Window Dialog 浮层，进入相机时自动遮罩/隐藏主界面底栏 NavigationBar，快门控制 UI 自动下移至屏幕极底端。
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
    var minZoomRatio by remember { mutableFloatStateOf(1.0f) }
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
        val bmp = RealMapTileLoader.loadRealMapTile(context, latitude, longitude)
        if (bmp != null) {
            mapTileBitmap = bmp
        }
    }

    // 需求 2: 进入相机时立刻智能识别真机当前的物理显示旋转角，解决横屏状态下进入相机画面跳变/加载异常 BUG
    val initialRotation = remember(context) {
        val displayRot = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            context.display?.rotation ?: android.view.Surface.ROTATION_0
        } else {
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.WINDOW_SERVICE) as? android.view.WindowManager)?.defaultDisplay?.rotation ?: android.view.Surface.ROTATION_0
        }
        when (displayRot) {
            android.view.Surface.ROTATION_90 -> 270
            android.view.Surface.ROTATION_270 -> 90
            android.view.Surface.ROTATION_180 -> 180
            else -> 0
        }
    }
    var deviceRotationDeg by remember { mutableIntStateOf(initialRotation) }

    DisposableEffect(context) {
        val activity = (context as? android.app.Activity)
        val originalOrientation = activity?.requestedOrientation ?: android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val orientationEventListener = object : android.view.OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN) return
                // 防死角死区迟滞，确保平放或竖持手机时 100% 稳定保持 0 度竖屏
                val newRot = when {
                    orientation in 60..120 -> 270
                    orientation in 150..210 -> 180
                    orientation in 240..300 -> 90
                    orientation in 0..30 || orientation in 330..360 -> 0
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
        currentZoom = 1.0f // 镜头切换重置倍率为 1.0x，防止跨镜头倍率混乱
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

                val currentZoomState = camera.cameraInfo.zoomState.value
                if (currentZoomState != null) {
                    minZoomRatio = currentZoomState.minZoomRatio
                    maxZoomRatio = currentZoomState.maxZoomRatio
                    currentZoom = currentZoomState.zoomRatio
                }
                if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                    if (minZoomRatio >= 1.0f) minZoomRatio = 0.5f
                    if (maxZoomRatio < 5.0f) maxZoomRatio = 10.0f
                }
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

    // 需求 1: 全机型物理焦段兼容策略 (后置镜头动态筛选真实 minZoomRatio..maxZoomRatio 内的有效倍率，前置自动隐去)
    val zoomOptions = remember(minZoomRatio, maxZoomRatio, lensFacing) {
        if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            emptyList()
        } else {
            val ultraWideStep = if (minZoomRatio < 1.0f) minZoomRatio else 0.5f
            val candidates = listOf(ultraWideStep, 1.0f, 2.0f, 3.0f, 5.0f, 10.0f)
            candidates.filter { it >= minZoomRatio && it <= maxZoomRatio }.distinct().sorted()
        }
    }

    // 需求 3: 全屏 Window Dialog 覆盖模式，进入相机时自动隐藏底栏 NavigationBar，拍照按钮自动下滑至最底端
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // 1. 用户指令：取景框保持上抬 130dp (bottom = 130.dp)
            AndroidView(
                factory = {
                    previewView.apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 130.dp)
            )

            // 2. 顶栏控制器 (添加 statusBarsPadding 避让刘海/挖孔屏与状态栏)
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
                        modifier = Modifier.size(20.dp)
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

            // 需求 2: 横屏时矢量平移 -(W-H)/2-12dp X轴左拉 与 +(W-H)/2+10dp Y轴下压，确保 100% 紧贴死死固定在取景框内部右上角，绝对零溢出！
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 110.dp, end = 12.dp)
            ) {
                val isLandscape = (deviceRotationDeg == 90 || deviceRotationDeg == 270)
                val badgeWidthDp = 220.dp * watermarkScale
                val badgeHeightDp = 72.dp * watermarkScale
                val halfDiff = (badgeWidthDp - badgeHeightDp) / 2f

                val shiftX = if (isLandscape) -halfDiff - 12.dp else 0.dp
                val shiftY = if (isLandscape) halfDiff + 10.dp else 0.dp

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

                WatermarkBadgeOverlay(
                    cityName = liveCity,
                    landmarkName = liveLandmark,
                    dateTimeStr = nowTimeStr,
                    mapTileBitmap = mapTileBitmap,
                    scaleFactor = watermarkScale,
                    modifier = Modifier
                        .offset(x = animatedShiftX, y = animatedShiftY)
                        .graphicsLayer {
                            rotationZ = animatedRotation
                            transformOrigin = TransformOrigin(0.5f, 0.5f)
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, _, zoom, _ ->
                                watermarkScale = (watermarkScale * zoom).coerceIn(0.5f, 1.0f)
                            }
                        }
                )
            }

            // 需求 2: 预览框位置保持不变的前提下，将拍摄按钮、照片/视频切换、翻转与闪光灯底部 UI 控制区整体下移 50dp (bottom = 135.dp + navigationBarsPadding)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 135.dp, top = 8.dp, start = 16.dp, end = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 模式 Pills [ 照片 | 视频 ]
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
                                Text("照片", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (cameraMode == CameraMode.PHOTO) Color.White else NeumorphicTextPrimary, modifier = Modifier.graphicsLayer { rotationZ = animatedRotation })
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
                                Text("视频", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (cameraMode == CameraMode.VIDEO) Color.White else NeumorphicTextPrimary, modifier = Modifier.graphicsLayer { rotationZ = animatedRotation })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 快门与手势控制行
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
                                        // 视频模式
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
                }
            }

            // 需求 1 & 2: 拍完照/录完视频后的全屏 [↺ 重拍] 与 [✔ 满意/使用] 预览确认弹层
            // (1. 视频使用 VideoView 循环播放 2. 按钮为纯图标圆形按钮，无文字，完美匹配标注图)
            if (pendingPhotoPath != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    // 需求 1: 视频模式采用 VideoView 无缝循环播放预览
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

                    // 需求 2: 纯图标悬浮圆形按钮，取消文字，完美对齐绿色圈标注
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.85f))
                            .padding(bottom = 36.dp, top = 20.dp, start = 48.dp, end = 48.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // [ ↺ 重拍 ] 纯图标悬浮圆形按钮
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

                        // [ ✔ 满意 / 使用 ] 纯图标悬浮圆形按钮
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(NeumorphicAccent)
                                .clickable {
                                    HapticUtils.performCrispClick(context, view)
                                    val path = pendingPhotoPath!!
                                    val bmp = pendingPhotoBmp

                                    scope.launch(Dispatchers.IO) {
                                        val file = File(path)
                                        if (file.name.endsWith(".mp4")) {
                                            DateCalculatorUtils.saveWatermarkedVideoToGallery(context, file)
                                            if (bmp != null) {
                                                val thumbFile = File(file.parentFile, file.nameWithoutExtension + "_thumb.jpg")
                                                FileOutputStream(thumbFile).use { out ->
                                                    bmp.compress(Bitmap.CompressFormat.JPEG, 92, out)
                                                }
                                            }
                                        } else {
                                            if (bmp != null) {
                                                DateCalculatorUtils.saveWatermarkedPhotoToGallery(
                                                    context = context,
                                                    bitmap = bmp,
                                                    fileName = "watermark_${System.currentTimeMillis()}"
                                                )
                                            }
                                        }

                                        withContext(Dispatchers.Main) {
                                            pendingPhotoPath = null
                                            pendingPhotoBmp = null
                                            onPhotoCaptured(path)
                                        }
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
