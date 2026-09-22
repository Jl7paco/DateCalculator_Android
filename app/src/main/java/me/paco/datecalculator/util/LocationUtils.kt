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

object LocationUtils {

    fun getCurrentCityName(context: Context): String {
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
                    val geocoder = Geocoder(context, Locale.CHINA)
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(bestLocation.latitude, bestLocation.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                        if (!city.isNullOrEmpty()) {
                            return city
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        val currentRegion = detectCurrentRegion(context)
        return WeatherUtils.getLocationName(currentRegion)
    }

    fun requestSingleLocationUpdate(context: Context, onCityUpdated: (String?) -> Unit) {
        try {
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (!hasFine && !hasCoarse) {
                onCityUpdated(null)
                return
            }

            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
            val provider = when {
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> null
            } ?: return

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
                                val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                                onCityUpdated(city)
                                return
                            }
                        } catch (_: Exception) {}
                        onCityUpdated(null)
                    }

                    @Deprecated("Deprecated in API 29")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                },
                Looper.getMainLooper()
            )
        } catch (_: Exception) {
            onCityUpdated(null)
        }
    }

    fun detectCurrentRegion(context: Context): HolidayRegion {
        // 1. 尝试从 TelephonyManager 获取 SIM 卡/基站网络国家码
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val rawCode = tm?.networkCountryIso ?: tm?.simCountryIso
            if (!rawCode.isNullOrEmpty()) {
                val matched = matchCountryCode(rawCode.uppercase())
                if (matched != null) return matched
            }
        } catch (_: Exception) {}

        // 2. 尝试从系统 GPS 地理位置获取
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
                    }
                }
            }
        } catch (_: Exception) {}

        // 3. 兜底策略：获取系统默认 Locale 地区
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
