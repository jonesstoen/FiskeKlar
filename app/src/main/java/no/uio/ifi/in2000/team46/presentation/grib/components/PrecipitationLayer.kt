package no.uio.ifi.in2000.team46.presentation.grib.components

import androidx.compose.runtime.*
import no.uio.ifi.in2000.team46.data.repository.Result
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import no.uio.ifi.in2000.team46.domain.grib.PrecipitationPoint
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.PrecipitationViewModel
import org.maplibre.android.style.expressions.Expression.interpolate
import org.maplibre.android.style.expressions.Expression.linear
import org.maplibre.android.style.expressions.Expression.literal
import org.maplibre.android.style.expressions.Expression.zoom
import org.maplibre.android.style.layers.CircleLayer

import org.maplibre.android.style.expressions.Expression.*
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
            // Fjern eksisterende lag og kilde hvis de finnes
            style.removeLayer(circleLayerId)
            style.removeLayer(textLayerId)
            style.removeSource(srcId)

            if (!isVisible || filteredPoints.isEmpty()) return@getStyle

            // Lag GeoJSON features med tekstlabel
            val features = filteredPoints.map { p ->
                Feature.fromGeometry(Point.fromLngLat(p.lon, p.lat)).apply {
                    addNumberProperty("precip", p.precipitation)
                    addStringProperty("precipLabel", String.format("%.2f", p.precipitation))
                }
            }
            val fc = FeatureCollection.fromFeatures(features)
            style.addSource(GeoJsonSource(srcId, fc))

            // Sirkel for nedbørsmengde
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
                        gt(get("precip"), literal(threshold)),
                        color(0xFFB2182B.toInt()), // rød hvis over terskel
                        color(0xFF64B5F6.toInt())  // blå ellers
                    )
                )
            )
            style.addLayer(circleLayer)

            // Tekstlag som viser nedbørsmengde i mm
            val textLayer = SymbolLayer(textLayerId, srcId).withProperties(
                textField(concat(get("precipLabel"), literal(" mm"))),
                textSize(10f),
                textColor(color(0xFF000000.toInt())),
                textHaloColor(color(0xFFFFFFFF.toInt())),
                textHaloWidth(1f),
                textAnchor("top"),
                textOffset(arrayOf(0f, 1.2f))
            ).withFilter(gte(zoom(), literal(8)))

            style.addLayerAbove(textLayer, circleLayerId)
        }
    }
}



