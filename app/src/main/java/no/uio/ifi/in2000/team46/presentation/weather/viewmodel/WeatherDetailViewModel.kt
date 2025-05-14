package no.uio.ifi.in2000.team46.presentation.weather.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.remote.api.WeatherService
import kotlinx.datetime.*
import no.uio.ifi.in2000.team46.domain.weather.SixHourBlock
import no.uio.ifi.in2000.team46.domain.weather.DailyForecast
import no.uio.ifi.in2000.team46.domain.weather.HourlyForecast
import no.uio.ifi.in2000.team46.domain.weather.WeatherData

enum class TimeRange(val title: String) {
    Now("Nå"),
    ThreeDays("3 dager")
}

sealed interface WeatherDetailEvent {
    data class OnTimeRangeSelected(val range: Int) : WeatherDetailEvent
    data class OnDayClicked(val date: String) : WeatherDetailEvent
    data object OnBackClicked : WeatherDetailEvent
    data class OnSearchQueryChanged(val query: String) : WeatherDetailEvent
    data object OnSearchExpandedChanged : WeatherDetailEvent
    data class OnLocationSelected(
        val latitude: Double,
        val longitude: Double,
        val locationName: String
    ) : WeatherDetailEvent
}

data class WeatherDetailState(
    val forecasts: List<DailyForecast> = emptyList(),
    val expandedDates: Set<String> = emptySet(),
    val selectedTimeRange: TimeRange = TimeRange.Now,
    val isLoading: Boolean = false,
    val error: String? = null,
    val weatherData: WeatherData? = null,
    val locationName: String = "",
    val feelsLike: Double = 0.0,
    val highTemp: Double = 0.0,
    val lowTemp: Double = 0.0,
    val weatherDescription: String = "",
    val windSpeed: Double? = null,
    val windDirection: Double? = null,
    val isSearchExpanded: Boolean = false,
    val searchQuery: String = ""
)

