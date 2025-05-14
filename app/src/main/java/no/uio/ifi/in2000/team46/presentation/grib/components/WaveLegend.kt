package no.uio.ifi.in2000.team46.presentation.grib.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// this composable shows a legend mapping wave height ranges to color swatches

@Composable
fun WaveLegend(
    modifier: Modifier = Modifier
) {
    // create translucent background with rounded corners and padding
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        // title for wave height legend
        Text("Bølgehøyde (m)", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))

        // individual legend entries with color box and label
        LegendItem(color = Color(0xFFADD8E6), label = "0–1 m")       // lightblue for calm waves
        LegendItem(color = Color(0xFF6495ED), label = "1–2 m")       // cornflowerblue for small waves
        LegendItem(color = Color(0xFF4169E1), label = "2–3 m")       // royalblue for moderate waves
        LegendItem(color = Color(0xFF27408B), label = "3–5 m")       // darkslateblue for larger waves
        LegendItem(color = Color(0xFF00008B), label = "5–8 m")       // darkblue for rough seas
        LegendItem(color = Color(0xFF800080), label = "≥ 8 m")        // purple for very rough seas
        LegendItem(color = Color(0xFFB2182B), label = "Over terskel") // red for waves over threshold
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    // row aligning color box and label text
    Row(verticalAlignment = Alignment.CenterVertically) {
        // colored box representing legend category
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, shape = RoundedCornerShape(4.dp))
        )
        Spacer(Modifier.width(6.dp))
        // label for the legend item
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
