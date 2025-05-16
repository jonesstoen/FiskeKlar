package no.uio.ifi.in2000.team46.data.remote.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.HttpURLConnection
import no.uio.ifi.in2000.team46.domain.weather.WeatherData
import no.uio.ifi.in2000.team46.domain.weather.WeatherDetails
import no.uio.ifi.in2000.team46.utils.metalerts.WeatherDescriptionMapper
import android.util.Log
import no.uio.ifi.in2000.team46.domain.weather.DailyForecast
import no.uio.ifi.in2000.team46.domain.weather.HourlyForecast

// weatherservice fetches and parses weather data from met norway's locationforecast api
// it provides basic weather, detailed forecast and summarized weather details

class WeatherService {
    companion object {
        private const val BASE_URL = "https://in2000.api.met.no/weatherapi/locationforecast/2.0"
        private const val USER_AGENT = "MetAlerts/1.0"
        private const val TIMEOUT = 5000 // 5 seconds
    }

    // fetches temperature and symbol code from the "compact" endpoint
    suspend fun getWeatherData(latitude: Double, longitude: Double): WeatherData {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/compact?lat=$latitude&lon=$longitude")
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

    // fetches 3-day detailed forecast and groups it by day
    suspend fun getDetailedForecast(latitude: Double, longitude: Double): List<DailyForecast> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/complete?lat=$latitude&lon=$longitude")
                val connection = createConnection(url)
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)

                val timeseries = jsonObject.getJSONObject("properties").getJSONArray("timeseries")
                val forecasts = mutableListOf<HourlyForecast>()
                val today = java.time.LocalDate.now()

                for (i in 0 until timeseries.length()) {
                    val obj = timeseries.getJSONObject(i)
                    val time = obj.getString("time")
                    val forecastDate = java.time.LocalDate.parse(time.substring(0, 10))

                    if (forecastDate.isBefore(today)) continue
                    if (forecastDate.isAfter(today.plusDays(2))) break

                    val data = obj.getJSONObject("data")
                    val instant = data.getJSONObject("instant").getJSONObject("details")

                    val symbolCode = data.optJSONObject("next_1_hours")
                        ?.getJSONObject("summary")?.optString("symbol_code")
                        ?: data.optJSONObject("next_6_hours")
                            ?.getJSONObject("summary")?.optString("symbol_code")
                        ?: data.optJSONObject("next_12_hours")
                            ?.getJSONObject("summary")?.optString("symbol_code")

                    val hourly = HourlyForecast(
                        time = time,
                        temperature = instant.optDouble("air_temperature"),
                        symbolCode = symbolCode,
                        windSpeed = instant.optDouble("wind_speed"),
                        windDirection = instant.optDouble("wind_from_direction")
                    )
                    forecasts.add(hourly)
                }

                forecasts
                    .groupBy { it.time.substring(0, 10) }
                    .map { (date, hourly) ->
                        DailyForecast(
                            date = date,
                            hourlyForecasts = hourly.sortedBy { it.time },
                            maxTemp = hourly.mapNotNull { it.temperature }.maxOrNull(),
                            minTemp = hourly.mapNotNull { it.temperature }.minOrNull(),
                            symbolCode = hourly.firstNotNull { it.symbolCode }
                        )
                    }
                    .sortedBy { it.date }
                    .take(3)
            } catch (e: Exception) {
                Log.e("WeatherService", "Error fetching detailed forecast: ${e.message}")
                emptyList()
            }
        }
    }

    // fetches current weather and 24h min/max stats from the "complete" endpoint
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

    // finds first non-null match for a given selector
    private fun <T, R> List<T>.firstNotNull(selector: (T) -> R?): R? {
        for (element in this) {
            val result = selector(element)
            if (result != null) return result
        }
        return null
    }

    // sets up a connection with GET method and required headers
    private fun createConnection(url: URL): HttpURLConnection {
        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("User-Agent", USER_AGENT)
            connectTimeout = TIMEOUT
            readTimeout = TIMEOUT
        }
    }

    // parses current weather and aggregates temperature stats for 24h
    private fun parseWeatherDetails(connection: HttpURLConnection): WeatherDetails {
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(response)
        val properties = jsonObject.getJSONObject("properties")
        val timeseries = properties.getJSONArray("timeseries")

        val firstTimeStep = timeseries.getJSONObject(0)
        val currentData = firstTimeStep.getJSONObject("data")
        val instant = currentData.getJSONObject("instant").getJSONObject("details")

        val temperature = instant.getDouble("air_temperature")
        val feelsLike = instant.optDouble("air_temperature_feels_like", temperature)
        val windSpeed = instant.optDouble("wind_speed", 0.0)
        val windDirection = instant.optDouble("wind_from_direction", 0.0)

        var highTemp = temperature
        var lowTemp = temperature

        for (i in 0 until minOf(24, timeseries.length())) {
            val timeStep = timeseries.getJSONObject(i)
            val timeInstant = timeStep.getJSONObject("data")
                .getJSONObject("instant").getJSONObject("details")
            val timeTemp = timeInstant.getDouble("air_temperature")
            highTemp = maxOf(highTemp, timeTemp)
            lowTemp = minOf(lowTemp, timeTemp)
        }

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

    // fallback object if no data is available or errors occur
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
