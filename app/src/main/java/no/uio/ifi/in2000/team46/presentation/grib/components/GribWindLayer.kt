package no.uio.ifi.in2000.team46.presentation.grib.components

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

                // Kartlegg terskler til drawable-ikonene (Beaufort skala)
                val iconMap = mapOf(
                    0.2   to R.drawable.windbarp05_030_svg,
                    1.5   to R.drawable.windbarp05_030_svg,
                    3.3   to R.drawable.windbarp05_030_svg,
                    5.4   to R.drawable.windbarp05_030_svg,
                    7.9   to R.drawable.windbarp05_030_svg,
                    10.7  to R.drawable.symbol_wind_speed_107,
                    13.8  to R.drawable.symbol_wind_speed_138,
                    17.1  to R.drawable.symbol_wind_speed_171,
                    20.7  to R.drawable.symbol_wind_speed_207,
                    24.4  to R.drawable.symbol_wind_speed_244,
                    28.4  to R.drawable.symbol_wind_speed_284,
                    32.6  to R.drawable.symbol_wind_speed_326,
                    Double.MAX_VALUE to R.drawable.symbol_wind_speed_max
                )

                // Legg til alle ikonene
                iconMap.forEach { (_, resId) ->
                    val iconName = mapView.context.resources.getResourceEntryName(resId)
                    if (style.getImage(iconName) == null) {
                        val bitmap = BitmapFactory.decodeResource(mapView.context.resources, resId)
                        style.addImage(iconName, bitmap, true)
                        Log.d("GribWindLayer", "La til ikon: $iconName")
                    }
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
                        addStringProperty("icon", selectIcon(v.speed, iconMap, mapView))
                    }
                }

                val featureCollection = FeatureCollection.fromFeatures(features.toTypedArray())
                Log.d("GribWindLayer", "Antall vektorer: ${features.size}")

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
                            iconAllowOverlap(false),
                            iconIgnorePlacement(false),
                            iconRotate(Expression.get("direction")),
                            iconSize(
                                Expression.interpolate(
                                    Expression.linear(), Expression.zoom(),
                                    Expression.stop(0.0, 0.05),    // Lav zoom = tiny piler
                                    Expression.stop(8.0, 0.1),     // Litt større
                                    Expression.stop(12.0, 0.18),   // Mer tydelig forskjell
                                    Expression.stop(16.0, 0.28),   // Ved høy zoom, klart større, men ikke for store
                                    Expression.stop(20.0, 0.4)     // Maks zoom
                                )

                            )
                        )
                    )
                }
            }
        }
    }

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            map.getStyle { style ->
                style.getLayer("wind_layer")?.let { style.removeLayer(it) }
                style.getSource("wind_source")?.let { style.removeSource(it) }
            }
        }
    }
}

private fun selectIcon(speed: Double, iconMap: Map<Double, Int>, mapView: MapView): String {
    val threshold = iconMap.keys.sorted().firstOrNull { speed <= it } ?: Double.MAX_VALUE
    val resId = iconMap[threshold] ?: error("No icon found for speed $speed")
    return mapView.context.resources.getResourceEntryName(resId)
}
