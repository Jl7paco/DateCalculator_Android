package me.paco.datecalculator.util

import me.paco.datecalculator.data.AppLanguage
import me.paco.datecalculator.data.HolidayRegion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.roundToInt

data class DailyWeather(
    val date: LocalDate,
    val dayName: String,
    val condition: String,
    val iconEmoji: String,
    val tempMin: Int,
    val tempMax: Int,
    val wind: String,
    val airQuality: String
)

object WeatherUtils {

    fun getLocationName(region: HolidayRegion): String {
        return when (region) {
            HolidayRegion.CHINA -> "深圳市"
            HolidayRegion.TAIWAN -> "台北市"
            HolidayRegion.HONG_KONG -> "香港特别行政区"
            HolidayRegion.MACAO -> "澳门特别行政区"
            HolidayRegion.SINGAPORE -> "新加坡"
            HolidayRegion.JAPAN -> "东京都"
            HolidayRegion.SOUTH_KOREA -> "首尔"
            HolidayRegion.UNITED_STATES -> "纽约"
            HolidayRegion.UNITED_KINGDOM -> "伦敦"
            HolidayRegion.GERMANY -> "柏林"
            HolidayRegion.FRANCE -> "巴黎"
            HolidayRegion.ITALY -> "罗马"
            HolidayRegion.AUSTRALIA -> "悉尼"
            else -> region.nativeName
        }
    }

    fun getLocalizedCondition(condition: String, language: AppLanguage): String {
        return when (language) {
            AppLanguage.SIMPLIFIED_CHINESE -> condition
            AppLanguage.TRADITIONAL_CHINESE -> when (condition) {
                "多云" -> "多雲"; "晴朗" -> "晴朗"; "阴天" -> "陰天"; "小雨" -> "小雨"; "雷阵雨" -> "雷陣雨"; "阵雨" -> "陣雨"; else -> condition
            }
            AppLanguage.ENGLISH -> when (condition) {
                "多云", "晴间多云" -> "Partly Cloudy"
                "晴朗" -> "Sunny"
                "阴天", "阴" -> "Overcast"
                "小雨", "毛毛雨" -> "Light Rain"
                "阵雨" -> "Showers"
                "雷阵雨", "强雷雨" -> "Thunderstorm"
                "有雾" -> "Foggy"
                "小雪", "阵雪" -> "Light Snow"
                else -> "Cloudy"
            }
            AppLanguage.JAPANESE -> when (condition) {
                "多云", "晴间多云" -> "晴れ時々曇り"
                "晴朗" -> "快晴"
                "阴天", "阴" -> "曇り"
                "小雨", "毛毛雨" -> "小雨"
                "阵雨" -> "にわか雨"
                "雷阵雨", "强雷雨" -> "雷雨"
                "有雾" -> "霧"
                "小雪", "阵雪" -> "小雪"
                else -> "曇り"
            }
            AppLanguage.KOREAN -> when (condition) {
                "多云", "晴间多云" -> "구름많음"
                "晴朗" -> "맑음"
                "阴天", "阴" -> "흐림"
                "小雨", "毛毛雨" -> "약한 비"
                "阵雨" -> "소나기"
                "雷阵雨", "强雷雨" -> "뇌우"
                "有雾" -> "안개"
                "小雪", "阵雪" -> "약한 눈"
                else -> "구름많음"
            }
        }
    }

    fun getCityCoordinates(cityName: String): Pair<Double, Double> {
        return when {
            cityName.contains("深圳") -> Pair(22.5431, 114.0579)
            cityName.contains("北京") -> Pair(39.9042, 116.4074)
            cityName.contains("上海") -> Pair(31.2304, 121.4737)
            cityName.contains("广州") -> Pair(23.1291, 113.2644)
            cityName.contains("成都") -> Pair(30.5728, 104.0668)
            cityName.contains("杭州") -> Pair(30.2741, 120.1551)
            cityName.contains("武汉") -> Pair(30.5928, 114.3055)
            cityName.contains("南京") -> Pair(32.0603, 118.7969)
            cityName.contains("西安") -> Pair(34.3416, 108.9398)
            cityName.contains("台北") -> Pair(25.0330, 121.5654)
            cityName.contains("香港") -> Pair(22.3193, 114.1694)
            cityName.contains("澳门") -> Pair(22.1987, 113.5439)
            cityName.contains("新加坡") -> Pair(1.3521, 103.8198)
            cityName.contains("东京") -> Pair(35.6762, 139.6503)
            cityName.contains("首尔") -> Pair(37.5665, 126.9780)
            cityName.contains("伦敦") -> Pair(51.5074, -0.1278)
            cityName.contains("纽约") -> Pair(40.7128, -74.0060)
            else -> Pair(22.5431, 114.0579)
        }
    }

