package no.uio.ifi.in2000.team46.presentation.fishlog.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import no.uio.ifi.in2000.team46.presentation.fishlog.viewmodel.FishingLogViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishingLogScreen(
    viewModel: FishingLogViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.entries.collectAsState(initial = emptyList())
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Bekreft sletting") },
            text = { Text("Vil du slette alle fangster?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllLogs()
                    showDeleteDialog = false
                }) {
                    Text("Slett")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Avbryt")
                }
            }
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fiskelogg") },
                navigationIcon = {
                    IconButton(onClick = { onNavigate("home") }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tilbake til hjem")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Slett alle", tint = Color.Red)
                    }
                }
            )
        },


        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigate("addFishingEntry") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
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
                items(entries.sortedByDescending { it.date }) { entry ->
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
    val dateFmt = DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale("no"))
    val dateText = LocalDate.parse(entry.date).format(dateFmt)
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // thumbnail  for entry
            entry.imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Fangstbilde",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.width(8.dp))
            }

            // date time fishtype and weight
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = if (entry.fishType.isBlank() && entry.weight == 0.0) "Ingen fangst"
                        else "${entry.fishType.ifBlank { "Ukjent fisk" }} — ${"%.1f".format(entry.weight)} kg${if (entry.count > 0) " (${entry.count} stk)" else ""}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // delete button
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Slett")
            }
        }
    }
}



