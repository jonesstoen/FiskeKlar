package no.uio.ifi.in2000.team46.presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import no.uio.ifi.in2000.team46.presentation.ui.viewmodel.fishlog.FishingLogViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishingLogDetailScreen(
    entryId: Int,
    viewModel: FishingLogViewModel,
    onBack: () -> Unit
) {
    // Hent opp listen med fiskeloggoppføringer fra viewModel og finn den med matchende id
    val entriesState by viewModel.entries.collectAsState(initial = emptyList())
    val entry = entriesState.find { it.id == entryId }

    if (entry != null) {
        // Oppføringen finnes – vis detaljene
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Fangst detaljer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Tilbake"
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Viser dato og tid – siden vi lagrer disse som Strings kan vi vise dem direkte.
            Text(
                text = "Dato: ${entry.date}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = "Tid: ${entry.time}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Viser sted, fisketype og vekt
            Text(
                text = "Sted: ${entry.location}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = "Fisketype: ${entry.fishType}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = "Vekt: ${entry.weight} kg",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Viser notater hvis de finnes
            entry.notes?.let { notes ->
                Text(
                    text = "Notater: $notes",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            // Viser bildet dersom imageUri er satt
            entry.imageUri?.let { uriString ->
                Spacer(modifier = Modifier.height(16.dp))
                AsyncImage(
                    model = uriString,
                    contentDescription = "Fangstbilde",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }
    } else {
        // Oppføringen ble ikke funnet – vis en passende melding (eller en loadingindikator)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Fant ikke fangst med ID: $entryId")
        }
    }
}
