package me.paco.datecalculator.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import me.paco.datecalculator.data.HolidayRegion
import java.util.Locale

object LocationUtils {

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
