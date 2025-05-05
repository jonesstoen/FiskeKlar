package no.uio.ifi.in2000.team46.domain.grib

data class CurrentVector(
    override val lon: Double,
    override val lat: Double,
    override val speed: Double,
    override val direction: Double
): Vector