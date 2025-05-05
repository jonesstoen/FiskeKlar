package no.uio.ifi.in2000.team46.presentation.map.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ZoomButton(
    modifier: Modifier = Modifier,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit
) {
    Column(modifier = Modifier) {
        FloatingActionButton(
            onClick = onZoomIn,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon (
                imageVector = Icons.Default.ZoomIn,
                contentDescription = "Zoom In"
            )
        }

        FloatingActionButton(
            onClick = onZoomOut
        ) {
            Icon(
                imageVector = Icons.Default.ZoomOut,
                contentDescription = "Zoom Out"
            )
        }
    }
}