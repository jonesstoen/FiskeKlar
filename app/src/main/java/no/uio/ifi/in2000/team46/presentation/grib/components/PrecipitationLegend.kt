package no.uio.ifi.in2000.team46.presentation.grib.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun PrecipitationLegend(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text("Nedbør (mm)", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))

        // Faste intervaller
        LegendItem(color = Color(0xFFADD8E6), label = "0–1 mm")    // very light blue
        LegendItem(color = Color(0xFF6495ED), label = "1–5 mm")    // light blue
        LegendItem(color = Color(0xFF4169E1), label = "5–10 mm")   // medium blue
        LegendItem(color = Color(0xFF27408B), label = "10–15 mm")  // darker blue
        LegendItem(color = Color(0xFF00008B), label = "15–20 mm")  // dark blue
        LegendItem(color = Color(0xFF800080), label = "≥ 20 mm")   // very dark blue
        LegendItem(color = Color(0xFFB2182B), label = "Over terskel")
    }
}

/** Samme LegendItem som hos bølge‐legenden */
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
