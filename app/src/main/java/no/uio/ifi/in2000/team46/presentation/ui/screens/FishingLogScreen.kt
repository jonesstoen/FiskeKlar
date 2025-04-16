package no.uio.ifi.in2000.team46.presentation.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog
import no.uio.ifi.in2000.team46.presentation.ui.components.AddFishingEntryDialog
import no.uio.ifi.in2000.team46.presentation.ui.components.BottomNavBar
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.fishlog.FishingLogViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishingLogScreen(
    viewModel: FishingLogViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var fisketype by remember { mutableStateOf("") }
    var vekt by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val entries by viewModel.entries.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fiskelogg") }
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = "fishlog",
                onNavigate = onNavigate
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
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
                    .padding(horizontal = 16.dp)
            ) {
                items(entries.sortedByDescending { it.date }) { entry ->
                    FishingEntryCard(
                        entry = entry,
                        onDelete = { viewModel.removeEntry(entry) },
                        onClick = { onNavigate("fishingLogDetail/${entry.id}") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (showAddDialog) {
            AddFishingEntryDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { date, location, fishType, weight, notes, imageUri ->
                    // Kall din ViewModel-metode for å lagre fangsten.
                    // Her kan du for eksempel sende LocalTime.now() for tidspunktet om du ønsker det.
                    if (notes != null) {
                        viewModel.addEntry(
                            date = date,
                            time = LocalTime.now(),
                            location = location,
                            fishType = fishType,
                            weight = weight,
                            notes = notes,
                            imageUri = imageUri
                        )
                    }
                    // Lukker dialogen etter bekreftelse
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun FishingEntryCard(
    entry: FishingLog,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                // Siden entry.date og entry.time nå er Strings, viser vi dem direkte.
                Text(
                    text = entry.date,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = entry.time,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Slett")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Sted: ${entry.location}",
            style = MaterialTheme.typography.bodyLarge
        )
        // Legg gjerne til flere felter, f.eks. fisketype og vekt
        Text(
            "Fisketype: ${entry.fishType}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            "Vekt: ${entry.weight} kg",
            style = MaterialTheme.typography.bodyLarge
        )
        entry.notes?.let { notes ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                notes,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


