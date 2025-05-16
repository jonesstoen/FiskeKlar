package no.uio.ifi.in2000.team46.data.remote.api

import com.google.gson.annotations.SerializedName

// geocodingresponse maps the structure of a geojson-style api response for geocoding
// it includes feature metadata and coordinates extracted from the response

data class GeocodingResponse(
    @SerializedName("features")
    val features: List<Feature> // list of matched locations from the geocoding query
)

data class Feature(
    @SerializedName("type")
    val type: String, // geojson feature type (e.g. "Feature")
    @SerializedName("properties")
    val properties: Properties, // metadata about the location
    @SerializedName("geometry")
    val geometry: Geometry // geographical data (coordinates)
)

data class Properties(
    @SerializedName("name")
    val name: String, // name of the location
    @SerializedName("country")
    val country: String?, // optional country name
    @SerializedName("region")
    val region: String?, // optional region name
    @SerializedName("locality")
    val locality: String? // optional locality name
)

data class Geometry(
    @SerializedName("type")
    val type: String, // geometry type (usually "Point")
    @SerializedName("coordinates")
    val coordinates: List<Double> // [longitude, latitude]
)
