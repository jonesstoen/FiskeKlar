package no.uio.ifi.in2000.team46.presentation.map.components.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

// summary: provides an info button to toggle a legend overlay and handles auto-close when layer is hidden
// main function: display toggle button and legend content with overlay background for dismissing

@Composable
fun LegendToggle(
    isLayerVisible: Boolean,
    verticalPosition: Int = 0,
    iconSpacing: Dp = 50.dp,
    isOpen: Boolean,
    onToggle: () -> Unit,        // callback to toggle legend state
    legend: @Composable () -> Unit
) {
    // close legend automatically when layer becomes invisible
    LaunchedEffect(isLayerVisible) {
        if (!isLayerVisible && isOpen) {
            onToggle()
        }
    }

    // calculate bottom offset for info button based on position and spacing
    val bottomOffset = 430.dp + iconSpacing * verticalPosition

    // 1) display info button when layer is visible
    if (isLayerVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 55.dp, bottom = bottomOffset)
                .zIndex(10f),
            contentAlignment = Alignment.BottomEnd
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "forklaring",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // 2) show legend overlay and transparent background to catch outside clicks
    if (isOpen) {
        // transparent full screen background that closes legend on click
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .clickable(onClick = onToggle)
                .zIndex(9f)
        )

        // container for legend content at bottom right
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 16.dp, bottom = 40.dp)
                .zIndex(10f),
            contentAlignment = Alignment.BottomEnd
        ) {
            legend()
        }
    }
}
