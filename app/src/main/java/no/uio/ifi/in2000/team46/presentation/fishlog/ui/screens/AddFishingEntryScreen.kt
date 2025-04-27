package no.uio.ifi.in2000.team46.presentation.fishlog.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import no.uio.ifi.in2000.team46.presentation.fishlog.ui.viewmodel.FishingLogViewModel
import java.io.File
import androidx.core.content.FileProvider
import no.uio.ifi.in2000.team46.presentation.fishlog.ui.components.FishTypeDropdown
import java.time.LocalDate
import java.time.LocalTime
import no.uio.ifi.in2000.team46.data.repository.LocationRepository
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFishingEntryScreen(
    viewModel: FishingLogViewModel,
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
        count: Int // 🐟 Ny parameter
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
    val fishTypes by viewModel.fishTypes.collectAsState()
    val locationRepository = remember { LocationRepository(context) }
    var userLocation by remember { mutableStateOf<android.location.Location?>(null) }

    // Hent brukerposisjon
    LaunchedEffect(Unit) {
        locationRepository.getCurrentLocation()?.let { location ->
            userLocation = location
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) imageUri = null
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Tilbake")
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
                                fishType,
                                weightValue,
                                notes.ifEmpty { null },
                                imageUri?.toString(),
                                userLocation?.latitude ?: 0.0,
                                userLocation?.longitude ?: 0.0,
                                fishCount
                            )
                        },
                        enabled = location.isNotEmpty() && fishType.isNotEmpty() && weightText.isNotEmpty(),
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Sted") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Område (valgfritt)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                FishTypeDropdown(
                    fishTypes = fishTypes.map { it.name },
                    selected = fishType,
                    onSelect = { fishType = it }
                )

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
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
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
                        enabled = fishCount > 1
                    ) {
                        Text("-")
                    }

                    Text(
                        text = fishCount.toString(),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Button(onClick = { fishCount++ }) {
                        Text("+")
                    }
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

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    )
}
