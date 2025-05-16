package no.uio.ifi.in2000.team46.domain.forbud
//WARNINGS: this class is not used, but is part of the forbud api which is not used in the app
data class ForbudOmråde(
    val geometry: Geometry,
    val objectId: Int,
    val info: String
)

data class Geometry(
    val type: String, // "Point"
    val coordinates: List<Double> // [longitude, latitude]
)
