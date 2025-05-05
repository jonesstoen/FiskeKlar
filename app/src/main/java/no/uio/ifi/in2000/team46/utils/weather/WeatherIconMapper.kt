package no.uio.ifi.in2000.team46.utils.weather

import no.uio.ifi.in2000.team46.R

/**
 * Utility-klasse for å mappe værkoder til ikoner
 */
object WeatherIconMapper {
    fun getWeatherIcon(symbolCode: String): Int? {
        return when {
            // Klarvær
            symbolCode.contains("clearsky_day") -> R.drawable.clearsky_day
            symbolCode.contains("clearsky_night") -> R.drawable.clearsky_night
            symbolCode.contains("clearsky_polartwilight") -> R.drawable.clearsky_polartwilight

            // Lettskyet
            symbolCode.contains("fair_day") -> R.drawable.fair_day
            symbolCode.contains("fair_night") -> R.drawable.fair_night
            symbolCode.contains("fair_polartwilight") -> R.drawable.fair_polartwilight

            // Delvis skyet
            symbolCode.contains("partlycloudy_day") -> R.drawable.partlycloudy_day
            symbolCode.contains("partlycloudy_night") -> R.drawable.partlycloudy_night
            symbolCode.contains("partlycloudy_polartwilight") -> R.drawable.partlycloudy_polartwilight

            // Skyet
            symbolCode.contains("cloudy") -> R.drawable.cloudy

            // Regn
            symbolCode.contains("lightrainshowers_day") -> R.drawable.lightrainshowers_day
            symbolCode.contains("lightrainshowers_night") -> R.drawable.lightrainshowers_night
            symbolCode.contains("lightrainshowers_polartwilight") -> R.drawable.lightrainshowers_polartwilight
            symbolCode.contains("rainshowers_day") -> R.drawable.rainshowers_day
            symbolCode.contains("rainshowers_night") -> R.drawable.rainshowers_night
            symbolCode.contains("rainshowers_polartwilight") -> R.drawable.rainshowers_polartwilight
            symbolCode.contains("heavyrainshowers_day") -> R.drawable.heavyrainshowers_day
            symbolCode.contains("heavyrainshowers_night") -> R.drawable.heavyrainshowers_night
            symbolCode.contains("heavyrainshowers_polartwilight") -> R.drawable.heavyrainshowers_polartwilight
            symbolCode.contains("lightrain") -> R.drawable.lightrain
            symbolCode.contains("rain") -> R.drawable.rain
            symbolCode.contains("heavyrain") -> R.drawable.heavyrain

            // Snø
            symbolCode.contains("lightsnowshowers_day") -> R.drawable.lightsnowshowers_day
            symbolCode.contains("lightsnowshowers_night") -> R.drawable.lightsnowshowers_night
            symbolCode.contains("lightsnowshowers_polartwilight") -> R.drawable.lightsnowshowers_polartwilight
            symbolCode.contains("snowshowers_day") -> R.drawable.snowshowers_day
            symbolCode.contains("snowshowers_night") -> R.drawable.snowshowers_night
            symbolCode.contains("snowshowers_polartwilight") -> R.drawable.snowshowers_polartwilight
            symbolCode.contains("heavysnowshowers_day") -> R.drawable.heavysnowshowers_day
            symbolCode.contains("heavysnowshowers_night") -> R.drawable.heavysnowshowers_night
            symbolCode.contains("heavysnowshowers_polartwilight") -> R.drawable.heavysnowshowers_polartwilight
            symbolCode.contains("lightsnow") -> R.drawable.lightsnow
            symbolCode.contains("snow") -> R.drawable.snow
            symbolCode.contains("heavysnow") -> R.drawable.heavysnow

            // Sludd
            symbolCode.contains("lightsleetshowers_day") -> R.drawable.lightsleetshowers_day
            symbolCode.contains("lightsleetshowers_night") -> R.drawable.lightsleetshowers_night
            symbolCode.contains("lightsleetshowers_polartwilight") -> R.drawable.lightsleetshowers_polartwilight
            symbolCode.contains("sleetshowers_day") -> R.drawable.sleetshowers_day
            symbolCode.contains("sleetshowers_night") -> R.drawable.sleetshowers_night
            symbolCode.contains("sleetshowers_polartwilight") -> R.drawable.sleetshowers_polartwilight
            symbolCode.contains("heavysleetshowers_day") -> R.drawable.heavysleetshowers_day
            symbolCode.contains("heavysleetshowers_night") -> R.drawable.heavysleetshowers_night
            symbolCode.contains("heavysleetshowers_polartwilight") -> R.drawable.heavysleetshowers_polartwilight
            symbolCode.contains("lightsleet") -> R.drawable.lightsleet
            symbolCode.contains("sleet") -> R.drawable.sleet
            symbolCode.contains("heavysleet") -> R.drawable.heavysleet

            // Tåke
            symbolCode.contains("fog") -> R.drawable.fog

            // Torden
            symbolCode.contains("lightrainshowersandthunder_day") -> R.drawable.lightrainshowersandthunder_day
            symbolCode.contains("lightrainshowersandthunder_night") -> R.drawable.lightrainshowersandthunder_night
            symbolCode.contains("lightrainshowersandthunder_polartwilight") -> R.drawable.lightrainshowersandthunder_polartwilight
            symbolCode.contains("rainshowersandthunder_day") -> R.drawable.rainshowersandthunder_day
            symbolCode.contains("rainshowersandthunder_night") -> R.drawable.rainshowersandthunder_night
            symbolCode.contains("rainshowersandthunder_polartwilight") -> R.drawable.rainshowersandthunder_polartwilight
            symbolCode.contains("heavyrainshowersandthunder_day") -> R.drawable.heavyrainshowersandthunder_day
            symbolCode.contains("heavyrainshowersandthunder_night") -> R.drawable.heavyrainshowersandthunder_night
            symbolCode.contains("heavyrainshowersandthunder_polartwilight") -> R.drawable.heavyrainshowersandthunder_polartwilight
            symbolCode.contains("lightrainandthunder") -> R.drawable.lightrainandthunder
            symbolCode.contains("rainandthunder") -> R.drawable.rainandthunder
            symbolCode.contains("heavyrainandthunder") -> R.drawable.heavyrainandthunder
            symbolCode.contains("lightsleetshowersandthunder_day") -> R.drawable.lightssleetshowersandthunder_day
            symbolCode.contains("lightsleetshowersandthunder_night") -> R.drawable.lightssleetshowersandthunder_night
            symbolCode.contains("lightsleetshowersandthunder_polartwilight") -> R.drawable.lightssleetshowersandthunder_polartwilight
            symbolCode.contains("sleetshowersandthunder_day") -> R.drawable.sleetshowersandthunder_day
            symbolCode.contains("sleetshowersandthunder_night") -> R.drawable.sleetshowersandthunder_night
            symbolCode.contains("sleetshowersandthunder_polartwilight") -> R.drawable.sleetshowersandthunder_polartwilight
            symbolCode.contains("heavysleetshowersandthunder_day") -> R.drawable.heavysleetshowersandthunder_day
            symbolCode.contains("heavysleetshowersandthunder_night") -> R.drawable.heavysleetshowersandthunder_night
            symbolCode.contains("heavysleetshowersandthunder_polartwilight") -> R.drawable.heavysleetshowersandthunder_polartwilight
            symbolCode.contains("lightsleetandthunder") -> R.drawable.lightsleetandthunder
            symbolCode.contains("sleetandthunder") -> R.drawable.sleetandthunder
            symbolCode.contains("heavysleetandthunder") -> R.drawable.heavysleetandthunder
            symbolCode.contains("lightsnowshowersandthunder_day") -> R.drawable.lightssnowshowersandthunder_day
            symbolCode.contains("lightsnowshowersandthunder_night") -> R.drawable.lightssnowshowersandthunder_night
            symbolCode.contains("lightsnowshowersandthunder_polartwilight") -> R.drawable.lightssnowshowersandthunder_polartwilight
            symbolCode.contains("snowshowersandthunder_day") -> R.drawable.snowshowersandthunder_day
            symbolCode.contains("snowshowersandthunder_night") -> R.drawable.snowshowersandthunder_night
            symbolCode.contains("snowshowersandthunder_polartwilight") -> R.drawable.snowshowersandthunder_polartwilight
            symbolCode.contains("heavysnowshowersandthunder_day") -> R.drawable.heavysnowshowersandthunder_day
            symbolCode.contains("heavysnowshowersandthunder_night") -> R.drawable.heavysnowshowersandthunder_night
            symbolCode.contains("heavysnowshowersandthunder_polartwilight") -> R.drawable.heavysnowshowersandthunder_polartwilight
            symbolCode.contains("lightsnowandthunder") -> R.drawable.lightsnowandthunder
            symbolCode.contains("snowandthunder") -> R.drawable.snowandthunder
            symbolCode.contains("heavysnowandthunder") -> R.drawable.heavysnowandthunder

            else -> null
        }
    }
} 