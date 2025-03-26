package no.uio.ifi.in2000.team46.map.layers


import android.graphics.Color
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
    // Observe metAlertsJson from the ViewModel
    val metAlertsJson by metAlertsViewModel.metAlertsJson.observeAsState()

    // When metAlertsJson changes, try to update the map style
    LaunchedEffect(metAlertsJson) {
        metAlertsJson?.let { json ->
            mapView.getMapAsync { maplibreMap ->
                maplibreMap.getStyle { style ->
                    // Check if the source already exists
                    val source = style.getSourceAs<GeoJsonSource>("metalerts-source")
                    if (source != null) {
                        // Update existing source with new JSON
                        source.setGeoJson(json)
                    } else {
                        // Use helper function to add the source and layer
                        addMetAlertsLayer(style, json)
                    }
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