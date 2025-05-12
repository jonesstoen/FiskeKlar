package no.uio.ifi.in2000.team46.presentation.grib.components

import android.graphics.Color.toArgb
import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
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
import org.maplibre.android.style.layers.SymbolLayer

@Composable
fun GribWaveLayer(
    waveViewModel: WaveViewModel,
    map: MapLibreMap,
    mapView: MapView
) {
    val isVisible by waveViewModel.isLayerVisible.collectAsState()
    val threshold by waveViewModel.waveThreshold.collectAsState()
    val filteredWaves by waveViewModel.filteredWaveVectors.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()


    LaunchedEffect(isVisible, filteredWaves, threshold) {
        map.getStyle { style ->
            // Remove existing layers and sources if they exist
            style.getLayer("wave_text_layer")?.let { style.removeLayer(it) }
            style.getLayer("wave_circle_layer")?.let { style.removeLayer(it) }
            style.getSource("wave_source")?.let { style.removeSource(it) }

            if (!isVisible || filteredWaves.isEmpty()) {
                Log.d("GribWaveLayer", "Layer hidden or no data, cleaned up.")
                return@getStyle
            }

            val featureCollection = filteredWaves.toFeatureCollection()
            style.addSource(GeoJsonSource("wave_source", featureCollection))


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
                        gt(get("height"), literal(threshold)),
                        color(0xFFB2182B.toInt()),
                        step(
                            get("height"),
                            color(0xFF2166AC.toInt()),
                            literal(1.0), color(0xFF4393C3.toInt()),
                            literal(2.0), color(0xFF92C5DE.toInt()),
                            literal(3.0), color(0xFFF4A582.toInt()),
                            literal(5.0), color(0xFFFFA500.toInt()),
                            literal(8.0), color(0xFFB2182B.toInt())
                        )
                    )
                )
            )

            val textLayer = SymbolLayer("wave_text_layer", "wave_source").withProperties(
                textField(concat(get("heightLabel"), literal(" m"))),
                textSize(14f),
                textColor(color(primaryColor)),
                textFont(arrayOf("Arial Bold")),
                textAnchor("top"),
                textOffset(arrayOf(0f, 1.2f)),
            ).withFilter(gte(zoom(), literal(8)))

            style.addLayer(circleLayer)
            style.addLayerAbove(textLayer, "wave_circle_layer")
            Log.d("GribWaveLayer", "Added wave layers to style.")
        }
    }

}

// convert wave data to geojson
private fun List<WaveVector>.toFeatureCollection(): FeatureCollection {
    val features = mapNotNull { w ->
        try {
            val point = Point.fromLngLat(w.lon, w.lat)
            val props = JsonObject().apply {
                addProperty("height", w.height.toFloat()) // behold som tall til farger
                addProperty("heightLabel", String.format("%.1f", w.height)) // ny string-verdi til tekst
            }
            Feature.fromGeometry(point, props)
        } catch (e: Exception) {
            Log.w("GribWaveLayer", "Skipping invalid point: $w", e)
            null
        }
    }
    Log.d("GribWaveLayer", "toFeatureCollection generated ${features.size} features.")
    return FeatureCollection.fromFeatures(features)
}
