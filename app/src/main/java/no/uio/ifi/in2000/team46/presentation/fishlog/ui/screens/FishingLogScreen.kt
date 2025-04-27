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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog
import no.uio.ifi.in2000.team46.presentation.fishlog.ui.viewmodel.FishingLogUiContract
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishingLogScreen(
    viewModel: FishingLogUiContract,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.entries.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Fiskelogg") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigate("addFishingEntry") },
                icon = { Icon(Icons.Default.Add, contentDescription = "Legg til fangst") },
                text = { Text("Legg til fangst") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { paddingValues ->
        if (entries.isEmpty()) {
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
                    Text(
                        "Ingen fangster enda",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Trykk på + knappen for å legge til din første fangst",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = entries.sortedByDescending { it.date },
                    key = { entry -> entry.id }
                ) { entry ->
                    FishingEntryCard(
                        entry = entry,
                        onDelete = { viewModel.removeEntry(entry) },
                        onClick = { onNavigate("fishingLogDetail/${entry.id}") }
                    )
                }
            }
        }
    }
}


@Composable
fun FishingEntryCard(
    entry: FishingLog,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale("no"))
    }
    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("HH:mm")
    }


    val dateText = LocalDate.parse(entry.date).format(dateFormatter)
    val timeText = LocalTime.parse(entry.time).format(timeFormatter)

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
            entry.imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Fangstbilde",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$dateText  •  $timeText",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${entry.fishType.ifBlank { "Ukjent fisk" }} — " +
                            "${"%.1f".format(entry.weight)} kg",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Slett")
            }
        }
    }
}
