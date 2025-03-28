package no.uio.ifi.in2000.team46.map.layers


import android.graphics.Color
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.weather.MetAlertsViewModel
import org.maplibre.android.maps.MapView

import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource


import org.maplibre.android.maps.Style

@Composable
fun MetAlertsLayerComponent(
    metAlertsViewModel: MetAlertsViewModel,
    mapView: MapView
) {
    val json by metAlertsViewModel.metAlertsJson.collectAsState()
    val isVisible by metAlertsViewModel.isLayerVisible.collectAsState()

    LaunchedEffect(isVisible, json) {
        mapView.getMapAsync { maplibreMap ->
            maplibreMap.getStyle { style ->
                if (isVisible && json != null) {
                    val source = style.getSourceAs<GeoJsonSource>("metalerts-source")
                    if (source != null) {
                        source.setGeoJson(json!!)
                        Log.d("MetAlertsLayerComponent", "Updated MetAlerts source")
                    } else {
                        addMetAlertsLayer(style, json!!)
                        Log.d("MetAlertsLayerComponent", "Added MetAlerts layer")
                    }
                } else {
                    style.removeLayer("metalerts-layer")
                    style.removeSource("metalerts-source")
                    Log.d("MetAlertsLayerComponent", "Removed MetAlerts layer")
                }
            }
        }
    }
}

fun addMetAlertsLayer(style: Style, json: String) {
    // Create and add GeoJsonSource with MetAlerts data
    val source = GeoJsonSource("metalerts-source", json)
    style.addSource(source)

    // Create FillLayer with dynamic color based on "riskMatrixColor"
    val fillColorExpression = Expression.match(
        Expression.get("riskMatrixColor"),
        Expression.literal("Yellow"), Expression.color(Color.parseColor("#FFFF00")),
        Expression.literal("Orange"), Expression.color(Color.parseColor("#FFA500")),
        Expression.literal("Red"), Expression.color(Color.RED),
        Expression.color(Color.GRAY) // fallback color
    )
    val metAlertsLayer = FillLayer("metalerts-layer", "metalerts-source").withProperties(
        PropertyFactory.fillColor(fillColorExpression),
        PropertyFactory.fillOpacity(0.5f)
    )
    style.addLayer(metAlertsLayer)
}