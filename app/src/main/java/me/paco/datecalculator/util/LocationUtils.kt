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
                }
            }
        } catch (_: Exception) {}

        val currentRegion = detectCurrentRegion(context)
        val defaultCity = currentRegion.nativeName
        val defaultCoords = WeatherUtils.getCityCoordinates(defaultCity)
        return DistrictLocation(
            fullDisplayName = defaultCity,
            cityName = defaultCity,
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
                        onLocationUpdated(null)
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

    fun detectCurrentRegion(context: Context): HolidayRegion {
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val rawCode = tm?.networkCountryIso ?: tm?.simCountryIso
            if (!rawCode.isNullOrEmpty()) {
                val matched = matchCountryCode(rawCode.uppercase())
                if (matched != null) return matched
            }
        } catch (_: Exception) {}

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
