package no.uio.ifi.in2000.team46.data.remote.weather

import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.HttpURLConnection
import no.uio.ifi.in2000.team46.domain.model.weather.WeatherData
import no.uio.ifi.in2000.team46.domain.model.weather.WeatherDetails
import no.uio.ifi.in2000.team46.utils.weather.WeatherDescriptionMapper
import android.util.Log
import androidx.annotation.RequiresApi
import no.uio.ifi.in2000.team46.domain.model.weather.DailyForecast
import no.uio.ifi.in2000.team46.domain.model.weather.HourlyForecast

/**
 * Service-klasse for å hente værdata fra Met API
 */
class WeatherService {
    companion object {
        private const val BASE_URL = "https://api.met.no/weatherapi/locationforecast/2.0"
        private const val USER_AGENT = "MetAlerts/1.0"
        private const val TIMEOUT = 5000 // 5 seconds
    }

    suspend fun getWeatherData(latitude: Double, longitude: Double): WeatherData {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=$latitude&lon=$longitude")
                val connection = createConnection(url)
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)
                
                val timeseries = jsonObject.getJSONObject("properties")
                    .getJSONArray("timeseries")
                    .getJSONObject(0)
                
                val instant = timeseries.getJSONObject("data")
                    .getJSONObject("instant")
                    .getJSONObject("details")
                
                val temperature = instant.optDouble("air_temperature")
                
                val symbolCode = timeseries.getJSONObject("data")
                    .optJSONObject("next_1_hours")
                    ?.getJSONObject("summary")
                    ?.optString("symbol_code")
                
                WeatherData(
                    temperature = temperature,
                    symbolCode = symbolCode,
                    latitude = latitude,
                    longitude = longitude
                )
            } catch (e: Exception) {
                Log.e("WeatherService", "Error fetching weather: ${e.message}")
                WeatherData(null, null, latitude, longitude)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getDetailedForecast(latitude: Double, longitude: Double): List<DailyForecast> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://api.met.no/weatherapi/locationforecast/2.0/complete?lat=$latitude&lon=$longitude")
                val connection = createConnection(url)
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)
                
                val timeseries = jsonObject.getJSONObject("properties").getJSONArray("timeseries")
                val forecasts = mutableListOf<HourlyForecast>()
                
                // Hent dagens dato
                val today = java.time.LocalDate.now()
                
                // Samle alle timesvarsler
                for (i in 0 until timeseries.length()) {
                    val timeseriesObj = timeseries.getJSONObject(i)
                    val time = timeseriesObj.getString("time")
                    val forecastDate = java.time.LocalDate.parse(time.substring(0, 10))
                    
                    // Hopp over datoer som er før dagens dato
                    if (forecastDate.isBefore(today)) {
                        continue
                    }
                    
                    val data = timeseriesObj.getJSONObject("data")
                    val instant = data.getJSONObject("instant").getJSONObject("details")
                    
                    val hourlyForecast = HourlyForecast(
                        time = time,
                        temperature = instant.optDouble("air_temperature"),
                        symbolCode = data.optJSONObject("next_1_hours")
                            ?.getJSONObject("summary")
                            ?.getString("symbol_code"),
                        windSpeed = instant.optDouble("wind_speed"),
                        windDirection = instant.optDouble("wind_from_direction")
                    )
                    forecasts.add(hourlyForecast)
                }
                
                // Grupper etter dato og sorter etter dato
                val dailyForecasts = forecasts
                    .groupBy { it.time.substring(0, 10) }
                    .map { (date, hourlyForecasts) ->
                        DailyForecast(
                            date = date,
                            hourlyForecasts = hourlyForecasts,
                            maxTemp = hourlyForecasts.mapNotNull { it.temperature }.maxOrNull(),
                            minTemp = hourlyForecasts.mapNotNull { it.temperature }.minOrNull(),
                            symbolCode = hourlyForecasts.firstNotNull { it.symbolCode }
                        )
                    }
                    .sortedBy { it.date }
                    .take(4) // Begrens til 4 dager (i dag + 3 neste)
                
                dailyForecasts
            } catch (e: Exception) {
                Log.e("WeatherService", "Error fetching detailed forecast: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun getWeatherDetails(latitude: Double, longitude: Double): WeatherDetails {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/complete?lat=$latitude&lon=$longitude")
                val connection = createConnection(url)

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    parseWeatherDetails(connection)
                } else {
                    createEmptyWeatherDetails()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                createEmptyWeatherDetails()
            }
        }
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

    private fun createConnection(url: URL): HttpURLConnection {
        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("User-Agent", USER_AGENT)
            connectTimeout = TIMEOUT
            readTimeout = TIMEOUT
        }
    }

    private fun parseWeatherDetails(connection: HttpURLConnection): WeatherDetails {
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(response)
        val properties = jsonObject.getJSONObject("properties")
        val timeseries = properties.getJSONArray("timeseries")
        
        // Fetch current weather
        val firstTimeStep = timeseries.getJSONObject(0)
        val currentData = firstTimeStep.getJSONObject("data")
        val instant = currentData.getJSONObject("instant").getJSONObject("details")
        
        val temperature = instant.getDouble("air_temperature")
        val feelsLike = instant.optDouble("air_temperature_feels_like", temperature)
        val windSpeed = instant.optDouble("wind_speed", 0.0)
        val windDirection = instant.optDouble("wind_from_direction", 0.0)
        
        // Find highest and lowest temperature for the day
        var highTemp = temperature
        var lowTemp = temperature
        
        // Iterate through forecasts for next 24 hours
        for (i in 0 until minOf(24, timeseries.length())) {
            val timeStep = timeseries.getJSONObject(i)
            val timeData = timeStep.getJSONObject("data")
            val timeInstant = timeData.getJSONObject("instant").getJSONObject("details")
            val timeTemp = timeInstant.getDouble("air_temperature")
            
            highTemp = maxOf(highTemp, timeTemp)
            lowTemp = minOf(lowTemp, timeTemp)
        }
        
        // Get weather description
        val next1Hours = currentData.optJSONObject("next_1_hours")
        val symbolCode = next1Hours?.optJSONObject("summary")?.optString("symbol_code", "unknown")
        val weatherDescription = WeatherDescriptionMapper.getWeatherDescription(symbolCode)

        return WeatherDetails(
            temperature = temperature,
            feelsLike = feelsLike,
            highTemp = highTemp,
            lowTemp = lowTemp,
            symbolCode = symbolCode,
            description = weatherDescription,
            windSpeed = windSpeed,
            windDirection = windDirection,
            weatherSymbol = symbolCode
        )
    }

    private fun createEmptyWeatherDetails() = WeatherDetails(
        temperature = null,
        feelsLike = null,
        highTemp = null,
        lowTemp = null,
        symbolCode = null,
        description = null,
        windSpeed = null,
        windDirection = null,
        weatherSymbol = null
    )
}
