package no.uio.ifi.in2000.team46.data.remote.geocoding

import com.google.gson.annotations.SerializedName

data class GeocodingResponse(
    @SerializedName("features")
    val features: List<Feature>
)

data class Feature(
    @SerializedName("type")
    val type: String,
    @SerializedName("properties")
    val properties: Properties,
    @SerializedName("geometry")
    val geometry: Geometry
)

data class Properties(
    @SerializedName("name")
    val name: String,
    @SerializedName("country")
    val country: String?,
    @SerializedName("region")
    val region: String?,
    @SerializedName("locality")
    val locality: String?
)

data class Geometry(
    @SerializedName("type")
    val type: String,
    @SerializedName("coordinates")
    val coordinates: List<Double>
)