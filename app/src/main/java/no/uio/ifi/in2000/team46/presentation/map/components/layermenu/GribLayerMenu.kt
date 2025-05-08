package no.uio.ifi.in2000.team46.presentation.map.components.layermenu

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.CurrentViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.GribViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.WaveViewModel

sealed class GribMenuState {
    object Main : GribMenuState()
    object Wind : GribMenuState()
    object Current : GribMenuState()
    object Wave : GribMenuState()
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
    onTogglePrecip: () -> Unit,
    onBack: () -> Unit,
    gribViewModel: GribViewModel,
    waveViewModel: WaveViewModel,
    currentViewModel: CurrentViewModel,
) {
    when (state) {
        is GribMenuState.Wind -> {
            WindLayerSettingsMenu(
                isChecked = isWind,
                onToggleLayer = onToggleWind,
                gribViewModel = gribViewModel,
                onBack = { onStateChange(GribMenuState.Main) }
            )
        }

        is GribMenuState.Wave -> {
            val waveThreshold by waveViewModel.waveThreshold.collectAsState()
            WaveLayerSettings(
                isChecked = isWave,
                threshold = waveThreshold,
                onToggle = onToggleWave,
                onThresholdChange = { waveViewModel.setWaveThreshold(it) },
                onBack = { onStateChange(GribMenuState.Main) }
            )
        }

        is GribMenuState.Current -> {
            val currentThreshold by currentViewModel.currentThreshold.collectAsState()
            CurrentLayerSettings(
                isChecked = isCurrent,
                threshold = currentThreshold,
                onToggle = onToggleCurrent,
                onThresholdChange = { currentViewModel.setCurrentThreshold(it) },
                onBack = { onStateChange(GribMenuState.Main) }
            )
        }

        is GribMenuState.Main -> {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Tilbake")
                }

                TextButton(onClick = { onStateChange(GribMenuState.Wind) }) {
                    Text("Vind Vektorer")
                }

                TextButton(onClick = { onStateChange(GribMenuState.Current) }) {
                    Text("Strøminnstillinger")
                }

                LayerToggleRow("Drift", isDrift, { onToggleDrift() })

                TextButton(onClick = { onStateChange(GribMenuState.Wave) }) {
                    Text("Bølgeinnstillinger")
                }

                LayerToggleRow("Regn", isPrecip, { onTogglePrecip() })
            }
        }
    }
}
