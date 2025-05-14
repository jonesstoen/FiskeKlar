package no.uio.ifi.in2000.team46.presentation.map.ais

import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.domain.ais.AisVesselPosition
import no.uio.ifi.in2000.team46.domain.ais.VesselIcons
import no.uio.ifi.in2000.team46.domain.ais.VesselTypes
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource

// composable that renders AIS vessel layer on map and shows vessel info dialog
// main function: observes vessel positions and toggle state, updates map layers, handles map interactions, and displays vessel info

@Composable
fun AisLayer(
    mapView: MapView,
    aisViewModel: AisViewModel
) {
    // tag for logging
    val TAG = "AisLayerComponent"
    // collect latest vessel positions from viewmodel
    val vesselPositions by aisViewModel.vesselPositions.collectAsState()
    // collect visibility flag for the layer
    val isLayerVisible by aisViewModel.isLayerVisible.collectAsState()
    // coroutine scope for async map interactions
    val coroutineScope = rememberCoroutineScope()

    // initial icon loading into map style
    LaunchedEffect(Unit) {
        VesselIcons.initializeIcons(mapView.context)
        mapView.getMapAsync { maplibreMap ->
            maplibreMap.getStyle { style ->
                // add each vessel icon to map style
                VesselIcons.getIcons().forEach { (iconName, bitmap) ->
                    style.addImage(iconName, bitmap)
                }
            }
        }
    }

    // update or remove AIS layer when vessel data or visibility changes
    LaunchedEffect(vesselPositions, isLayerVisible) {
        if (isLayerVisible) {
            updateAisLayer(mapView, vesselPositions)
        } else {
            removeAisLayer(mapView)
        }
    }

    // listen to camera moves to update visible region in viewmodel
    LaunchedEffect(isLayerVisible) {
        if (isLayerVisible) {
            mapView.getMapAsync { maplibreMap ->
                val initialBounds = maplibreMap.projection.visibleRegion.latLngBounds
                updateViewportBounds(initialBounds, aisViewModel)

                maplibreMap.addOnCameraMoveListener {
                    val newBounds = maplibreMap.projection.visibleRegion.latLngBounds
                    coroutineScope.launch {
                        updateViewportBounds(newBounds, aisViewModel)
                    }
                }
            }
        }
    }

    // handle map clicks to show vessel info dialog
    LaunchedEffect(Unit) {
        mapView.getMapAsync { maplibreMap ->
            maplibreMap.addOnMapClickListener { point ->
                val screenPoint = maplibreMap.projection.toScreenLocation(point)
                val features = maplibreMap.queryRenderedFeatures(
                    android.graphics.PointF(screenPoint.x, screenPoint.y),
                    "ais-vessels-layer"
                )

                if (features.isNotEmpty()) {
                    // parse vessel properties and show dialog via viewmodel
                    val vessel = features[0]
                    val properties = vessel.properties()

                    aisViewModel.showVesselInfo(
                        mmsi = properties?.get("mmsi")?.asString ?: "",
                        name = properties?.get("name")?.asString ?: "",
                        speed = properties?.get("speedOverGround")?.asDouble ?: 0.0,
                        course = properties?.get("courseOverGround")?.asDouble ?: 0.0,
                        heading = properties?.get("trueHeading")?.asDouble ?: 0.0,
                        shipType = properties?.get("shipType")?.asInt ?: 0
                    )
                    true
                } else {
                    false
                }
            }
        }
    }

    // show vessel info dialog if a vessel is selected
    if (aisViewModel.selectedVessel.value != null) {
        VesselInfoDialog(
            vessel = aisViewModel.selectedVessel.value!!,
            onDismiss = { aisViewModel.hideVesselInfo() }
        )
    }
}

