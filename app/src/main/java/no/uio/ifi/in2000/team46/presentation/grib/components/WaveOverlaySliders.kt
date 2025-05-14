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
import no.uio.ifi.in2000.team46.domain.grib.WaveVector
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.WaveViewModel
import no.uio.ifi.in2000.team46.data.repository.Result
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// composable that displays wave settings overlay with threshold slider and timestamp selector

@Composable
fun WaveOverlaySliders(
    waveViewModel: WaveViewModel,
    onClose: () -> Unit
) {
    // observe current threshold value
    val threshold by waveViewModel.waveThreshold.collectAsState()
    // observe currently selected timestamp
    val selectedTimestamp by waveViewModel.selectedTimestamp.collectAsState()
    // observe wave data result
    val waveResult by waveViewModel.waveData.collectAsState()

    // extract sorted distinct timestamps when data is available
    val timestamps = remember(waveResult) {
        if (waveResult is Result.Success) {
            (waveResult as Result.Success<List<WaveVector>>).data
                .map { it.timestamp }
                .distinct()
                .sorted()
        } else emptyList()
    }

    // find index of selected timestamp or default to zero
    val selectedIndex = timestamps.indexOfFirst { it == selectedTimestamp }.coerceAtLeast(0)

    // local states for section expansion
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
            // header with title and close button
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bølgeinnstillinger", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "lukk")
                    Spacer(Modifier.width(4.dp))
                    Text("lukk")
                }
            }

            // threshold section header
            SectionHeader(
                title = "Terskel for bølgehøyde",
                isExpanded = showThresholdSection,
                onToggle = { showThresholdSection = !showThresholdSection }
            )

            AnimatedVisibility(
                visible = showThresholdSection,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // display current threshold in meters
                    Text("${threshold.toInt()} m", style = MaterialTheme.typography.bodyMedium)
                    // slider to adjust wave height threshold
                    Slider(
                        value = threshold.toFloat(),
                        onValueChange = { waveViewModel.setWaveThreshold(it.toDouble()) },
                        valueRange = 0f..10f,
                        steps = 9
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0 m", style = MaterialTheme.typography.labelSmall)
                        Text("10 m", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            HorizontalDivider()

            // timestamp section header
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
                    // show loading indicator when timestamps are not ready
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
                        // display formatted selected timestamp
                        Text(
                            text = formatTimestamp(timestamps[selectedIndex]),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        // slider to select timestamp index
                        Slider(
                            value = selectedIndex.toFloat(),
                            onValueChange = { index ->
                                timestamps.getOrNull(index.toInt())?.let {
                                    waveViewModel.setSelectedTimestamp(it)
                                }
                            },
                            valueRange = 0f..(timestamps.size - 1).toFloat(),
                            steps = (timestamps.size - 2).coerceAtLeast(0)
                        )
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("tidligste", style = MaterialTheme.typography.labelSmall)
                            Text("seneste", style = MaterialTheme.typography.labelSmall)
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

private fun formatTimestamp(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE d. MMMM 'kl.' HH:mm")
        .withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(timestamp))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
