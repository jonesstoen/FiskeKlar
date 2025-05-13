package no.uio.ifi.in2000.team46.domain.grib

// this file defines the CurrentVector data class which represents ocean current direction and speed at a specific location and time
// implements the Vector interface to ensure consistency across geospatial vector types

data class CurrentVector(
    override val lon: Double,
    override val lat: Double,
    override val speed: Double,
    override val direction: Double,
    val timestamp: Long
): Vector