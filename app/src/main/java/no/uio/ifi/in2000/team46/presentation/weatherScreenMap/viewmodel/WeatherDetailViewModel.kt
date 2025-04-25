package no.uio.ifi.in2000.team46.presentation.weatherScreenMap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.remote.weather.WeatherService
import no.uio.ifi.in2000.team46.presentation.weatherScreenMap.screens.WeatherDetailEvent
import kotlinx.datetime.*
import no.uio.ifi.in2000.team46.domain.model.weather.SixHourBlock
enum class TimeRange(val title: String) {
    Now("Nå"),
    ThreeDays("3 dager")
}

data class TimeBlock(
    val start: Int,
    val end: Int,
    val temp: Double?,
    val symbol: String?,
    val wind: Double?,
    val windDir: Double?
)

data class WeatherDetailState(
    val forecasts: List<WeatherService.DailyForecast> = emptyList(),
    val expandedDates: Set<String> = emptySet(),
    val selectedTimeRange: TimeRange = TimeRange.Now,
    val isLoading: Boolean = false,
    val error: String? = null
)

class WeatherDetailViewModel(
    private val weatherService: WeatherService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WeatherDetailState())
    val uiState: StateFlow<WeatherDetailState> = _uiState.asStateFlow()
    
    fun loadForecast(latitude: Double?, longitude: Double?) {
        if (latitude == null || longitude == null) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val allForecasts = weatherService.getDetailedForecast(latitude, longitude)
                val filteredForecasts = filterAndProcessForecasts(allForecasts)
                _uiState.update { 
                    it.copy(
                        forecasts = filteredForecasts,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    private fun filterAndProcessForecasts(forecasts: List<WeatherService.DailyForecast>): List<WeatherService.DailyForecast> {
        val now = Clock.System.now()
        val currentDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val currentHour = currentDateTime.hour
        
        return forecasts.mapIndexed { index, forecast ->
            val forecastDate = LocalDate.parse(forecast.date)
            val isToday = forecastDate.dayOfYear == currentDateTime.date.dayOfYear
            
            if (isToday) {
                // For dagens prognose, filtrer ut timer som har passert
                val filteredHourlyForecasts = forecast.hourlyForecasts.filter { hourlyForecast ->
                    val forecastHour = Instant.parse(hourlyForecast.time)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .hour
                    forecastHour >= currentHour
                }
                forecast.copy(hourlyForecasts = filteredHourlyForecasts)
            } else if (index <= 2) { // Bare behold de neste to dagene
                forecast
            } else {
                null
            }
        }.filterNotNull()
    }
    
    fun onEvent(event: WeatherDetailEvent) {
        when (event) {
            is WeatherDetailEvent.OnTimeRangeSelected -> {
                val range = TimeRange.values()[event.range]
                _uiState.update { it.copy(selectedTimeRange = range) }
            }
            is WeatherDetailEvent.OnDayClicked -> {
                toggleDayExpansion(event.date)
            }
            WeatherDetailEvent.OnBackClicked -> {
                // Håndter tilbake-navigasjon
            }
        }
    }
    
    private fun toggleDayExpansion(date: String) {
        _uiState.update { currentState ->
            val expandedDates = currentState.expandedDates.toMutableSet()
            if (expandedDates.contains(date)) {
                expandedDates.remove(date)
            } else {
                expandedDates.add(date)
            }
            currentState.copy(expandedDates = expandedDates)
        }
    }

}

fun createSixHourBlocks(forecasts: List<WeatherService.HourlyForecast>): List<SixHourBlock> {
    val blocks = mutableListOf<SixHourBlock>()
    val now = Clock.System.now()
    val currentDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
    val currentHour = currentDateTime.hour

    // Standard tidsblokker
    val timeBlocks = listOf(0 to 6, 6 to 12, 12 to 18, 18 to 0)

    // Finn dagens første relevante blokk
    val firstForecast = forecasts.firstOrNull()?.let {
        Instant.parse(it.time).toLocalDateTime(TimeZone.currentSystemDefault())
    }

    if (firstForecast != null && firstForecast.date == currentDateTime.date) {
        // Dette er for dagens prognoser
        val currentBlock = when (currentHour) {
            in 0..5 -> 0 to 6
            in 6..11 -> 6 to 12
            in 12..17 -> 12 to 18
            else -> 18 to 0
        }

        // Hvis vi er i en påbegynt blokk, lag en spesialblokk fra nåværende time til slutten av blokken
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

            // Add remaining whole blocks for the day
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
        // Dette er for fremtidige dager - bruk standard blokker
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