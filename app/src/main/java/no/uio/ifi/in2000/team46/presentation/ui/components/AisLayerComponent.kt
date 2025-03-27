package no.uio.ifi.in2000.team46.presentation.ui.components

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.model.AisVesselPosition
import no.uio.ifi.in2000.team46.data.model.VesselIcons
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.maplibre.MapViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.ais.AisViewModel
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource

@Composable
fun AisLayerComponent(
    mapViewModel: MapViewModel, // Replace MapViewComponent with MapViewModel
    aisViewModel: AisViewModel
) {
    val TAG = "AisLayerComponent"
    val vesselPositions by aisViewModel.vesselPositions.observeAsState(emptyList())
    val isLayerVisible by aisViewModel.isLayerVisible.observeAsState(false)
    val isLoading by aisViewModel.isLoading.observeAsState(false)
    val error by aisViewModel.error.observeAsState()
    val coroutineScope = rememberCoroutineScope()

    // Get the MapView from MapViewModel instead of a separate component
    val mapView = mapViewModel.mapView

    LaunchedEffect(vesselPositions, isLayerVisible) {
        if (isLayerVisible) {
            updateAisLayer(mapView, vesselPositions)
        } else {
            removeAisLayer(mapView)
        }
    }

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
}

private fun updateViewportBounds(bounds: LatLngBounds, viewModel: AisViewModel) {
    try {
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

private fun updateAisLayer(mapViewModel: MapViewModel, vessels: List<AisVesselPosition>) {
    val TAG = "AisLayerComponent"

    if (vessels.isEmpty()) {
        Log.d(TAG, "No vessels to display")
        return
    }

    mapViewModel.mapView.getMapAsync { maplibreMap ->
        maplibreMap.getStyle { style ->
            try {
                style.getLayer("ais-vessels-layer")?.let { style.removeLayer(it) }
                style.getLayer("ais-vessels-text-layer")?.let { style.removeLayer(it) }
                style.getSource("ais-vessels-source")?.let { style.removeSource(it) }

                val features = JSONArray()

                vessels.forEach { vessel ->
                    try {
                        val vesselStyle = VesselIcons.getVesselStyle(vessel.shipType)

                        val feature = JSONObject().apply {
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
                        }
                        features.put(feature)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing vessel ${vessel.mmsi}", e)
                    }
                }

                val featureCollection = JSONObject().apply {
                    put("type", "FeatureCollection")
                    put("features", features)
                }

                val sourceId = "ais-vessels-source"
                val source = GeoJsonSource(sourceId, featureCollection.toString())
                style.addSource(source)

                val symbolLayer = SymbolLayer("ais-vessels-layer", sourceId).withProperties(
                    PropertyFactory.iconImage(Expression.get("iconType")),
                    PropertyFactory.iconSize(1.0f),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.iconRotate(Expression.get("courseOverGround")),
                    PropertyFactory.iconColor(Expression.get("color"))
                )
                style.addLayer(symbolLayer)

                val textLayer = SymbolLayer("ais-vessels-text-layer", sourceId).withProperties(
                    PropertyFactory.textField(Expression.get("name")),
                    PropertyFactory.textSize(12f),
                    PropertyFactory.textColor(Expression.get("color")),
                    PropertyFactory.textHaloColor(android.graphics.Color.WHITE),
                    PropertyFactory.textHaloWidth(1f),
                    PropertyFactory.textOffset(arrayOf(0f, 1.5f)),
                    PropertyFactory.textAllowOverlap(false)
                )
                style.addLayer(textLayer)

            } catch (e: Exception) {
                Log.e(TAG, "Error updating AIS layer", e)
            }
        }
    }
}

private fun removeAisLayer(mapViewModel: MapViewModel) {
    mapViewModel.mapView.getMapAsync { maplibreMap ->
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