package no.uio.ifi.in2000.team46.domain.grib

data class DriftVector(
    val lon: Double,
    val lat: Double,
    val speed: Double,              // total drift speed (vind + strøm)
    val direction: Double,          // total drift direction
    val windSpeed: Double,          // lagrer vindhastigheten
    val windDirection: Double,      // vindretning
    val currentSpeed: Double,       // strømstyrke
    val currentDirection: Double    // strømretning
)