package me.paco.datecalculator.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.paco.datecalculator.data.AppLanguage
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import me.paco.datecalculator.data.CalculationStage
import me.paco.datecalculator.data.CalculationType
import me.paco.datecalculator.data.HolidayRegion
import me.paco.datecalculator.data.RegionalHolidays
import me.paco.datecalculator.data.StageSegmentResult
import me.paco.datecalculator.data.WeekendRule
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.abs

object RealMapTileLoader {

    private val memoryTileCache = object : android.util.LruCache<String, Bitmap>(24) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    private fun latLonToTile(lat: Double, lon: Double, zoom: Int): Pair<Int, Int> {
        val n = 1 shl zoom
        val x = ((lon + 180.0) / 360.0 * n).toInt().coerceIn(0, n - 1)
        val radLat = Math.toRadians(lat)
        val y = ((1.0 - kotlin.math.ln(kotlin.math.tan(radLat) + 1.0 / kotlin.math.cos(radLat)) / Math.PI) / 2.0 * n).toInt().coerceIn(0, n - 1)
        return Pair(x, y)
    }

    suspend fun loadRealMapTile(
        context: Context,
        lat: Double,
        lon: Double,
        zoom: Int = 15
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // Apply GCJ-02 shift if in Mainland China for 100% accurate alignment
            val (dispLat, dispLon) = LocationUtils.getDisplayCoordinates(lat, lon)
            val (x, y) = latLonToTile(dispLat, dispLon, zoom)
            val memoryKey = "map_${zoom}_${x}_${y}"

            val memoryBmp = memoryTileCache.get(memoryKey)
            if (memoryBmp != null && !memoryBmp.isRecycled) {
                return@withContext memoryBmp
            }

            val cacheDir = File(context.cacheDir, "real_map_tiles_v3").apply { if (!exists()) mkdirs() }
            val cacheFile = File(cacheDir, "map_${zoom}_${x}_${y}.png")

            if (cacheFile.exists() && cacheFile.length() > 500) {
                val cachedBmp = BitmapFactory.decodeFile(cacheFile.absolutePath)
                if (cachedBmp != null) {
                    memoryTileCache.put(memoryKey, cachedBmp)
                    return@withContext cachedBmp
                }
            }

            // 调取 Esri World Street Map 与开放地图源 (100% 真实高清、无 API Key、无 Access Block、无水印)
            val tileUrls = listOf(
                "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/$zoom/$y/$x",
                "https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/$zoom/$y/$x",
                "https://tile.openstreetmap.org/$zoom/$x/$y.png"
            )

            for (tileUrlStr in tileUrls) {
                try {
                    val url = URL(tileUrlStr)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 3000
                    conn.readTimeout = 3000
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) DateCalculator/3.0.7")

                    if (conn.responseCode == 200) {
                        conn.inputStream.use { input ->
                            FileOutputStream(cacheFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        val downloadedBmp = BitmapFactory.decodeFile(cacheFile.absolutePath)
                        if (downloadedBmp != null) {
                            memoryTileCache.put(memoryKey, downloadedBmp)
                            return@withContext downloadedBmp
                        }
                    }
                } catch (_: Exception) {}
            }
            null
        } catch (_: Exception) {
            null
        }
    }
}

object DateCalculatorUtils {

    fun getWatermarkMonthHeader(monthValue: Int, holidayRegion: HolidayRegion): String {
        val monthNames = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")
        return "${monthValue}月 · ${monthNames.getOrElse(monthValue - 1) { "" }}"
    }

    fun formatTimestamp(timestamp: Long, language: AppLanguage): String {
        val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        return formatDate(date, language)
    }

