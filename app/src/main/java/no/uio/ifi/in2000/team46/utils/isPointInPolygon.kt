package no.uio.ifi.in2000.team46.utils

import org.maplibre.android.geometry.LatLng

/// Function to check if a point is inside a polygon, used for checkking if
/// user is inside a polygon from metalerts

fun isPointInPolygon(pointLat: Double, pointLon: Double, polygon: List<Pair<Double, Double>>): Boolean {
    var inside = false
    var j = polygon.lastIndex
    for (i in polygon.indices) {
        val xi = polygon[i].first
        val yi = polygon[i].second
        val xj = polygon[j].first
        val yj = polygon[j].second

        val intersect = ((yi > pointLat) != (yj > pointLat)) &&
                (pointLon < (xj - xi) * (pointLat - yi) / (yj - yi + 0.0) + xi)
        if (intersect)
            inside = !inside
        j = i
    }
    return inside
}