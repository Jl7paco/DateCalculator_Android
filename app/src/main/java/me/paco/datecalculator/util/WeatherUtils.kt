package me.paco.datecalculator.util

import me.paco.datecalculator.data.AppLanguage
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import kotlin.math.abs

data class DailyWeather(
    val dayName: String,
    val iconEmoji: String,
    val condition: String,
    val tempMin: Int,
    val tempMax: Int
)

data class RealTimeWeatherData(
    val currentTemp: Int,
    val currentFeelsLikeTemp: Int,
    val dailyList: List<DailyWeather>
)

object WeatherUtils {

    fun getCityCoordinates(cityName: String): Pair<Double, Double> {
        val clean = cityName.replace("市", "").replace("特别行政区", "").trim()
        return when {
            clean.contains("北京") -> Pair(39.9042, 116.4074)
            clean.contains("上海") -> Pair(31.2304, 121.4737)
            clean.contains("广州") -> Pair(23.1291, 113.2644)
            clean.contains("深圳") -> Pair(22.5431, 114.0579)
            clean.contains("杭州") -> Pair(30.2741, 120.1551)
            clean.contains("成都") -> Pair(30.5728, 104.0668)
            clean.contains("武汉") -> Pair(30.5928, 114.3055)
            clean.contains("南京") -> Pair(32.0603, 118.7969)
            clean.contains("重庆") -> Pair(29.5630, 106.5516)
            clean.contains("天津") -> Pair(39.3434, 117.3616)
            clean.contains("西安") -> Pair(34.3416, 108.9398)
            clean.contains("台北") -> Pair(25.0330, 121.5654)
            clean.contains("香港") -> Pair(22.3193, 114.1694)
            clean.contains("澳门") -> Pair(22.1987, 113.5439)
            clean.contains("新加坡") -> Pair(1.3521, 103.8198)
            clean.contains("东京") -> Pair(35.6762, 139.6503)
            clean.contains("首尔") -> Pair(37.5665, 126.9780)
            clean.contains("伦敦") -> Pair(51.5074, -0.1278)
            clean.contains("纽约") -> Pair(40.7128, -74.0060)
            else -> Pair(22.5431, 114.0579)
        }
    }

    fun getEnglishCityName(cityName: String): String {
        return when {
            cityName.contains("北京") -> "Beijing"
            cityName.contains("上海") -> "Shanghai"
            cityName.contains("广州") -> "Guangzhou"
            cityName.contains("深圳") -> "Shenzhen"
            cityName.contains("杭州") -> "Hangzhou"
            cityName.contains("成都") -> "Chengdu"
            cityName.contains("武汉") -> "Wuhan"
            cityName.contains("南京") -> "Nanjing"
            cityName.contains("重庆") -> "Chongqing"
            cityName.contains("天津") -> "Tianjin"
            cityName.contains("西安") -> "Xi'an"
            cityName.contains("台北") -> "Taipei"
            cityName.contains("香港") -> "Hong Kong"
            cityName.contains("澳门") -> "Macau"
            cityName.contains("新加坡") -> "Singapore"
            cityName.contains("东京") -> "Tokyo"
            cityName.contains("首尔") -> "Seoul"
            cityName.contains("伦敦") -> "London"
            cityName.contains("纽约") -> "New York"
            else -> cityName
        }
    }

    fun getLocalizedCondition(condition: String, language: AppLanguage): String {
        return when (language.getEffectiveLanguage()) {
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
                "小雪", "阵雪" -> "粉雪"
                else -> "曇り"
            }
            AppLanguage.KOREAN -> when (condition) {
                "多云", "晴间多云" -> "구름조금"
                "晴朗" -> "맑음"
                "阴天", "阴" -> "흐림"
                "小雨", "毛毛雨" -> "가랑비"
                "阵雨" -> "소나기"
                "雷阵雨", "强雷雨" -> "뇌우"
                "有雾" -> "안개"
                "小雪", "阵雪" -> "함박눈"
                else -> "구름많음"
            }
            else -> condition
        }
    }

    /**
     * 实时抓取 Open-Meteo 当前温度、当前体感温度与未来 4 天每日预报
     */
    fun fetchRealLiveWeather(lat: Double, lon: Double): RealTimeWeatherData? {
        return try {
            val urlStr = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,apparent_temperature&daily=weathercode,temperature_2m_max,temperature_2m_min&timezone=auto"
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 3000
            conn.readTimeout = 3000

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                val current = json.optJSONObject("current")
                val curTemp = current?.optDouble("temperature_2m")?.toInt() ?: 25
                val curFeels = current?.optDouble("apparent_temperature")?.toInt() ?: (curTemp + 2)

                val daily = json.getJSONObject("daily")
                val codes = daily.getJSONArray("weathercode")
                val maxTemps = daily.getJSONArray("temperature_2m_max")
                val minTemps = daily.getJSONArray("temperature_2m_min")

                val dayNames = listOf("今天", "明天", "后天", "大后天")
                val weatherList = mutableListOf<DailyWeather>()

                for (i in 0 until 4.coerceAtMost(codes.length())) {
                    val code = codes.getInt(i)
                    val maxT = maxTemps.getDouble(i).toInt()
                    val minT = minTemps.getDouble(i).toInt()
                    val (icon, cond) = parseWmoWeatherCode(code)

                    weatherList.add(
                        DailyWeather(
                            dayName = dayNames.getOrElse(i) { "第${i + 1}天" },
                            iconEmoji = icon,
                            condition = cond,
                            tempMin = minT,
                            tempMax = maxT
                        )
                    )
                }
                RealTimeWeatherData(
                    currentTemp = curTemp,
                    currentFeelsLikeTemp = curFeels,
                    dailyList = weatherList
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun parseWmoWeatherCode(code: Int): Pair<String, String> {
        return when (code) {
            0 -> Pair("☀️", "晴朗")
            1, 2, 3 -> Pair("☁️", "多云")
            45, 48 -> Pair("🌫️", "有雾")
            51, 53, 55 -> Pair("🌧️", "毛毛雨")
            61, 63, 65 -> Pair("🌧️", "小雨")
            80, 81, 82 -> Pair("🌧️", "阵雨")
            95, 96, 99 -> Pair("⛈️", "雷阵雨")
            71, 73, 75, 85, 86 -> Pair("❄️", "小雪")
            else -> Pair("🌤️", "晴间多云")
        }
    }

    /**
     * 根据离线离散数学算式推算 4 天天气模拟预报
     */
    fun getWeatherForecast(date: LocalDate): RealTimeWeatherData {
        val baseSeed = date.toEpochDay().toInt()
        val tempBase = 22 + (abs(baseSeed) % 8)

        val dayNames = listOf("今天", "明天", "后天", "大后天")
        val conditions = listOf(
            Pair("☀️", "晴朗"),
            Pair("☁️", "阴天"),
            Pair("🌧️", "小雨"),
            Pair("⛈️", "雷阵雨")
        )

        val result = mutableListOf<DailyWeather>()
        for (i in 0 until 4) {
            val condIdx = abs(baseSeed * 13 + i * 17) % conditions.size
            val (icon, name) = conditions[condIdx]
            val minT = tempBase + (i % 2)
            val maxT = minT + 6 + (abs(baseSeed + i) % 4)

            result.add(
                DailyWeather(
                    dayName = dayNames[i],
                    iconEmoji = icon,
                    condition = name,
                    tempMin = minT,
                    tempMax = maxT
                )
            )
        }
        return RealTimeWeatherData(
            currentTemp = tempBase + 3,
            currentFeelsLikeTemp = tempBase + 5,
            dailyList = result
        )
    }
}
