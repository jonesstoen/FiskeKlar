package no.uio.ifi.in2000.team46.presentation.favorites.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import no.uio.ifi.in2000.team46.presentation.favorites.viewmodel.FavoritesViewModel

/**
 * Skjerm for å legge til et nytt favorittsted.
 *
 * viewModel ViewModel for håndtering av favoritter.
 * navController Navigasjonskontroller for navigering mellom skjermer.
 * onCancel Funksjon som kalles når brukeren avbryter handlingen.
 * onSave Funksjon som kalles når brukeren lagrer favorittstedet.
 * defaultName Standardnavn for stedet, hvis tilgjengelig.
 * suggestions Liste over foreslåtte navn for autocompleting.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFavoriteScreen(
    viewModel: FavoritesViewModel,
    navController: NavController,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    defaultName: String = "",
    suggestions: List<String> = emptyList()
) {

    // ----------- State og data -----------
    val coroutineScope = rememberCoroutineScope()
    var name by remember {
        mutableStateOf(
            navController.currentBackStackEntry?.savedStateHandle?.get<String>("savedName")
                ?: navController.currentBackStackEntry?.arguments?.getString("name")
                ?: defaultName
        )
    }
    var locationType by remember { mutableStateOf("POINT") }
    var notes by remember { mutableStateOf("") }
    var selectedFishTypes by remember { mutableStateOf(setOf<String>()) }
    var latitude by remember { mutableStateOf(59.91) }
    var longitude by remember { mutableStateOf(10.75) }
    var areaPoints by remember { mutableStateOf(emptyList<Pair<Double, Double>>()) }
    var showDuplicateDialog by remember { mutableStateOf(false) }

    // ----------- Håndtering av kartvalg og lagring av tilstand -----------
    val pickedPoint = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Pair<Double, Double>>("pickedPoint")
        ?.observeAsState()
    val pickedArea = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<List<Pair<Double, Double>>> ("pickedArea")
        ?.observeAsState()
    val savedLocationType = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>("savedLocationType")
        ?.observeAsState()

    // Oppdater state når bruker velger punkt/område på kartet
    LaunchedEffect(pickedPoint?.value, pickedArea?.value, savedLocationType?.value) {
        savedLocationType?.value?.let { type -> locationType = type }
        pickedPoint?.value?.let { (lat, lon) ->
            latitude = lat
            longitude = lon
            areaPoints = emptyList()
            navController.currentBackStackEntry?.savedStateHandle?.remove<Pair<Double, Double>>("pickedPoint")
        }
        pickedArea?.value?.let { points ->
            areaPoints = points
            latitude = points.map { it.first }.average()
            longitude = points.map { it.second }.average()
            navController.currentBackStackEntry?.savedStateHandle?.remove<List<Pair<Double, Double>>>("pickedArea")
        }
        savedLocationType?.value?.let {
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("savedLocationType")
        }
    }

    // Sett automatisk notat hvis defaultName brukes
    LaunchedEffect(defaultName) {
        if (defaultName.isNotBlank() && notes.isBlank()) {
            notes = "Automatisk lagt til via forslag"
        }
    }

    // Lagrer midlertidig state hvis bruker går til kartvelger
    fun saveCurrentState() {
        navController.currentBackStackEntry?.savedStateHandle?.set("savedName", name)
        navController.currentBackStackEntry?.savedStateHandle?.set("savedLocationType", locationType)
        navController.currentBackStackEntry?.savedStateHandle?.set("savedNotes", notes)
        navController.currentBackStackEntry?.savedStateHandle?.set("savedFishTypes", selectedFishTypes.toList())
    }

    // ----------- Kalkuler areal hvis område -----------
    val areaKm2 = if (locationType == "AREA" && areaPoints.size >= 3) {
        viewModel.calculateAreaInSquareKm(areaPoints)
    } else 0.0

    // ----------- Håndtering av duplikatnavn -----------
    if (showDuplicateDialog) {
        AlertDialog(
            onDismissRequest = { showDuplicateDialog = false },
            title = {
                Text(
                    text = "Feil",
                    color = Color(0xFFE53935),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Et favorittsted med dette navnet finnes allerede.\n\nVelg et annet navn.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDuplicateDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    )
                ) {
                    Text("OK")
                }
            }
        )
    }

    // ----------- UI: Scaffold, TopAppBar, hovedinnhold -----------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Legg til favoritt") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Tilbake")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ----------- Typevalg (punkt/område) -----------
            Text(
                "Type favoritt",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(25.dp))
            ) {
                listOf("POINT" to "Punkt", "AREA" to "Område").forEach { (value, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (locationType == value) Color(0xFF3B5F8A) else Color.Transparent,
                                RoundedCornerShape(25.dp)
                            )
                            .clickable { locationType = value },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = if (locationType == value) Color.White else Color(0xFF3B5F8A),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ----------- Navn og forslag/autocomplete -----------
            Text(
                "Stedsnavn",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Skriv navn på stedet...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                val matchingSuggestions = suggestions.filter {
                    it.contains(name, ignoreCase = true) && !it.equals(name, ignoreCase = true)
                }
                if (matchingSuggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column {
                            matchingSuggestions.forEach { suggestion ->
                                Text(
                                    text = suggestion,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { name = suggestion }
                                        .padding(12.dp)
                                )
                                Divider()
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ----------- Kartvelger -----------
                Text("Velg posisjon", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFACD1E6), RoundedCornerShape(8.dp))
                    .clickable {
                        saveCurrentState()
                        navController.navigate("mapPicker/$locationType")
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Trykk for å velge posisjon", color = Color(0xFF3B5F8A), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (locationType == "AREA") {
                        if (areaPoints.size >= 3 && areaKm2 > 0.0) {
                            if (areaKm2 >= 1.0) {
                                String.format("Areal: %.2f km² (%d punkter)", areaKm2, areaPoints.size)
                            } else {
                                String.format("Areal: %.0f m² (%d punkter)", areaKm2 * 1_000_000, areaPoints.size)
                            }
                        } else if (areaPoints.isNotEmpty()) {
                            "${areaPoints.size} punkter (velg minst 3 for areal)"
                        } else {
                            "Areal: 0 m² (0 punkter)"
                        }
                    } else {
                        String.format("%.4f° N, %.4f° E", latitude, longitude)
                    },
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ----------- Notater -----------
            Text("Notater (valgfritt)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Skriv notater om stedet...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ----------- Knapper (Avbryt/Lagre) -----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF3B5F8A)
                    )
                ) {
                    Text("Avbryt")
                }
                Button(
                    onClick = {
                        val existingFavorite = viewModel.favorites.value.find { it.name.equals(name, ignoreCase = true) }
                        if (existingFavorite != null) {
                            showDuplicateDialog = true
                        } else {
                            viewModel.addFavorite(
                                name = name,
                                locationType = locationType,
                                latitude = latitude,
                                longitude = longitude,
                                areaPoints = if (locationType == "AREA") areaPoints else null,
                                notes = notes.takeIf { it.isNotBlank() },
                                targetFishTypes = selectedFishTypes.toList()
                            )
                            onSave()
                            viewModel.removeSavedSuggestion(name)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = name.isNotBlank()
                ) {
                    Text("Lagre")
                }
            }
        }
    }
}

// =====================
// UI-KOMPONENTER
// =====================

@Composable
fun FishTypeChip(
    name: String,
    selected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        color = if (selected) Color(0xFFDCE8F0) else Color.White,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, if (selected) Color(0xFF3B5F8A) else Color(0xFFA7BFD1))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = selected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B5F8A))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(name)
        }
    }
}