class WeatherDetailViewModel(
    private val weatherService: WeatherService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WeatherDetailState())
    val uiState: StateFlow<WeatherDetailState> = _uiState.asStateFlow()

    companion object {
        fun createSixHourBlocks(forecasts: List<HourlyForecast>): List<SixHourBlock> {
            val blocks = mutableListOf<SixHourBlock>()
            val now = Clock.System.now()
            val currentDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
            val currentHour = currentDateTime.hour

            //standard time blocks
            val timeBlocks = listOf(0 to 6, 6 to 12, 12 to 18, 18 to 0)

            //find the first relevant block for today
            val firstForecast = forecasts.firstOrNull()?.let {
                Instant.parse(it.time).toLocalDateTime(TimeZone.currentSystemDefault())
            }

            if (firstForecast != null && firstForecast.date == currentDateTime.date) {
                // this is for today, use the current time to determine the blocks
                val currentBlock = when (currentHour) {
                    in 0..5 -> 0 to 6
                    in 6..11 -> 6 to 12
                    in 12..17 -> 12 to 18
                    else -> 18 to 0
                }

                //if we are in a started block, create a special block from the current hour to the end of the block
                if (currentHour in currentBlock.first until currentBlock.second ||
                    (currentBlock.first == 18 && currentHour >= 18)) {
                    val endHour = if (currentBlock.first == 18) 0 else currentBlock.second
                    val relevantForecasts = forecasts.filter { forecast ->
                        val forecastHour = Instant.parse(forecast.time)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .hour
                        forecastHour in currentHour..(if (endHour == 0) 23 else endHour - 1)
                    }

                    if (relevantForecasts.isNotEmpty()) {
                        val middleForecast = relevantForecasts[relevantForecasts.size / 2]
                        blocks.add(
                            SixHourBlock(
                                startHour = currentHour,
                                endHour = endHour,
                                temperature = middleForecast.temperature,
                                symbolCode = middleForecast.symbolCode,
                                windSpeed = middleForecast.windSpeed,
                                windDirection = middleForecast.windDirection
                            )
                        )
                    }

                    // add remaining whole blocks for the day
                    for (block in timeBlocks) {
                        if (block.first > currentBlock.first) {
                            val blockForecasts = forecasts.filter { forecast ->
                                val forecastHour = Instant.parse(forecast.time)
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .hour
                                forecastHour in block.first..(if (block.second == 0) 23 else block.second - 1)
                            }

                            if (blockForecasts.isNotEmpty()) {
                                val middleForecast = blockForecasts[blockForecasts.size / 2]
                                blocks.add(
                                    SixHourBlock(
                                        startHour = block.first,
                                        endHour = block.second,
                                        temperature = middleForecast.temperature,
                                        symbolCode = middleForecast.symbolCode,
                                        windSpeed = middleForecast.windSpeed,
                                        windDirection = middleForecast.windDirection
                                    )
                                )
                            }
                        }
                    }
                }
            } else {

                //this is for future days, use standard blocks
                for ((startHour, endHour) in timeBlocks) {
                    val blockForecasts = forecasts.filter { forecast ->
                        val forecastHour = Instant.parse(forecast.time)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .hour
                        forecastHour in startHour..(if (endHour == 0) 23 else endHour - 1)
                    }

                    if (blockForecasts.isNotEmpty()) {
                        val middleForecast = blockForecasts[blockForecasts.size / 2]
                        blocks.add(
                            SixHourBlock(
                                startHour = startHour,
                                endHour = endHour,
                                temperature = middleForecast.temperature,
                                symbolCode = middleForecast.symbolCode,
                                windSpeed = middleForecast.windSpeed,
                                windDirection = middleForecast.windDirection
                            )
                        )
                    }
                }
            }

            return blocks
        }
    }

    fun onEvent(event: WeatherDetailEvent) {
        when (event) {
            is WeatherDetailEvent.OnTimeRangeSelected -> {
                _uiState.update { it.copy(
                    selectedTimeRange = TimeRange.entries[event.range]
                )}
            }
            is WeatherDetailEvent.OnDayClicked -> {
                _uiState.update { currentState ->
                    val expandedDates = currentState.expandedDates.toMutableSet()
                    if (event.date in expandedDates) {
                        expandedDates.remove(event.date)
                    } else {
                        expandedDates.add(event.date)
                    }
                    currentState.copy(expandedDates = expandedDates)
                }
            }
            is WeatherDetailEvent.OnSearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is WeatherDetailEvent.OnSearchExpandedChanged -> {
                _uiState.update { it.copy(
                    isSearchExpanded = !it.isSearchExpanded,
                    searchQuery = if (!it.isSearchExpanded) "" else it.searchQuery
                )}
            }
            is WeatherDetailEvent.OnLocationSelected -> {
                updateLocation(
                    latitude = event.latitude,
                    longitude = event.longitude,
                    locationName = event.locationName
                )
            }
            WeatherDetailEvent.OnBackClicked -> {
                // Handle back navigation if needed
            }
        }
    }

    // Cache for weather data to avoid unnecessary API calls
    private val weatherCache = mutableMapOf<Pair<Double, Double>, Pair<Long, WeatherDetailState>>()
    private val CACHE_EXPIRY_MS = 5 * 60 * 1000 // 5 minutes


    fun updateLocation(latitude: Double, longitude: Double, locationName: String) {
        viewModelScope.launch {
            // Check if we have cached data that's still valid
            val cacheKey = Pair(latitude, longitude)
            val cachedData = weatherCache[cacheKey]
            val currentTime = System.currentTimeMillis()

            if (cachedData != null && (currentTime - cachedData.first < CACHE_EXPIRY_MS)) {
                // Use cached data if it's still valid
                val cachedState = cachedData.second
                _uiState.update {
                    cachedState.copy(locationName = locationName, isLoading = false)
                }
                return@launch
            }

            // No valid cache, need to fetch data
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Fetch both basic weather and forecast concurrently
                val weatherDetailsDeferred = viewModelScope.async {
                    weatherService.getWeatherDetails(latitude, longitude)
                }
                val forecastsDeferred = viewModelScope.async {
                    weatherService.getDetailedForecast(latitude, longitude)
                }

                // Wait for both to complete
                val weatherDetails = weatherDetailsDeferred.await()
                val forecasts = forecastsDeferred.await()

                val weatherData = WeatherData(
                    temperature = weatherDetails.temperature,
                    symbolCode = weatherDetails.symbolCode ?: "",
                    latitude = latitude,
                    longitude = longitude
                )

                val filteredForecasts = filterAndProcessForecasts(forecasts)

                // Update state with all data at once
                val newState = _uiState.value.copy(
                    weatherData = weatherData,
                    locationName = locationName,
                    feelsLike = weatherDetails.feelsLike ?: 0.0,
                    highTemp = weatherDetails.highTemp ?: 0.0,
                    lowTemp = weatherDetails.lowTemp ?: 0.0,
                    weatherDescription = weatherDetails.description ?: "",
                    windSpeed = weatherDetails.windSpeed,
                    windDirection = weatherDetails.windDirection,
                    forecasts = filteredForecasts,
                    isLoading = false,
                    error = null,
                    isSearchExpanded = false
                )

                // Cache the new state
                weatherCache[cacheKey] = Pair(currentTime, newState)

                // Update the UI
                _uiState.update { newState }

            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "En feil oppstod ved henting av værdata"
                )}
            }
        }
    }

    fun loadForecast(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val forecasts = weatherService.getDetailedForecast(latitude, longitude)
                val filteredForecasts = filterAndProcessForecasts(forecasts)
                _uiState.update { it.copy(
                    forecasts = filteredForecasts,
                    isLoading = false
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "En feil oppstod ved henting av værvarsel"
                )}
            }
        }
    }

    private fun filterAndProcessForecasts(forecasts: List<DailyForecast>): List<DailyForecast> {
        val now = Clock.System.now()
        val currentDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val currentHour = currentDateTime.hour
        
        return forecasts.mapIndexed { index, forecast ->
            val forecastDate = LocalDate.parse(forecast.date)
            val isToday = forecastDate.dayOfYear == currentDateTime.date.dayOfYear
            
            if (isToday) {
                val filteredHourlyForecasts = forecast.hourlyForecasts.filter { hourlyForecast ->
                    val forecastHour = Instant.parse(hourlyForecast.time)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .hour
                    forecastHour >= currentHour
                }
                forecast.copy(hourlyForecasts = filteredHourlyForecasts)
            } else if (index <= 2) {
                forecast
            } else {
                null
            }
        }.filterNotNull()
    }
}