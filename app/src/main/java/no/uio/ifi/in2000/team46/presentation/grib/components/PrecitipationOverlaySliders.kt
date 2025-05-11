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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import no.uio.ifi.in2000.team46.presentation.grib.viewmodel.PrecipitationViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun PrecipitationOverlaySliders(
    viewModel: PrecipitationViewModel,
    onClose: () -> Unit
) {
    val threshold by viewModel.precipThreshold.collectAsState()
    val timestamp by viewModel.selectedTimestamp.collectAsState()
    val data by viewModel.data.collectAsState()

    val allTimestamps = remember(data) {
        (data as? no.uio.ifi.in2000.team46.data.repository.Result.Success)?.data
            ?.map { it.timestamp }
            ?.distinct()
            ?.sorted()
            ?: emptyList()
    }

    if (allTimestamps.isEmpty()) return

    val selectedIndex = allTimestamps.indexOfFirst { it == timestamp }.coerceAtLeast(0)

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
            // Header
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Nedbørsinnstillinger", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Lukk")
                    Spacer(Modifier.width(4.dp))
                    Text("Lukk")
                }
            }

            // Terskel
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
                    Text("${threshold.toInt()} mm", style = MaterialTheme.typography.bodyMedium)
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

            Divider()

            // Tidspunkt
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
                if (allTimestamps.isEmpty()) {
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
                            text = formatTimestamp(allTimestamps[selectedIndex]),
                            style = MaterialTheme.typography.bodyMedium
                        )
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

private fun formatTimestamp(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE d. MMMM 'kl.' HH:mm")
        .withZone(ZoneId.systemDefault())
    return formatter.format(Instant.ofEpochMilli(timestamp))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
