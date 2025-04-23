package no.uio.ifi.in2000.team46.domain.model.metalerts


data class SunInfo(
    val properties: SunInfoProperties
)

data class SunInfoProperties(
    val sunrise: Sunrise,
    val sunset: Sunset
)

data class Sunrise(
    val time: String
)

data class Sunset(
    val time: String
)