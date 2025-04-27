package no.uio.ifi.in2000.team46.presentation.map.ui.components.layers

import androidx.compose.runtime.Composable
import no.uio.ifi.in2000.team46.data.local.parser.calculateDriftImpactForVector
import no.uio.ifi.in2000.team46.presentation.grib.CurrentViewModel
import no.uio.ifi.in2000.team46.presentation.grib.DriftViewModel
import no.uio.ifi.in2000.team46.presentation.grib.GribViewModel
import no.uio.ifi.in2000.team46.presentation.grib.components.DriftLayer
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
import no.uio.ifi.in2000.team46.data.repository.Result

@Composable
fun MapLayers(
    map: MapLibreMap,
    mapView: MapView,
    aisViewModel: AisViewModel,
    metAlertsViewModel: MetAlertsViewModel,
    forbudViewModel: ForbudViewModel,
    gribViewModel: GribViewModel,
    currentViewModel: CurrentViewModel,
    driftViewModel: DriftViewModel
) {
    MetAlertsLayerComponent(metAlertsViewModel, mapView)
    AisLayer(mapView, aisViewModel)
    ForbudLayer(mapView, forbudViewModel)
    GribWindLayer(
        gribViewModel = gribViewModel,
        map           = map,
        mapView       = mapView
    )
    GribCurrentLayer(
        currentViewModel = currentViewModel,
        map              = map,
        mapView          = mapView
    )
    DriftLayer(
        driftViewModel = driftViewModel,
        map = map,
        mapView = mapView,
        onDriftVectorSelected = { totalSpeed, totalDirection, _, point ->
            val matchingDriftVector = driftViewModel.driftData.value
                ?.let { result -> (result as? Result.Success)?.data }
                ?.find { it.lon == point.longitude() && it.lat == point.latitude() }

            matchingDriftVector?.let { driftVector ->
                val driftImpact = calculateDriftImpactForVector(driftVector)

                driftViewModel.selectDriftVectorInfo(
                    speed        = totalSpeed,
                    direction    = totalDirection,
                    driftImpact  = driftImpact,
                    point        = point
                )
            }
        },
        onDriftVectorCleared = {
            driftViewModel.clearDriftVectorInfo()
        }
    )


}

