package no.uio.ifi.in2000.team46.data.api.weather

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.HttpURLConnection
import no.uio.ifi.in2000.team46.domain.model.weather.WeatherData

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

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)
                val properties = jsonObject.getJSONObject("properties")
                val timeseries = properties.getJSONArray("timeseries")
                val firstTimeStep = timeseries.getJSONObject(0)
                val data = firstTimeStep.getJSONObject("data")

                val instant = data.getJSONObject("instant")
                val details = instant.getJSONObject("details")
                val temperature = details.getDouble("air_temperature")

                val next1Hours = data.optJSONObject("next_1_hours")
                val symbolCode = next1Hours?.getJSONObject("summary")?.getString("symbol_code")

                WeatherData(temperature, symbolCode)
            } else {
                WeatherData(null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            WeatherData(null, null)
        }
    }
}