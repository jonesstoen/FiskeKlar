package no.uio.ifi.in2000.team46.presentation.fishlog.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import no.uio.ifi.in2000.team46.R
import no.uio.ifi.in2000.team46.presentation.fishlog.ui.viewmodel.FishingLogViewModel
import androidx.compose.foundation.layout.size
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FishingLogDetailScreen(
    entryId: Int,
    viewModel: FishingLogViewModel,
    onBack: () -> Unit
) {
    // get the entry from the viewModel
    val entries by viewModel.entries.collectAsState(initial = emptyList())
    val entry: FishingLog? = entries.find { it.id == entryId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fangst detaljer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Tilbake")
                    }
                }
            )
        }
    ) { padding ->
        if (entry == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Finner ikke fangst med ID = $entryId")
            }
            return@Scaffold
        }

        // formatting of date and time

        val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
        val time = LocalTime.parse(entry.time).format(timeFmt)
        val dateFmt = DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale("no"))
        val date = LocalDate.parse(entry.date).format(dateFmt)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date and time
            Text(date, style = MaterialTheme.typography.titleLarge)


            //card for all details
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // picture if added
                    entry.imageUri?.let { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "Fangstbilde",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // place
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(entry.location.ifBlank { "Ukjent sted" }, style = MaterialTheme.typography.bodyLarge)
                    }

                    //fishtype and weight
                    val ingenFangst = entry.fishType.isBlank() && entry.weight == 0.0
                    if (ingenFangst) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.fish),
                                contentDescription = "Fish Icon",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Ingen fangst", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.fish),
                                contentDescription = "Fish Icon",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(entry.fishType.ifBlank { "Ukjent fisketype" }, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.width(16.dp))
                            Icon(Icons.Default.Scale, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("${"%.1f".format(entry.weight)} kg", style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    // notes section
                    entry.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                        Text("Notater", style = MaterialTheme.typography.titleSmall)
                        Text(notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
