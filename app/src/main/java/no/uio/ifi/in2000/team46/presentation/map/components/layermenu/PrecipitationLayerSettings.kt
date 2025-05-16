package no.uio.ifi.in2000.team46.presentation.map.components.layermenu

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

//Warnings: the warnings here is beacuase of unused parameters after refactoring parts of the project
@Composable
fun PrecipitationLayerSettings(
    isChecked: Boolean,
    threshold: Double,
    onToggle: (Boolean) -> Unit,
    onThresholdChange: (Double) -> Unit,
    onBack: () -> Unit,
    onShowSliders: () -> Unit
) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Tilbake")
        }

        LayerToggleRow("Vis regn", isChecked, onToggle)


        Button(
            onClick = onShowSliders,
            enabled = isChecked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Åpne regnkontroller")
        }
    }
}
