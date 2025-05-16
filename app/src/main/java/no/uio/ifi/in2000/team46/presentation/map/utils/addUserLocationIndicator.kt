package no.uio.ifi.in2000.team46.presentation.map.utils

import android.animation.ValueAnimator
import android.graphics.Color
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression

// summary: adds user location indicator as a pulsating circle layer on the map using geojson source and animations

fun addUserLocationIndicator(map: MapLibreMap, style: Style, latitude: Double, longitude: Double) {
    val sourceId = "user-location-source"
    val layerId = "user-location-layer"

    // remove existing layer and source if present
    style.getLayer(layerId)?.let { style.removeLayer(it) }
    style.getSource(sourceId)?.let { style.removeSource(it) }

    // create geojson source with point at user location
    val point = Point.fromLngLat(longitude, latitude)
    val geoJsonSource = GeoJsonSource(sourceId, Feature.fromGeometry(point))
    style.addSource(geoJsonSource)

    // create circle layer to represent user location
    val circleLayer = CircleLayer(layerId, sourceId).withProperties(
        // set circle color to typical blue
        PropertyFactory.circleColor(Color.parseColor("#3bb2d0")),
        // interpolate radius based on zoom level
        PropertyFactory.circleRadius(
            Expression.interpolate(
                Expression.linear(), Expression.zoom(),
                Expression.stop(0, 5f), // radius at zoom level 0
                Expression.stop(22, 10f) // radius at zoom level 22
            )
        ),
        // set opacity for the circle
        PropertyFactory.circleOpacity(0.8f),
        // set stroke color to light sky blue
        PropertyFactory.circleStrokeColor(Color.parseColor("#87CEEB")),
        // set stroke width
        PropertyFactory.circleStrokeWidth(2f)
    )
    style.addLayer(circleLayer)

    // add pulsating animation effect by updating circle radius
    val animator = ValueAnimator.ofFloat(3f, 5f).apply {
        duration = 1000 // animation duration in ms
        repeatMode = ValueAnimator.REVERSE
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            circleLayer.setProperties(PropertyFactory.circleRadius(animatedValue))
        }
    }
    animator.start()

    // add listener to adjust radius when camera idle (zoom changed)
    map.addOnCameraIdleListener {
        val zoom = map.cameraPosition.zoom
        val radius = interpolateRadius(zoom)
        circleLayer.setProperties(PropertyFactory.circleRadius(radius))
    }
}

// interpolate radius based on zoom level thresholds
fun interpolateRadius(zoom: Double): Float {
    return when {
        zoom < 10 -> 5f
        zoom < 15 -> 10f
        else -> 15f
    }
}
