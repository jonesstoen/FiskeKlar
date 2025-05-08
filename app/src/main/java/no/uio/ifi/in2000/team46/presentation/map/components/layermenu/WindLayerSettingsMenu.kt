package no.uio.ifi.in2000.team46.presentation.map.components.layermenu



import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.GribViewModel

@Composable
fun WindLayerSettingsMenu(
    isChecked: Boolean,
    onToggleLayer: (Boolean) -> Unit,
    gribViewModel: GribViewModel,
    onBack: () -> Unit
) {
    val threshold = gribViewModel.windThreshold.collectAsState()

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Tilbake")
        }

        // wind layer toggle
        LayerToggleRow("Aktiver vindlag", isChecked, onToggleLayer)

        // wind threshold slider
        Text("Terskel for vind: ${threshold.value.toInt()} m/s")
        Slider(
            value = threshold.value.toFloat(),
            onValueChange = { gribViewModel.setWindThreshold(it.toDouble()) },
            valueRange = 5f..30f,
            steps = 25,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
