package no.uio.ifi.in2000.team46.map.layers


import android.graphics.BitmapFactory
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
import org.maplibre.android.style.layers.SymbolLayer
import no.uio.ifi.in2000.team46.R
import org.maplibre.android.style.layers.PropertyFactory.*


@Composable
fun MetAlertsLayerComponent(
    metAlertsViewModel: MetAlertsViewModel,
    mapView: MapView
) {
    val context = LocalContext.current
    val json by metAlertsViewModel.metAlertsJson.collectAsState()
    val isVisible by metAlertsViewModel.isLayerVisible.collectAsState()

    LaunchedEffect(isVisible, json) {
        mapView.getMapAsync { maplibreMap ->
            maplibreMap.getStyle { style ->
                if (isVisible && json != null) {
                    val source = style.getSourceAs<GeoJsonSource>("metalerts-source")
                    if (source != null) {
                        try {
                            source.setGeoJson(json!!)
                            Log.d("MetAlertsLayerComponent", "Oppdatert GeoJson-data")
                        } catch (e: Exception) {
                            Log.e("MetAlertsLayerComponent", "Kunne ikke sette GeoJson", e)
                        }
                    } else {
                        addMetAlertsLayer(context, style, json!!)
                        // added  click listener
                        maplibreMap.addOnMapClickListener { point ->
                            val screenPoint = maplibreMap.projection.toScreenLocation(point)
                            val features = maplibreMap.queryRenderedFeatures(screenPoint, "metalerts-layer")

                            if (features.isNotEmpty()) {
                                val featureId = features[0].getStringProperty("id")
                                metAlertsViewModel.selectFeature(featureId)
                                true
                            } else {
                                false
                            }
                        }
                    }
                } else {
                    style.getLayer("metalerts-layer")?.let { style.removeLayer(it) }
                    style.getLayer("metalerts-icons")?.let { style.removeLayer(it) }
                    style.getSource("metalerts-source")?.let { style.removeSource(it) }

                    Log.d("MetAlertsLayerComponent", "Fjernet metalerts-lag")
                }
            }
        }
    }
}

fun addMetAlertsLayer(context: android.content.Context, style: Style, json: String) {
    // creating and adding GeoJsonSource with MetAlerts data
    val source = GeoJsonSource("metalerts-source", json)
    style.addSource(source)

    // adding waring icons to the style
    style.addImage("icon-warning-yellow",
        BitmapFactory.decodeResource(context.resources, R.drawable.icon_warning_generic_yellow_png))
    style.addImage("icon-warning-orange",
        BitmapFactory.decodeResource(context.resources, R.drawable.icon_warning_generic_orange_png))
    style.addImage("icon-warning-red",
        BitmapFactory.decodeResource(context.resources, R.drawable.icon_warning_generic_red_png))

    // coloring the layer based on the riskMatrixColor property
    val fillColorExpression = Expression.match(
        Expression.get("riskMatrixColor"),
        Expression.literal("Yellow"), Expression.color(Color.parseColor("#FFFF00")),
        Expression.literal("Orange"), Expression.color(Color.parseColor("#FFA500")),
        Expression.literal("Red"), Expression.color(Color.RED),
        Expression.color(Color.GRAY)
    )
    // creating and adding FillLayer with MetAlerts data
    val fillLayer = FillLayer("metalerts-layer", "metalerts-source")
    fillLayer.setProperties(
        fillColor(fillColorExpression),
        fillOpacity(0.5f)
    )
    style.addLayer(fillLayer)

    // creating and adding SymbolLayer with the warning icons
    val iconImageExpression = Expression.match(
        Expression.get("riskMatrixColor"),
        Expression.literal("Yellow"), Expression.literal("icon-warning-yellow"),
        Expression.literal("Orange"), Expression.literal("icon-warning-orange"),
        Expression.literal("Red"), Expression.literal("icon-warning-red"),
        Expression.literal("icon-warning-yellow") // fallback
    )

    val symbolLayer = SymbolLayer("metalerts-icons", "metalerts-source")
    symbolLayer.setProperties(
        iconImage(iconImageExpression),
        iconSize(0.3f),
        iconAllowOverlap(true)
    )
    style.addLayerAbove(symbolLayer, "metalerts-layer")
}