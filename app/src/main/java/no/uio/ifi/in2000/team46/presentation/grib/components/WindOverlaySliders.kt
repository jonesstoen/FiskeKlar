package no.uio.ifi.in2000.team46.presentation.grib.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import no.uio.ifi.in2000.team46.domain.grib.WindVector
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.GribViewModel
import no.uio.ifi.in2000.team46.data.repository.Result
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// this component provides sliders for adjusting wind threshold and selecting timestamp for wind overlay

@Composable
fun WindOverlaySliders(
    gribViewModel: GribViewModel,
    onClose: () -> Unit
) {
    // collect threshold, selected timestamp and wind data from viewmodel
    val threshold by gribViewModel.windThreshold.collectAsState()
    val selectedTimestamp by gribViewModel.selectedTimestamp.collectAsState()
    val windResult by gribViewModel.windData.collectAsState()

    // derive sorted list of distinct timestamps when wind data is loaded
    val timestamps = remember(windResult) {
        if (windResult is Result.Success) {
            (windResult as Result.Success<List<WindVector>>).data
                .map { it.timestamp }
                .distinct()
                .sorted()
        } else emptyList()
    }

    // find index of currently selected timestamp or default to 0
    val selectedIndex = timestamps.indexOfFirst { it == selectedTimestamp }.coerceAtLeast(0)

    // local state for expanding or collapsing sections
    var showThresholdSection by remember { mutableStateOf(false) }
    var showTimestampSection by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(20f)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // header row with title and close button
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Vindinnstillinger", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Lukk")
                    Spacer(Modifier.width(4.dp))
                    Text("Lukk")
                }
            }

            // section header for wind threshold
            SectionHeader(
                title = "Terskel for vind",
                isExpanded = showThresholdSection,
                onToggle = { showThresholdSection = !showThresholdSection }
            )

            AnimatedVisibility(
                visible = showThresholdSection,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // display current threshold value
                    Text(
                        text = "${threshold.toInt()} m/s",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    // slider to adjust wind threshold between 5 and 30 m/s
                    Slider(
                        value = threshold.toFloat(),
                        onValueChange = { gribViewModel.setWindThreshold(it.toDouble()) },
                        valueRange = 5f..30f,
                        steps = 25
                    )
                    // labels for min and max values
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("5 m/s", style = MaterialTheme.typography.labelSmall)
                        Text("30 m/s", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            HorizontalDivider()

            // section header for timestamp selection
            SectionHeader(
                title = "Tidspunkt",
                isExpanded = showTimestampSection,
                onToggle = { showTimestampSection = !showTimestampSection }
            )

            AnimatedVisibility(
                visible = showTimestampSection,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                if (timestamps.isEmpty()) {
                    // show loading indicator while timestamps are not ready
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // display formatted timestamp for selected index
                        Text(
                            text = formatTimestamp(timestamps[selectedIndex]),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        // slider to pick among available timestamps
                        Slider(
                            value = selectedIndex.toFloat(),
                            onValueChange = { index ->
                                timestamps.getOrNull(index.toInt())?.let {
                                    gribViewModel.setSelectedTimestamp(it)
                                }
                            },
                            valueRange = 0f..(timestamps.size - 1).toFloat(),
                            steps = (timestamps.size - 2).coerceAtLeast(0)
                        )
                        // labels for earliest and latest options
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tidligste", style = MaterialTheme.typography.labelSmall)
                            Text("Seneste", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, isExpanded: Boolean, onToggle: () -> Unit) {
    // reusable header for collapsible sections
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    // format epoch millis into local date string with day name, date and time
    val formatter = DateTimeFormatter.ofPattern("EEEE d. MMMM 'kl.' HH:mm")
        .withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(timestamp))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
