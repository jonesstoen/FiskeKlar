package no.uio.ifi.in2000.team46.domain.model.weather

data class WeatherData(
    val temperature: Double?,
    val symbolCode: String?,
    val latitude: Double? = null,
    val longitude: Double? = null
)