package no.uio.ifi.in2000.team46.domain.grib

data class PrecipitationPoint(
    val lon: Double,
    val lat: Double,
    /**
     * Value of total precipitation at this grid point.
     * According to the GRIB metadata this is in meters of water
     * (often you’ll want to multiply by 1000 for mm).
     */
    val precipitation: Double
)