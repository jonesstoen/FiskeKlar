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
import org.maplibre.android.style.layers.CircleLayer

@Composable
fun PrecipitationLayer(vm: PrecipitationViewModel, map: MapLibreMap) {
    val isVisible     by vm.isLayerVisible.collectAsState()
    val result        by vm.data.collectAsState()
    val srcId         = "precip_source"
    val circleLayerId = "precip_circle_layer"

    LaunchedEffect(isVisible, result) {
        map.getStyle { style ->
            // remove old
            if (!isVisible || result !is Result.Success) {
                style.removeLayer(circleLayerId)
                style.removeSource(srcId)
                return@getStyle
            }

            val points = (result as Result.Success<List<PrecipitationPoint>>).data
            if (points.isEmpty()) return@getStyle

            // build GeoJSON
            val features = points.map { p ->
                Feature.fromGeometry(Point.fromLngLat(p.lon, p.lat))
                    .apply { addNumberProperty("precip", p.precipitation) }
            }
            val fc = FeatureCollection.fromFeatures(features.toTypedArray())

            // clear + add source
            style.removeLayer(circleLayerId)
            style.removeSource(srcId)
            style.addSource(GeoJsonSource(srcId, fc))

            // circle‐layer: size by zoom, color by precip
            val layer = CircleLayer(circleLayerId, srcId).withProperties(
                circleRadius(
                    Expression.interpolate(
                        Expression.linear(), Expression.zoom(),
                        Expression.literal(0),  Expression.literal(2f),
                        Expression.literal(5),  Expression.literal(6f),
                        Expression.literal(10), Expression.literal(12f)
                    )
                ),
                circleOpacity(Expression.literal(0.8f)),
                circleColor(
                    Expression.step(
                        Expression.get("precip"),
                        Expression.color(0xFFB0E2FF.toInt()),  // <= 0mm: pale
                        Expression.literal(1.0), Expression.color(0xFF64B5F6.toInt()), // >=1mm
                        Expression.literal(5.0), Expression.color(0xFF1976D2.toInt()), // >=5mm
                        Expression.literal(10.0),Expression.color(0xFF0D47A1.toInt())  // >=10mm
                    )
                )
            )
            style.addLayer(layer)
        }
    }
}
