package no.uio.ifi.in2000.team46.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import no.uio.ifi.in2000.team46.domain.model.weather.WeatherData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDetailScreen(
    navController: NavController,
    weatherData: WeatherData,
    locationName: String,
    feelsLike: Double,
    highTemp: Double,
    lowTemp: Double,
    weatherDescription: String,
    hasWarning: Boolean = false
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(locationName) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Gå tilbake")
                    }
                },
                actions = {
                    if (hasWarning) {
                        Icon(
                            painter = painterResource(id = no.uio.ifi.in2000.team46.R.drawable.icon_warning_generic_yellow_png),
                            contentDescription = "Værvarsel",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main temperature and icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                weatherData.symbolCode?.let { code ->
                    val iconRes = getWeatherIcon(code)
                    iconRes?.let {
                        Icon(
                            painter = painterResource(id = it),
                            contentDescription = "Værikon",
                            modifier = Modifier.size(64.dp),
                            tint = Color.Unspecified
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "${weatherData.temperature?.toInt()}°",
                    fontSize = 48.sp
                )
            }

            // Weather description
            Text(
                text = weatherDescription,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Weather details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WeatherDetailRow("Føles som", "${feelsLike.toInt()}°")
                WeatherDetailRow("Høyeste temperatur", "${highTemp.toInt()}°")
                WeatherDetailRow("Laveste temperatur", "${lowTemp.toInt()}°")
            }
        }
    }
}

@Composable
private fun WeatherDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label)
        Text(text = value)
    }
}

