package no.uio.ifi.in2000.team46.presentation.fishlog.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog
import no.uio.ifi.in2000.team46.presentation.fishlog.ui.viewmodel.FishingLogViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.LocalTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishingLogScreen(
    viewModel: FishingLogViewModel,
    navigateTo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val fishingEntries by viewModel.entries.collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Fiskelogg") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navigateTo("addFishingEntry") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Legg til fangst") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { paddingValues ->
        if (fishingEntries.isEmpty()) {
            DisplayEmptyState(paddingValues)
        } else {
            DisplayFishingLogList(fishingEntries, navigateTo, paddingValues, viewModel)
        }
    }
}

@Composable
fun DisplayEmptyState(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Ingen fangster enda", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Trykk på + knappen for å legge til din første fangst", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun DisplayFishingLogList(fishingEntries: List<FishingLog>, navigateTo: (String) -> Unit, paddingValues: PaddingValues, viewModel: FishingLogViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(fishingEntries.sortedByDescending { it.date }) { entry ->
            DisplayFishingEntryCard(entry, onDelete = { viewModel.removeEntry(entry) }, onClick = { navigateTo("fishingLogDetail/${entry.id}") })
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DisplayFishingEntryCard(
    fishingLogEntry: FishingLog,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val dateTimeFormatter = DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale("no"))
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val formattedDate = LocalDate.parse(fishingLogEntry.date).format(dateTimeFormatter)
    val formattedTime = LocalTime.parse(fishingLogEntry.time).format(timeFormatter)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            fishingLogEntry.imageUri?.let { imageUri ->
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Fangstbilde",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            DisplayFishingEntryDetails(fishingLogEntry, formattedDate, formattedTime)

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Slett")
            }
        }
    }
}

@Composable
fun RowScope.DisplayFishingEntryDetails(fishingLogEntry: FishingLog, formattedDate: String, formattedTime: String) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = "$formattedDate  •  $formattedTime", style = MaterialTheme.typography.titleSmall)
        Text(
            text = "${fishingLogEntry.fishType.ifBlank { "Ukjent fisk" }} — " +
                    "${"%.1f".format(fishingLogEntry.weight)} kg",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
