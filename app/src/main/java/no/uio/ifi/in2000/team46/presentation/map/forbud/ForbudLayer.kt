package no.uio.ifi.in2000.team46.presentation.map.forbud

import android.util.Log
import androidx.compose.runtime.*
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource

import android.content.Context
import android.graphics.BitmapFactory


import androidx.compose.ui.platform.LocalContext
import no.uio.ifi.in2000.team46.R

import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.PropertyFactory.*
@Composable
fun ForbudLayer(
    mapView: MapView,
    viewModel: ForbudViewModel
) {
    val context = LocalContext.current
    val geoJson by viewModel.geoJson.collectAsState()
    val isVisible by viewModel.isLayerVisible.collectAsState()

    LaunchedEffect(isVisible, geoJson) {
        mapView.getMapAsync { maplibreMap ->
            maplibreMap.getStyle { style ->
                if (isVisible && geoJson != null) {
                    val source = style.getSourceAs<GeoJsonSource>("forbud-source")
                    if (source != null) {
                        try {
                            source.setGeoJson(geoJson!!)
                            Log.d("ForbudLayer", "Oppdaterte GeoJSON for forbud")
                        } catch (e: Exception) {
                            Log.e("ForbudLayer", "Feil ved setting av GeoJSON", e)
                        }
                    } else {
                        addForbudLayer(context, style, geoJson!!)
                    }
                } else {
                    style.getLayer("forbud-icons")?.let { style.removeLayer(it) }
                    style.getLayer("forbud-fill")?.let { style.removeLayer(it) }
                    style.getSource("forbud-source")?.let { style.removeSource(it) }
                    Log.d("ForbudLayer", "Fjernet forbudslag")
                }
            }
        }
    }
}

fun addForbudLayer(context: Context, style: Style, geoJson: String) {
    val source = GeoJsonSource("forbud-source", geoJson)
    style.addSource(source)

    // Fyll polygonene med oransje farge og litt gjennomsiktighet
    val fillLayer = FillLayer("forbud-fill", "forbud-source").withProperties(
        fillColor("#FFA500"), // Oransje
        fillOpacity(0.4f)
    )
    style.addLayerBelow(fillLayer, "waterway-label") // eller annen passende layer

    // Legg til varselikon (symbol) oppå
    style.addImage(
        "icon-warning-orange",
        BitmapFactory.decodeResource(context.resources, R.drawable.icon_warning_generic_orange_png)
    )

    val symbolLayer = SymbolLayer("forbud-icons", "forbud-source").apply {
        setProperties(
            iconImage("icon-warning-orange"),
            iconSize(0.4f),
            iconAllowOverlap(true)
        )
    }

    style.addLayer(symbolLayer)

    Log.d("ForbudLayer", "La til forbudslag med polygoner i oransje")
}
