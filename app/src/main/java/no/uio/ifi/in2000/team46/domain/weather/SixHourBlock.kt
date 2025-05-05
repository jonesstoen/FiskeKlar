package no.uio.ifi.in2000.team46.domain.weather

data class SixHourBlock(
    val startHour: Int,
    val endHour: Int,
    val temperature: Double?,
    val symbolCode: String?,
    val windSpeed: Double?,
    val windDirection: Double?
)