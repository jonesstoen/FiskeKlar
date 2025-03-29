package no.uio.ifi.in2000.team46.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team46.R

@Composable
fun WeatherDisplay(
    temperature: Double?,
    symbolCode: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .padding(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            symbolCode?.let { code ->
                val iconRes = when {
                    // Klarvær
                    code.contains("clearsky_day") -> R.drawable.clearsky_day
                    code.contains("clearsky_night") -> R.drawable.clearsky_night
                    code.contains("clearsky_polartwilight") -> R.drawable.clearsky_polartwilight

                    // Lettskyet
                    code.contains("fair_day") -> R.drawable.fair_day
                    code.contains("fair_night") -> R.drawable.fair_night
                    code.contains("fair_polartwilight") -> R.drawable.fair_polartwilight

                    // Delvis skyet
                    code.contains("partlycloudy_day") -> R.drawable.partlycloudy_day
                    code.contains("partlycloudy_night") -> R.drawable.partlycloudy_night
                    code.contains("partlycloudy_polartwilight") -> R.drawable.partlycloudy_polartwilight

                    // Skyet
                    code.contains("cloudy") -> R.drawable.cloudy

                    // Regn
                    code.contains("lightrainshowers_day") -> R.drawable.lightrainshowers_day
                    code.contains("lightrainshowers_night") -> R.drawable.lightrainshowers_night
                    code.contains("lightrainshowers_polartwilight") -> R.drawable.lightrainshowers_polartwilight
                    code.contains("rainshowers_day") -> R.drawable.rainshowers_day
                    code.contains("rainshowers_night") -> R.drawable.rainshowers_night
                    code.contains("rainshowers_polartwilight") -> R.drawable.rainshowers_polartwilight
                    code.contains("heavyrainshowers_day") -> R.drawable.heavyrainshowers_day
                    code.contains("heavyrainshowers_night") -> R.drawable.heavyrainshowers_night
                    code.contains("heavyrainshowers_polartwilight") -> R.drawable.heavyrainshowers_polartwilight
                    code.contains("lightrain") -> R.drawable.lightrain
                    code.contains("rain") -> R.drawable.rain
                    code.contains("heavyrain") -> R.drawable.heavyrain

                    // Snø
                    code.contains("lightsnowshowers_day") -> R.drawable.lightsnowshowers_day
                    code.contains("lightsnowshowers_night") -> R.drawable.lightsnowshowers_night
                    code.contains("lightsnowshowers_polartwilight") -> R.drawable.lightsnowshowers_polartwilight
                    code.contains("snowshowers_day") -> R.drawable.snowshowers_day
                    code.contains("snowshowers_night") -> R.drawable.snowshowers_night
                    code.contains("snowshowers_polartwilight") -> R.drawable.snowshowers_polartwilight
                    code.contains("heavysnowshowers_day") -> R.drawable.heavysnowshowers_day
                    code.contains("heavysnowshowers_night") -> R.drawable.heavysnowshowers_night
                    code.contains("heavysnowshowers_polartwilight") -> R.drawable.heavysnowshowers_polartwilight
                    code.contains("lightsnow") -> R.drawable.lightsnow
                    code.contains("snow") -> R.drawable.snow
                    code.contains("heavysnow") -> R.drawable.heavysnow

                    // Sludd
                    code.contains("lightsleetshowers_day") -> R.drawable.lightsleetshowers_day
                    code.contains("lightsleetshowers_night") -> R.drawable.lightsleetshowers_night
                    code.contains("lightsleetshowers_polartwilight") -> R.drawable.lightsleetshowers_polartwilight
                    code.contains("sleetshowers_day") -> R.drawable.sleetshowers_day
                    code.contains("sleetshowers_night") -> R.drawable.sleetshowers_night
                    code.contains("sleetshowers_polartwilight") -> R.drawable.sleetshowers_polartwilight
                    code.contains("heavysleetshowers_day") -> R.drawable.heavysleetshowers_day
                    code.contains("heavysleetshowers_night") -> R.drawable.heavysleetshowers_night
                    code.contains("heavysleetshowers_polartwilight") -> R.drawable.heavysleetshowers_polartwilight
                    code.contains("lightsleet") -> R.drawable.lightsleet
                    code.contains("sleet") -> R.drawable.sleet
                    code.contains("heavysleet") -> R.drawable.heavysleet

                    // Tåke
                    code.contains("fog") -> R.drawable.fog

                    // Torden
                    code.contains("lightrainshowersandthunder_day") -> R.drawable.lightrainshowersandthunder_day
                    code.contains("lightrainshowersandthunder_night") -> R.drawable.lightrainshowersandthunder_night
                    code.contains("lightrainshowersandthunder_polartwilight") -> R.drawable.lightrainshowersandthunder_polartwilight
                    code.contains("rainshowersandthunder_day") -> R.drawable.rainshowersandthunder_day
                    code.contains("rainshowersandthunder_night") -> R.drawable.rainshowersandthunder_night
                    code.contains("rainshowersandthunder_polartwilight") -> R.drawable.rainshowersandthunder_polartwilight
                    code.contains("heavyrainshowersandthunder_day") -> R.drawable.heavyrainshowersandthunder_day
                    code.contains("heavyrainshowersandthunder_night") -> R.drawable.heavyrainshowersandthunder_night
                    code.contains("heavyrainshowersandthunder_polartwilight") -> R.drawable.heavyrainshowersandthunder_polartwilight
                    code.contains("lightrainandthunder") -> R.drawable.lightrainandthunder
                    code.contains("rainandthunder") -> R.drawable.rainandthunder
                    code.contains("heavyrainandthunder") -> R.drawable.heavyrainandthunder
                    code.contains("lightsleetshowersandthunder_day") -> R.drawable.lightssleetshowersandthunder_day
                    code.contains("lightsleetshowersandthunder_night") -> R.drawable.lightssleetshowersandthunder_night
                    code.contains("lightsleetshowersandthunder_polartwilight") -> R.drawable.lightssleetshowersandthunder_polartwilight
                    code.contains("sleetshowersandthunder_day") -> R.drawable.sleetshowersandthunder_day
                    code.contains("sleetshowersandthunder_night") -> R.drawable.sleetshowersandthunder_night
                    code.contains("sleetshowersandthunder_polartwilight") -> R.drawable.sleetshowersandthunder_polartwilight
                    code.contains("heavysleetshowersandthunder_day") -> R.drawable.heavysleetshowersandthunder_day
                    code.contains("heavysleetshowersandthunder_night") -> R.drawable.heavysleetshowersandthunder_night
                    code.contains("heavysleetshowersandthunder_polartwilight") -> R.drawable.heavysleetshowersandthunder_polartwilight
                    code.contains("lightsleetandthunder") -> R.drawable.lightsleetandthunder
                    code.contains("sleetandthunder") -> R.drawable.sleetandthunder
                    code.contains("heavysleetandthunder") -> R.drawable.heavysleetandthunder
                    code.contains("lightsnowshowersandthunder_day") -> R.drawable.lightssnowshowersandthunder_day
                    code.contains("lightsnowshowersandthunder_night") -> R.drawable.lightssnowshowersandthunder_night
                    code.contains("lightsnowshowersandthunder_polartwilight") -> R.drawable.lightssnowshowersandthunder_polartwilight
                    code.contains("snowshowersandthunder_day") -> R.drawable.snowshowersandthunder_day
                    code.contains("snowshowersandthunder_night") -> R.drawable.snowshowersandthunder_night
                    code.contains("snowshowersandthunder_polartwilight") -> R.drawable.snowshowersandthunder_polartwilight
                    code.contains("heavysnowshowersandthunder_day") -> R.drawable.heavysnowshowersandthunder_day
                    code.contains("heavysnowshowersandthunder_night") -> R.drawable.heavysnowshowersandthunder_night
                    code.contains("heavysnowshowersandthunder_polartwilight") -> R.drawable.heavysnowshowersandthunder_polartwilight
                    code.contains("lightsnowandthunder") -> R.drawable.lightsnowandthunder
                    code.contains("snowandthunder") -> R.drawable.snowandthunder
                    code.contains("heavysnowandthunder") -> R.drawable.heavysnowandthunder

                    else -> null
                }

                iconRes?.let {
                    Icon(
                        painter = painterResource(id = it),
                        contentDescription = "Weather icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                }
            }

            Text(
                text = if (temperature != null) "${temperature.toInt()}°" else "--°",
                color = Color.Black
            )
        }
    }
}