package no.uio.ifi.in2000.team46.domain.grib

data class WindVector(
    override val lon: Double,
    override val lat: Double,
    override val speed: Double,
    override val direction: Double,
    val timestamp: Long,
) : Vector