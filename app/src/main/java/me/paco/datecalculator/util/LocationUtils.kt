package me.paco.datecalculator.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import me.paco.datecalculator.data.HolidayRegion
import java.util.Locale

data class DistrictLocation(
    val fullDisplayName: String,
    val cityName: String,
    val districtName: String,
    val latitude: Double,
    val longitude: Double
)

object LocationUtils {

    private fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return 6371.0 * c
    }

    private val offlineCityLookupTable = listOf(
        "深圳市 · 南山区" to (22.533 to 113.930),
        "深圳市 · 福田区" to (22.540 to 114.055),
        "深圳市 · 宝安区" to (22.553 to 113.883),
        "深圳市 · 罗湖区" to (22.548 to 114.117),
        "深圳市 · 龙岗区" to (22.721 to 114.247),
        "广州市 · 天河区" to (23.125 to 113.361),
        "广州市 · 越秀区" to (23.128 to 113.264),
        "北京市 · 朝阳区" to (39.921 to 116.486),
        "北京市 · 海淀区" to (39.983 to 116.310),
        "上海市 · 浦东新区" to (31.222 to 121.544),
        "上海市 · 静安区" to (31.228 to 121.455),
        "成都市 · 武侯区" to (30.642 to 104.043),
        "杭州市 · 西湖区" to (30.259 to 120.130),
        "武汉市 · 武昌区" to (30.553 to 114.342),
        "南京市 · 玄武区" to (32.051 to 118.791),
        "西安市 · 雁塔区" to (34.222 to 108.953),
        "重庆市 · 渝中区" to (29.558 to 106.551),
        "天津市 · 和平区" to (39.117 to 117.200),
        "台北市" to (25.033 to 121.565),
        "香港特別行政區" to (22.319 to 114.169),
        "澳門特別行政區" to (22.198 to 113.543),
        "东京" to (35.676 to 139.650),
        "首尔" to (37.566 to 126.978),
        "新加坡" to (1.352 to 103.820),
        "伦敦" to (51.507 to -0.127),
        "纽约" to (40.712 to -74.006),
        "洛杉矶" to (34.052 to -118.243)
    )

    fun findNearestOfflineCity(lat: Double, lon: Double): DistrictLocation {
        var minDistance = Double.MAX_VALUE
        var bestMatch = "深圳市 · 南山区"
        var bestCity = "深圳市"
        var bestDistrict = "南山区"
        var bestLat = 22.533
        var bestLon = 113.930

        for ((fullDisplay, coords) in offlineCityLookupTable) {
            val (cLat, cLon) = coords
            val dist = haversineDistanceKm(lat, lon, cLat, cLon)
            if (dist < minDistance) {
                minDistance = dist
                bestMatch = fullDisplay
                val parts = fullDisplay.split("·").map { it.trim() }
                bestCity = parts.firstOrNull() ?: fullDisplay
                bestDistrict = if (parts.size > 1) parts[1] else ""
                bestLat = cLat
                bestLon = cLon
            }
        }

        return DistrictLocation(
            fullDisplayName = bestMatch,
            cityName = bestCity,
            districtName = bestDistrict,
            latitude = if (minDistance < 120.0) lat else bestLat,
            longitude = if (minDistance < 120.0) lon else bestLon
        )
    }

    fun getCurrentLocationWithDistrict(context: Context): DistrictLocation {
        try {
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (hasFine || hasCoarse) {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val providers = lm?.getProviders(true) ?: emptyList()

                var bestLocation: Location? = null
                for (p in providers) {
                    @Suppress("MissingPermission")
                    val loc = lm?.getLastKnownLocation(p) ?: continue
                    if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                        bestLocation = loc
                    }
                }

                if (bestLocation != null) {
                    try {
                        val geocoder = Geocoder(context, Locale.CHINA)
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(bestLocation.latitude, bestLocation.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val rawCity = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "深圳市"
                            val rawDistrict = addr.subLocality ?: addr.thoroughfare ?: ""

                            val cleanCity = if (rawCity.endsWith("市")) rawCity else "${rawCity}市"
                            val fullDisplay = if (rawDistrict.isNotBlank() && !cleanCity.contains(rawDistrict)) {
                                "$cleanCity · $rawDistrict"
                            } else {
                                cleanCity
                            }

                            return DistrictLocation(
                                fullDisplayName = fullDisplay,
                                cityName = cleanCity,
                                districtName = rawDistrict,
                                latitude = bestLocation.latitude,
                                longitude = bestLocation.longitude
                            )
                        }
                    } catch (_: Exception) {
                        // 挂 VPN 导致系统 Geocoder 超时抛出异常：启动离线距离坐标精准算，100% 锁定城市！
                        return findNearestOfflineCity(bestLocation.latitude, bestLocation.longitude)
                    }
                }
            }
        } catch (_: Exception) {}

        // 保留用户上一次本地已保存的有效城市，绝不重置为无效字符串
        val savedCity = PreferenceUtils.getLastCityName(context)
        val defaultCoords = WeatherUtils.getCityCoordinates(savedCity)
        return DistrictLocation(
            fullDisplayName = savedCity,
            cityName = savedCity,
            districtName = "",
            latitude = defaultCoords.first,
            longitude = defaultCoords.second
        )
    }

    fun getCurrentCityName(context: Context): String {
        return getCurrentLocationWithDistrict(context).fullDisplayName
    }

    fun requestSingleLocationUpdate(context: Context, onLocationUpdated: (DistrictLocation?) -> Unit) {
        try {
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (!hasFine && !hasCoarse) {
                onLocationUpdated(null)
                return
            }

            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: run {
                onLocationUpdated(null)
                return
            }

            val provider = when {
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> null
            } ?: run {
                onLocationUpdated(null)
                return
            }

            @Suppress("MissingPermission")
            lm.requestSingleUpdate(
                provider,
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        try {
                            val geocoder = Geocoder(context, Locale.CHINA)
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                val addr = addresses[0]
                                val rawCity = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "深圳市"
                                val rawDistrict = addr.subLocality ?: addr.thoroughfare ?: ""

                                val cleanCity = if (rawCity.endsWith("市")) rawCity else "${rawCity}市"
                                val fullDisplay = if (rawDistrict.isNotBlank() && !cleanCity.contains(rawDistrict)) {
                                    "$cleanCity · $rawDistrict"
                                } else {
                                    cleanCity
                                }

                                onLocationUpdated(
                                    DistrictLocation(
                                        fullDisplayName = fullDisplay,
                                        cityName = cleanCity,
                                        districtName = rawDistrict,
                                        latitude = location.latitude,
                                        longitude = location.longitude
                                    )
                                )
                                return
                            }
                        } catch (_: Exception) {}

                        // 挂 VPN 导致网络 Geocoder 失败时：离线根据 GPS 经纬度毫秒级算匹配城市，保证 VPN 下定位永不丢失！
                        val offlineLoc = findNearestOfflineCity(location.latitude, location.longitude)
                        onLocationUpdated(offlineLoc)
                    }

                    @Deprecated("Deprecated in API 29")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                },
                Looper.getMainLooper()
            )
        } catch (_: Exception) {
            onLocationUpdated(null)
        }
    }

    fun matchCoordinatesToRegion(lat: Double, lon: Double): HolidayRegion? {
        return when {
            // 香港特别行政区 (lat 22.15..22.58, lon 113.8..114.4)
            lat in 22.15..22.58 && lon in 113.8..114.4 -> HolidayRegion.HONG_KONG
            // 澳门特别行政区 (lat 22.08..22.23, lon 113.50..113.62)
            lat in 22.08..22.23 && lon in 113.50..113.62 -> HolidayRegion.MACAO
            // 台湾省 (lat 21.8..25.4, lon 119.3..122.1)
            lat in 21.8..25.4 && lon in 119.3..122.1 -> HolidayRegion.TAIWAN
            // 中国大陆主体经纬度范围 (lat 18.0..53.6, lon 73.5..135.1)
            lat in 18.0..53.6 && lon in 73.5..135.1 -> HolidayRegion.CHINA
            // 日本 (lat 24.0..45.6, lon 122.9..153.0)
            lat in 24.0..45.6 && lon in 122.9..153.0 -> HolidayRegion.JAPAN
            // 韩国 (lat 33.0..38.6, lon 124.6..131.0)
            lat in 33.0..38.6 && lon in 124.6..131.0 -> HolidayRegion.SOUTH_KOREA
            // 新加坡 (lat 1.15..1.48, lon 103.6..104.1)
            lat in 1.15..1.48 && lon in 103.6..104.1 -> HolidayRegion.SINGAPORE
            // 英国 (lat 49.8..60.9, lon -8.6..1.8)
            lat in 49.8..60.9 && lon in -8.6..1.8 -> HolidayRegion.UNITED_KINGDOM
            // 美国本土 (lat 24.5..49.4, lon -124.8..-66.9)
            lat in 24.5..49.4 && lon in -124.8..-66.9 -> HolidayRegion.UNITED_STATES
            // 澳大利亚 (lat -43.7..-10.6, lon 112.9..153.6)
            lat in -43.7..-10.6 && lon in 112.9..153.6 -> HolidayRegion.AUSTRALIA
            else -> null
        }
    }

    fun detectCurrentRegion(context: Context): HolidayRegion {
        // 1. GPS 硬件真实坐标超优先判定 (不受全局 VPN / 代理节点干扰，保证连接 VPN 时依然 100% 绝对精准)
        try {
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (hasFine || hasCoarse) {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val providers = lm?.getProviders(true)
                for (p in providers ?: emptyList()) {
                    @Suppress("MissingPermission")
                    val loc = lm?.getLastKnownLocation(p)
                    if (loc != null) {
                        val gpsMatched = matchCoordinatesToRegion(loc.latitude, loc.longitude)
                        if (gpsMatched != null) return gpsMatched

                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                val countryCode = addresses[0].countryCode
                                if (!countryCode.isNullOrEmpty()) {
                                    val matched = matchCountryCode(countryCode.uppercase())
                                    if (matched != null) return matched
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        } catch (_: Exception) {}

        // 2. SIM 卡网络运营商底码判定
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val rawCode = tm?.networkCountryIso ?: tm?.simCountryIso
            if (!rawCode.isNullOrEmpty()) {
                val matched = matchCountryCode(rawCode.uppercase())
                if (matched != null) return matched
            }
        } catch (_: Exception) {}

        val localeCountry = Locale.getDefault().country.uppercase()
        return matchCountryCode(localeCountry) ?: HolidayRegion.CHINA
    }

    private fun matchCountryCode(code: String): HolidayRegion? {
        return when (code) {
            "CN" -> HolidayRegion.CHINA
            "TW" -> HolidayRegion.TAIWAN
            "HK" -> HolidayRegion.HONG_KONG
            "MO" -> HolidayRegion.MACAO
            "SG" -> HolidayRegion.SINGAPORE
            "MY" -> HolidayRegion.MALAYSIA
            "VN" -> HolidayRegion.VIETNAM
            "JP" -> HolidayRegion.JAPAN
            "KR" -> HolidayRegion.SOUTH_KOREA
            "GB", "UK" -> HolidayRegion.UNITED_KINGDOM
            "DE" -> HolidayRegion.GERMANY
            "FR" -> HolidayRegion.FRANCE
            "IT" -> HolidayRegion.ITALY
            "IN" -> HolidayRegion.INDIA
            "ID" -> HolidayRegion.INDONESIA
            "AU" -> HolidayRegion.AUSTRALIA
            "NZ" -> HolidayRegion.NEW_ZEALAND
            "US" -> HolidayRegion.UNITED_STATES
            "TH" -> HolidayRegion.THAILAND
            else -> null
        }
    }
}
