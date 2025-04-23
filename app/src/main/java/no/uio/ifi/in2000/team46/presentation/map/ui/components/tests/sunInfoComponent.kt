package no.uio.ifi.in2000.team46.presentation.map.ui.components.tests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun SunInfoComponent(sunInfoViewModel: SunInfoViewModel, modifier: Modifier = Modifier) {
    val sunInfo by sunInfoViewModel.sunInfo.observeAsState()
    val error by sunInfoViewModel.error.observeAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(Color.Transparent)
            .padding(8.dp)
    ) {
        if (error != null) {
            Text(text = "Error: $error", color = Color.Red)
        } else {
            sunInfo?.let {
                Text(text = "City: Oslo")
                it.properties.sunrise?.let { sunrise ->
                    val formattedTime = formatTime(sunrise.time)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.WbSunny, contentDescription = "Sunrise")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = ": $formattedTime Local Time")
                    }
                } ?: Text(text = "Sunrise: N/A")
                it.properties.sunset?.let { sunset ->
                    val formattedTime = formatTime(sunset.time)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.NightsStay, contentDescription = "Sunset")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = ": $formattedTime Local Time")
                    }
                } ?: Text(text = "Sunset: N/A")
            } ?: Text(text = "No sun info available")
        }
    }
}

fun formatTime(time: String): String? {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX", Locale.getDefault())
    val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val date = inputFormat.parse(time)
    return date?.let { outputFormat.format(it) }
}