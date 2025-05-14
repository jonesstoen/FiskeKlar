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
import no.uio.ifi.in2000.team46.domain.grib.CurrentVector
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.CurrentViewModel
import no.uio.ifi.in2000.team46.data.repository.Result
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// this composable renders a floating bottom panel with sliders for adjusting current layer settings
// user can control current threshold and selected timestamp, and toggle sections open/closed

@Composable
fun CurrentOverlaySliders(
    currentViewModel: CurrentViewModel,
    onClose: () -> Unit
) {
    val threshold by currentViewModel.currentThreshold.collectAsState()
    val selectedTimestamp by currentViewModel.selectedTimestamp.collectAsState()
    val currentResult by currentViewModel.currentData.collectAsState()
    val isLoading by currentViewModel.isLoading.collectAsState()

    // extract unique sorted timestamps from data
    val timestamps = remember(currentResult) {
        if (currentResult is Result.Success) {
            (currentResult as Result.Success<List<CurrentVector>>).data
                .map { it.timestamp }
                .distinct()
                .sorted()
        } else emptyList()
    }

    val selectedIndex = timestamps.indexOfFirst { it == selectedTimestamp }.coerceAtLeast(0)

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
                Text("Strøminnstillinger", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Lukk")
                    Spacer(Modifier.width(4.dp))
                    Text("Lukk")
                }
            }

            // section for threshold slider
            SectionHeader(
                title = "Terskel for strøm",
                isExpanded = showThresholdSection,
                onToggle = { showThresholdSection = !showThresholdSection }
            )

            AnimatedVisibility(
                visible = showThresholdSection,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${threshold.toInt()} knop", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = threshold.toFloat(),
                        onValueChange = { currentViewModel.setCurrentThreshold(it.toDouble()) },
                        valueRange = 0f..5f,
                        steps = 9
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0 knop", style = MaterialTheme.typography.labelSmall)
                        Text("5 knop", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            HorizontalDivider()

            // section for timestamp slider
            SectionHeader(
                title = "Tidspunkt",
                isExpanded = showTimestampSection,
                onToggle = { showTimestampSection = !showTimestampSection }
            )

            if (isLoading) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }

            AnimatedVisibility(
                visible = showTimestampSection,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                if (timestamps.isEmpty()) {
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
                        Text(
                            text = formatTimestamp(timestamps[selectedIndex]),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = selectedIndex.toFloat(),
                            onValueChange = { index ->
                                timestamps.getOrNull(index.toInt())?.let {
                                    currentViewModel.setSelectedTimestamp(it)
                                }
                            },
                            valueRange = 0f..(timestamps.size - 1).toFloat(),
                            steps = (timestamps.size - 2).coerceAtLeast(0)
                        )
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

// formats a timestamp in millis into a readable string like "Tuesday 14. May kl. 16:00"
private fun formatTimestamp(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE d. MMMM 'kl.' HH:mm")
        .withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(timestamp))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
