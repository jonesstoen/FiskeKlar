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

        LegendItem(color = Color(33, 102, 172), label = "0 m")    // dyp blå
        LegendItem(color = Color(103, 169, 207), label = "1 m")
        LegendItem(color = Color(209, 229, 240), label = "2 m")
        LegendItem(color = Color(253, 219, 199), label = "3 m")
        LegendItem(color = Color(239, 138,  98), label = "5 m")
        LegendItem(color = Color(178,  24,  43), label = "≥ 8 m")
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
