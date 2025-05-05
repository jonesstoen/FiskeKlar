package no.uio.ifi.in2000.team46.domain.metalerts

import com.google.gson.annotations.SerializedName

// Responsen over representeres som en FeatureCollection
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
    // "when" er et reservert ord, så vi bruker "timeInfo" (men beholder JSON-nøkkelen via @SerializedName)
    @SerializedName("when")
    val timeInfo: WhenInfo,
    @SerializedName("type")
    val type: String
)

data class Geometry(
    // Avhengig av type (Polygon vs MultiPolygon) kan koordinatene ha ulik dybde.
    // For enkelhets skyld kan du bruke en "Any"-type eller eventuelt definere en egendefinert deserializer.
    @SerializedName("coordinates")
    val coordinates: Any,
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

// Siden "when" er et Kotlin-nøkkelord, bruker vi "WhenInfo" for å mappe JSON-feltet "when"
data class WhenInfo(
    @SerializedName("interval")
    val interval: List<String>
)
