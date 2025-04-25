package no.uio.ifi.in2000.team46.domain.model.weather

data class DailyForecast(
    val date: String,
    val hourlyForecasts: List<HourlyForecast>,
    val maxTemp: Double?,
    val minTemp: Double?,
    val symbolCode: String?
)