package no.uio.ifi.in2000.team46.presentation.grib.components

import android.util.Log
import androidx.compose.runtime.*
import no.uio.ifi.in2000.team46.domain.grib.WaveVector
import no.uio.ifi.in2000.team46.data.repository.Result
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.WaveViewModel
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.expressions.Expression.interpolate
import org.maplibre.android.style.expressions.Expression.linear
import org.maplibre.android.style.expressions.Expression.literal
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.expressions.Expression.zoom
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleOpacity
import org.maplibre.android.style.layers.PropertyFactory.circleRadius

import com.google.gson.JsonObject
import org.maplibre.android.style.expressions.Expression.color
import org.maplibre.android.style.expressions.Expression.step
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point



@Composable
fun GribWaveLayer(
    waveViewModel: WaveViewModel,
    map: MapLibreMap,
    mapView: MapView
) {
    val isVisible by waveViewModel.isLayerVisible.collectAsState()
    val waveResult by waveViewModel.waveData.collectAsState()

    LaunchedEffect(isVisible, waveResult) {
        map.getStyle { style ->
            // Fjern gamle lag/kilde hvis ikke synlig eller ingen data
            if (!isVisible || waveResult !is Result.Success) {
                style.getLayer("wave_circle_layer")?.let { style.removeLayer(it) }
                style.getSource("wave_source")?.let { style.removeSource(it) }
                Log.d("GribWaveLayer", "Layer hidden or no data, removed existing layers/sources.")
                return@getStyle
            }

            val waves = (waveResult as Result.Success<List<WaveVector>>).data
            Log.d("GribWaveLayer", "Received wave vectors: size=${waves.size}")
            if (waves.isEmpty()) {
                Log.d("GribWaveLayer", "No wave data to render.")
                return@getStyle
            }
            val maxWave = waves.maxByOrNull { it.height }
            maxWave?.let {
                Log.d("GribWaveLayer", "Høyeste bølge: ${it.height} m ved (${it.lat}, ${it.lon})")
            }

            // Lag GeoJSON-kilde
            val featureCollection = waves.toFeatureCollection().also {
                Log.d("GribWaveLayer", "Created GeoJSON FeatureCollection with ${it.features()?.size ?: 0} features.")
            }

            // Fjern gamle kilde og lag
            style.getLayer("wave_circle_layer")?.let { style.removeLayer(it) }
            style.getSource("wave_source")?.let { style.removeSource(it) }

            // Legg til ny kilde
            style.addSource(GeoJsonSource("wave_source", featureCollection))

            // CircleLayer med enhetlig farge per punkt basert på høyde
            val circleLayer = CircleLayer("wave_circle_layer", "wave_source").withProperties(
                circleRadius(
                    interpolate(
                    linear(), zoom(),
                        literal(0),  literal(2f),    // globalt utsyn
                        literal(5),  literal(4f),    // middels zoom
                        literal(10), literal(12f),   // nærmere
                        literal(14), literal(20f),   // enda nærmere
                        literal(16), literal(28f)    // maks størrelse
                )
                ),
                circleOpacity(literal(0.8f)),    // litt gjennomsiktig
                circleColor(
                    step(
                        get("height"),
                        // <=0m: mørk blå
                        color(0xFF2166AC.toInt()),
                        // >=1m: noe lysere blå
                        literal(1.0), color(0xFF4393C3.toInt()),
                        // >=2m: enda lysere blå
                        literal(2.0), color(0xFF92C5DE.toInt()),
                        // >=3m: lys peach
                        literal(3.0), color(0xFFF4A582.toInt()),
                        // >=5m: oransje
                        literal(5.0), color(0xFFFFA500.toInt()),
                        // >=8m: rød
                        literal(8.0), color(0xFFB2182B.toInt())
                    )

                )
            )
            style.addLayer(circleLayer)

            Log.d("GribWaveLayer", "Added CircleLayer 'wave_circle_layer' on top of style  .")
        }
    }
}

// Extension for å konvertere WaveVector-liste til GeoJSON
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

