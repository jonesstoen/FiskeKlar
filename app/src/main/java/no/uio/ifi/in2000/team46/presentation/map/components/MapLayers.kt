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
import no.uio.ifi.in2000.team46.presentation.map.forbud.ForbudLayer
import no.uio.ifi.in2000.team46.presentation.map.ais.AisViewModel
import no.uio.ifi.in2000.team46.presentation.map.forbud.ForbudViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModel
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsLayerComponent
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import no.uio.ifi.in2000.team46.data.repository.Result
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.PrecipitationViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.WaveViewModel
import no.uio.ifi.in2000.team46.presentation.grib.components.GribWaveLayer
import no.uio.ifi.in2000.team46.presentation.grib.components.PrecipitationLayer
import no.uio.ifi.in2000.team46.presentation.map.favorites.FavoritesLayerViewModel
import no.uio.ifi.in2000.team46.presentation.map.favorites.FavoritesLayer

// the purpose of this class is to modulate the map layers, to avoid repetition, when adding them to the compose map
@Composable
fun MapLayers(
    map: MapLibreMap,
    mapView: MapView,
    aisViewModel: AisViewModel,
    metAlertsViewModel: MetAlertsViewModel,
    forbudViewModel: ForbudViewModel,
    gribViewModel: GribViewModel,
    currentViewModel: CurrentViewModel,
    driftViewModel: DriftViewModel,
    waveViewModel: WaveViewModel,
    precipitationViewModel: PrecipitationViewModel,
    favoritesViewModel: FavoritesLayerViewModel,
    isDarkTheme: Boolean
) {
    MetAlertsLayerComponent(metAlertsViewModel, mapView)
    AisLayer(mapView, aisViewModel)
    ForbudLayer(mapView, forbudViewModel)
    GribWindLayer(
        gribViewModel = gribViewModel,
        map           = map,
        mapView       = mapView,
        filterVectors = true,
        isDarkMode = isDarkTheme
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

    FavoritesLayer(
        mapView = mapView,
        viewModel = favoritesViewModel
    )
}

