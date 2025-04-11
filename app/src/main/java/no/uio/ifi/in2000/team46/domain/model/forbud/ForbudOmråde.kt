package no.uio.ifi.in2000.team46.domain.model.forbud

data class ForbudOmråde(
    val geometry: Geometry,
    val objectId: Int,
    val info: String
)

data class Geometry(
    val type: String, // "Point"
    val coordinates: List<Double> // [longitude, latitude]
)
