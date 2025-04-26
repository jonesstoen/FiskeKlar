package no.uio.ifi.in2000.team46.presentation.grib.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import no.uio.ifi.in2000.team46.presentation.grib.DriftViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DriftVectorInfoBox(viewModel: DriftViewModel) {
    val selected by viewModel.selectedDriftVector.collectAsState()

    selected?.let {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.9f))
                .clip(RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Column {
                Text("Speed: ${"%.2f".format(it.speed)} m/s")
                Text("Direction: ${"%.1f".format(it.direction)}°")
                Text("Estimated Drift: ${"%.0f".format(it.driftImpact)} meters/hour")
            }
        }
    }
}
