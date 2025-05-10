package no.uio.ifi.in2000.team46.presentation.map.components.layermenu

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WaveLayerSettings(
    isChecked: Boolean,
    threshold: Double,
    onToggle: (Boolean) -> Unit,
    onThresholdChange: (Double) -> Unit,
    onBack: () -> Unit,
    onShowSliders: () -> Unit
) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Tilbake")
        }

        LayerToggleRow("Vis bølger", isChecked, onToggle)

        Button(
            onClick = onShowSliders,
            enabled = isChecked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Åpne bølgekontroller")
        }
    }
}
