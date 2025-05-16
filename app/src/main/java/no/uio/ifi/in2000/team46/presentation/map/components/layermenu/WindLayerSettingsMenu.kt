package no.uio.ifi.in2000.team46.presentation.map.components.layermenu

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team46.domain.grib.WindVector
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.GribViewModel
import no.uio.ifi.in2000.team46.data.repository.Result

//Warnings: the warnings here is beacuase of unused parameters after refactoring parts of the project
@Composable
fun WindLayerSettingsMenu(
    isChecked: Boolean,
    onToggleLayer: (Boolean) -> Unit,
    gribViewModel: GribViewModel,
    onBack: () -> Unit,
    onShowSliders: () -> Unit
) {
    // collect state from the ViewModel
    val threshold by gribViewModel.windThreshold.collectAsState()
    val windResult by gribViewModel.windData.collectAsState()
    val selectedTimestamp by gribViewModel.selectedTimestamp.collectAsState()

    // extract sorted unique timestamps if data is available
    val timestamps = remember(windResult) {
        if (windResult is Result.Success) {
            (windResult as Result.Success<List<WindVector>>)
                .data.map { it.timestamp }.distinct().sorted()
        } else emptyList()
    }
    val selectedIndex = timestamps.indexOfFirst { it == selectedTimestamp }.coerceAtLeast(0)

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // back button to return to main GRIB menu
        TextButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Tilbake")
        }

        // toggle for enabling/disabling wind layer
        LayerToggleRow("Aktiver vindlag", isChecked, onToggleLayer)

        // open sliders only if wind layer is active
        Button(
            onClick = onShowSliders,
            enabled = isChecked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Åpne vindkontroller")
        }
    }
}
