package no.uio.ifi.in2000.team46.data.remote.weather

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.HttpURLConnection
import no.uio.ifi.in2000.team46.domain.model.weather.WeatherData
import no.uio.ifi.in2000.team46.domain.model.weather.WeatherDetails
import android.util.Log

class WeatherService {
    companion object {
        private const val BASE_URL = "https://api.met.no/weatherapi/locationforecast/2.0"
    }

    suspend fun getWeatherData(lat: Double, lon: Double): WeatherData = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/compact?lat=$lat&lon=$lon")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "MetAlerts/1.0")
            connection.connectTimeout = 5000 // 5 seconds timeout
            connection.readTimeout = 5000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)
                val properties = jsonObject.getJSONObject("properties")
                val timeseries = properties.getJSONArray("timeseries")
                
                if (timeseries.length() == 0) {
                    Log.w("WeatherService", "No weather data available for coordinates: lat=$lat, lon=$lon")
                    return@withContext WeatherData(null, null)
                }
                
                val firstTimeStep = timeseries.getJSONObject(0)
                val data = firstTimeStep.getJSONObject("data")

                val instant = data.getJSONObject("instant")
                val details = instant.getJSONObject("details")
                val temperature = details.getDouble("air_temperature")

                val next1Hours = data.optJSONObject("next_1_hours")
                val symbolCode = next1Hours?.getJSONObject("summary")?.getString("symbol_code")

                WeatherData(temperature, symbolCode)
            } else {
                Log.e("WeatherService", "API call failed with status code: ${connection.responseCode}")
                WeatherData(null, null)
            }
        } catch (e: Exception) {
            Log.e("WeatherService", "Error fetching weather data: ${e.message}")
            WeatherData(null, null)
        }
    }

    suspend fun getWeatherDetails(lat: Double, lon: Double): WeatherDetails = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/complete?lat=$lat&lon=$lon")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "MetAlerts/1.0")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
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
                val weatherDescription = getWeatherDescription(symbolCode)

                WeatherDetails(
                    temperature = temperature,
                    feelsLike = feelsLike,
                    highTemp = highTemp,
                    lowTemp = lowTemp,
                    symbolCode = symbolCode,
                    description = weatherDescription
                )
            } else {
                WeatherDetails(
                    temperature = null,
                    feelsLike = null,
                    highTemp = null,
                    lowTemp = null,
                    symbolCode = null,
                    description = null
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            WeatherDetails(
                temperature = null,
                feelsLike = null,
                highTemp = null,
                lowTemp = null,
                symbolCode = null,
                description = null
            )
        }
    }

    private fun getWeatherDescription(symbolCode: String?): String {
        return when {
            symbolCode == null -> "Weather data not available"
            symbolCode.contains("clearsky") -> "Clear sky"
            symbolCode.contains("fair") -> "Fair"
            symbolCode.contains("partlycloudy") -> "Partly cloudy"
            symbolCode.contains("cloudy") -> "Cloudy"
            symbolCode.contains("rainshowers") -> "Rain showers"
            symbolCode.contains("rain") -> "Rain"
            symbolCode.contains("snowshowers") -> "Snow showers"
            symbolCode.contains("snow") -> "Snow"
            symbolCode.contains("sleet") -> "Sleet"
            symbolCode.contains("fog") -> "Fog"
            symbolCode.contains("thunder") -> "Thunder"
            else -> "Variable weather"
        }
    }
}
