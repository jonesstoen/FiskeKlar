package no.uio.ifi.in2000.team46.map.utils
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

fun addUserLocationIndicator(map: MapLibreMap,style: Style, latitude: Double, longitude: Double) {
    val sourceId = "user-location-source"
    val layerId = "user-location-layer"

    // Remove an existing source/layer if they exist (optional)
    style.getLayer(layerId)?.let { style.removeLayer(it) }
    style.getSource(sourceId)?.let { style.removeSource(it) }


    // Create a GeoJSON source with a point feature at the user's location.
    val point = Point.fromLngLat(longitude, latitude)
    val geoJsonSource = GeoJsonSource(sourceId, Feature.fromGeometry(point))
    style.addSource(geoJsonSource)

    // Create a CircleLayer to display the location as a blue circle.
    val circleLayer = CircleLayer(layerId, sourceId).withProperties(
        PropertyFactory.circleColor(Color.parseColor("#3bb2d0")), // Typical blue
        PropertyFactory.circleRadius(
            Expression.interpolate(
                Expression.linear(), Expression.zoom(),
                Expression.stop(0, 5f), // Radius at zoom level 0
                Expression.stop(22, 10f) // Radius at zoom level 22
            )
        ),
        PropertyFactory.circleOpacity(0.8f),
        PropertyFactory.circleStrokeColor(Color.parseColor("#87CEEB")), // Black stroke
        PropertyFactory.circleStrokeWidth(2f) // Stroke width
    )
    style.addLayer(circleLayer)

    // Add pulsating effect using ValueAnimator
    val animator = ValueAnimator.ofFloat(3f, 5f).apply {
        duration = 1000
        repeatMode = ValueAnimator.REVERSE
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            circleLayer.setProperties(PropertyFactory.circleRadius(animatedValue))
        }
    }
    animator.start()

    // Add a listener to track zoom level changes
    map.addOnCameraIdleListener(object : MapLibreMap.OnCameraIdleListener {
        override fun onCameraIdle() {
            val zoom = map.cameraPosition.zoom
            val radius = interpolateRadius(zoom)
            circleLayer.setProperties(PropertyFactory.circleRadius(radius))
        }
    })
}

// Function to interpolate radius based on zoom level
fun interpolateRadius(zoom: Double): Float {
    return when {
        zoom < 10 -> 5f
        zoom < 15 -> 10f
        else -> 15f
    }
}