package no.uio.ifi.in2000.team46.presentation.grib.components

import androidx.compose.runtime.*
import no.uio.ifi.in2000.team46.data.repository.Result
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import no.uio.ifi.in2000.team46.domain.grib.PrecipitationPoint
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.PrecipitationViewModel
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.SymbolLayer

@Composable
fun PrecipitationLayer(vm: PrecipitationViewModel, map: MapLibreMap) {
    val isVisible by vm.isLayerVisible.collectAsState()
    val threshold by vm.precipThreshold.collectAsState()
    val filteredPoints by vm.filteredPrecipPoints.collectAsState()

    val srcId = "precip_source"
    val circleLayerId = "precip_circle_layer"
    val textLayerId = "precip_text_layer"

    LaunchedEffect(isVisible, filteredPoints, threshold) {
        map.getStyle { style ->
            // Fjern gamle lag/kilder
            style.removeLayer(circleLayerId)
            style.removeLayer(textLayerId)
            style.removeSource(srcId)

            if (!isVisible || filteredPoints.isEmpty()) return@getStyle

            // Bygg GeoJSON-kilde
            val features = filteredPoints.map { p ->
                Feature.fromGeometry(Point.fromLngLat(p.lon, p.lat)).apply {
                    addNumberProperty("precip", p.precipitation)
                    addStringProperty("precipLabel", String.format("%.2f", p.precipitation))
                }
            }
            style.addSource(GeoJsonSource(srcId, FeatureCollection.fromFeatures(features)))

            // CircleLayer med fargeskala
            val circleLayer = CircleLayer(circleLayerId, srcId).withProperties(
                circleRadius(
                    interpolate(
                        linear(), zoom(),
                        literal(0),  literal(2f),
                        literal(5),  literal(4f),
                        literal(10), literal(12f),
                        literal(14), literal(20f),
                        literal(16), literal(28f)
                    )
                ),
                circleOpacity(literal(0.8f)),
                circleColor(
                    switchCase(
                        // 1) Rødt om over terskel
                        gt(get("precip"), literal(threshold)), color(0xFFB2182B.toInt()),
                        // 2) Ellers fargeskala som i legenden:
                        step(
                            get("precip"),
                            color(0xFFADD8E6.toInt()),  // 0–1 mm
                            literal(1.0),  color(0xFF6495ED.toInt()),  // 1–5 mm
                            literal(5.0),  color(0xFF4169E1.toInt()),  // 5–10 mm
                            literal(10.0), color(0xFF27408B.toInt()),  // 10–15 mm
                            literal(15.0), color(0xFF00008B.toInt()),  // 15–20 mm
                            literal(20.0), color(0xFF800080.toInt())   // ≥20 mm
                        )
                    )
                )
            )
            style.addLayer(circleLayer)

            val textLayer = SymbolLayer(textLayerId, srcId).withProperties(
                textField(concat(get("precipLabel"), literal(" mm"))),
                textSize(10f),
                textColor(color(0xFF000000.toInt())),
                textHaloColor(color(0xFFFFFFFF.toInt())),
                textHaloWidth(1f),
                textAnchor("top"),
                textOffset(arrayOf(0f, 1.2f))
            ).withFilter(gte(zoom(), literal(8f)))
            style.addLayerAbove(textLayer, circleLayerId)
        }
    }
}
