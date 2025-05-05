package no.uio.ifi.in2000.team46.presentation.fishlog.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import no.uio.ifi.in2000.team46.presentation.fishlog.viewmodel.FishingLogViewModel
import java.io.File
import androidx.core.content.FileProvider
import no.uio.ifi.in2000.team46.presentation.fishlog.components.FishTypeDropdown
import java.time.LocalDate
import java.time.LocalTime
import no.uio.ifi.in2000.team46.data.repository.LocationRepository
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuDefaults
import no.uio.ifi.in2000.team46.data.local.database.entities.FavoriteLocation
import no.uio.ifi.in2000.team46.presentation.favorites.viewmodel.FavoritesViewModel
import androidx.navigation.NavController
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.draw.alpha

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFishingEntryScreen(
    viewModel: FishingLogViewModel,
    favoritesViewModel: FavoritesViewModel,
    navController: NavController,
    onCancel: () -> Unit,
    onSave: (
        date: LocalDate,
        time: LocalTime,
        location: String,
        fishType: String,
        weight: Double,
        notes: String?,
        imageUri: String?,
        latitude: Double,
        longitude: Double,
        count: Int
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var location by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var fishType by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var time by remember { mutableStateOf(LocalTime.now()) }
    var fishCount by remember { mutableStateOf(1) } // 🐟 Start alltid på 1
    var showLocationInfo by remember { mutableStateOf(false) }
    var showAreaInfo by remember { mutableStateOf(false) }
    var showFishTypeInfo by remember { mutableStateOf(false) }
    var showNotesInfo by remember { mutableStateOf(false) }
    val fishTypes by viewModel.fishTypes.collectAsState()
    val locationRepository = remember { LocationRepository(context) }
    var userLocation by remember { mutableStateOf<android.location.Location?>(null) }
    val favorites by favoritesViewModel.favorites.collectAsState()
    var showAreaDropdown by remember { mutableStateOf(false) }
    var selectedFavorite by remember { mutableStateOf<FavoriteLocation?>(null) }
    var gotCatch by remember { mutableStateOf(true) }

    // Hent brukerposisjon
    LaunchedEffect(Unit) {
        locationRepository.getCurrentLocation()?.let { location ->
            userLocation = location
        }
    }

    // Håndter når brukeren returnerer med et nytt område
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.get<String>("newFavorite")?.let { newFavorite ->
            area = newFavorite
            selectedFavorite = favorites.find { it.name == newFavorite }
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("newFavorite")
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) imageUri = null
    }

    // Gjenopprett tilstanden når vi kommer tilbake
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.apply {
            get<String>("savedLocation")?.let { location = it }
            get<String>("savedFishType")?.let { fishType = it }
            get<String>("savedWeight")?.let { weightText = it }
            get<String>("savedNotes")?.let { notes = it }
            get<Int>("savedFishCount")?.let { fishCount = it }
            get<String>("savedImageUri")?.let { imageUri = android.net.Uri.parse(it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ny fangst") },
                navigationIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onCancel()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tilbake")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            val weightValue = weightText.toDoubleOrNull() ?: 0.0
                            onSave(
                                date,
                                time,
                                location,
                                if (gotCatch) fishType else "",
                                if (gotCatch) weightValue else 0.0,
                                notes.ifEmpty { null },
                                imageUri?.toString(),
                                userLocation?.latitude ?: 0.0,
                                userLocation?.longitude ?: 0.0,
                                if (gotCatch) fishCount else 0
                            )
                        },
                        enabled = location.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Lagre", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(16.dp)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    },
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Sted", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { showLocationInfo = !showLocationInfo }) {
                            Icon(Icons.Default.Info, contentDescription = "Info om sted")
                        }
                    }
                    if (showLocationInfo) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            color = Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Skriv inn nøyaktig sted hvor fisken ble fanget (f.eks. navn på vann, elv, kystområde).",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { showLocationInfo = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Lukk info")
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Sted") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Område", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { showAreaInfo = !showAreaInfo }) {
                            Icon(Icons.Default.Info, contentDescription = "Info om område")
                        }
                    }
                    if (showAreaInfo) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            color = Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Velg et eksisterende område du har brukt før, eller legg til et nytt fiskeområde for gjenbruk senere.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { showAreaInfo = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Lukk info")
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = showAreaDropdown,
                            onExpandedChange = { showAreaDropdown = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = area,
                                onValueChange = { area = it },
                                readOnly = true,
                                label = { Text("Velg område") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAreaDropdown)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = showAreaDropdown,
                                onDismissRequest = { showAreaDropdown = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                favorites.forEach { favorite ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (favorite.locationType == "AREA") Color(0xFF4CAF50) else Color(0xFFD32F2F),
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                        Text(
                                                            text = if (favorite.locationType == "AREA") "O" else "P",
                                                            color = Color.White,
                                                            style = MaterialTheme.typography.titleMedium
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(favorite.name)
                                            }
                                        },
                                        onClick = {
                                            area = favorite.name
                                            selectedFavorite = favorite
                                            showAreaDropdown = false
                                        }
                                    )
                                }
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("Nytt område") },
                                    onClick = {
                                        showAreaDropdown = false
                                        // Lagre tilstanden før navigering
                                        navController.currentBackStackEntry?.savedStateHandle?.apply {
                                            set("savedLocation", location)
                                            set("savedFishType", fishType)
                                            set("savedWeight", weightText)
                                            set("savedNotes", notes)
                                            set("savedFishCount", fishCount)
                                            set("savedImageUri", imageUri?.toString())
                                        }
                                        navController.navigate("addFavorite?clearFields=true") {
                                            popUpTo("addFishingEntry") { 
                                                saveState = true 
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Fikk du fangst?", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Nei")
                        Switch(
                            checked = gotCatch,
                            onCheckedChange = { gotCatch = it },
                            thumbContent = null
                        )
                        Text("Ja")
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Fisketype", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { showFishTypeInfo = !showFishTypeInfo }) {
                            Icon(Icons.Default.Info, contentDescription = "Info om fisketype")
                        }
                    }
                    if (showFishTypeInfo) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            color = Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Angi hvilken fisk du fanget (f.eks. ørret, torsk, gjedde).",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { showFishTypeInfo = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Lukk info")
                                }
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .alpha(if (gotCatch) 1f else 0.5f)
                            .then(
                                if (!gotCatch) Modifier.pointerInput(Unit) { } else Modifier
                            )
                    ) {
                        FishTypeDropdown(
                            fishTypes = fishTypes.map { it.name },
                            selected = fishType,
                            onSelect = { if (gotCatch) fishType = it }
                        )
                    }
                }

                Text("Vekt", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' || it == ',' }) {
                            weightText = input.replace(',', '.') // Bytt komma til punktum
                        }
                    },
                    label = { Text("Vekt (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    enabled = gotCatch
                )

                // 🐟 Legg til antall fisk
                Text("Antall fisk", style = MaterialTheme.typography.titleMedium)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { if (fishCount > 1) fishCount-- },
                        enabled = gotCatch && fishCount > 1
                    ) {
                        Text("-")
                    }

                    Text(
                        text = fishCount.toString(),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Button(onClick = { fishCount++ }, enabled = gotCatch) {
                        Text("+")
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Notater", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { showNotesInfo = !showNotesInfo }) {
                            Icon(Icons.Default.Info, contentDescription = "Info om notater")
                        }
                    }
                    if (showNotesInfo) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            color = Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Legg til ekstra informasjon som værforhold, agn, teknikk, utstyr eller andre detaljer.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { showNotesInfo = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Lukk info")
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notater (vær, agn, etc.)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        })
                    )
                }

                Button(
                    onClick = {
                        val tmpFile = File.createTempFile("fangst_", ".jpg", context.cacheDir).apply {
                            createNewFile(); deleteOnExit()
                        }
                        val tmpUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            tmpFile
                        )
                        imageUri = tmpUri
                        takePictureLauncher.launch(tmpUri)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Legg til bilde")
                }

                imageUri?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = "Forhåndsvisning av fangstbilde",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    )
}
