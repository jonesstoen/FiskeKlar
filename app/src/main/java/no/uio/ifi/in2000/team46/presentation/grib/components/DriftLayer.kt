package no.uio.ifi.in2000.team46.presentation.grib.components

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.*
import no.uio.ifi.in2000.team46.R
import no.uio.ifi.in2000.team46.data.local.parser.DriftVector
import no.uio.ifi.in2000.team46.data.local.parser.calculateDriftImpact
import no.uio.ifi.in2000.team46.data.repository.Result
import no.uio.ifi.in2000.team46.presentation.grib.DriftViewModel
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point


@Composable
fun DriftLayer(
    driftViewModel: DriftViewModel,
    map: MapLibreMap,
    mapView: MapView,
    onDriftVectorSelected: (speed: Double, direction: Double, driftImpact: Double, point: Point) -> Unit,

    onDriftVectorCleared: () -> Unit
) {
    val isVisible by driftViewModel.isLayerVisible.collectAsState()
    val driftResult by driftViewModel.driftData.collectAsState(initial = null)

    if (isVisible && driftResult is Result.Success) {
        val driftData = (driftResult as Result.Success<List<DriftVector>>).data

        LaunchedEffect(driftData) {
            map.getStyle { style ->
                val sourceId = "drift_source"
                val layerId = "drift_layer"
                val iconId = "drift_icon"

                if (style.getImage(iconId) == null) {
                    val arrowBitmap = BitmapFactory.decodeResource(
                        mapView.context.resources, R.drawable.drift_arrow_icon
                    )
                    style.addImage(iconId, arrowBitmap, false)
                }

                val features = driftData.map {
                    Feature.fromGeometry(Point.fromLngLat(it.lon, it.lat)).apply {
                        addNumberProperty("direction", it.direction)
                        addNumberProperty("speed", it.speed)
                    }
                }
                val featureCollection = FeatureCollection.fromFeatures(features)

                val existingSource = style.getSourceAs<GeoJsonSource>(sourceId)
                if (existingSource != null) {
                    existingSource.setGeoJson(featureCollection.toJson())
                } else {
                    style.addSource(GeoJsonSource(sourceId, featureCollection.toJson()))
                }

                if (style.getLayer(layerId) == null) {
                    style.addLayer(
                        SymbolLayer(layerId, sourceId).withProperties(
                            iconImage(iconId),
                            iconRotationAlignment("map"),
                            iconRotate(Expression.get("direction")),
                            iconAllowOverlap(true),
                            iconSize(
                                Expression.interpolate(
                                    Expression.linear(), Expression.zoom(),
                                    Expression.stop(0.0, 0.20),
                                    Expression.stop(8.0, 0.30),
                                    Expression.stop(12.0, 0.40),
                                    Expression.stop(16.0, 0.50),
                                    Expression.stop(20.0, 0.60)
                                )
                            )
                        )
                    )
                }
            }
        }
        map.addOnMapClickListener { latLng ->
            val pixel = map.projection.toScreenLocation(latLng)
            val features = map.queryRenderedFeatures(pixel, "drift_layer")

            if (features.isNotEmpty()) {
                val feature = features.first()
                val speed = feature.getNumberProperty("speed")?.toDouble() ?: 0.0
                val direction = feature.getNumberProperty("direction")?.toDouble() ?: 0.0

                // Finn driftVector basert på at dette er samme punkt som feature!
                val matchingDriftVector = driftData.find {
                    it.lon == (feature.geometry() as? Point)?.longitude()
                            && it.lat == (feature.geometry() as? Point)?.latitude()
                }

                matchingDriftVector?.let { vector ->
                    val driftImpact = calculateDriftImpact(
                        windSpeed       = vector.windSpeed,
                        windDirection   = vector.windDirection,
                        currentSpeed    = vector.currentSpeed,
                        boatLength      = 7.5,
                        boatBeam        = 2.5,
                        draft           = 1.0,
                        freeboardHeight = 0.8,
                        boatMass        = 2.5,
                        waveHeight      = 1.0,
                        wavePeriod      = 6.0
                    )

                    val pointGeoJson = Point.fromLngLat(latLng.longitude, latLng.latitude)
                    onDriftVectorSelected(vector.speed, vector.direction, driftImpact, pointGeoJson)
                }
            } else {
                onDriftVectorCleared()
            }
            true
        }

    }

    // Fjern laget når det ikke er synlig
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            map.getStyle { style ->
                style.getLayer("drift_layer")?.let { style.removeLayer(it) }
                style.getSource("drift_source")?.let { style.removeSource(it) }
            }
        }
    }
}
