package no.uio.ifi.in2000.team46.domain.metalerts

import com.google.gson.annotations.SerializedName

// this file defines the structure of the metalerts api response based on the featurecollection format from yr/met.no
// used for decoding weather alert data including geometry, timing, severity and other metadata


data class MetAlertsResponse(
    @SerializedName("features")
    val features: List<Feature>,
    @SerializedName("lang")
    val lang: String,
    @SerializedName("lastChange")
    val lastChange: String,
    @SerializedName("type")
    val type: String
)

data class Feature(
    @SerializedName("geometry")
    val geometry: Geometry,
    @SerializedName("properties")
    val properties: Properties,
    @SerializedName("when")
    val timeInfo: WhenInfo,
    @SerializedName("type")
    val type: String
)

data class Geometry(
    @SerializedName("coordinates")
    val coordinates: Any, // coordinates of the shape (can be polygon or multipolygon)
    @SerializedName("type")
    val type: String
)

data class Properties(
    @SerializedName("altitude_above_sea_level")
    val altitudeAboveSeaLevel: Int,
    @SerializedName("area")
    val area: String,
    @SerializedName("awarenessResponse")
    val awarenessResponse: String,
    @SerializedName("awarenessSeriousness")
    val awarenessSeriousness: String,
    @SerializedName("awareness_level")
    val awarenessLevel: String,
    @SerializedName("awareness_type")
    val awarenessType: String,
    @SerializedName("ceiling_above_sea_level")
    val ceilingAboveSeaLevel: Int,
    @SerializedName("certainty")
    val certainty: String,
    @SerializedName("consequences")
    val consequences: String,
    @SerializedName("contact")
    val contact: String,
    @SerializedName("county")
    val county: List<String>,
    @SerializedName("description")
    val description: String,
    @SerializedName("event")
    val event: String,
    @SerializedName("eventAwarenessName")
    val eventAwarenessName: String,
    @SerializedName("eventEndingTime")
    val eventEndingTime: String,
    @SerializedName("geographicDomain")
    val geographicDomain: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("instruction")
    val instruction: String,
    @SerializedName("resources")
    val resources: List<Resource>,
    @SerializedName("riskMatrixColor")
    val riskMatrixColor: String,
    @SerializedName("severity")
    val severity: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("triggerLevel")
    val triggerLevel: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("web")
    val web: String
)

data class Resource(
    @SerializedName("description")
    val description: String,
    @SerializedName("mimeType")
    val mimeType: String,
    @SerializedName("uri")
    val uri: String
)

// because "when" is a Kotlin keyword, we use "WhenInfo" to map the JSON field "when"
data class WhenInfo(
    @SerializedName("interval")
    val interval: List<String>
)
