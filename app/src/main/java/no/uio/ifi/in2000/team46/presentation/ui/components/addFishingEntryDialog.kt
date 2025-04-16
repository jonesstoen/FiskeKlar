package no.uio.ifi.in2000.team46.presentation.ui.components


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File
import androidx.core.content.FileProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFishingEntryDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        date: LocalDate,
        location: String,
        fishType: String,
        weight: Double,
        notes: String?,
        imageUri: String?
    ) -> Unit
) {
    val context = LocalContext.current

    var location by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var fishType by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) {
            imageUri = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ny fangst") },
        text = {
            Column {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Sted") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Område") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fishType,
                    onValueChange = { fishType = it },
                    label = { Text("Fisketype") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Vekt (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val tmpFile = File.createTempFile("fangst_", ".jpg", context.cacheDir).apply {
                            createNewFile()
                            deleteOnExit()
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
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = it,
                        contentDescription = "Forhåndsvisning av fangstbilde",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notater (vær, agn, etc.)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val weightDouble = weight.toDoubleOrNull() ?: 0.0
                val combinedNotes = "Område: $area\nNotater: $notes"
                onConfirm(LocalDate.now(), location, fishType, weightDouble, combinedNotes, imageUri?.toString())
            }) {
                Text("Lagre")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Avbryt")
            }
        }
    )
}
