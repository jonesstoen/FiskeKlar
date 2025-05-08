package no.uio.ifi.in2000.team46.presentation.grib.components

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.*
import no.uio.ifi.in2000.team46.data.repository.Result
import no.uio.ifi.in2000.team46.domain.grib.CurrentVector
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.CurrentViewModel
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.expressions.Expression.literal
import org.maplibre.android.style.expressions.Expression.match
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import no.uio.ifi.in2000.team46.R
import org.maplibre.android.style.layers.SymbolLayer

@Composable
fun GribCurrentLayer(
    currentViewModel: CurrentViewModel,
    map: MapLibreMap,
    mapView: MapView,
    filterVectors: Boolean = false,
    filterStep: Int = 3
) {
    val isVisible by currentViewModel.isLayerVisible.collectAsState()
    val currentResult by currentViewModel.currentData.collectAsState()
    val threshold by currentViewModel.currentThreshold.collectAsState()

    if (isVisible && currentResult is Result.Success) {
        val currentData = (currentResult as Result.Success<List<CurrentVector>>).data

        LaunchedEffect(currentData, threshold) {
            map.getStyle { style ->
                val sourceId = "current_source"
                val layerId = "current_layer"

                val normalIconName = "current_icon"
                val redIconName = "current_icon_red"

                val normalBitmap = BitmapFactory.decodeResource(mapView.context.resources, R.drawable.current_icon)
                val redBitmap = BitmapFactory.decodeResource(mapView.context.resources, R.drawable.current_icon_red)

                if (style.getImage(normalIconName) == null) style.addImage(normalIconName, normalBitmap, false)
                if (style.getImage(redIconName) == null) style.addImage(redIconName, redBitmap, false)

                val filteredData = if (filterVectors) {
                    currentData.filterIndexed { index, _ -> index % filterStep == 0 }
                } else {
                    currentData
                }

                val features = filteredData.mapNotNull { v ->
                    Feature.fromGeometry(Point.fromLngLat(v.lon, v.lat)).apply {
                        addNumberProperty("direction", v.direction)
                        addNumberProperty("speed", v.speed)
                        val iconName = if (v.speed > threshold) redIconName else normalIconName
                        addStringProperty("icon", iconName)
                    }
                }

                val featureCollection = FeatureCollection.fromFeatures(features.toTypedArray())

                style.getSourceAs<GeoJsonSource>(sourceId)?.setGeoJson(featureCollection.toJson())
                    ?: style.addSource(GeoJsonSource(sourceId, featureCollection.toJson()))

                if (style.getLayer(layerId) == null) {
                    style.addLayer(
                        SymbolLayer(layerId, sourceId).withProperties(
                            iconImage(get("icon")),
                            iconAllowOverlap(true),
                            iconIgnorePlacement(true),
                            iconRotationAlignment("map"),
                            iconRotate(get("direction")),
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

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            map.getStyle { style ->
                style.getLayer("current_layer")?.let { style.removeLayer(it) }
                style.getSource("current_source")?.let { style.removeSource(it) }
            }
        }
    }
}
