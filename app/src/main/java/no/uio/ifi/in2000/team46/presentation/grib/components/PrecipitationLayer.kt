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

@Composable
fun PrecipitationLayer(vm: PrecipitationViewModel, map: MapLibreMap) {
    val isVisible     by vm.isLayerVisible.collectAsState()
    val result        by vm.data.collectAsState()
    val threshold     by vm.precipThreshold.collectAsState()
    val srcId         = "precip_source"
    val circleLayerId = "precip_circle_layer"

    LaunchedEffect(isVisible, result, threshold) {
        map.getStyle { style ->
            if (!isVisible || result !is Result.Success) {
                style.removeLayer(circleLayerId)
                style.removeSource(srcId)
                return@getStyle
            }

            val points = (result as Result.Success<List<PrecipitationPoint>>).data
            if (points.isEmpty()) return@getStyle

            val features = points.map { p ->
                Feature.fromGeometry(Point.fromLngLat(p.lon, p.lat))
                    .apply { addNumberProperty("precip", p.precipitation) }
            }
            val fc = FeatureCollection.fromFeatures(features)

            style.removeLayer(circleLayerId)
            style.removeSource(srcId)
            style.addSource(GeoJsonSource(srcId, fc))

            val layer = CircleLayer(circleLayerId, srcId).withProperties(
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
                    Expression.switchCase(
                        Expression.gt(Expression.get("precip"), literal(threshold)),
                        Expression.color(0xFFB2182B.toInt()), // Rødt hvis over terskel
                        Expression.color(0xFF64B5F6.toInt())  // Standard blå
                    )
                )
            )
            style.addLayer(layer)
        }
    }
}