    suspend fun fetchRealLiveWeather(latitude: Double, longitude: Double): List<DailyWeather>? {
        return withContext(Dispatchers.IO) {
            try {
                val urlStr = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&daily=weathercode,temperature_2m_max,temperature_2m_min&timezone=Asia%2FShanghai"
                val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 3500
                    readTimeout = 3500
                    requestMethod = "GET"
                }

                if (conn.responseCode == 200) {
                    val jsonText = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(jsonText)
                    val daily = json.getJSONObject("daily")
                    val timeArray = daily.getJSONArray("time")
                    val maxTempArray = daily.getJSONArray("temperature_2m_max")
                    val minTempArray = daily.getJSONArray("temperature_2m_min")
                    val codeArray = daily.getJSONArray("weathercode")

                    val result = mutableListOf<DailyWeather>()
                    val today = LocalDate.now()

                    for (i in 0 until minOf(4, timeArray.length())) {
                        val date = today.plusDays(i.toLong())
                        val minTemp = minTempArray.getDouble(i).roundToInt()
                        val maxTemp = maxTempArray.getDouble(i).roundToInt()
                        val weatherCode = codeArray.getInt(i)

                        val (condition, emoji) = parseWmoCode(weatherCode)
                        val dayLabel = when (i) {
                            0 -> "今天"
                            1 -> "明天"
                            2 -> "后天"
                            else -> when (date.dayOfWeek.value) {
                                1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"; 5 -> "周五"; 6 -> "周六"; else -> "周日"
                            }
                        }

                        result.add(
                            DailyWeather(
                                date = date,
                                dayName = dayLabel,
                                condition = condition,
                                iconEmoji = emoji,
                                tempMin = minTemp,
                                tempMax = maxTemp,
                                wind = "微风 2级",
                                airQuality = "35 优"
                            )
                        )
                    }
                    if (result.isNotEmpty()) return@withContext result
                }
            } catch (_: Exception) {}
            null
        }
    }

    private fun parseWmoCode(code: Int): Pair<String, String> {
        return when (code) {
            0 -> Pair("晴朗", "☀️")
            1, 2 -> Pair("多云", "⛅")
            3 -> Pair("阴天", "☁️")
            45, 48 -> Pair("有雾", "🌫️")
            51, 53, 55 -> Pair("毛毛雨", "🌧️")
            56, 57 -> Pair("冻雨", "🌧️")
            61, 63, 65 -> Pair("阵雨", "🌧️")
            66, 67 -> Pair("冻阵雨", "🌧️")
            71, 73, 75 -> Pair("小雪", "🌨️")
            77 -> Pair("雪粒", "🌨️")
            80, 81, 82 -> Pair("雷阵雨", "⛈️")
            85, 86 -> Pair("阵雪", "🌨️")
            95, 96, 99 -> Pair("强雷雨", "⛈️")
            else -> Pair("多云", "⛅")
        }
    }

    fun getWeatherForecast(baseDate: LocalDate = LocalDate.now()): List<DailyWeather> {
        val weatherTypes = listOf(
            Triple("多云", "⛅", "东风 2级"),
            Triple("阴天", "☁️", "东北风 2级"),
            Triple("雷阵雨", "⛈️", "南风 3级"),
            Triple("小雨", "🌧️", "东南风 2级"),
            Triple("晴朗", "☀️", "微风 1级")
        )

        val aqiList = listOf("32 优", "42 优", "55 良")

        return (0..3).map { dayOffset ->
            val date = baseDate.plusDays(dayOffset.toLong())
            val epoch = date.toEpochDay()
            val hash = (epoch * 31 + dayOffset * 17).toInt()

            val typeIdx = abs(hash) % weatherTypes.size
            val weatherInfo = weatherTypes[typeIdx]
            val aqi = aqiList[abs(hash * 3) % aqiList.size]

            val tempMin = 25 + (abs(hash) % 3)
            val tempMax = 31 + (abs(hash) % 3)

            val dayLabel = when (dayOffset) {
                0 -> "今天"
                1 -> "明天"
                2 -> "后天"
                else -> when (date.dayOfWeek.value) {
                    1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"; 5 -> "周五"; 6 -> "周六"; else -> "周日"
                }
            }

            DailyWeather(
                date = date,
                dayName = dayLabel,
                condition = weatherInfo.first,
                iconEmoji = weatherInfo.second,
                tempMin = tempMin,
                tempMax = tempMax,
                wind = weatherInfo.third,
                airQuality = aqi
            )
        }
    }
}
