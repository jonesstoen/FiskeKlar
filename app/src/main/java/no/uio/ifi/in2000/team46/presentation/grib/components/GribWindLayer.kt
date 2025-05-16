package no.uio.ifi.in2000.team46.presentation.grib.components

import android.annotation.SuppressLint
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

// this composable draws wind vector icons on the maplibre map based on grib wind data
// icons are selected dynamically based on speed, threshold and theme mode
// the layer is zoom-aware and reduces clutter when zoomed out

@SuppressLint("DiscouragedApi")
@Composable
fun GribWindLayer(
    gribViewModel: GribViewModel,
    map: MapLibreMap,
    mapView: MapView,
    filterVectors: Boolean = false,
    isDarkMode: Boolean
) {
    val isVisible by gribViewModel.isLayerVisible.collectAsState()
    val threshold by gribViewModel.windThreshold.collectAsState()
    val filteredVectors by gribViewModel.filteredWindVectors.collectAsState()

    // monitor zoom level
    val zoomState = remember { mutableDoubleStateOf(0.0) }
    LaunchedEffect(map, isVisible) {
        while (isVisible) {
            zoomState.doubleValue = map.cameraPosition.zoom
            delay(200)
        }
    }

    // draw vectors when layer is visible and data is ready
    if (isVisible && filteredVectors.isNotEmpty()) {
        LaunchedEffect(filteredVectors, threshold, zoomState.doubleValue) {
            map.getStyle { style ->
                val sourceId = "wind_source"
                val layerId = "wind_layer"

                // icon resource map based on speed intervals (Beaufort scale)
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

                // add icons to map style if not already present
                iconMap.forEach { (_, resId) ->
                    val baseName = mapView.context.resources.getResourceEntryName(resId)
                    val whiteName = "${baseName}_white"
                    val redName = "${baseName}_red"

                    val whiteResId = mapView.context.resources.getIdentifier(whiteName, "drawable", mapView.context.packageName)
                    if (whiteResId != 0 && style.getImage(whiteName) == null) {
                        val whiteBitmap = BitmapFactory.decodeResource(mapView.context.resources, whiteResId)
                        style.addImage(whiteName, whiteBitmap)
                    }

                    if (style.getImage(baseName) == null) {
                        val bitmap = BitmapFactory.decodeResource(mapView.context.resources, resId)
                        style.addImage(baseName, bitmap)
                    }

                    val redResId = mapView.context.resources.getIdentifier(redName, "drawable", mapView.context.packageName)
                    if (redResId != 0 && style.getImage(redName) == null) {
                        val redBitmap = BitmapFactory.decodeResource(mapView.context.resources, redResId)
                        style.addImage(redName, redBitmap)
                    }
                }

                // determine how much to filter vectors based on zoom level
                val dynamicStep = when {
                    zoomState.doubleValue >= 7.5 -> 1
                    zoomState.doubleValue >= 6.5 -> 3
                    zoomState.doubleValue >= 5.0 -> 5
                    else                   -> 8
                }

                val dataToDraw = if (filterVectors) {
                    filteredVectors.filterIndexed { index, _ -> index % dynamicStep == 0 }
                } else filteredVectors

                // create feature collection with direction, speed and selected icon
                val features = dataToDraw.mapNotNull { v ->
                    Feature.fromGeometry(Point.fromLngLat(v.lon, v.lat)).apply {
                        addNumberProperty("direction", v.direction)
                        addNumberProperty("speed", v.speed)
                        addStringProperty("icon", selectIcon(v.speed, threshold, iconMap, mapView, isDarkMode))
                    }
                }

                val featureCollection = FeatureCollection.fromFeatures(features.toTypedArray())
                Log.d("GribWindLayer", "number of vectors shown: ${features.size}")

                // update or add source
                val existingSource = style.getSourceAs<GeoJsonSource>(sourceId)
                if (existingSource != null) {
                    existingSource.setGeoJson(featureCollection.toJson())
                } else {
                    style.addSource(GeoJsonSource(sourceId, featureCollection.toJson()))
                }

                // create wind layer if missing
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

    // remove layer and source when not visible
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            map.getStyle { style ->
                style.getLayer("wind_layer")?.let { style.removeLayer(it) }
                style.getSource("wind_source")?.let { style.removeSource(it) }
            }
        }
    }

    // warning when zoom level is too low
    AnimatedVisibility(
        visible = isVisible && zoomState.doubleValue < 7.0,
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

// selects appropriate icon name based on speed, threshold and theme
private fun selectIcon(
    speed: Double,
    threshold: Double,
    iconMap: Map<Double, Int>,
    mapView: MapView,
    isDarkMode: Boolean
): String {
    val thresholdKey = iconMap.keys.sorted().firstOrNull { speed <= it } ?: Double.MAX_VALUE
    val resId = iconMap[thresholdKey] ?: error("no icon found for speed $speed")
    val baseName = mapView.context.resources.getResourceEntryName(resId)

    return when {
        speed > threshold -> "${baseName}_red"
        isDarkMode -> "${baseName}_white"
        else -> baseName
    }
}
