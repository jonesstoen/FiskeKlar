package no.uio.ifi.in2000.team46.presentation.grib.components

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import no.uio.ifi.in2000.team46.R
import no.uio.ifi.in2000.team46.data.repository.Result
import no.uio.ifi.in2000.team46.domain.grib.WindVector
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.GribViewModel
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.android.style.layers.SymbolLayer

@Composable
fun GribWindLayer(
    gribViewModel: GribViewModel,
    map: MapLibreMap,
    mapView: MapView,
    filterVectors: Boolean = false
) {
    val isVisible by gribViewModel.isLayerVisible.collectAsState()
    val windResult by gribViewModel.windData.collectAsState(initial = null)
    val threshold by gribViewModel.windThreshold.collectAsState()

    // observe zoom level in a reactive way using polling
    val zoomState = remember { mutableStateOf(0.0) }
    LaunchedEffect(map, isVisible) {
        while (isVisible) {
            zoomState.value = map.cameraPosition.zoom
            delay(200) // poll zoom 5 times per second
        }
    }

    if (isVisible && windResult is Result.Success) {
        val windData = (windResult as Result.Success<List<WindVector>>).data

        LaunchedEffect(windData, threshold, zoomState.value) {
            map.getStyle { style ->
                val sourceId = "wind_source"
                val layerId = "wind_layer"

                // map wind speed thresholds to drawable icons (based on beaufort scale)
                val iconMap = mapOf(
                    0.2 to R.drawable.symbol_wind_speed_00,
                    1.5 to R.drawable.symbol_wind_speed_15,
                    3.3 to R.drawable.symbol_wind_speed_33,
                    5.4 to R.drawable.symbol_wind_speed_54,
                    7.9 to R.drawable.symbol_wind_speed_79,
                    10.7 to R.drawable.symbol_wind_speed_107,
                    13.8 to R.drawable.symbol_wind_speed_138,
                    17.1 to R.drawable.symbol_wind_speed_171,
                    20.7 to R.drawable.symbol_wind_speed_207,
                    24.4 to R.drawable.symbol_wind_speed_244,
                    28.4 to R.drawable.symbol_wind_speed_284,
                    32.6 to R.drawable.symbol_wind_speed_326,
                    Double.MAX_VALUE to R.drawable.symbol_wind_speed_max
                )

                // register both normal and red alert icons
                iconMap.forEach { (_, resId) ->
                    val baseName = mapView.context.resources.getResourceEntryName(resId)
                    if (style.getImage(baseName) == null) {
                        val bitmap = BitmapFactory.decodeResource(mapView.context.resources, resId)
                        style.addImage(baseName, bitmap)
                    }

                    val redName = "${baseName}_red"
                    val redResId = mapView.context.resources.getIdentifier(redName, "drawable", mapView.context.packageName)
                    if (redResId != 0 && style.getImage(redName) == null) {
                        val redBitmap = BitmapFactory.decodeResource(mapView.context.resources, redResId)
                        style.addImage(redName, redBitmap)
                    }
                }

                // dynamically adjust filter level based on zoom
                val dynamicStep = when {
                    zoomState.value >= 7.5 -> 1 // no filtering when zoomed in
                    zoomState.value >= 6.5 -> 3
                    zoomState.value >= 5.0 -> 5
                    else                   -> 8
                }


                val filteredData = if (filterVectors) {
                    windData.filterIndexed { index, _ -> index % dynamicStep == 0 }
                } else {
                    windData
                }
                Log.d("GribWindLayer", "zoom=${zoomState.value}, step=$dynamicStep, filtered=${filteredData.size}")


                // convert wind vectors to geojson features with icon selection
                val features = filteredData.mapNotNull { v ->
                    Feature.fromGeometry(Point.fromLngLat(v.lon, v.lat)).apply {
                        addNumberProperty("direction", v.direction)
                        addNumberProperty("speed", v.speed)
                        addStringProperty("icon", selectIcon(v.speed, threshold, iconMap, mapView))
                    }
                }

                val featureCollection = FeatureCollection.fromFeatures(features.toTypedArray())
                Log.d("GribWindLayer", "number of wind vectors: ${features.size}")

                val existingSource = style.getSourceAs<GeoJsonSource>(sourceId)
                if (existingSource != null) {
                    existingSource.setGeoJson(featureCollection.toJson())
                } else {
                    style.addSource(GeoJsonSource(sourceId, featureCollection.toJson()))
                }

                if (style.getLayer(layerId) == null) {
                    style.addLayer(
                        SymbolLayer(layerId, sourceId).withProperties(
                            iconImage(Expression.get("icon")),
                            iconAllowOverlap(true),
                            iconIgnorePlacement(true),
                            iconRotationAlignment("map"),
                            iconRotate(Expression.get("direction")),
                            iconSize(
                                Expression.interpolate(
                                    Expression.linear(), Expression.zoom(),
                                    Expression.stop(0.0, 0.10),
                                    Expression.stop(8.0, 0.20),
                                    Expression.stop(12.0, 0.30),
                                    Expression.stop(16.0, 0.40),
                                    Expression.stop(20.0, 0.50)
                                )
                            )
                        )
                    )
                }
            }
        }
    }

    // remove layer when visibility is false
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            map.getStyle { style ->
                style.getLayer("wind_layer")?.let { style.removeLayer(it) }
                style.getSource("wind_source")?.let { style.removeSource(it) }
            }
        }
    }

    // show hint if zoom level is too low for meaningful detail
    AnimatedVisibility(
        visible = isVisible && zoomState.value < 7.0,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "Zoom inn for flere detaljer",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

// selects icon name based on wind speed and threshold
private fun selectIcon(
    speed: Double,
    threshold: Double,
    iconMap: Map<Double, Int>,
    mapView: MapView
): String {
    val thresholdKey = iconMap.keys.sorted().firstOrNull { speed <= it } ?: Double.MAX_VALUE
    val resId = iconMap[thresholdKey] ?: error("no icon found for speed $speed")
    val baseName = mapView.context.resources.getResourceEntryName(resId)
    return if (speed > threshold) "${baseName}_red" else baseName
}
