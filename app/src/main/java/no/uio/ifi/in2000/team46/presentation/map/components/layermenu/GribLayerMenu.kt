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
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.PrecipitationViewModel
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.WaveViewModel
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch


sealed class GribMenuState {
    object Main : GribMenuState()
    object Wind : GribMenuState()
    object Current : GribMenuState()
    object Wave : GribMenuState()
    object Precipitation : GribMenuState()
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
    precipitationViewModel: PrecipitationViewModel
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
                onBack = { onStateChange(GribMenuState.Main) }
            )
        }

        GribMenuState.Main -> {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Tilbake")
                }

                ListItem(
                    headlineContent = { Text("Vind Vektorer") },
                    leadingContent = { Icon(Icons.Default.Air, contentDescription = null) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    modifier = Modifier.clickable { onStateChange(GribMenuState.Wind) }
                )

                ListItem(
                    headlineContent = { Text("Strøm Vektorer") },
                    leadingContent = { Icon(Icons.Default.Water, contentDescription = null) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    modifier = Modifier.clickable { onStateChange(GribMenuState.Current) }
                )

                ListItem(
                    headlineContent = { Text("Drift Vektorer") },
                    leadingContent = { Icon(Icons.Default.DirectionsBoat, contentDescription = null) },
                    trailingContent = {
                        Switch(
                            checked = isDrift,
                            onCheckedChange = { onToggleDrift() }
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text("Bølger") },
                    leadingContent = { Icon(Icons.Default.Waves, contentDescription = null) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    modifier = Modifier.clickable { onStateChange(GribMenuState.Wave) }
                )

                ListItem(
                    headlineContent = { Text("Regn") },
                    leadingContent = { Icon(Icons.Default.InvertColors, contentDescription = null) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    modifier = Modifier.clickable { onStateChange(GribMenuState.Precipitation) }
                )
            }
        }

    }
}

