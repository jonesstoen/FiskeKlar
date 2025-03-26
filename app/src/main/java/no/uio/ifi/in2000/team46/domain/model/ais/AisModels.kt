package no.uio.ifi.in2000.team46.domain.model.ais



import com.google.gson.annotations.SerializedName

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

data class AisVesselTrack(
    val mmsi: Long,
    val positions: List<AisPosition>
)

data class AisPosition(
    val lat: Double,
    val lon: Double,
    val timestamp: String
)