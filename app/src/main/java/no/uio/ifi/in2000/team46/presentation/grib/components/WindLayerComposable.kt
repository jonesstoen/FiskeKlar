package no.uio.ifi.in2000.team46.presentation.grib.components

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import no.uio.ifi.in2000.team46.data.local.parser.WindVector
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import no.uio.ifi.in2000.team46.R
import org.maplibre.android.style.layers.SymbolLayer

@Composable
fun WindLayer(
    map: MapLibreMap,
    mapView: MapView,
    windData: List<WindVector>,
    threshold: Double = 1.0,                       // Grense for "farlig" vind (m/s)
    filterVectors: Boolean = false,                 // Slå filtering av/på
    filterStep: Int = 3                            // Hopp over hvert X punkt når filtering er på
) {
    LaunchedEffect(windData) {
        map.getStyle { style ->
            val sourceId = "wind_source"
            val layerId = "wind_layer"
            val iconId = "wind_icon"

            // Legg til vindpil-ikon hvis det ikke finnes
            if (style.getImage(iconId) == null) {
                val arrowBitmap = BitmapFactory.decodeResource(
                    mapView.context.resources, R.drawable.ic_wind_arrow
                )
                style.addImage(iconId, arrowBitmap, true)
            }

            // Filtrer vektorene hvis ønskelig (debug/test)
            val filteredData = if (filterVectors) {
                windData.filterIndexed { index, _ -> index % filterStep == 0 }
            } else {
                windData
            }

            Log.d("WindLayer", "Antall vektorer som tegnes: ${filteredData.size}")

            // Lag GeoJSON FeatureCollection fra vinddataene
            val features = filteredData.mapNotNull { v ->
                Feature.fromGeometry(Point.fromLngLat(v.lon, v.lat)).apply {
                    addNumberProperty("direction", v.direction)
                    addNumberProperty("speed", v.speed)
                }
            }
            val featureCollection = FeatureCollection.fromFeatures(features.toTypedArray())

            // Oppdater eller legg til GeoJsonSource
            val existingSource = style.getSourceAs<GeoJsonSource>(sourceId)
            if (existingSource != null) {
                existingSource.setGeoJson(featureCollection.toJson())
            } else {
                style.addSource(GeoJsonSource(sourceId, featureCollection.toJson()))
            }

            // Legg til SymbolLayer hvis det ikke finnes
            if (style.getLayer(layerId) == null) {
                val windLayer = SymbolLayer(layerId, sourceId).withProperties(
                    iconImage(iconId),
                    iconAllowOverlap(false),                     // Viktig for å unngå overlapping
                    iconIgnorePlacement(false),
                    iconRotate(Expression.get("direction")),
                    iconSize(
                        Expression.interpolate(
                            Expression.linear(), Expression.get("speed"),
                            Expression.stop(0.0, 0.05),           // Små piler for lav hastighet
                            Expression.stop(20.0, 0.2)           // Større piler for høy hastighet
                        )
                    ),
                    iconColor(
                        Expression.step(
                            Expression.get("speed"),
                            Expression.color(0xFF0000FF.toInt()), // Blå under terskel
                            Expression.literal(threshold),       // Grenseverdi
                            Expression.color(0xFFFF0000.toInt()) // Rød over terskel
                        )
                    )
                )
                style.addLayer(windLayer)
            }
        }
    }
}