private fun getWeatherIcon(symbolCode: String): Int? {
    return when {
        // Clear sky
        symbolCode.contains("clearsky_day") -> no.uio.ifi.in2000.team46.R.drawable.clearsky_day
        symbolCode.contains("clearsky_night") -> no.uio.ifi.in2000.team46.R.drawable.clearsky_night
        symbolCode.contains("clearsky_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.clearsky_polartwilight

        // light cloudy
        symbolCode.contains("fair_day") -> no.uio.ifi.in2000.team46.R.drawable.fair_day
        symbolCode.contains("fair_night") -> no.uio.ifi.in2000.team46.R.drawable.fair_night
        symbolCode.contains("fair_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.fair_polartwilight

        // Partly cloudy
        symbolCode.contains("partlycloudy_day") -> no.uio.ifi.in2000.team46.R.drawable.partlycloudy_day
        symbolCode.contains("partlycloudy_night") -> no.uio.ifi.in2000.team46.R.drawable.partlycloudy_night
        symbolCode.contains("partlycloudy_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.partlycloudy_polartwilight

        // Cloudy
        symbolCode.contains("cloudy") -> no.uio.ifi.in2000.team46.R.drawable.cloudy

        // Rain
        symbolCode.contains("lightrainshowers_day") -> no.uio.ifi.in2000.team46.R.drawable.lightrainshowers_day
        symbolCode.contains("lightrainshowers_night") -> no.uio.ifi.in2000.team46.R.drawable.lightrainshowers_night
        symbolCode.contains("lightrainshowers_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.lightrainshowers_polartwilight
        symbolCode.contains("rainshowers_day") -> no.uio.ifi.in2000.team46.R.drawable.rainshowers_day
        symbolCode.contains("rainshowers_night") -> no.uio.ifi.in2000.team46.R.drawable.rainshowers_night
        symbolCode.contains("rainshowers_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.rainshowers_polartwilight
        symbolCode.contains("heavyrainshowers_day") -> no.uio.ifi.in2000.team46.R.drawable.heavyrainshowers_day
        symbolCode.contains("heavyrainshowers_night") -> no.uio.ifi.in2000.team46.R.drawable.heavyrainshowers_night
        symbolCode.contains("heavyrainshowers_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.heavyrainshowers_polartwilight
        symbolCode.contains("lightrain") -> no.uio.ifi.in2000.team46.R.drawable.lightrain
        symbolCode.contains("rain") -> no.uio.ifi.in2000.team46.R.drawable.rain
        symbolCode.contains("heavyrain") -> no.uio.ifi.in2000.team46.R.drawable.heavyrain

        // Snow
        symbolCode.contains("lightsnowshowers_day") -> no.uio.ifi.in2000.team46.R.drawable.lightsnowshowers_day
        symbolCode.contains("lightsnowshowers_night") -> no.uio.ifi.in2000.team46.R.drawable.lightsnowshowers_night
        symbolCode.contains("lightsnowshowers_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.lightsnowshowers_polartwilight
        symbolCode.contains("snowshowers_day") -> no.uio.ifi.in2000.team46.R.drawable.snowshowers_day
        symbolCode.contains("snowshowers_night") -> no.uio.ifi.in2000.team46.R.drawable.snowshowers_night
        symbolCode.contains("snowshowers_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.snowshowers_polartwilight
        symbolCode.contains("heavysnowshowers_day") -> no.uio.ifi.in2000.team46.R.drawable.heavysnowshowers_day
        symbolCode.contains("heavysnowshowers_night") -> no.uio.ifi.in2000.team46.R.drawable.heavysnowshowers_night
        symbolCode.contains("heavysnowshowers_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.heavysnowshowers_polartwilight
        symbolCode.contains("lightsnow") -> no.uio.ifi.in2000.team46.R.drawable.lightsnow
        symbolCode.contains("snow") -> no.uio.ifi.in2000.team46.R.drawable.snow
        symbolCode.contains("heavysnow") -> no.uio.ifi.in2000.team46.R.drawable.heavysnow

        // sleet
        symbolCode.contains("lightsleetshowers_day") -> no.uio.ifi.in2000.team46.R.drawable.lightsleetshowers_day
        symbolCode.contains("lightsleetshowers_night") -> no.uio.ifi.in2000.team46.R.drawable.lightsleetshowers_night
        symbolCode.contains("lightsleetshowers_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.lightsleetshowers_polartwilight
        symbolCode.contains("sleetshowers_day") -> no.uio.ifi.in2000.team46.R.drawable.sleetshowers_day
        symbolCode.contains("sleetshowers_night") -> no.uio.ifi.in2000.team46.R.drawable.sleetshowers_night
        symbolCode.contains("sleetshowers_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.sleetshowers_polartwilight
        symbolCode.contains("heavysleetshowers_day") -> no.uio.ifi.in2000.team46.R.drawable.heavysleetshowers_day
        symbolCode.contains("heavysleetshowers_night") -> no.uio.ifi.in2000.team46.R.drawable.heavysleetshowers_night
        symbolCode.contains("heavysleetshowers_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.heavysleetshowers_polartwilight
        symbolCode.contains("lightsleet") -> no.uio.ifi.in2000.team46.R.drawable.lightsleet
        symbolCode.contains("sleet") -> no.uio.ifi.in2000.team46.R.drawable.sleet
        symbolCode.contains("heavysleet") -> no.uio.ifi.in2000.team46.R.drawable.heavysleet

        // Fog
        symbolCode.contains("fog") -> no.uio.ifi.in2000.team46.R.drawable.fog

        // Lightning
        symbolCode.contains("lightrainshowersandthunder_day") -> no.uio.ifi.in2000.team46.R.drawable.lightrainshowersandthunder_day
        symbolCode.contains("lightrainshowersandthunder_night") -> no.uio.ifi.in2000.team46.R.drawable.lightrainshowersandthunder_night
        symbolCode.contains("lightrainshowersandthunder_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.lightrainshowersandthunder_polartwilight
        symbolCode.contains("rainshowersandthunder_day") -> no.uio.ifi.in2000.team46.R.drawable.rainshowersandthunder_day
        symbolCode.contains("rainshowersandthunder_night") -> no.uio.ifi.in2000.team46.R.drawable.rainshowersandthunder_night
        symbolCode.contains("rainshowersandthunder_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.rainshowersandthunder_polartwilight
        symbolCode.contains("heavyrainshowersandthunder_day") -> no.uio.ifi.in2000.team46.R.drawable.heavyrainshowersandthunder_day
        symbolCode.contains("heavyrainshowersandthunder_night") -> no.uio.ifi.in2000.team46.R.drawable.heavyrainshowersandthunder_night
        symbolCode.contains("heavyrainshowersandthunder_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.heavyrainshowersandthunder_polartwilight
        symbolCode.contains("lightrainandthunder") -> no.uio.ifi.in2000.team46.R.drawable.lightrainandthunder
        symbolCode.contains("rainandthunder") -> no.uio.ifi.in2000.team46.R.drawable.rainandthunder
        symbolCode.contains("heavyrainandthunder") -> no.uio.ifi.in2000.team46.R.drawable.heavyrainandthunder
        symbolCode.contains("lightsleetshowersandthunder_day") -> no.uio.ifi.in2000.team46.R.drawable.lightssleetshowersandthunder_day
        symbolCode.contains("lightsleetshowersandthunder_night") -> no.uio.ifi.in2000.team46.R.drawable.lightssleetshowersandthunder_night
        symbolCode.contains("lightsleetshowersandthunder_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.lightssleetshowersandthunder_polartwilight
        symbolCode.contains("sleetshowersandthunder_day") -> no.uio.ifi.in2000.team46.R.drawable.sleetshowersandthunder_day
        symbolCode.contains("sleetshowersandthunder_night") -> no.uio.ifi.in2000.team46.R.drawable.sleetshowersandthunder_night
        symbolCode.contains("sleetshowersandthunder_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.sleetshowersandthunder_polartwilight
        symbolCode.contains("heavysleetshowersandthunder_day") -> no.uio.ifi.in2000.team46.R.drawable.heavysleetshowersandthunder_day
        symbolCode.contains("heavysleetshowersandthunder_night") -> no.uio.ifi.in2000.team46.R.drawable.heavysleetshowersandthunder_night
        symbolCode.contains("heavysleetshowersandthunder_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.heavysleetshowersandthunder_polartwilight
        symbolCode.contains("lightsleetandthunder") -> no.uio.ifi.in2000.team46.R.drawable.lightsleetandthunder
        symbolCode.contains("sleetandthunder") -> no.uio.ifi.in2000.team46.R.drawable.sleetandthunder
        symbolCode.contains("heavysleetandthunder") -> no.uio.ifi.in2000.team46.R.drawable.heavysleetandthunder
        symbolCode.contains("lightsnowshowersandthunder_day") -> no.uio.ifi.in2000.team46.R.drawable.lightssnowshowersandthunder_day
        symbolCode.contains("lightsnowshowersandthunder_night") -> no.uio.ifi.in2000.team46.R.drawable.lightssnowshowersandthunder_night
        symbolCode.contains("lightsnowshowersandthunder_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.lightssnowshowersandthunder_polartwilight
        symbolCode.contains("snowshowersandthunder_day") -> no.uio.ifi.in2000.team46.R.drawable.snowshowersandthunder_day
        symbolCode.contains("snowshowersandthunder_night") -> no.uio.ifi.in2000.team46.R.drawable.snowshowersandthunder_night
        symbolCode.contains("snowshowersandthunder_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.snowshowersandthunder_polartwilight
        symbolCode.contains("heavysnowshowersandthunder_day") -> no.uio.ifi.in2000.team46.R.drawable.heavysnowshowersandthunder_day
        symbolCode.contains("heavysnowshowersandthunder_night") -> no.uio.ifi.in2000.team46.R.drawable.heavysnowshowersandthunder_night
        symbolCode.contains("heavysnowshowersandthunder_polartwilight") -> no.uio.ifi.in2000.team46.R.drawable.heavysnowshowersandthunder_polartwilight
        symbolCode.contains("lightsnowandthunder") -> no.uio.ifi.in2000.team46.R.drawable.lightsnowandthunder
        symbolCode.contains("snowandthunder") -> no.uio.ifi.in2000.team46.R.drawable.snowandthunder
        symbolCode.contains("heavysnowandthunder") -> no.uio.ifi.in2000.team46.R.drawable.heavysnowandthunder

        else -> null
    }
} 