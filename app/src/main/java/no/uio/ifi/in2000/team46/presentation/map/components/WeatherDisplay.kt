package no.uio.ifi.in2000.team46.presentation.map.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import no.uio.ifi.in2000.team46.utils.metalerts.WeatherIconMapper
import no.uio.ifi.in2000.team46.utils.metalerts.WeatherDescriptionMapper
import no.uio.ifi.in2000.team46.presentation.map.viewmodel.MapViewModel
import no.uio.ifi.in2000.team46.data.remote.api.WeatherService
import no.uio.ifi.in2000.team46.domain.weather.WeatherDetails
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.roundToInt

// This function displays the weather information on the map
@Composable
fun WeatherDisplay(
    temperature: Double?,
    symbolCode: String?,
    mapViewModel: MapViewModel,
    navController: NavController,
    weatherService: WeatherService,
    modifier: Modifier = Modifier
) {
    WeatherDescriptionMapper.getWeatherDescription(symbolCode)
    val locationName by mapViewModel.locationName.collectAsState()
    var weatherDetails by remember { mutableStateOf<WeatherDetails?>(null) }
    val selectedLocation by mapViewModel.selectedLocation.collectAsState()
    val userLocation by mapViewModel.userLocation.collectAsState()
    val isLocationExplicitlySelected by mapViewModel.isLocationExplicitlySelected.collectAsState()

    LaunchedEffect(temperature, symbolCode) {
        if (temperature != null && symbolCode != null) {
            val location = if (isLocationExplicitlySelected && selectedLocation != null) {
                selectedLocation
            } else {
                userLocation?.let { Pair(it.latitude, it.longitude) }
            }
            
            location?.let { (lat, lon) ->
                val details = weatherService.getWeatherDetails(lat, lon)
                weatherDetails = details
            }
        }
    }

    Card(
        onClick = {
            weatherDetails?.let { details ->
                val location = if (isLocationExplicitlySelected && selectedLocation != null) {
                    selectedLocation
                } else {
                    userLocation?.let { Pair(it.latitude, it.longitude) }
                }
                
                location?.let { (lat, lon) ->
                    val encodedLocationName = URLEncoder.encode(locationName, StandardCharsets.UTF_8.toString())
                    navController.navigate(
                        "weather_detail/${details.temperature}/${details.feelsLike}/" +
                        "${details.highTemp}/${details.lowTemp}/${details.symbolCode}/" +
                        "${details.description}/${encodedLocationName}/" +
                        "${details.windSpeed}/${details.windDirection}/" +
                        "${lat}/${lon}"
                    )
                }
            }
        },
        modifier = modifier
            .width(IntrinsicSize.Min)
            .height(IntrinsicSize.Min),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            temperature?.let {
                Text(
                    text = "${it.roundToInt()}°",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            symbolCode?.let { code ->
                val iconRes = WeatherIconMapper.getWeatherIcon(code)
                iconRes?.let {
                    Icon(
                        painter = painterResource(id = it),
                        contentDescription = WeatherDescriptionMapper.getWeatherDescription(code),
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}