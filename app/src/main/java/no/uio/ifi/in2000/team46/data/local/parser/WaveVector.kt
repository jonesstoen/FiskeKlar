package no.uio.ifi.in2000.team46.data.local.parser

data class WaveVector(
    val lon: Double,
    val lat: Double,
    val height: Double,     // significant wave height (swh)
    val direction: Double   // gjenværende bølgeretning (mwd + 180 mod 360)
)
