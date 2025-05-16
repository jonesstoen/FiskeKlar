package no.uio.ifi.in2000.team46.domain.grib

// this file defines the WaveVector data class which represents wave height and direction at a specific location and time
// used for visualizing or processing wave forecast data extracted from grib files

data class WaveVector(
    val lon: Double,
    val lat: Double,
    val height: Double,     // significant wave height (swh)
    val direction: Double,
    val timestamp: Long
)
