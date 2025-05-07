package no.uio.ifi.in2000.team46.presentation.map.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.GribViewModel
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
    onToggleWave: () -> Unit,
    onTogglePrecip: () -> Unit,
    onBack: () -> Unit,
    gribViewModel: GribViewModel
) {
    var showWindMenu by remember { mutableStateOf(false) }

    if (showWindMenu) {
        WindLayerSettingsMenu(
            isChecked = isWind,
            onToggleLayer = onToggleWind,
            gribViewModel = gribViewModel,
            onBack = { showWindMenu = false }
        )
    } else {
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
            LayerToggleRow("Bølger", isWave, { onToggleWave() })
            LayerToggleRow("Regn", isPrecip, { onTogglePrecip() })
        }
    }
}

