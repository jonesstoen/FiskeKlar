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

@Composable
fun LegendToggle(
    isLayerVisible: Boolean,
    verticalPosition: Int = 0,   // 0 for første knapp, 1 for andre, osv.
    iconSpacing: Dp = 50.dp,     // avstand mellom knapper vertikalt
    isOpen: Boolean,             // om denne legenden er åpen
    onToggle: () -> Unit,        // toggle-callback fra forelder
    legend: @Composable () -> Unit
) {
    // Sørg for å lukke dersom laget skrus helt av
    LaunchedEffect(isLayerVisible) {
        if (!isLayerVisible && isOpen) {
            onToggle()
        }
    }

    // Beregn vertical offset for Info-knappen
    val bottomOffset = 140.dp + iconSpacing * verticalPosition

    // 1) Info-knapp
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
                    contentDescription = "Forklaring",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // 2) Legend + bakgrunn som fanger trykk utenfor
    if (isOpen) {
        // Transparent fullskjerms-bakgrunn
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .clickable(onClick = onToggle)
                .zIndex(9f)
        )

        // Selve legenden plassert nederst til høyre
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
