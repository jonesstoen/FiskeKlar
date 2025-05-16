package no.uio.ifi.in2000.team46.domain.grib

// this file defines the PrecipitationPoint data class representing total precipitation at a specific location and time
// used for rendering or analyzing gridded precipitation data from grib files

data class PrecipitationPoint(
    val lon: Double,
    val lat: Double,
    /**
     * Value of total precipitation at this grid point.
     * According to the GRIB metadata this is in meters of water
     */
    val precipitation: Double,
    val timestamp : Long
)