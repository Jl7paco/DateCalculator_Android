package me.paco.datecalculator.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
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
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
 * 100% 重构水印相机 Screen (满足 7 大需求：不裁剪全传感器取景、极简防侵占控制面板、极速极速拍完预览确认【重拍/满意】、取消相册入口)
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

    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var minZoomRatio by remember { mutableFloatStateOf(1.0f) }
    var maxZoomRatio by remember { mutableFloatStateOf(10.0f) }
    var currentZoom by remember { mutableFloatStateOf(1.0f) }

    var watermarkScale by remember { mutableFloatStateOf(1.0f) }

    var isCapturing by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableLongStateOf(0L) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }

    // 需求 6: 拍完照片后的 [重拍 / 满意确认] 预览过渡状态
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

    // 需求 4: 极速离屏预载 Map Tile
    LaunchedEffect(latitude, longitude) {
        val bmp = RealMapTileLoader.loadRealMapTile(context, latitude, longitude)
        if (bmp != null) {
            mapTileBitmap = bmp
        }
    }

    // 需求 1: 强制锁屏为 PORTRAIT 竖屏布局，仅在横持手机时将文字与图标做 90/270 度顺滑旋转 (与 iOS/原生相机保持 100% 相同)
    var deviceRotationDeg by remember { mutableIntStateOf(0) }
    DisposableEffect(context) {
        val activity = (context as? android.app.Activity)
        val originalOrientation = activity?.requestedOrientation ?: android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val orientationEventListener = object : android.view.OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN) return
                val rotation = when (orientation) {
                    in 45..134 -> 270
                    in 135..224 -> 180
                    in 225..314 -> 90
                    else -> 0
                }
                deviceRotationDeg = rotation
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

    // 需求 2: 取景预览设置 FIT_CENTER，绝不裁剪任何镜头画面！
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

    // 后置镜头焦段选项
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

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 1. 全屏不裁剪 Viewfinder (FIT_CENTER 100% 完整物理视角)
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

        // 2. 顶部 Floating Badge 水印 (与成品样式 100% 相同)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, end = 16.dp, start = 16.dp)
        ) {
            WatermarkBadgeOverlay(
                cityName = liveCity,
                landmarkName = liveLandmark,
                dateTimeStr = nowTimeStr,
                mapTileBitmap = mapTileBitmap,
                scaleFactor = watermarkScale,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .graphicsLayer { rotationZ = animatedRotation }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            watermarkScale = (watermarkScale * zoom).coerceIn(0.5f, 1.0f)
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
        }

        // 需求 3: 极简低矮靠底控制 Deck (绝不过度侵占取景视野)
        val bottomDeckShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .neumorphicExtruded(shape = bottomDeckShape, elevation = 6.dp)
                .background(NeumorphicBg, shape = bottomDeckShape)
                .clip(bottomDeckShape)
                .padding(bottom = 20.dp, top = 12.dp, start = 20.dp, end = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 焦段选择 (仅后置)
                if (zoomOptions.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        zoomOptions.forEach { z ->
                            val isSelected = abs(currentZoom - z) < 0.15f
                            val labelText = if (z == 0.5f) "0.5" else if (z == 1.0f) "1.0" else if (z == 2.0f) "2.0" else if (z == 3.0f) "3.0" else "5.0"

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
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
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color.White else NeumorphicTextPrimary,
                                    modifier = Modifier.graphicsLayer { rotationZ = animatedRotation }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 模式 Pills [ 照片 | 视频 ]
                val modeSwitchShape = RoundedCornerShape(16.dp)
                Box(
                    modifier = Modifier
                        .neumorphicInset(shape = modeSwitchShape, elevation = 2.dp)
                        .background(NeumorphicSunkenBg, shape = modeSwitchShape)
                        .clip(modeSwitchShape)
                        .padding(horizontal = 3.dp, vertical = 3.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (cameraMode == CameraMode.PHOTO) NeumorphicAccent else Color.Transparent)
                                .clickable {
                                    if (!isRecording && !isCapturing) cameraMode = CameraMode.PHOTO
                                }
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text("照片", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (cameraMode == CameraMode.PHOTO) Color.White else NeumorphicTextPrimary, modifier = Modifier.graphicsLayer { rotationZ = animatedRotation })
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (cameraMode == CameraMode.VIDEO) Color.Red else Color.Transparent)
                                .clickable {
                                    if (!isRecording && !isCapturing) cameraMode = CameraMode.VIDEO
                                }
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text("视频", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (cameraMode == CameraMode.VIDEO) Color.White else NeumorphicTextPrimary, modifier = Modifier.graphicsLayer { rotationZ = animatedRotation })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 快门与手势控制行 (需求 5: 已经取消访问相册的入口按钮)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Flash Button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .neumorphicExtruded(shape = CircleShape, elevation = 4.dp)
                            .background(NeumorphicBg, shape = CircleShape)
                            .clip(CircleShape)
                            .clickable { flashEnabled = !flashEnabled },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (flashEnabled) NeumorphicAccent else NeumorphicTextPrimary,
                            modifier = Modifier.size(18.dp).graphicsLayer { rotationZ = animatedRotation }
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
                            .size(68.dp)
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

                                    // 需求 4: 后台协程处理图片旋转与 1:1 水印合成，极速响应！
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
                                                        // 需求 6: 修正 EXIF 旋转角度
                                                        val srcBmp = if (rotationDeg != 0) {
                                                            val matrix = Matrix().apply { postRotate(rotationDeg.toFloat()) }
                                                            Bitmap.createBitmap(rawBmp, 0, 0, rawBmp.width, rawBmp.height, matrix, true)
                                                        } else rawBmp

                                                        // 1:1 绘制防伪水印
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
                                .size(if (isRecording) 24.dp else 50.dp)
                                .clip(if (isRecording) RoundedCornerShape(6.dp) else CircleShape)
                                .background(if (cameraMode == CameraMode.VIDEO) Color.Red else NeumorphicAccent)
                        )
                    }

                    // 翻转按钮
                    Box(
                        modifier = Modifier
                            .size(42.dp)
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
                            modifier = Modifier.size(18.dp).graphicsLayer { rotationZ = animatedRotation }
                        )
                    }
                }
            }
        }

        // 需求 6: 拍完照后的全屏 [重拍 ↺  /  满意 ✔] 预览确认弹层
        if (pendingPhotoPath != null && pendingPhotoBmp != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                Image(
                    bitmap = pendingPhotoBmp!!.asImageBitmap(),
                    contentDescription = "Preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                // 底部 [ ↺ 重拍 ] 与 [ ✔ 满意 / 使用照片 ] 按键栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.85f))
                        .padding(bottom = 36.dp, top = 20.dp, start = 24.dp, end = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ↺ 重拍
                    Button(
                        onClick = {
                            // 丢弃照片，删除临时文件并返回相机
                            try { File(pendingPhotoPath!!).delete() } catch (_: Exception) {}
                            pendingPhotoPath = null
                            pendingPhotoBmp = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retake", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("↺ 重拍", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    // ✔ 满意 / 使用照片
                    Button(
                        onClick = {
                            val path = pendingPhotoPath!!
                            val bmp = pendingPhotoBmp!!

                            // 同步保存到系统相册
                            DateCalculatorUtils.saveWatermarkedPhotoToGallery(
                                context = context,
                                bitmap = bmp,
                                fileName = "watermark_${System.currentTimeMillis()}"
                            )

                            pendingPhotoPath = null
                            pendingPhotoBmp = null
                            onPhotoCaptured(path)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeumorphicAccent),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Confirm", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("✔ 满意 / 使用照片", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