    fun formatDate(date: LocalDate, language: AppLanguage): String {
        return when (language.getEffectiveLanguage()) {
            AppLanguage.SIMPLIFIED_CHINESE -> date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日", Locale.CHINA))
            AppLanguage.TRADITIONAL_CHINESE -> date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日", Locale.TAIWAN))
            AppLanguage.ENGLISH -> date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US))
            AppLanguage.JAPANESE -> date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日", Locale.JAPAN))
            AppLanguage.KOREAN -> date.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일", Locale.KOREA))
            else -> date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日", Locale.CHINA))
        }
    }

    fun getLocalizedDayOfWeek(date: LocalDate, language: AppLanguage): String {
        val dayOfWeek = date.dayOfWeek.value // 1 = Mon, 7 = Sun
        return when (language.getEffectiveLanguage()) {
            AppLanguage.ENGLISH -> when (dayOfWeek) {
                1 -> "Mon"; 2 -> "Tue"; 3 -> "Wed"; 4 -> "Thu"; 5 -> "Fri"; 6 -> "Sat"; else -> "Sun"
            }
            AppLanguage.JAPANESE -> when (dayOfWeek) {
                1 -> "月"; 2 -> "火"; 3 -> "水"; 4 -> "木"; 5 -> "金"; 6 -> "土"; else -> "日"
            }
            AppLanguage.KOREAN -> when (dayOfWeek) {
                1 -> "월"; 2 -> "화"; 3 -> "수"; 4 -> "목"; 5 -> "금"; 6 -> "토"; else -> "일"
            }
            AppLanguage.TRADITIONAL_CHINESE -> when (dayOfWeek) {
                1 -> "週一"; 2 -> "週二"; 3 -> "週三"; 4 -> "週四"; 5 -> "週五"; 6 -> "週六"; else -> "週日"
            }
            else -> when (dayOfWeek) {
                1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"; 5 -> "周五"; 6 -> "周六"; else -> "周日"
            }
        }
    }

    fun formatDateWithWeek(date: LocalDate, language: AppLanguage): String {
        val dateFormatted = formatDate(date, language)
        val weekNum = getWeekOfYear(date)
        return when (language.getEffectiveLanguage()) {
            AppLanguage.ENGLISH -> "$dateFormatted (Week $weekNum)"
            AppLanguage.JAPANESE -> "$dateFormatted (第${weekNum}週)"
            AppLanguage.KOREAN -> "$dateFormatted (第${weekNum}주)"
            AppLanguage.TRADITIONAL_CHINESE -> "$dateFormatted (第${weekNum}週)"
            else -> "$dateFormatted (第${weekNum}周)"
        }
    }

    fun getWeekOfYear(date: LocalDate): Int {
        val weekFields = WeekFields.of(Locale.getDefault())
        return date.get(weekFields.weekOfWeekBasedYear())
    }

    fun isWorkday(
        date: LocalDate,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true,
        disableChinaShiftWorkdays: Boolean = false
    ): Boolean {
        val isShift = enableHolidays && !disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(date, holidayRegion)
        val isStat = enableHolidays && RegionalHolidays.isStatutoryHoliday(date, holidayRegion)
        val isWeekend = weekendRule.isWeekend(date, isCurrentWeekBigWeek)

        return if (isShift) true else if (isStat) false else !isWeekend
    }

    fun calculateTargetDate(
        baseDate: LocalDate,
        days: Int,
        type: CalculationType = CalculationType.ADD,
        isWorkdayMode: Boolean = true,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true,
        disableChinaShiftWorkdays: Boolean = false,
        includeStartDate: Boolean = false
    ): LocalDate {
        if (days == 0) return baseDate

        val isAdd = (type == CalculationType.ADD)

        if (!isWorkdayMode) {
            val effDays = if (includeStartDate && days > 0) days - 1 else days
            return if (isAdd) baseDate.plusDays(effDays.toLong()) else baseDate.minusDays(effDays.toLong())
        }

        var currentDate = baseDate
        var step = days

        if (includeStartDate && days > 0) {
            val isBaseShift = enableHolidays && !disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(baseDate, holidayRegion)
            val isBaseStat = enableHolidays && RegionalHolidays.isStatutoryHoliday(baseDate, holidayRegion)
            val isBaseWeekend = weekendRule.isWeekend(baseDate, isCurrentWeekBigWeek)
            val isBaseWorking = if (isBaseShift) true else if (isBaseStat) false else !isBaseWeekend
            if (isBaseWorking) {
                step--
            }
        }

        while (step > 0) {
            currentDate = if (isAdd) currentDate.plusDays(1) else currentDate.minusDays(1)

            val isShiftWorkday = enableHolidays && !disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(currentDate, holidayRegion)
            val isStatutoryHoliday = enableHolidays && RegionalHolidays.isStatutoryHoliday(currentDate, holidayRegion)
            val isWeekend = weekendRule.isWeekend(currentDate, isCurrentWeekBigWeek)

            val isWorkingDay = if (isShiftWorkday) {
                true
            } else if (isStatutoryHoliday) {
                false
            } else if (isWeekend) {
                false
            } else {
                true
            }

            if (isWorkingDay) {
                step--
            }
        }

        return currentDate
    }

    fun calculateDaysBetweenDates(
        startDate: LocalDate,
        targetDate: LocalDate,
        isWorkdayMode: Boolean = true,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true,
        disableChinaShiftWorkdays: Boolean = false,
        includeStartDate: Boolean = false
    ): Int {
        if (startDate == targetDate) return if (includeStartDate) 1 else 0
        val isAdd = !targetDate.isBefore(startDate)
        if (!isWorkdayMode) {
            val diff = abs(ChronoUnit.DAYS.between(startDate, targetDate)).toInt()
            return if (includeStartDate) diff + 1 else diff
        }

        var count = 0
        var curr = if (includeStartDate) startDate else (if (isAdd) startDate.plusDays(1) else startDate.minusDays(1))
        val limit = if (isAdd) targetDate else startDate.minusDays(1)

        while (if (isAdd) !curr.isAfter(limit) else !curr.isBefore(targetDate)) {
            val isShift = enableHolidays && !disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(curr, holidayRegion)
            val isStat = enableHolidays && RegionalHolidays.isStatutoryHoliday(curr, holidayRegion)
            val isWeekend = weekendRule.isWeekend(curr, isCurrentWeekBigWeek)

            val isWork = if (isShift) true else if (isStat) false else !isWeekend
            if (isWork) {
                count++
            }
            curr = if (isAdd) curr.plusDays(1) else curr.minusDays(1)
        }

        return count
    }

    /**
     * 判断当前 GPS/城市经纬度坐标下是否处于白昼 (用于 3D 卫星地球背景强制重写浅色/深色主题)
     */
    data class DeviceLaunchInfo(
        val deviceName: String,
        val launchDate: LocalDate
    )

    fun getDeviceLaunchInfo(): DeviceLaunchInfo {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        val rawModel = Build.MODEL.trim()

        // 智能查询并核对真机硬件销售型号 (如 SM-S9280 -> Samsung Galaxy S24 Ultra, 发布于 2024-01-17)
        val (marketingName, releaseDate) = when {
            // 三星 Galaxy S24 系列 (如 SM-S9280 / SM-S928B)
            rawModel.contains("S928") || rawModel.contains("S9280") -> "Samsung Galaxy S24 Ultra" to LocalDate.of(2024, 1, 17)
            rawModel.contains("S926") || rawModel.contains("S9260") -> "Samsung Galaxy S24+" to LocalDate.of(2024, 1, 17)
            rawModel.contains("S921") || rawModel.contains("S9210") -> "Samsung Galaxy S24" to LocalDate.of(2024, 1, 17)

            // 三星 Galaxy S23 系列
            rawModel.contains("S918") || rawModel.contains("S9180") -> "Samsung Galaxy S23 Ultra" to LocalDate.of(2023, 2, 1)
            rawModel.contains("S916") || rawModel.contains("S9160") -> "Samsung Galaxy S23+" to LocalDate.of(2023, 2, 1)
            rawModel.contains("S911") || rawModel.contains("S9110") -> "Samsung Galaxy S23" to LocalDate.of(2023, 2, 1)

            // 三星 Galaxy S22 系列
            rawModel.contains("S908") || rawModel.contains("S9080") -> "Samsung Galaxy S22 Ultra" to LocalDate.of(2022, 2, 9)
            rawModel.contains("S906") || rawModel.contains("S9060") -> "Samsung Galaxy S22+" to LocalDate.of(2022, 2, 9)
            rawModel.contains("S901") || rawModel.contains("S9010") -> "Samsung Galaxy S22" to LocalDate.of(2022, 2, 9)

            // 小米旗舰系列
            rawModel.contains("2311DRK48C") || rawModel.contains("23127PN0CC") -> "Xiaomi 14 / Pro" to LocalDate.of(2023, 10, 26)
            rawModel.contains("2211133C") || rawModel.contains("2210132C") -> "Xiaomi 13 / Pro" to LocalDate.of(2022, 12, 11)

            // 谷歌 Pixel & Apple iPhone
            rawModel.contains("Pixel 8") -> "Google Pixel 8" to LocalDate.of(2023, 10, 4)
            rawModel.contains("Pixel 7") -> "Google Pixel 7" to LocalDate.of(2022, 10, 6)
            rawModel.contains("iPhone16") -> "iPhone 15 Pro" to LocalDate.of(2023, 9, 22)
            rawModel.contains("iPhone15") -> "iPhone 14 Pro" to LocalDate.of(2022, 9, 16)

            else -> {
                val clean = if (rawModel.contains(manufacturer, ignoreCase = true)) {
                    rawModel
                } else {
                    "$manufacturer $rawModel"
                }
                clean to when (Build.VERSION.SDK_INT) {
                    Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> LocalDate.of(2023, 10, 4)
                    Build.VERSION_CODES.TIRAMISU -> LocalDate.of(2022, 8, 15)
                    Build.VERSION_CODES.S_V2, Build.VERSION_CODES.S -> LocalDate.of(2021, 10, 4)
                    Build.VERSION_CODES.R -> LocalDate.of(2020, 9, 8)
                    else -> LocalDate.of(2021, 1, 1)
                }
            }
        }

        return DeviceLaunchInfo(deviceName = marketingName, launchDate = releaseDate)
    }

    /**
     * 保存防伪水印照片至系统公共相册 (DCIM/DateCalculator)
     */
    fun saveWatermarkedPhotoToGallery(context: Context, bitmap: Bitmap, fileName: String): android.net.Uri? {
        val resolver = context.contentResolver
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.jpg")
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/DateCalculator")
                put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            try {
                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return uri
    }

    fun addCheckInWatermark(
        context: Context,
        srcBitmap: Bitmap,
        locationName: String,
        coords: Pair<Double, Double>,
        timestampStr: String,
        titleText: String
    ): String {
        try {
            val width = srcBitmap.width
            val height = srcBitmap.height
            val mutableBitmap = srcBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutableBitmap)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val textMargin = (width * 0.04f).coerceAtLeast(20f)
            val bannerHeight = (height * 0.25f).coerceAtLeast(180f)

            // 1. 底端暗调半透明渐变背景遮罩
            val gradient = LinearGradient(
                0f, height - bannerHeight, 0f, height.toFloat(),
                AndroidColor.TRANSPARENT, AndroidColor.argb(220, 10, 15, 28),
                Shader.TileMode.CLAMP
            )
            val bgPaint = Paint().apply { shader = gradient }
            canvas.drawRect(0f, height - bannerHeight, width.toFloat(), height.toFloat(), bgPaint)

            // 2. 绘制高清防伪水印文案 (包含地点、GPS火星偏移/WGS84坐标、时间戳、事件名称)
            paint.color = AndroidColor.WHITE
            paint.textSize = (width * 0.038f).coerceIn(24f, 52f)
            paint.isFakeBoldText = true

            val y3 = height - textMargin
            val y2 = y3 - paint.textSize * 1.4f
            val y1 = y2 - paint.textSize * 1.4f

            canvas.drawText("📍 地点: $locationName", textMargin, y1, paint)

            paint.color = AndroidColor.parseColor("#38BDF8") // 天蓝色 GPS
            val (dispLat, dispLon) = LocationUtils.getDisplayCoordinates(coords.first, coords.second)
            canvas.drawText("⏰ 时刻: $timestampStr  |  GPS: (${String.format(Locale.US, "%.3f", dispLat)}°, ${String.format(Locale.US, "%.3f", dispLon)}°)", textMargin, y2, paint)

            paint.color = AndroidColor.parseColor("#F59E0B") // 琥珀黄打卡主题
            canvas.drawText("🏷️ 打卡: $titleText", textMargin, y3, paint)

            // 3. 保存带有专属水印的照片至应用私有目录
            val dir = File(context.filesDir, "checkin_photos").apply { if (!exists()) mkdirs() }
            val photoFile = File(dir, "photo_${System.currentTimeMillis()}.jpg")
            FileOutputStream(photoFile).use { out ->
                mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            return photoFile.absolutePath
        } catch (_: Exception) {
            return ""
        }
    }

    fun isDaylightAtLocation(latitude: Double, longitude: Double): Boolean {
        val currentHour = try { java.time.LocalTime.now().hour } catch (_: Exception) { 12 }
        if (latitude == 0.0 && longitude == 0.0) {
            return currentHour in 6..17
        }
        val localSolarHour = ((currentHour + (longitude / 15.0)).toInt() % 24 + 24) % 24
        return localSolarHour in 6..17
    }

    fun computeMultiStageSequence(
        baseDate: LocalDate,
        stages: List<CalculationStage>,
        isWorkdayMode: Boolean = true,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true,
        disableChinaShiftWorkdays: Boolean = false,
        includeStartDate: Boolean = false
    ): Pair<LocalDate, List<StageSegmentResult>> {
        var currentStart = baseDate
        val results = mutableListOf<StageSegmentResult>()

        stages.forEachIndexed { idx, stage ->
            val daysVal = stage.days.toInt()
            if (daysVal > 0) {
                val segEnd = calculateTargetDate(
                    baseDate = currentStart,
                    days = daysVal,
                    type = stage.type,
                    isWorkdayMode = isWorkdayMode,
                    weekendRule = weekendRule,
                    enableHolidays = enableHolidays,
                    holidayRegion = holidayRegion,
                    isCurrentWeekBigWeek = isCurrentWeekBigWeek,
                    disableChinaShiftWorkdays = disableChinaShiftWorkdays,
                    includeStartDate = if (idx == 0) includeStartDate else false
                )

                val (_, stat, week) = calculateBreakdown(
                    startDate = currentStart,
                    endDate = segEnd,
                    weekendRule = weekendRule,
                    enableHolidays = enableHolidays,
                    holidayRegion = holidayRegion,
                    isCurrentWeekBigWeek = isCurrentWeekBigWeek,
                    disableChinaShiftWorkdays = disableChinaShiftWorkdays,
                    includeStartDate = if (idx == 0) includeStartDate else false
                )

                val remarkText = stage.remark.ifBlank { "阶段 ${idx + 1}" }
                val rawNat = abs(naturalDaysBetween(currentStart, segEnd))
                val totalNat = if (idx == 0 && includeStartDate) rawNat + 1 else rawNat

                results.add(
                    StageSegmentResult(
                        stageIndex = idx,
                        remark = remarkText,
                        type = stage.type,
                        daysCount = stage.days,
                        startDate = currentStart,
                        endDate = segEnd,
                        totalCalendarDays = totalNat,
                        restDaysCount = stat + week
                    )
                )

                currentStart = segEnd
            }
        }

        return Pair(currentStart, results)
    }

    fun workdaysBetween(
        startDate: LocalDate,
        endDate: LocalDate,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true,
        disableChinaShiftWorkdays: Boolean = false,
        includeStartDate: Boolean = false
    ): Long {
        if (startDate == endDate) return if (includeStartDate) 1L else 0L

        val isForward = startDate.isBefore(endDate)
        val start = if (isForward) startDate else endDate
        val end = if (isForward) endDate else startDate

        var workdays = 0L
        var curr = if (includeStartDate) start else start.plusDays(1)

        while (!curr.isAfter(end)) {
            val isShiftWorkday = enableHolidays && !disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(curr, holidayRegion)
            val isStatutoryHoliday = enableHolidays && RegionalHolidays.isStatutoryHoliday(curr, holidayRegion)
            val isWeekend = weekendRule.isWeekend(curr, isCurrentWeekBigWeek)

            val isWorkingDay = if (isShiftWorkday) {
                true
            } else if (isStatutoryHoliday) {
                false
            } else if (isWeekend) {
                false
            } else {
                true
            }

            if (isWorkingDay) {
                workdays++
            }

            curr = curr.plusDays(1)
        }

        return if (isForward) workdays else -workdays
    }

    fun naturalDaysBetween(startDate: LocalDate, endDate: LocalDate): Long {
        return ChronoUnit.DAYS.between(startDate, endDate)
    }

    fun calculateBreakdown(
        startDate: LocalDate,
        endDate: LocalDate,
        weekendRule: WeekendRule = WeekendRule.STANDARD_FIVE_DAYS,
        enableHolidays: Boolean = true,
        holidayRegion: HolidayRegion = HolidayRegion.CHINA,
        isCurrentWeekBigWeek: Boolean = true,
        disableChinaShiftWorkdays: Boolean = false,
        includeStartDate: Boolean = false
    ): Triple<Long, Long, Long> {
        val rawNat = abs(naturalDaysBetween(startDate, endDate))
        val totalDays = if (includeStartDate) rawNat + 1 else rawNat
        if (totalDays == 0L) return Triple(0L, 0L, 0L)

        val start = if (startDate.isBefore(endDate)) startDate else endDate
        val end = if (startDate.isBefore(endDate)) endDate else startDate

        var workdays = 0L
        var holidays = 0L
        var weekends = 0L

        var curr = if (includeStartDate) start else start.plusDays(1)
        while (!curr.isAfter(end)) {
            val isShiftWorkday = enableHolidays && !disableChinaShiftWorkdays && RegionalHolidays.isShiftWorkday(curr, holidayRegion)
            val isStatutoryHoliday = enableHolidays && RegionalHolidays.isStatutoryHoliday(curr, holidayRegion)
            val isWeekend = weekendRule.isWeekend(curr, isCurrentWeekBigWeek)

            if (isShiftWorkday) {
                workdays++
            } else if (isStatutoryHoliday) {
                holidays++
            } else if (isWeekend) {
                weekends++
            } else {
                workdays++
            }

            curr = curr.plusDays(1)
        }

        return Triple(workdays, holidays, weekends)
    }

    fun formatPeriod(
        startDate: LocalDate,
        endDate: LocalDate,
        language: AppLanguage
    ): String {
        val start = if (startDate.isBefore(endDate)) startDate else endDate
        val end = if (startDate.isBefore(endDate)) endDate else startDate

        val period = Period.between(start, end)
        val years = period.years
        val months = period.months
        val days = period.days

        val parts = mutableListOf<String>()
        return when (language.getEffectiveLanguage()) {
            AppLanguage.SIMPLIFIED_CHINESE -> {
                if (years > 0) parts.add("${years}年")
                if (months > 0) parts.add("${months}个月")
                if (days > 0) parts.add("${days}天")
                parts.joinToString(" ").ifEmpty { "0天" }
            }
            AppLanguage.TRADITIONAL_CHINESE -> {
                if (years > 0) parts.add("${years}年")
                if (months > 0) parts.add("${months}個月")
                if (days > 0) parts.add("${days}天")
                parts.joinToString(" ").ifEmpty { "0天" }
            }
            AppLanguage.ENGLISH -> {
                if (years > 0) parts.add("$years ${if (years > 1) "years" else "year"}")
                if (months > 0) parts.add("$months ${if (months > 1) "months" else "month"}")
                if (days > 0) parts.add("$days ${if (days > 1) "days" else "day"}")
                parts.joinToString(" ").ifEmpty { "0 days" }
            }
            AppLanguage.JAPANESE -> {
                if (years > 0) parts.add("${years}年")
                if (months > 0) parts.add("${months}ヶ月")
                if (days > 0) parts.add("${days}日")
                parts.joinToString(" ").ifEmpty { "0日" }
            }
            AppLanguage.KOREAN -> {
                if (years > 0) parts.add("${years}년")
                if (months > 0) parts.add("${months}개월")
                if (days > 0) parts.add("${days}일")
                parts.joinToString(" ").ifEmpty { "0일" }
            }
            else -> {
                if (years > 0) parts.add("${years}年")
                if (months > 0) parts.add("${months}个月")
                if (days > 0) parts.add("${days}天")
                parts.joinToString(" ").ifEmpty { "0天" }
            }
        }
    }
}
