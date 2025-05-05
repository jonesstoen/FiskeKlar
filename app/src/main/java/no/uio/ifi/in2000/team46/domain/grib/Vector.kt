package no.uio.ifi.in2000.team46.domain.grib

sealed interface Vector {
    val lon: Double
    val lat: Double
    val speed: Double
    val direction: Double
}