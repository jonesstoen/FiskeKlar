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

@Composable
fun WaveLegend(
    modifier: Modifier = Modifier
) {
    // Bakgrunn med litt gjennomsiktighet
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.8f), shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text("Bølgehøyde (m)", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))

        LegendItem(color = Color(0xFFADD8E6), label = "0–1 m")       // LightBlue
        LegendItem(color = Color(0xFF6495ED), label = "1–2 m")       // CornflowerBlue
        LegendItem(color = Color(0xFF4169E1), label = "2–3 m")       // RoyalBlue
        LegendItem(color = Color(0xFF27408B), label = "3–5 m")       // DarkSlateBlue
        LegendItem(color = Color(0xFF00008B), label = "5–8 m")       // DarkBlue
        LegendItem(color = Color(0xFF800080), label = "≥ 8 m")        // Purple
        LegendItem(color = Color(0xFFB2182B), label = "Over terskel")
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, shape = RoundedCornerShape(4.dp))
        )
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
