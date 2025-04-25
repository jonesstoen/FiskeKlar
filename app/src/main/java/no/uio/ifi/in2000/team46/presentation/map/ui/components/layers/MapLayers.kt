package no.uio.ifi.in2000.team46.presentation.map.ui.components.layers

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import no.uio.ifi.in2000.team46.presentation.grib.CurrentViewModel
import no.uio.ifi.in2000.team46.presentation.grib.GribViewModel
import no.uio.ifi.in2000.team46.presentation.grib.components.GribCurrentLayer
import no.uio.ifi.in2000.team46.presentation.grib.components.GribWindLayer
import no.uio.ifi.in2000.team46.presentation.map.ais.AisLayer
import no.uio.ifi.in2000.team46.presentation.map.forbud.ForbudLayer
import no.uio.ifi.in2000.team46.presentation.map.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.map.forbud.ForbudViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModel
import no.uio.ifi.in2000.team46.presentation.map.ui.components.MetAlertsLayerComponent
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@Composable
fun MapLayers(
    map: MapLibreMap,
    mapView: MapView,
    aisViewModel: AisViewModel,
    metAlertsViewModel: MetAlertsViewModel,
    forbudViewModel: ForbudViewModel,
    gribViewModel: GribViewModel,
    currentViewModel: CurrentViewModel
) {
    MetAlertsLayerComponent(metAlertsViewModel, mapView)
    AisLayer(mapView, aisViewModel)
    ForbudLayer(mapView, forbudViewModel)
    GribWindLayer(
        gribViewModel = gribViewModel,
        map           = map,
        mapView       = mapView
    )
    GribCurrentLayer( // ⬅️ Ny!
        currentViewModel = currentViewModel,
        map              = map,
        mapView          = mapView
    )
    Log.d("MapLayers", "Is Current Layer Visible: ${currentViewModel.isLayerVisible.collectAsState().value}")
    Log.d("MapLayers", "Current Data: ${currentViewModel.currentData.collectAsState().value}")

}
