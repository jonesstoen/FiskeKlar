package no.uio.ifi.in2000.team46.domain.weather

data class WeatherDetails(
    val temperature: Double?,
    val feelsLike: Double?,
    val highTemp: Double?,
    val lowTemp: Double?,
    val symbolCode: String?,
    val description: String?,
    val windSpeed: Double?,
    val windDirection: Double?,
    val weatherSymbol: String?
)