package no.uio.ifi.in2000.team46.domain.grib

data class WaveVector(
    val lon: Double,
    val lat: Double,
    val height: Double,     // significant wave height (swh)
    val direction: Double,  // gjenværende bølgeretning (mwd + 180 mod 360)
    val timestamp: Long
)
