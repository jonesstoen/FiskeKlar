package no.uio.ifi.in2000.team46.domain.model.weather

data class HourlyForecast(
    val time: String,
    val temperature: Double?,
    val symbolCode: String?,
    val windSpeed: Double?,
    val windDirection: Double?
)