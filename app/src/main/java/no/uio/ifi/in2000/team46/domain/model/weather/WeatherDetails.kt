package no.uio.ifi.in2000.team46.domain.model.weather

data class WeatherDetails(
    val temperature: Double?,
    val feelsLike: Double?,
    val highTemp: Double?,
    val lowTemp: Double?,
    val symbolCode: String?,
    val description: String?
) 