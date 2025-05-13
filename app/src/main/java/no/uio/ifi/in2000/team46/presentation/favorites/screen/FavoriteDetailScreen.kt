package no.uio.ifi.in2000.team46.presentation.favorites.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import no.uio.ifi.in2000.team46.data.local.database.entities.FavoriteLocation
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog
import no.uio.ifi.in2000.team46.presentation.favorites.viewmodel.FavoritesViewModel
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.geometry.Size
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteDetailScreen(
    favoriteId: Int,
    viewModel: FavoritesViewModel,
    onBack: () -> Unit,
    onAddFishingLog: (String) -> Unit,
    onNavigateToMap: (Double?, Double?, String?) -> Unit,
    onNavigateToWeather: (Double, Double, String) -> Unit
) {
    // ----------- State -----------
    var favorite by remember { mutableStateOf<FavoriteLocation?>(null) }
    var fishingLogs by remember { mutableStateOf<List<FishingLog>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isEditingNotes by remember { mutableStateOf(false) }
    var notesEdit by remember { mutableStateOf("") }

    // ----------- Hent favoritt og fiskelogger -----------
    LaunchedEffect(favoriteId) {
        viewModel.getFavoriteById(favoriteId).collectLatest { favoriteLocation ->
            favorite = favoriteLocation
            if (favoriteLocation != null) {
                viewModel.getFishingLogsForLocation(favoriteLocation.name).collectLatest { logs ->
                    fishingLogs = logs.filter { it.count > 0 }
                }
            }
        }
    }

    // ----------- Slettedialog -----------
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Bekreft sletting") },
            text = { Text("Er du sikker på at du vil slette dette favorittstedet?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        favorite?.let { favoriteLocation ->
                            viewModel.deleteFavorite(favoriteLocation)
                        }
                        showDeleteDialog = false
                        onBack()
                    }
                ) {
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

    // ----------- UI: Scaffold, TopAppBar, hovedinnhold -----------
    Scaffold(
        topBar = {
            // Header med gradient og ikon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF3B5F8A), Color(0xFF4CAF50))
                        )
                    ),
                contentAlignment = Alignment.BottomStart
            ) {
                // Tilbake-knapp øverst til venstre
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Tilbake",
                        tint = Color.White
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 56.dp, bottom = 16.dp) // flytt navn litt til høyre for pil
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = favorite?.name ?: "",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Slett-knapp øverst til høyre
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Slett favoritt",
                        tint = Color.White
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        favorite?.let { favoriteLocation ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(0.dp, 0.dp, 0.dp, 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Type-indikator som badge
                Row(
                    modifier = Modifier.padding(start = 24.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (favoriteLocation.locationType == "POINT") Color(0xFFE53935) else Color(0xFF4CAF50),
                        shape = RoundedCornerShape(50),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (favoriteLocation.locationType == "POINT") Icons.Default.Place else Icons.Default.Map,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (favoriteLocation.locationType == "POINT") "Favorittsted" else "Favorittområde",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))

                // Notater med tydelig redigeringsknapp
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Notater",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = {
                                    isEditingNotes = !isEditingNotes
                                    notesEdit = favoriteLocation.notes ?: ""
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Rediger notater",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        if (isEditingNotes) {
                            OutlinedTextField(
                                value = notesEdit,
                                onValueChange = { notesEdit = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row {
                                TextButton(onClick = { isEditingNotes = false }) { Text("Avbryt") }
                                TextButton(onClick = {
                                    viewModel.updateFavoriteNotes(favoriteLocation, notesEdit)
                                    isEditingNotes = false
                                }) { Text("Lagre") }
                            }
                        } else {
                            Text(
                                text = favoriteLocation.notes ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))

                // Fangstinformasjon med ikoner og beste fangst på én linje
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Fangstinformasjon",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Totalt antall fisk:")
                            }
                            Text(
                                text = "${fishingLogs.sumOf { it.count }}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Beste fangst:")
                            }
                            val best = fishingLogs.maxByOrNull { it.weight }
                            Text(
                                text = best?.let { "${it.weight} kg ${it.fishType}" } ?: "Ingen",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sist fanget:")
                            }
                            val last = fishingLogs.maxByOrNull { "${it.date} ${it.time}" }
                            Text(
                                text = last?.date ?: "Ingen",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))

                // Fangststatistikk med ramme
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Fangststatistikk (antall)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        val grouped = fishingLogs.groupBy { it.fishType }
                        val data = grouped.mapValues { it.value.sumOf { log -> log.count } }
                        if (data.isEmpty()) {
                            Text("Ingen fisk registrert på dette stedet.")
                        } else {
                            HorizontalBarChart(
                                data = data,
                                modifier = Modifier.fillMaxWidth(),
                                barColor = Color(0xFF3B5F8A)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Runde knapper med ikoner og skygge
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { onAddFishingLog(favoriteLocation.name) },
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Legg til fangst")
                        }
                        Button(
                            onClick = {
                                if (favoriteLocation.locationType == "POINT") {
                                    onNavigateToMap(favoriteLocation.latitude, favoriteLocation.longitude, null)
                                } else {
                                    val areaPointsJson = favoriteLocation.areaPoints ?: "[]"
                                    onNavigateToMap(null, null, areaPointsJson)
                                }
                            },
                            shape = RoundedCornerShape(50),
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Vis på kart")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            // For both point and area locations, we can use the latitude and longitude
                            // Area locations also store a center point in the same fields
                            if (favoriteLocation.latitude != null && favoriteLocation.longitude != null) {
                                onNavigateToWeather(
                                    favoriteLocation.latitude, 
                                    favoriteLocation.longitude, 
                                    favoriteLocation.name
                                )
                            }
                        },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Icon(Icons.Default.WbSunny, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Se været for dette stedet")
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalBarChart(
    data: Map<String, Int>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    val max = data.values.maxOrNull() ?: 1
    val sortedData = data.toList().sortedByDescending { it.second }
    Column(modifier = modifier) {
        sortedData.forEach { (label, value) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    label,
                    modifier = Modifier.width(80.dp),
                    maxLines = 1
                )
                Canvas(
                    modifier = Modifier
                        .height(24.dp)
                        .weight(1f)
                ) {
                    val barWidth = (size.width * (value / max.toFloat())).coerceAtLeast(1f)
                    drawRect(
                        color = barColor,
                        size = Size(barWidth, size.height)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("$value")
            }
        }
    }
}