package no.uio.ifi.in2000.team46.presentation.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WarningLayerMenu(isChecked: Boolean, onToggle: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Tilbake")
        }
        LayerToggleRow("Farevarsler", isChecked, { onToggle() })
    }
}