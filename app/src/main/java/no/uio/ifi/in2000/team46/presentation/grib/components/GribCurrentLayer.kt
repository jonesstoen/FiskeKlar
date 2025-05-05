package no.uio.ifi.in2000.team46.presentation.grib.components
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.Log
import androidx.compose.runtime.*
import no.uio.ifi.in2000.team46.data.local.parser.WindVector
import no.uio.ifi.in2000.team46.data.repository.Result

import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import no.uio.ifi.in2000.team46.R
import no.uio.ifi.in2000.team46.data.local.parser.CurrentVector
import no.uio.ifi.in2000.team46.presentation.grib.CurrentViewModel
import org.maplibre.android.style.layers.SymbolLayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import no.uio.ifi.in2000.team46.data.repository.CurrentRepository
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

    if (isVisible && currentResult is Result.Success) {
        val currentData = (currentResult as Result.Success<List<CurrentVector>>).data

        LaunchedEffect(currentData) {
            map.getStyle { style ->
                val sourceId = "current_source"
                val layerId = "current_layer"
                val iconId = "current_arrow"

                if (style.getImage(iconId) == null) {
                    val arrowBitmap = BitmapFactory.decodeResource(
                        mapView.context.resources, R.drawable.current_icon)
                    style.addImage(iconId, arrowBitmap, false)
                }

                val filteredData = if (filterVectors) {
                    currentData.filterIndexed { index, _ -> index % filterStep == 0 }
                } else {
                    currentData
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

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            map.getStyle { style ->
                style.getLayer("current_layer")?.let { style.removeLayer(it) }
                style.getSource("current_source")?.let { style.removeSource(it) }
            }
        }
    }
}




class CurrentViewModelFactory(
    private val repository: CurrentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CurrentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CurrentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
