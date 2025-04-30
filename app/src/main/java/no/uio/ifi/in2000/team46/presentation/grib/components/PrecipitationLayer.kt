package no.uio.ifi.in2000.team46.presentation.grib.components

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.*
import no.uio.ifi.in2000.team46.data.local.parser.WindVector
import no.uio.ifi.in2000.team46.data.repository.Result
import no.uio.ifi.in2000.team46.presentation.grib.GribViewModel
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import no.uio.ifi.in2000.team46.R
import no.uio.ifi.in2000.team46.data.local.parser.PrecipitationPoint
import no.uio.ifi.in2000.team46.presentation.grib.PrecipitationViewModel
import org.maplibre.android.style.layers.HeatmapLayer
import org.maplibre.android.style.layers.SymbolLayer

@Composable
fun PrecipitationLayer(
    vm: PrecipitationViewModel,
    map: MapLibreMap
) {
    val isVisible by vm.isLayerVisible.collectAsState()
    val result by vm.data.collectAsState()

    LaunchedEffect(isVisible, result) {
        map.getStyle { style ->
            val srcId = "precip_src"
            val layerId = "precip_heatmap"

            if (isVisible) {
                // 1) Build a “live” list of features:
                val features = when (result) {
                    is Result.Success<*> -> {
                        val data = (result as Result.Success<List<PrecipitationPoint>>).data
                        if (data.isNotEmpty()) {
                            // real data
                            data.map { p ->
                                Feature.fromGeometry(Point.fromLngLat(p.lon, p.lat))
                                    .apply { addNumberProperty("precip", p.precipitation) }
                            }
                        } else {
                            // fallback to test data
                            listOf(
                                PrecipitationPoint(10.75, 59.91, 5.0),
                                PrecipitationPoint(10.80, 59.92, 15.0),
                                PrecipitationPoint(10.70, 59.90, 25.0)
                            ).map { p ->
                                Feature.fromGeometry(Point.fromLngLat(p.lon, p.lat))
                                    .apply { addNumberProperty("precip", p.precipitation) }
                            }
                        }
                    }

                    else -> {
                        // no result yet → test data
                        listOf(
                            PrecipitationPoint(10.75, 59.91, 5.0),
                            PrecipitationPoint(10.80, 59.92, 15.0),
                            PrecipitationPoint(10.70, 59.90, 25.0)
                        ).map { p ->
                            Feature.fromGeometry(Point.fromLngLat(p.lon, p.lat))
                                .apply { addNumberProperty("precip", p.precipitation) }
                        }
                    }
                }

                val fc = FeatureCollection.fromFeatures(features.toTypedArray())

                // rest is unchanged: add/update source & layer
                style.getSourceAs<GeoJsonSource>(srcId)
                    ?.setGeoJson(fc.toJson())
                    ?: style.addSource(GeoJsonSource(srcId, fc.toJson()))

                if (style.getLayer(layerId) == null) {
                    style.addLayer(
                        HeatmapLayer(layerId, srcId).withProperties(
                            heatmapRadius(20f),
                            heatmapIntensity(
                                Expression.interpolate(
                                    Expression.linear(),
                                    Expression.get("precip"),
                                    Expression.stop(0, 0),
                                    Expression.stop(2, 0.5f),
                                    Expression.stop(10, 1f)
                                )
                            ),
                            heatmapColor(
                                Expression.interpolate(
                                    Expression.linear(),
                                    Expression.get("precip"),
                                    Expression.stop(0, Expression.rgba(0, 0, 255, 0)),
                                    Expression.stop(5, Expression.rgba(0, 255, 255, 128)),
                                    Expression.stop(20, Expression.rgba(255, 0, 0, 192))
                                )
                            )
                        )
                    )
                }
            } else {
                // hide
                style.getLayer(layerId)?.let { style.removeLayer(it) }
                style.getSource(srcId)?.let { style.removeSource(it) }
            }
        }
    }
}