@Composable
private fun VesselInfoDialog(
    vessel: AisVesselPosition,
    onDismiss: () -> Unit
) {
    // alert dialog displaying detailed info about selected vessel
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(vessel.name, fontSize = 25.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // display vessel properties in rows
                Row {
                    Text("MMSI: ", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("${vessel.mmsi}", fontSize = 20.sp)
                }
                Row {
                    Text("Type: ", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(VesselTypes.ALL_TYPES.entries.find { it.value == vessel.shipType }?.key
                        ?: "ukjent", fontSize = 20.sp)
                }
                Row {
                    Text("fart: ", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("${vessel.speedOverGround} knop", fontSize = 20.sp)
                }
                Row {
                    Text("kurs: ", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("${vessel.courseOverGround}°", fontSize = 20.sp)
                }
                Row {
                    Text("stevning: ", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("${vessel.trueHeading}°", fontSize = 20.sp)
                }
            }
        },
        confirmButton = {
            // close button for dialog
            TextButton(onClick = onDismiss) {
                Text("lukk")
            }
        }
    )
}

private fun updateViewportBounds(bounds: LatLngBounds, viewModel: AisViewModel) {
    try {
        // update viewmodel with current map bounds
        viewModel.updateVisibleRegion(
            minLon = bounds.longitudeWest,
            minLat = bounds.latitudeSouth,
            maxLon = bounds.longitudeEast,
            maxLat = bounds.latitudeNorth
        )
    } catch (e: Exception) {
        Log.e("AisLayerComponent", "Error updating viewport bounds", e)
    }
}

private fun updateAisLayer(mapView: MapView, vessels: List<AisVesselPosition>) {
    val TAG = "AisLayerComponent"
    // return early if no vessels to draw
    if (vessels.isEmpty()) {
        Log.d(TAG, "No vessels to display")
        return
    }

    mapView.getMapAsync { maplibreMap ->
        maplibreMap.getStyle { style ->
            try {
                // remove existing AIS layers and source
                style.getLayer("ais-vessels-text-layer")?.let { style.removeLayer(it) }
                style.getLayer("ais-vessels-layer")?.let { style.removeLayer(it) }
                style.getSource("ais-vessels-source")?.let { style.removeSource(it) }

                // build geojson feature collection from vessel data
                val features = JSONArray().apply {
                    vessels.forEach { vessel ->
                        val vesselStyle = VesselIcons.getVesselStyle(vessel.shipType)
                        put(JSONObject().apply {
                            put("type", "Feature")
                            put("geometry", JSONObject().apply {
                                put("type", "Point")
                                put("coordinates", JSONArray().apply {
                                    put(vessel.longitude)
                                    put(vessel.latitude)
                                })
                            })
                            put("properties", JSONObject().apply {
                                put("mmsi", vessel.mmsi)
                                put("name", vessel.name)
                                put("speedOverGround", vessel.speedOverGround ?: 0.0)
                                put("courseOverGround", vessel.courseOverGround ?: 0.0)
                                put("trueHeading", vessel.trueHeading ?: 0)
                                put("shipType", vessel.shipType)
                                put("iconType", vesselStyle.iconType)
                                put("color", String.format("#%06X", 0xFFFFFF and vesselStyle.color))
                            })
                        })
                    }
                }

                val featureCollection = JSONObject().apply {
                    // assemble feature collection for map source
                    put("type", "FeatureCollection")
                    put("features", features)
                }

                // add geojson source to map style
                val sourceId = "ais-vessels-source"
                val source = GeoJsonSource(sourceId, featureCollection.toString())
                style.addSource(source)

                // symbol layer for vessel icons
                val symbolLayer = SymbolLayer("ais-vessels-layer", sourceId).withProperties(
                    PropertyFactory.iconImage(Expression.get("iconType")),
                    PropertyFactory.iconSize(1.0f),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.iconRotate(Expression.get("courseOverGround")),
                    PropertyFactory.iconColor(Expression.get("color")),
                    PropertyFactory.iconOpacity(1f)
                )
                style.addLayer(symbolLayer)

                // text layer for vessel names
                val textLayer = SymbolLayer("ais-vessels-text-layer", sourceId).withProperties(
                    PropertyFactory.textField(Expression.get("name")),
                    PropertyFactory.textSize(12f),
                    PropertyFactory.textColor(Expression.get("color")),
                    PropertyFactory.textHaloColor(Color.WHITE),
                    PropertyFactory.textHaloWidth(1f),
                    PropertyFactory.textOffset(arrayOf(0f, 1.5f)),
                    PropertyFactory.textAllowOverlap(false)
                )
                textLayer.minZoom = 11.0f
                style.addLayer(textLayer)

            } catch (e: Exception) {
                Log.e(TAG, "Error updating AIS layer", e)
            }
        }
    }
}

private fun removeAisLayer(mapView: MapView) {
    // remove AIS layers and source from map style
    mapView.getMapAsync { maplibreMap ->
        maplibreMap.getStyle { style ->
            try {
                style.getLayer("ais-vessels-text-layer")?.let { style.removeLayer(it) }
                style.getLayer("ais-vessels-layer")?.let { style.removeLayer(it) }
                style.getSource("ais-vessels-source")?.let { style.removeSource(it) }
            } catch (e: Exception) {
                Log.e("AisLayerComponent", "Error removing AIS layer", e)
            }
        }
    }
}
