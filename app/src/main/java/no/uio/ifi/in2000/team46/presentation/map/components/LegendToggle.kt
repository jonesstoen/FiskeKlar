package no.uio.ifi.in2000.team46.presentation.map.components


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun LegendToggle(
    isLayerVisible: Boolean,
    iconOffset: Dp = 100.dp,
    legendOffset: Dp = 160.dp,
    legend: @Composable () -> Unit
) {
    val showLegend = rememberSaveable { mutableStateOf(false) }

    if (isLayerVisible) {
        IconButton(
            onClick = { showLegend.value = !showLegend.value },
            modifier = Modifier
                .padding(top = iconOffset, end = 12.dp)
                .zIndex(10f)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Forklaring",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (showLegend.value) {
        Box(
            modifier = Modifier
                .padding(top = legendOffset, end = 12.dp)
                .zIndex(9f)
        ) {
            legend()
        }
    }
}
