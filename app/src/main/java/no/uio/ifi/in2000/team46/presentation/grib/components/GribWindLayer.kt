package no.uio.ifi.in2000.team46.presentation.grib.components

// no/uio/ifi/in2000/team46/presentation/grib/components/GribWindLayer.kt

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.*
import no.uio.ifi.in2000.team46.data.local.parser.WindVector
import no.uio.ifi.in2000.team46.data.repository.Result
import no.uio.ifi.in2000.team46.presentation.grib.GribViewModel
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import no.uio.ifi.in2000.team46.R
import org.maplibre.android.style.layers.SymbolLayer

@Composable
fun GribWindLayer(
    gribViewModel: GribViewModel,
    map: MapLibreMap,
    mapView: MapView,
    threshold: Double = 1.0,
    filterVectors: Boolean = false,
    filterStep: Int = 3
) {
    val isVisible by gribViewModel.isLayerVisible.collectAsState()
    val windResult by gribViewModel.windData.collectAsState(initial = null)

    if (isVisible && windResult is Result.Success) {
        val windData = (windResult as Result.Success<List<WindVector>>).data

        LaunchedEffect(windData) {
            map.getStyle { style ->
                val sourceId = "wind_source"
                val layerId = "wind_layer"
                val iconId = "wind_icon"

                if (style.getImage(iconId) == null) {
                    val arrowBitmap = BitmapFactory.decodeResource(
                        mapView.context.resources, R.drawable.ic_wind_arrow
                    )
                    style.addImage(iconId, arrowBitmap, true)
                }

                val filteredData = if (filterVectors) {
                    windData.filterIndexed { index, _ -> index % filterStep == 0 }
                } else {
                    windData
                }

                val features = filteredData.mapNotNull { v ->
                    Feature.fromGeometry(Point.fromLngLat(v.lon, v.lat)).apply {
                        addNumberProperty("direction", v.direction)
                        addNumberProperty("speed", v.speed)
                    }
                }
                val featureCollection = FeatureCollection.fromFeatures(features.toTypedArray())

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
                            iconAllowOverlap(false),
                            iconIgnorePlacement(false),
                            iconRotate(Expression.get("direction")),
                            iconSize(
                                Expression.interpolate(
                                    Expression.linear(), Expression.get("speed"),
                                    Expression.stop(0.0, 0.05),
                                    Expression.stop(20.0, 0.2)
                                )
                            ),
                            iconColor(
                                Expression.step(
                                    Expression.get("speed"),
                                    Expression.color(0xFF0000FF.toInt()),
                                    Expression.literal(threshold),
                                    Expression.color(0xFFFF0000.toInt())
                                )
                            )
                        )
                    )
                }
            }
        }
    }

    // Rydd opp hvis laget slås av
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            map.getStyle { style ->
                style.getLayer("wind_layer")?.let { style.removeLayer(it) }
                style.getSource("wind_source")?.let { style.removeSource(it) }
            }
        }
    }
}
