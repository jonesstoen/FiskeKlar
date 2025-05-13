package no.uio.ifi.in2000.team46.domain.ais

// represents a single AIS position report for a vessel
data class AisVesselPosition(
    val courseOverGround: Double?,
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val rateOfTurn: Double?,
    val shipType: Int,
    val speedOverGround: Double?,
    val trueHeading: Int?,
    val navigationalStatus: Int?,
    val mmsi: Long,
    val msgtime: String
)
// represents a historical track for a vessel based on its MMSI
data class AisVesselTrack(
    val mmsi: Long,
    val positions: List<AisPosition>
)

// represents a single position point with timestamp
data class AisPosition(
    val lat: Double,
    val lon: Double,
    val timestamp: String
)