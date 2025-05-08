package no.uio.ifi.in2000.team46.presentation.map.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.GribViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.WaveViewModel

@Composable
fun GribLayerMenu(
    isWind: Boolean,
    isCurrent: Boolean,
    isDrift: Boolean,
    isWave: Boolean,
    isPrecip: Boolean,
    onToggleWind: (Boolean) -> Unit,
    onToggleCurrent: () -> Unit,
    onToggleDrift: () -> Unit,
    onToggleWave: (Boolean) -> Unit,
    onTogglePrecip: () -> Unit,
    onBack: () -> Unit,
    gribViewModel: GribViewModel,
    waveViewModel: WaveViewModel
) {
    var showWindMenu by remember { mutableStateOf(false) }
    var showWaveMenu by remember { mutableStateOf(false) }

    when {
        showWindMenu -> {
            WindLayerSettingsMenu(
                isChecked = isWind,
                onToggleLayer = onToggleWind,
                gribViewModel = gribViewModel,
                onBack = { showWindMenu = false }
            )
        }

        showWaveMenu -> {
            val threshold by waveViewModel.waveThreshold.collectAsState()

            WaveLayerSettings(
                isChecked = isWave,
                threshold = threshold,
                onToggle = onToggleWave,
                onThresholdChange = { waveViewModel.setWaveThreshold(it) },
                onBack = { showWaveMenu = false }
            )
        }

        else -> {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Tilbake")
                }

                TextButton(onClick = { showWindMenu = true }) {
                    Text(" Vind Vektorer")
                }

                LayerToggleRow("Strøm", isCurrent, { onToggleCurrent() })
                LayerToggleRow("Drift", isDrift, { onToggleDrift() })

                TextButton(onClick = { showWaveMenu = true }) {
                    Text(" Bølgeinnstillinger")
                }

                LayerToggleRow("Regn", isPrecip, { onTogglePrecip() })
            }
        }
    }
}
