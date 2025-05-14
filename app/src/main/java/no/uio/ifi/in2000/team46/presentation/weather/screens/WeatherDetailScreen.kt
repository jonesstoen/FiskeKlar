package no.uio.ifi.in2000.team46.presentation.weather.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocationOn
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
import no.uio.ifi.in2000.team46.domain.weather.WeatherData
import no.uio.ifi.in2000.team46.data.remote.api.WeatherService
import no.uio.ifi.in2000.team46.domain.weather.SixHourBlock
import no.uio.ifi.in2000.team46.domain.weather.DailyForecast
import no.uio.ifi.in2000.team46.domain.weather.HourlyForecast
import no.uio.ifi.in2000.team46.presentation.map.viewmodel.SearchViewModel
import no.uio.ifi.in2000.team46.presentation.weather.viewmodel.WeatherDetailViewModel
import no.uio.ifi.in2000.team46.presentation.weather.viewmodel.TimeRange
import no.uio.ifi.in2000.team46.presentation.weather.viewmodel.WeatherDetailEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import org.maplibre.android.maps.MapLibreMap
import no.uio.ifi.in2000.team46.data.remote.api.Feature
import no.uio.ifi.in2000.team46.presentation.map.utils.rememberMapViewWithLifecycle
import no.uio.ifi.in2000.team46.presentation.weather.utils.DateTimeFormatter
import no.uio.ifi.in2000.team46.presentation.weather.viewmodel.WeatherDetailViewModel.Companion.createSixHourBlocks
import no.uio.ifi.in2000.team46.utils.metalerts.WeatherIconMapper
import kotlin.math.roundToInt


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
    viewModel: WeatherDetailViewModel,
    searchViewModel: SearchViewModel? = null,
    weatherService: WeatherService? = null,
    isFromHomeScreen: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    
    val mapView = rememberMapViewWithLifecycle()
    
    LaunchedEffect(mapView) {
        mapView.getMapAsync { map ->
            mapLibreMap = map
        }
    }
    
    LaunchedEffect(weatherData) {
        if (weatherData.latitude != null && weatherData.longitude != null) {
            viewModel.updateLocation(
                latitude = weatherData.latitude,
                longitude = weatherData.longitude,
                locationName = locationName
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.locationName.ifEmpty { locationName },
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Tilbake"
                        )
                    }
                },
                actions = {
                    if (isFromHomeScreen && searchViewModel != null) {
                        IconButton(
                            onClick = { viewModel.onEvent(WeatherDetailEvent.OnSearchExpandedChanged) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Søk"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            TimeRangeSelector(
                selectedTimeRange = uiState.selectedTimeRange,
                onTimeRangeSelected = { range -> 
                    viewModel.onEvent(WeatherDetailEvent.OnTimeRangeSelected(range.ordinal))
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isFromHomeScreen && searchViewModel != null && uiState.isSearchExpanded) {
                val searchResults by searchViewModel.searchResults.collectAsState()
                val isSearching by searchViewModel.isSearching.collectAsState()
                val showingHistory by searchViewModel.showingHistory.collectAsState()
                
                SearchBox(
                    map = mapLibreMap,
                    searchResults = searchResults,
                    onSearch = { query -> 
                        searchViewModel.search(query)
                        viewModel.onEvent(WeatherDetailEvent.OnSearchQueryChanged(query))
                    },
                    onResultSelected = { feature ->
                        val coordinates = feature.geometry.coordinates
                        viewModel.onEvent(
                            WeatherDetailEvent.OnLocationSelected(
                            latitude = coordinates[1],
                            longitude = coordinates[0],
                            locationName = feature.properties.name
                        ))
                    },
                    isSearching = isSearching,
                    showingHistory = showingHistory
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                when (uiState.selectedTimeRange) {
                    TimeRange.Now -> {
                        CurrentWeather(
                            temperature = uiState.weatherData?.temperature ?: weatherData.temperature ?: 0.0,
                            feelsLike = uiState.feelsLike,
                            highTemp = uiState.highTemp,
                            lowTemp = uiState.lowTemp,
                            symbolCode = uiState.weatherData?.symbolCode ?: weatherData.symbolCode ?: "",
                            description = uiState.weatherDescription.ifEmpty { weatherDescription },
                            windSpeed = uiState.windSpeed ?: windSpeed ?: 0.0,
                            windDirection = uiState.windDirection ?: windDirection ?: 0.0,
                            isLarge = true
                )
                    }
                    TimeRange.ThreeDays -> {
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

@Composable
private fun ForecastView(
    forecasts: List<DailyForecast>,
    expandedDates: Set<String>,
    onDayClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = forecasts, key = { it.date }) { forecast ->
            DayForecast(
                forecast = forecast,
                isExpanded = forecast.date in expandedDates,
                onDayClick = { onDayClick(forecast.date) }
            )
        }
    }
}

@Composable
private fun DayForecast(
    forecast: DailyForecast,
    isExpanded: Boolean,
    onDayClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
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
private fun SixHourBlocks(forecasts: List<HourlyForecast>) {
    val blocks = createSixHourBlocks(forecasts)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            SixHourBlock(block = block)
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
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onPrimaryContainer
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
private fun HourlyForecast(forecasts: List<HourlyForecast>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        forecasts.forEach { forecast ->
            HourlyWeatherRow(forecast = forecast)
        }
    }
}

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
    forecast: HourlyForecast
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
        color = MaterialTheme.colorScheme.background,
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
                                MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                    Text(
                        text = timeRange.title,
                        color = if (selectedTimeRange == timeRange)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentWeather(
    temperature: Double,
    feelsLike: Double,
    highTemp: Double,
    lowTemp: Double,
    symbolCode: String,
    description: String,
    windSpeed: Double,
    windDirection: Double,
    isLarge: Boolean
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
                text = "${temperature.roundToInt()}°",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = MaterialTheme.typography.displayLarge.fontSize * 1.5
                ),
                modifier = Modifier.align(Alignment.Center)
        )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        WeatherIcon(
            symbolCode = symbolCode,
            description = description,
            modifier = Modifier.size(120.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        WindInfo(
            windSpeed = windSpeed,
            windDirection = windDirection,
            isLarge = isLarge
        )
        
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
    symbolCode: String,
    description: String,
    modifier: Modifier = Modifier
) {
    val iconRes = WeatherIconMapper.getWeatherIcon(symbolCode)
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

@Composable
fun SearchBox(
    map: MapLibreMap?,
    searchResults: List<Feature>,
    onSearch: (String) -> Unit,
    onResultSelected: (Feature) -> Unit,
    isSearching: Boolean,
    showingHistory: Boolean
) {
    var searchText by remember { mutableStateOf("") }
    var isSearchBoxFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Surface(
            shadowElevation = 8.dp,
            shape = if (isSearchBoxFocused && (searchResults.isNotEmpty() || showingHistory)) 
                RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                ) 
            else RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { 
                    searchText = it
                    onSearch(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isSearchBoxFocused = it.isFocused },
                placeholder = { Text("Søk etter sted...") },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Søk",
                        tint = Color.Gray
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }

        AnimatedVisibility(
            visible = isSearchBoxFocused && (searchResults.isNotEmpty() || showingHistory),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 0.dp,
                    bottomStart = 24.dp,
                    bottomEnd = 24.dp
                ),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    if (showingHistory && searchText.isEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Nylige søk",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    items(searchResults) { feature ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onResultSelected(feature)
                                    searchText = ""
                                    focusManager.clearFocus()
                                }
                                .padding(vertical = 2.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
) {
    Row(
                                modifier = Modifier
            .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(end = 16.dp, top = 2.dp)
                                        .size(20.dp),
                                    tint = Color.DarkGray
                                )

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
        Text(
                                        text = feature.properties.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )

                                    val location = buildString {
                                        feature.properties.locality?.let { locality ->
                                            append(locality)
                                        }
                                        feature.properties.region?.let { region ->
                                            if (isNotEmpty()) append(", ")
                                            append(region)
                                        }
                                    }

                                    if (location.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
        Text(
                                            text = location,
            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(start = 52.dp),
                                thickness = 0.5.dp,
                                color =MaterialTheme.colorScheme.outline
                            )
                        }
        }
    }
            }
        }
    }
}

