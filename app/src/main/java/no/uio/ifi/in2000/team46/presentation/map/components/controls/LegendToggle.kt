package no.uio.ifi.in2000.team46.presentation.map.components.controls


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.times

@Composable
fun LegendToggle(
    isLayerVisible: Boolean,
    verticalPosition: Int = 0, // 0 for first button, 1 for second button, etc.
    iconSpacing: Dp = 50.dp, // Spacing between buttons
    legend: @Composable () -> Unit
) {
    val showLegend = rememberSaveable { mutableStateOf(false) }
    // Hide legend if the layer is no longer visible
    LaunchedEffect(isLayerVisible) {
        if (!isLayerVisible) {
            showLegend.value = false
        }
    }

    // Calculate vertical offset based on position
    val bottomOffset = 140.dp + (verticalPosition * iconSpacing)

    if (isLayerVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 55.dp, bottom = bottomOffset)
                .zIndex(10f),
            contentAlignment = Alignment.BottomEnd
        ) {
            IconButton(
                onClick = { showLegend.value = !showLegend.value },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Forklaring",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showLegend.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 16.dp, bottom = 40.dp)
                .zIndex(9f),
            contentAlignment = Alignment.BottomEnd
        ) {
            legend()
        }
    }
}


