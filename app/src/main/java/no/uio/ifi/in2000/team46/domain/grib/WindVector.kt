package no.uio.ifi.in2000.team46.domain.grib

// this file defines the WindVector data class which represents wind direction and speed at a specific location and time
// implements the Vector interface to provide consistency across vector types (wind, current, wave)

data class WindVector(
    override val lon: Double,
    override val lat: Double,
    override val speed: Double,
    override val direction: Double,
    val timestamp: Long,
) : Vector