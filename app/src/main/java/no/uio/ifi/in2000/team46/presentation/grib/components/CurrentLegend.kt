package no.uio.ifi.in2000.team46.presentation.grib.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team46.R

@Composable
fun CurrentLegend(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    // simple list of two current icons with explanations
    val currentLegendItems = listOf(
        Pair("Moderat strøm", "current_icon"),
        Pair("Over terskel", "current_icon_red")
    )

    Surface(
        modifier = modifier
            .padding(8.dp)
            .width(220.dp),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Strømstyrker", style = MaterialTheme.typography.titleSmall)

            currentLegendItems.forEach { (label, drawableName) ->
                val resId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                if (resId != 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = resId),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Text("Trykk på ikonene på kartet for mer info", style = MaterialTheme.typography.bodySmall)
        }
    }
}

