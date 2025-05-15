package no.uio.ifi.in2000.team46.presentation.grib.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import no.uio.ifi.in2000.team46.domain.grib.WaveVector
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.WaveViewModel
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import com.google.gson.JsonObject

// composable that syncs map with wave data points styling circles and labels based on threshold and zoom level

@Composable
fun GribWaveLayer(
    waveViewModel: WaveViewModel,
    map: MapLibreMap,
    mapView: MapView
) {
    // observe visibility flag
    val isVisible by waveViewModel.isLayerVisible.collectAsState()
    // observe wave height threshold
    val threshold by waveViewModel.waveThreshold.collectAsState()
    // observe filtered wave vectors from viewmodel
    val filteredWaves by waveViewModel.filteredWaveVectors.collectAsState()

    // get primary theme color for text labels
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

    LaunchedEffect(isVisible, filteredWaves, threshold) {
        map.getStyle { style ->
            // remove existing wave layers and source if present
            style.getLayer("wave_text_layer")?.let { style.removeLayer(it) }
            style.getLayer("wave_circle_layer")?.let { style.removeLayer(it) }
            style.getSource("wave_source")?.let { style.removeSource(it) }

            // skip adding if layer hidden or no data
            if (!isVisible || filteredWaves.isEmpty()) {
                Log.d("GribWaveLayer", "layer hidden or no data, cleaned up.")
                return@getStyle
            }

            // convert filtered waves to geojson source
            val featureCollection = filteredWaves.toFeatureCollection()
            style.addSource(GeoJsonSource("wave_source", featureCollection))

            // create circle layer with radius interpolated by zoom and color by height
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
                circleColor(
                    switchCase(
                        // red if above threshold
                        gt(get("height"), literal(threshold)),
                        color(0xFFB2182B.toInt()),
                        // otherwise stepped color scale from lightblue to purple
                        step(
                            get("height"),
                            color(0xFFADD8E6.toInt()), literal(1.0), color(0xFF6495ED.toInt()),
                            literal(2.0), color(0xFF4169E1.toInt()), literal(3.0), color(0xFF27408B.toInt()),
                            literal(5.0), color(0xFF00008B.toInt()), literal(8.0), color(0xFF800080.toInt())
                        )
                    )
                )
            )
            style.addLayer(circleLayer)

            // create symbol layer for height labels appearing above zoom level 8
            val textLayer = SymbolLayer("wave_text_layer", "wave_source").withProperties(
                textField(concat(get("heightLabel"), literal(" m"))),
                textSize(14f),
                textColor(color(primaryColor)),
                textFont(arrayOf("Arial Bold")),
                textAnchor("top"),
                textOffset(arrayOf(0f, 1.2f)),
            ).withFilter(gte(zoom(), literal(8)))

            style.addLayerAbove(textLayer, "wave_circle_layer")
            Log.d("GribWaveLayer", "added wave layers to style.")
        }
    }
}

// extension to convert list of wave vectors to geojson feature collection
@SuppressLint("DefaultLocale")
private fun List<WaveVector>.toFeatureCollection(): FeatureCollection {
    val features = mapNotNull { w ->
        try {
            val point = Point.fromLngLat(w.lon, w.lat)
            val props = JsonObject().apply {
                addProperty("height", w.height.toFloat())
                addProperty("heightLabel", String.format("%.1f", w.height))
            }
            Feature.fromGeometry(point, props)
        } catch (e: Exception) {
            Log.w("GribWaveLayer", "skipping invalid point: \$w", e)
            null
        }
    }
    Log.d("GribWaveLayer", "toFeatureCollection generated \${features.size} features.")
    return FeatureCollection.fromFeatures(features)
}
