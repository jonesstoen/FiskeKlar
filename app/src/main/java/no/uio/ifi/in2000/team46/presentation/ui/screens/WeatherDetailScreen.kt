package no.uio.ifi.in2000.team46.presentation.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import no.uio.ifi.in2000.team46.R
import no.uio.ifi.in2000.team46.domain.model.weather.WeatherData
import no.uio.ifi.in2000.team46.utils.weather.WeatherIconMapper
import no.uio.ifi.in2000.team46.data.remote.weather.WeatherService
import kotlin.math.roundToInt
import android.util.Log
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Clock

@RequiresApi(Build.VERSION_CODES.O)
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
    windSpeed: Double? = null,
    windDirection: Double? = null,
    hasWarning: Boolean = false
) {
    var selectedTimeRange by remember { mutableStateOf(TimeRange.Now) }
    var expandedDay by remember { mutableStateOf<String?>(null) }
    var forecast by remember { mutableStateOf<List<WeatherService.DailyForecast>>(emptyList()) }
    val weatherService = remember { WeatherService() }
    
    LaunchedEffect(weatherData) {
        if (weatherData.latitude != null && weatherData.longitude != null) {
            Log.d("WeatherDetailScreen", "Henter værvarsel for ${weatherData.latitude}, ${weatherData.longitude}")
            try {
                forecast = weatherService.getDetailedForecast(
                    weatherData.latitude,
                    weatherData.longitude
                )
                Log.d("WeatherDetailScreen", "Mottok ${forecast.size} dager med værvarsel")
            } catch (e: Exception) {
                Log.e("WeatherDetailScreen", "Feil ved henting av værvarsel: ${e.message}", e)
            }
        } else {
            Log.w("WeatherDetailScreen", "Mangler koordinater for værvarselet")
        }
    }

    // Logg når tidslinje endres
    LaunchedEffect(selectedTimeRange) {
        Log.d("WeatherDetailScreen", "Byttet til ${selectedTimeRange.name}, forecast size: ${forecast.size}")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(locationName) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Gå tilbake")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tidslinje-velger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TimeRange.values().forEach { timeRange ->
                    FilledTonalButton(
                        onClick = { selectedTimeRange = timeRange },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (selectedTimeRange == timeRange) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    ) {
                        Text(timeRange.title)
                    }
                }
            }

            when (selectedTimeRange) {
                TimeRange.Now -> CurrentWeather(
                    weatherData = weatherData,
                    feelsLike = feelsLike,
                    highTemp = highTemp,
                    lowTemp = lowTemp,
                    weatherDescription = weatherDescription,
                    windSpeed = windSpeed,
                    windDirection = windDirection
                )
                TimeRange.ThreeDays -> {
                    if (forecast.isEmpty()) {
                        CircularProgressIndicator()
                    } else {
                        ThreeDaysForecast(
                            forecast = forecast,
                            expandedDay = expandedDay,
                            onDayClick = { date -> expandedDay = if (expandedDay == date) null else date }
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CurrentWeather(
    weatherData: WeatherData,
    feelsLike: Double,
    highTemp: Double,
    lowTemp: Double,
    weatherDescription: String,
    windSpeed: Double?,
    windDirection: Double?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Temperatur
        Text(
            text = "${weatherData.temperature?.roundToInt()}°",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Værikon
        weatherData.symbolCode?.let { code ->
            val iconRes = WeatherIconMapper.getWeatherIcon(code)
            iconRes?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = weatherDescription,
                    modifier = Modifier.size(120.dp),
                    tint = Color.Unspecified
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Værbeskrivelse
        Text(
            text = weatherDescription,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Vindinfo
        if (windSpeed != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${windSpeed.roundToInt()} m/s",
                    style = MaterialTheme.typography.titleMedium
                )
                if (windDirection != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_up),
                        contentDescription = "Vindretning",
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(rotationZ = windDirection.toFloat() + 180f),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Høyeste og laveste temperatur
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Laveste",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${lowTemp.roundToInt()}°",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Høyeste",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${highTemp.roundToInt()}°",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Føles som
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Føles som",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${feelsLike.roundToInt()}°",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun parseHour(timeString: String): Int {
    return try {
        val instant = Instant.parse(timeString)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        localDateTime.hour
    } catch (e: Exception) {
        Log.e("WeatherDetailScreen", "Feil ved parsing av tid: $timeString", e)
        0 // Returnerer 0 som fallback
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ThreeDaysForecast(
    forecast: List<WeatherService.DailyForecast>,
    expandedDay: String?,
    onDayClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(forecast.take(3)) { dailyForecast ->
            val date = Instant.parse("${dailyForecast.date}T00:00:00Z")
                .toLocalDateTime(TimeZone.currentSystemDefault())
            val formattedDate = "${date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}. ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }}"
            val isExpanded = expandedDay == dailyForecast.date
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Overskrift med dato
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${dailyForecast.maxTemp?.roundToInt()}° / ${dailyForecast.minTemp?.roundToInt()}°",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 6-timers blokker
                    val timeBlocks = if (dailyForecast.date == Clock.System.now()
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date.toString()) {
                        // For dagens dato, vis bare gjenværende timer
                        val currentHour = Clock.System.now()
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .hour
                        val startBlock = (currentHour / 6) * 6
                        listOf(
                            startBlock to minOf(startBlock + 6, 24),
                            if (startBlock + 6 < 24) startBlock + 6 to minOf(startBlock + 12, 24) else null,
                            if (startBlock + 12 < 24) startBlock + 12 to minOf(startBlock + 18, 24) else null,
                            if (startBlock + 18 < 24) startBlock + 18 to 24 else null
                        ).filterNotNull()
                    } else {
                        listOf(0 to 6, 6 to 12, 12 to 18, 18 to 24)
                    }

                    timeBlocks.forEach { (startHour, endHour) ->
                        val blockForecasts = dailyForecast.hourlyForecasts.filter { hourly ->
                            val forecastHour = Instant.parse(hourly.time)
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .hour
                            forecastHour in startHour until endHour
                        }
                        
                        if (blockForecasts.isNotEmpty()) {
                            val avgTemp = blockForecasts.mapNotNull { it.temperature }.average()
                            val avgWindSpeed = blockForecasts.mapNotNull { it.windSpeed }.average()
                            val avgWindDir = blockForecasts.mapNotNull { it.windDirection }.average()
                            val representativeSymbol = blockForecasts
                                .firstOrNull { it.symbolCode != null }
                                ?.symbolCode

                            WeatherBlock(
                                timeRange = "$startHour-$endHour",
                                temperature = avgTemp,
                                windSpeed = avgWindSpeed,
                                windDirection = avgWindDir,
                                symbolCode = representativeSymbol
                            )
                        }
                    }

                    // Detaljer-knapp
                    TextButton(
                        onClick = { onDayClick(dailyForecast.date) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Detaljer")
                            Icon(
                                imageVector = if (isExpanded) 
                                    Icons.Default.KeyboardArrowUp 
                                else 
                                    Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Skjul detaljer" else "Vis detaljer"
                            )
                        }
                    }

                    // Detaljert timevisning
                    if (isExpanded) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        dailyForecast.hourlyForecasts.forEach { hourly ->
                            val hour = Instant.parse(hourly.time)
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .hour
                            HourlyWeatherRow(
                                hour = hour,
                                temperature = hourly.temperature,
                                windSpeed = hourly.windSpeed,
                                windDirection = hourly.windDirection,
                                symbolCode = hourly.symbolCode
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherBlock(
    timeRange: String,
    temperature: Double,
    windSpeed: Double,
    windDirection: Double,
    symbolCode: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tidsperiode
        Text(
            text = "$timeRange",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(48.dp)
        )

        // Værikon
        symbolCode?.let { code ->
            val iconRes = WeatherIconMapper.getWeatherIcon(code)
            iconRes?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color.Unspecified
                )
            }
        }

        // Temperatur
        Text(
            text = "${temperature.roundToInt()}°",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )

        // Vind
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${windSpeed.roundToInt()} m/s",
                style = MaterialTheme.typography.bodyMedium
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_up),
                contentDescription = "Vindretning",
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer(rotationZ = windDirection.toFloat() + 180f),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun HourlyWeatherRow(
    hour: Int,
    temperature: Double?,
    windSpeed: Double?,
    windDirection: Double?,
    symbolCode: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tidspunkt
        Text(
            text = String.format("%02d:00", hour),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(48.dp)
        )

        // Værikon
        symbolCode?.let { code ->
            val iconRes = WeatherIconMapper.getWeatherIcon(code)
            iconRes?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
            }
        }

        // Temperatur
        temperature?.let {
            Text(
                text = "${it.roundToInt()}°",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Vind
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            windSpeed?.let {
                Text(
                    text = "${it.roundToInt()} m/s",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            windDirection?.let {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_up),
                    contentDescription = "Vindretning",
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer(rotationZ = it.toFloat() + 180f),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

enum class TimeRange(val title: String) {
    Now("Nå"),
    ThreeDays("3 dager")
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDate(dateString: String): String {
    val date = Instant.parse("${dateString}T00:00:00Z")
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return "${date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}. ${date.month.name.substring(0, 3).lowercase().replaceFirstChar { it.uppercase() }}"
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatTime(timeString: String): String {
    val time = Instant.parse(timeString)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
}

private fun <T, R> List<T>.firstNotNull(selector: (T) -> R?): R? {
    for (element in this) {
        val result = selector(element)
        if (result != null) {
            return result
        }
    }
    return null
} 