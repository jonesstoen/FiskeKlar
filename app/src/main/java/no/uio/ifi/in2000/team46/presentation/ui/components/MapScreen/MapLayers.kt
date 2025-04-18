package no.uio.ifi.in2000.team46.presentation.ui.components.MapScreen

import androidx.compose.runtime.Composable
import no.uio.ifi.in2000.team46.map.layers.AisLayer
import no.uio.ifi.in2000.team46.map.layers.ForbudLayer
import no.uio.ifi.in2000.team46.map.layers.MetAlertsLayerComponent
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.forbud.ForbudViewModel
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.weather.MetAlertsViewModel
import org.maplibre.android.maps.MapView

@Composable
fun MapLayers(
    mapView: MapView,
    aisViewModel: AisViewModel,
    metAlertsViewModel: MetAlertsViewModel,
    forbudViewModel: ForbudViewModel
) {
    // Her kaller du de eksisterende layer‑composablene dine:
    MetAlertsLayerComponent(metAlertsViewModel, mapView)
    AisLayer(mapView, aisViewModel)
    ForbudLayer(mapView, forbudViewModel)
}
