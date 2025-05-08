package no.uio.ifi.in2000.team46.presentation.grib.components

import android.util.Log
import androidx.compose.runtime.*
import no.uio.ifi.in2000.team46.domain.grib.WaveVector
import no.uio.ifi.in2000.team46.data.repository.Result
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.WaveViewModel
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import com.google.gson.JsonObject

@Composable
fun GribWaveLayer(
    waveViewModel: WaveViewModel,
    map: MapLibreMap,
    mapView: MapView
) {
    val isVisible by waveViewModel.isLayerVisible.collectAsState()
    val waveResult by waveViewModel.waveData.collectAsState()
    val threshold by waveViewModel.waveThreshold.collectAsState()

    LaunchedEffect(isVisible, waveResult, threshold) {
        map.getStyle { style ->
            if (!isVisible || waveResult !is Result.Success) {
                style.getLayer("wave_circle_layer")?.let { style.removeLayer(it) }
                style.getSource("wave_source")?.let { style.removeSource(it) }
                Log.d("GribWaveLayer", "Layer hidden or no data, removed existing layers/sources.")
                return@getStyle
            }

            val waves = (waveResult as Result.Success<List<WaveVector>>).data
            Log.d("GribWaveLayer", "Aktiv terskelverdi: $threshold")
            Log.d("GribWaveLayer", "Received wave vectors: size=${waves.size}")

            val featureCollection = waves.toFeatureCollection().also {
                Log.d("GribWaveLayer", "Created GeoJSON FeatureCollection with ${it.features()?.size ?: 0} features.")
            }

            style.getLayer("wave_circle_layer")?.let { style.removeLayer(it) }
            style.getSource("wave_source")?.let { style.removeSource(it) }
            style.addSource(GeoJsonSource("wave_source", featureCollection))

            // draw layer with radius and conditional coloring
            val circleLayer = CircleLayer("wave_circle_layer", "wave_source").withProperties(
                circleRadius(
                    interpolate(
                        linear(), zoom(),
                        literal(0), literal(2f),
                        literal(5), literal(4f),
                        literal(10), literal(12f),
                        literal(14), literal(20f),
                        literal(16), literal(28f)
                    )
                ),
                circleOpacity(literal(0.8f)),

                // color: red if height > threshold, else use step scale
                circleColor(
                    switchCase(
                        gt(get("height"), literal(threshold)),
                        color(0xFFB2182B.toInt()),  // red if over threshold
                        step(
                            get("height"),
                            color(0xFF2166AC.toInt()),               // <= 0m
                            literal(1.0), color(0xFF4393C3.toInt()),  // >= 1m
                            literal(2.0), color(0xFF92C5DE.toInt()),  // >= 2m
                            literal(3.0), color(0xFFF4A582.toInt()),  // >= 3m
                            literal(5.0), color(0xFFFFA500.toInt()),  // >= 5m
                            literal(8.0), color(0xFFB2182B.toInt())   // >= 8m
                        )
                    )
                )

            )

            style.addLayer(circleLayer)
            Log.d("GribWaveLayer", "Added CircleLayer 'wave_circle_layer' to style.")
        }
    }
}

// convert wave data to geojson
private fun List<WaveVector>.toFeatureCollection(): FeatureCollection {
    val features = mapNotNull { w ->
        try {
            val point = Point.fromLngLat(w.lon, w.lat)
            val props = JsonObject().apply { addProperty("height", w.height.toFloat()) }
            Feature.fromGeometry(point, props)
        } catch (e: Exception) {
            Log.w("GribWaveLayer", "Skipping invalid point: $w", e)
            null
        }
    }
    Log.d("GribWaveLayer", "toFeatureCollection generated ${features.size} features.")
    return FeatureCollection.fromFeatures(features)
}
