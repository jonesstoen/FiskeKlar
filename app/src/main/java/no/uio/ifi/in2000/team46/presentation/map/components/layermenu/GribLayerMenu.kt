package no.uio.ifi.in2000.team46.presentation.map.components.layermenu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.CurrentViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.GribViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.PrecipitationViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.WaveViewModel

// defines all sub-navigation options for GRIB layers
sealed class GribMenuState {
    data object Main : GribMenuState()
    data object Wind : GribMenuState()
    data object Current : GribMenuState()
    data object Wave : GribMenuState()
    data object Precipitation : GribMenuState()
}

@Composable
fun GribLayerMenu(
    state: GribMenuState,
    onStateChange: (GribMenuState) -> Unit,
    isWind: Boolean,
    isCurrent: Boolean,
    isDrift: Boolean,
    isWave: Boolean,
    isPrecip: Boolean,
    onToggleWind: (Boolean) -> Unit,
    onToggleCurrent: (Boolean) -> Unit,
    onToggleDrift: () -> Unit,
    onToggleWave: (Boolean) -> Unit,
    onTogglePrecip: (Boolean) -> Unit,
    onBack: () -> Unit,
    gribViewModel: GribViewModel,
    waveViewModel: WaveViewModel,
    currentViewModel: CurrentViewModel,
    precipitationViewModel: PrecipitationViewModel,
    onShowWindSliders: () -> Unit,
    onShowCurrentSliders: () -> Unit,
    onShowPrecipSliders: () -> Unit,
    onLayerMenuExpandedChange: (Boolean) -> Unit
) {
    // uses ViewModel to mutate menu navigation state
    fun goTo(state: GribMenuState) = gribViewModel.setGribMenuState(state)

    when (state) {
        is GribMenuState.Wind -> {
            WindLayerSettingsMenu(
                isChecked = isWind,
                onToggleLayer = onToggleWind,
                gribViewModel = gribViewModel,
                onBack = { goTo(GribMenuState.Main) },
                onShowSliders = onShowWindSliders
            )
        }

        is GribMenuState.Wave -> {
            val waveThreshold by waveViewModel.waveThreshold.collectAsState()
            WaveLayerSettings(
                isChecked = isWave,
                threshold = waveThreshold,
                onToggle = onToggleWave,
                onThresholdChange = { waveViewModel.setWaveThreshold(it) },
                onBack = { goTo(GribMenuState.Main) },
                onShowSliders = { waveViewModel.setShowWaveSliders(true) }
            )
        }

        is GribMenuState.Current -> {
            val currentThreshold by currentViewModel.currentThreshold.collectAsState()
            CurrentLayerSettings(
                isChecked = isCurrent,
                threshold = currentThreshold,
                onToggle = onToggleCurrent,
                onThresholdChange = { currentViewModel.setCurrentThreshold(it) },
                onBack = { goTo(GribMenuState.Main) },
                onShowSliders = onShowCurrentSliders
            )
        }

        is GribMenuState.Precipitation -> {
            val threshold by precipitationViewModel.precipThreshold.collectAsState()
            PrecipitationLayerSettings(
                isChecked = isPrecip,
                threshold = threshold,
                onToggle = { isChecked ->
                    if (isChecked) precipitationViewModel.toggleLayerVisibility()
                    else precipitationViewModel.deactivateLayer()
                },
                onThresholdChange = { precipitationViewModel.setPrecipThreshold(it) },
                onBack = { goTo(GribMenuState.Main) },
                onShowSliders = {
                    onLayerMenuExpandedChange(false)
                    precipitationViewModel.setShowPrecipSliders(true)
                }
            )
        }

        // main entry screen for GRIB menu options
        GribMenuState.Main -> {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Tilbake")
                }

                ListItem(
                    headlineContent = { Text("Vind Vektorer") },
                    leadingContent = { Icon(Icons.Default.Air, contentDescription = null) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    modifier = Modifier.clickable { goTo(GribMenuState.Wind) }
                )

                ListItem(
                    headlineContent = { Text("Strøm Vektorer") },
                    leadingContent = { Icon(Icons.Default.Water, contentDescription = null) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    modifier = Modifier.clickable { goTo(GribMenuState.Current) }
                )
                // drift is not implemented yet
                /*ListItem(
                    headlineContent = { Text("Drift Vektorer") },
                    leadingContent = { Icon(Icons.Default.DirectionsBoat, contentDescription = null) },
                    trailingContent = {
                        Switch(
                            checked = isDrift,
                            onCheckedChange = { onToggleDrift() }
                        )
                    }
                )*/

                ListItem(
                    headlineContent = { Text("Bølger") },
                    leadingContent = { Icon(Icons.Default.Waves, contentDescription = null) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    modifier = Modifier.clickable { goTo(GribMenuState.Wave) }
                )

                ListItem(
                    headlineContent = { Text("Regn") },
                    leadingContent = { Icon(Icons.Default.InvertColors, contentDescription = null) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    modifier = Modifier.clickable { goTo(GribMenuState.Precipitation) }
                )
            }
        }
    }
}
