package no.uio.ifi.in2000.team46.presentation.map.metalerts


import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.SymbolLayer
import no.uio.ifi.in2000.team46.R
import no.uio.ifi.in2000.team46.domain.metalerts.Feature
import org.maplibre.android.style.layers.PropertyFactory.*


@Composable
fun MetAlertsLayerComponent(
    metAlertsViewModel: MetAlertsViewModel,
    mapView: MapView
) {
    val context = LocalContext.current
    val json by metAlertsViewModel.metAlertsJson.collectAsState()
    val isVisible by metAlertsViewModel.isLayerVisible.collectAsState()
    val metAlertsResponse by metAlertsViewModel.metAlertsResponse.collectAsState()

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
                        // Call filterAndAssignIcons and pass the result to addMetAlertsLayer
                    val featuresWithIcons = metAlertsResponse?.let { response ->
                        metAlertsViewModel.filterAndAssignIcons(response)
                    }

                    addMetAlertsLayer(context, style, json!!, featuresWithIcons)
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

fun addMetAlertsLayer(
    context: android.content.Context,
    style: Style,
    json: String,
    featuresWithIcons: List<Pair<Feature, String>>?
) {
    // creating and adding GeoJsonSource with MetAlerts data
    val source = GeoJsonSource("metalerts-source", json)
    style.addSource(source)
    // creating a map of icon names to their resource IDs
    //TODO: maybe move this to a separate file? not in this method
    val iconResourceMap = mapOf(
        "icon-warning-wind-yellow" to R.drawable.icon_warning_wind_yellow,
        "icon-warning-wind-orange" to R.drawable.icon_warning_wind_orange,
        "icon-warning-wind-red" to R.drawable.icon_warning_wind_red,
        "icon-warning-rain-yellow" to R.drawable.icon_warning_rain_yellow,
        "icon-warning-rain-orange" to R.drawable.icon_warning_rain_orange,
        "icon-warning-rain-red" to R.drawable.icon_warning_rain_red,
        "icon-warning-flood-yellow" to R.drawable.icon_warning_flood_yellow,
        "icon-warning-flood-orange" to R.drawable.icon_warning_flood_orange,
        "icon-warning-flood-red" to R.drawable.icon_warning_flood_red,
        "icon-warning-generic-yellow" to R.drawable.icon_warning_generic_yellow_png,
        "icon-warning-generic-orange" to R.drawable.icon_warning_generic_orange_png,
        "icon-warning-generic-red" to R.drawable.icon_warning_generic_red_png
    )
    // Add icons dynamically based on featuresWithIcons
    featuresWithIcons?.forEach { (_, iconName) ->
        // Check if the iconName exists in the resource map
        val resourceId = iconResourceMap[iconName]
        Log.d("addMetAlertsLayer", "Icon name: $iconName, Resource ID: $resourceId")
        // decoding the resource to bitmap
        val bitmap = resourceId?.let { BitmapFactory.decodeResource(context.resources, it) }
        // adding the bitmap to the style
        if (bitmap != null) {
            style.addImage(iconName, bitmap)
            Log.d("addMetAlertsLayer", "Added image for $iconName")
        } else {
            Log.e("addMetAlertsLayer", "Failed to decode resource for $iconName")
        }
    }
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

    // setting the icon image to the one we added above
    symbolLayer.setProperties(
        iconImage(Expression.get("icon")),
        iconSize(0.3f),
        iconAllowOverlap(true)
    )
    // setting the filter to show the icons only when zoom level is 5 or higher
    symbolLayer.setFilter(
        Expression.gte(Expression.zoom(), 5.0)

    )
    style.addLayerAbove(symbolLayer, "metalerts-layer")
}