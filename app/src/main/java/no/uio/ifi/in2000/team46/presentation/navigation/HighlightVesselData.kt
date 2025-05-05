package no.uio.ifi.in2000.team46.presentation.navigation

data class HighlightVesselData(
    val userLat: Double,
    val userLon: Double,
    val vesselLat: Double,
    val vesselLon: Double,
    val vesselName: String,
    val shipType: Int
)