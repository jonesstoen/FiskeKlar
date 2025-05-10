package no.uio.ifi.in2000.team46.presentation.favorites.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

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
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // ----------- State og data -----------
    val coroutineScope = rememberCoroutineScope()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val clearFields = navController.currentBackStackEntry?.arguments?.getString("clearFields") == "true"
    var name by remember {
        mutableStateOf(
            if (clearFields) ""
            else savedStateHandle?.get<String>("savedName")
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
    var showNameInfo by remember { mutableStateOf(false) }
    var showPositionInfo by remember { mutableStateOf(false) }
    var showNotesInfo by remember { mutableStateOf(false) }

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tilbake")
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
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                }
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
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(25.dp))
            ) {
                listOf("POINT" to "Punkt", "AREA" to "Område").forEach { (value, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (locationType == value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(25.dp)
                            )
                            .clickable { locationType = value },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = if (locationType == value) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ----------- Navn og forslag/autocomplete -----------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Stedsnavn",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showNameInfo = !showNameInfo }) {
                    Icon(Icons.Default.Info, contentDescription = "Info om stedsnavn")
                }
            }
            if (showNameInfo) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Gi området et navn så du enkelt finner det igjen.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        IconButton(onClick = { showNameInfo = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Lukk info")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Skriv inn stedsnavn...") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ----------- Kartvelger -----------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Velg posisjon",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showPositionInfo = !showPositionInfo }) {
                    Icon(Icons.Default.Info, contentDescription = "Info om posisjon")
                }
            }
            if (showPositionInfo) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (locationType == "POINT") 
                                "Trykk på kartet for å velge det eksakte punktet du fisker fra."
                            else 
                                "Marker 3–5 punkter på kartet for å tegne inn området du ønsker å lagre.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        IconButton(onClick = { showPositionInfo = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Lukk info")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                    .clickable {
                        saveCurrentState()
                        navController.navigate("mapPicker/$locationType")
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Trykk for å velge posisjon", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (isSystemInDarkTheme()) {
                    MaterialTheme.colorScheme.surfaceBright
                } else {
                    MaterialTheme.colorScheme.surfaceDim
                },
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
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ----------- Notater -----------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Notater (valgfritt)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showNotesInfo = !showNotesInfo }) {
                    Icon(Icons.Default.Info, contentDescription = "Info om notater")
                }
            }
            if (showNotesInfo) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Legg til informasjon som dybde, strømforhold, fisketyper eller andre observasjoner dersom du ønsker det",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        IconButton(onClick = { showNotesInfo = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Lukk info")
                        }
                    }
                }
            }
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
                    onClick = {
                        // Nullstill all state i savedStateHandle
                        savedStateHandle?.remove<String>("savedName")
                        savedStateHandle?.remove<String>("savedLocationType")
                        savedStateHandle?.remove<String>("savedNotes")
                        savedStateHandle?.remove<List<String>>("savedFishTypes")
                        onCancel()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                            // Nullstill all state i savedStateHandle
                            savedStateHandle?.remove<String>("savedName")
                            savedStateHandle?.remove<String>("savedLocationType")
                            savedStateHandle?.remove<String>("savedNotes")
                            savedStateHandle?.remove<List<String>>("savedFishTypes")
                            onSave()
                        }
                    },
                    enabled = name.isNotBlank() && (pickedPoint != null || pickedArea != null),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Lagre")
                }
            }
        }
    }
}
