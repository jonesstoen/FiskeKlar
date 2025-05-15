package no.uio.ifi.in2000.team46.presentation.map.components


import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import no.uio.ifi.in2000.team46.presentation.grib.components.*
import no.uio.ifi.in2000.team46.presentation.map.components.controls.LegendController
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsLegend
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.*
import no.uio.ifi.in2000.team46.data.repository.Result
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import no.uio.ifi.in2000.team46.presentation.map.components.controls.LegendToggle
import no.uio.ifi.in2000.team46.presentation.map.metalerts.MetAlertsViewModel
import androidx.compose.ui.Alignment

// the purpose of this class is to modulate the legend panel, to avoird repetition
@Composable
fun LegendPanel(
    modifier: Modifier = Modifier,
    isDark: Boolean,
    legendController: LegendController,
    gribViewModel: GribViewModel,
    waveViewModel: WaveViewModel,
    precipitationViewModel: PrecipitationViewModel,
    currentViewModel: CurrentViewModel,
    metAlertsViewModel: MetAlertsViewModel
) {
    val isWindLayerVisible by gribViewModel.isLayerVisible.collectAsState()
    val isWaveLayerVisible by waveViewModel.isLayerVisible.collectAsState()
    val isPrecipLayerVisible by precipitationViewModel.isLayerVisible.collectAsState()
    val isCurrentLayerVisible by currentViewModel.isLayerVisible.collectAsState()
    val isMetAlertsVisible by metAlertsViewModel.isLayerVisible.collectAsState()
    val waveResult by waveViewModel.waveData.collectAsState()

    // IDs: 0=wind, 1=waves, 2=Rain, 3=current, 4=MetAlerts

    LegendToggle(
        isLayerVisible = isWindLayerVisible,
        verticalPosition = 0,
        isOpen = legendController.isOpen(0),
        onToggle = { legendController.toggle(0) }
    ) {
        WindLegend(modifier = Modifier.wrapContentWidth(Alignment.End), isDark = isDark)
    }

    LegendToggle(
        isLayerVisible = isWaveLayerVisible && waveResult is Result.Success,
        verticalPosition = 1,
        isOpen = legendController.isOpen(1),
        onToggle = { legendController.toggle(1) }
    ) {
        WaveLegend(modifier = Modifier.wrapContentWidth(Alignment.End))
    }

    LegendToggle(
        isLayerVisible = isPrecipLayerVisible,
        verticalPosition = 2,
        isOpen = legendController.isOpen(2),
        onToggle = { legendController.toggle(2) }
    ) {
        PrecipitationLegend(modifier = Modifier.wrapContentWidth(Alignment.End))
    }

    LegendToggle(
        isLayerVisible = isCurrentLayerVisible,
        verticalPosition = 3,
        isOpen = legendController.isOpen(3),
        onToggle = { legendController.toggle(3) }
    ) {

        CurrentLegend(modifier = Modifier.wrapContentWidth(Alignment.End))
    }

    LegendToggle(
        isLayerVisible = isMetAlertsVisible,
        verticalPosition = 4,
        isOpen = legendController.isOpen(4),
        onToggle = { legendController.toggle(4) }
    ) {
        MetAlertsLegend(modifier = Modifier.wrapContentWidth(Alignment.End))
    }
}
