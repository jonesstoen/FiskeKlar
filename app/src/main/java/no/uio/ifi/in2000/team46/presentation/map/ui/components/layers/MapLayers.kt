package no.uio.ifi.in2000.team46.presentation.map.ui.components.layers

import androidx.compose.runtime.Composable
import no.uio.ifi.in2000.team46.presentation.map.ais.AisLayer
import no.uio.ifi.in2000.team46.presentation.map.forbud.ForbudLayer
import no.uio.ifi.in2000.team46.presentation.map.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.map.forbud.ForbudViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModel
import no.uio.ifi.in2000.team46.presentation.map.ui.components.MetAlertsLayerComponent
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
