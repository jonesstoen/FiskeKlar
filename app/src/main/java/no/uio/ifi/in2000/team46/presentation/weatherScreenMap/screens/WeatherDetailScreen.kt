package no.uio.ifi.in2000.team46.presentation.weatherScreenMap.screens

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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import no.uio.ifi.in2000.team46.R
import no.uio.ifi.in2000.team46.domain.model.weather.WeatherData
import no.uio.ifi.in2000.team46.data.remote.weather.WeatherService
import no.uio.ifi.in2000.team46.domain.model.weather.SixHourBlock
import no.uio.ifi.in2000.team46.presentation.ui.screens.Background
import no.uio.ifi.in2000.team46.presentation.weatherScreenMap.viewmodel.*
import no.uio.ifi.in2000.team46.presentation.weatherScreenMap.utils.DateTimeFormatter
import no.uio.ifi.in2000.team46.utils.weather.WeatherIconMapper
import kotlin.math.roundToInt

sealed interface WeatherDetailEvent {
    data class OnTimeRangeSelected(val range: Int) : WeatherDetailEvent
    data class OnDayClicked(val date: String) : WeatherDetailEvent
    data object OnBackClicked : WeatherDetailEvent
}

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
    hasWarning: Boolean = false,
    viewModel: WeatherDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTimeRange by remember { mutableStateOf(TimeRange.Now) }
    
    LaunchedEffect(weatherData) {
        if (weatherData.latitude != null && weatherData.longitude != null) {
            viewModel.loadForecast(weatherData.latitude, weatherData.longitude)
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text(locationName) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Gå tilbake")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        },
        bottomBar = {
            TimeRangeSelector(
                selectedTimeRange = selectedTimeRange,
                onTimeRangeSelected = { 
                    selectedTimeRange = it
                    viewModel.onEvent(WeatherDetailEvent.OnTimeRangeSelected(it.ordinal))
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
                    if (uiState.isLoading) {
                        CircularProgressIndicator()
                    } else {
                        ForecastView(
                            forecasts = uiState.forecasts,
                            expandedDates = uiState.expandedDates,
                            onDayClick = { date -> 
                                viewModel.onEvent(WeatherDetailEvent.OnDayClicked(date))
                            }
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ForecastView(
    forecasts: List<WeatherService.DailyForecast>,
    expandedDates: Set<String>,
    onDayClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(forecasts) { forecast ->
            DayForecast(
                forecast = forecast,
                isExpanded = forecast.date in expandedDates,
                onDayClick = { onDayClick(forecast.date) }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DayForecast(
    forecast: WeatherService.DailyForecast,
    isExpanded: Boolean,
    onDayClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            DayHeader(
                date = forecast.date,
                maxTemp = forecast.maxTemp,
                minTemp = forecast.minTemp
            )

            if (!isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                SixHourBlocks(forecast.hourlyForecasts)
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                HourlyForecast(forecast.hourlyForecasts)
            }

            ExpandButton(
                isExpanded = isExpanded,
                onClick = onDayClick
            )
        }
    }
}

@Composable
private fun SixHourBlocks(forecasts: List<WeatherService.HourlyForecast>) {
    val blocks = createSixHourBlocks(forecasts)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            SixHourBlock(block)
        }
    }
}

@Composable
private fun SixHourBlock(
    block: SixHourBlock
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tidsperiode
        Text(
            text = "${block.startHour.toString().padStart(2, '0')}-${block.endHour.toString().padStart(2, '0')}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        // Værsymbol
        if (block.symbolCode != null) {
            Icon(
                painter = painterResource(id = WeatherIconMapper.getWeatherIcon(block.symbolCode) ?: R.drawable.ic_weather_unknown),
                contentDescription = block.symbolCode,
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
        }

        // Temperatur
        if (block.temperature != null) {
            Text(
                text = "${block.temperature.roundToInt()}°",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Red,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }

        // Vind info
        if (block.windSpeed != null && block.windDirection != null) {
            WindInfo(
                windSpeed = block.windSpeed,
                windDirection = block.windDirection,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HourlyForecast(forecasts: List<WeatherService.HourlyForecast>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        forecasts.forEach { forecast ->
            HourlyWeatherRow(forecast = forecast)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DayHeader(
    date: String,
    maxTemp: Double?,
    minTemp: Double?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = DateTimeFormatter.formatDate(date),
            style = MaterialTheme.typography.titleMedium
        )
        if (maxTemp != null && minTemp != null) {
            Text(
                text = "${maxTemp.roundToInt()}° / ${minTemp.roundToInt()}°",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun HourlyWeatherRow(
    forecast: WeatherService.HourlyForecast
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = DateTimeFormatter.formatTime(forecast.time),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        if (forecast.symbolCode != null) {
            Icon(
                painter = painterResource(id = WeatherIconMapper.getWeatherIcon(forecast.symbolCode) ?: R.drawable.ic_weather_unknown),
                contentDescription = forecast.symbolCode,
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
        }

        if (forecast.temperature != null) {
            Text(
                text = "${forecast.temperature.roundToInt()}°",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }

        if (forecast.windSpeed != null && forecast.windDirection != null) {
            WindInfo(
                windSpeed = forecast.windSpeed,
                windDirection = forecast.windDirection,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WindInfo(
    windSpeed: Double,
    windDirection: Double,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = "${windSpeed.roundToInt()} m/s",
            style = if (isLarge) MaterialTheme.typography.displaySmall
            else MaterialTheme.typography.bodyMedium
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_up),
            contentDescription = "Vindretning",
            modifier = Modifier
                .size(if (isLarge) 36.dp else 24.dp)
                .rotate(windDirection.toFloat()),
            tint = Color.Unspecified
        )
    }
}

@Composable
private fun TimeRangeSelector(
    selectedTimeRange: TimeRange,
    onTimeRangeSelected: (TimeRange) -> Unit
) {
    Surface(
        color = Background,
        modifier = Modifier.fillMaxWidth()
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
                .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
            TimeRange.entries.forEach { timeRange ->
            FilledTonalButton(
                onClick = { onTimeRangeSelected(timeRange) },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (selectedTimeRange == timeRange) 
                            MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                    Text(
                        text = timeRange.title,
                        color = if (selectedTimeRange == timeRange)
                            MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    )
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
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            TemperatureDisplay(
                label = "Føles som",
                temperature = feelsLike,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        Text(
            text = "${weatherData.temperature?.roundToInt()}°",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = MaterialTheme.typography.displayLarge.fontSize * 1.5
                ),
                modifier = Modifier.align(Alignment.Center)
        )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        WeatherIcon(
            symbolCode = weatherData.symbolCode,
            description = weatherDescription,
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = weatherDescription,
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (windSpeed != null && windDirection != null) {
        WindInfo(
            windSpeed = windSpeed,
                windDirection = windDirection,
                isLarge = true
        )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TemperatureDisplay(
                label = "Laveste",
                temperature = lowTemp
            )
            TemperatureDisplay(
                label = "Høyeste",
                temperature = highTemp
            )
        }
    }
}

@Composable
private fun WeatherIcon(
    symbolCode: String?,
    description: String,
    modifier: Modifier = Modifier
) {
    val iconRes = WeatherIconMapper.getWeatherIcon(symbolCode ?: "")
    iconRes?.let {
        Icon(
            painter = painterResource(id = it),
            contentDescription = description,
            modifier = modifier.size(120.dp),
            tint = Color.Unspecified
        )
    }
}

@Composable
private fun TemperatureDisplay(
    label: String,
    temperature: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "${temperature.roundToInt()}°",
            style = style
        )
    }
}

@Composable
private fun ExpandButton(
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = if (isExpanded) "Lukk" else "Detaljer")
            Icon(
                imageVector = if (isExpanded) 
                    Icons.Default.KeyboardArrowUp 
                else 
                    Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Skjul detaljer" else "Vis detaljer"
            )
        }
    }
}

enum class TimeRange(val title: String) {
    Now("Nå"),
    ThreeDays("3 dager")
}

