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
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.PrecipitationViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// composable that displays precipitation threshold and timestamp sliders as overlay

@Composable
fun PrecipitationOverlaySliders(
    viewModel: PrecipitationViewModel,
    onClose: () -> Unit
) {
    // observe precip threshold value
    val threshold by viewModel.precipThreshold.collectAsState()
    // observe selected timestamp
    val timestamp by viewModel.selectedTimestamp.collectAsState()
    // observe precipitation data result
    val data by viewModel.data.collectAsState()

    // extract sorted distinct timestamps when data is ready
    val allTimestamps = remember(data) {
        (data as? no.uio.ifi.in2000.team46.data.repository.Result.Success)?.data
            ?.map { it.timestamp }
            ?.distinct()
            ?.sorted()
            ?: emptyList()
    }

    // return early if no timestamps available
    if (allTimestamps.isEmpty()) return

    // find index of current timestamp or default to zero
    val selectedIndex = allTimestamps.indexOfFirst { it == timestamp }.coerceAtLeast(0)

    // local states for expanded sections
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
            // header row with title and close action
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Nedbørsinnstillinger", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "lukk")
                    Spacer(Modifier.width(4.dp))
                    Text("lukk")
                }
            }

            // threshold section header
            SectionHeader(
                title = "Terskel for nedbør",
                isExpanded = showThresholdSection,
                onToggle = { showThresholdSection = !showThresholdSection }
            )

            AnimatedVisibility(
                visible = showThresholdSection,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // display current threshold in mm
                    Text("${threshold.toInt()} mm", style = MaterialTheme.typography.bodyMedium)
                    // slider for adjusting precipitation threshold
                    Slider(
                        value = threshold.toFloat(),
                        onValueChange = { viewModel.setPrecipThreshold(it.toDouble()) },
                        valueRange = 0f..20f,
                        steps = 19
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0 mm", style = MaterialTheme.typography.labelSmall)
                        Text("20 mm", style = MaterialTheme.typography.labelSmall)
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // formatted display of selected timestamp
                    Text(
                        text = formatTimestamp(allTimestamps[selectedIndex]),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    // slider for selecting from available timestamps
                    Slider(
                        value = selectedIndex.toFloat(),
                        onValueChange = { index ->
                            allTimestamps.getOrNull(index.toInt())?.let {
                                viewModel.setSelectedTimestamp(it)
                            }
                        },
                        valueRange = 0f..(allTimestamps.size - 1).toFloat(),
                        steps = (allTimestamps.size - 2).coerceAtLeast(0)
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
