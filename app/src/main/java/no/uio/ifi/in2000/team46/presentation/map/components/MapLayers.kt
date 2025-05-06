package no.uio.ifi.in2000.team46.presentation.map.components

import androidx.compose.runtime.Composable
import no.uio.ifi.in2000.team46.domain.usecase.drift.calculateDriftImpactForVector
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.CurrentViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.DriftViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.GribViewModel
import no.uio.ifi.in2000.team46.presentation.grib.components.DriftLayer
import no.uio.ifi.in2000.team46.presentation.grib.components.GribCurrentLayer
import no.uio.ifi.in2000.team46.presentation.grib.components.GribWindLayer
import no.uio.ifi.in2000.team46.presentation.map.ais.AisLayer
import no.uio.ifi.in2000.team46.presentation.map.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsLayerComponent
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import no.uio.ifi.in2000.team46.data.repository.Result
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.PrecipitationViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.WaveViewModel
import no.uio.ifi.in2000.team46.presentation.grib.components.GribWaveLayer
import no.uio.ifi.in2000.team46.presentation.grib.components.PrecipitationLayer

@Composable
fun MapLayers(
    map: MapLibreMap,
    mapView: MapView,
    aisViewModel: AisViewModel,
    metAlertsViewModel: MetAlertsViewModel,
    gribViewModel: GribViewModel,
    currentViewModel: CurrentViewModel,
    driftViewModel: DriftViewModel,
    waveViewModel: WaveViewModel,
    precipitationViewModel: PrecipitationViewModel
) {
    MetAlertsLayerComponent(metAlertsViewModel, mapView)
    AisLayer(mapView, aisViewModel)
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
    GribWaveLayer(
        waveViewModel = waveViewModel,
        map           = map,
        mapView       = mapView
    )

    PrecipitationLayer(
        vm = precipitationViewModel,
        map = map
    )


}

