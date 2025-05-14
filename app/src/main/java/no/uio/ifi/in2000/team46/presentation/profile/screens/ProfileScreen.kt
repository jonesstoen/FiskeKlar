package no.uio.ifi.in2000.team46.presentation.profile.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import no.uio.ifi.in2000.team46.presentation.profile.component.ProfileContent
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileViewModel
import java.io.File
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.net.Uri

// this file defines the ProfileScreen composable, which allows users to either view their profile
// or edit it using a form that supports setting name, username, and profile image from camera or gallery

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToTheme: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val mostCaughtFish by viewModel.mostCaughtFish.collectAsState()
    val favoriteLocation by viewModel.favoriteLocation.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // show input form if user is not registered or in edit mode
            if (user == null || isEditing) {
                UserInputForm(
                    modifier         = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    nameDefault      = user?.name ?: "",
                    usernameDefault  = user?.username ?: "",
                    imageUriDefault  = user?.profileImageUri
                ) { name, username, imageUri ->
                    viewModel.saveUser(name, username, imageUri)
                    isEditing = false
                }
            } else {
                // show profile content with statistics and navigation
                ProfileContent(
                    modifier    = Modifier
                        .fillMaxSize(),
                    user        = user!!,
                    mostCaughtFish = mostCaughtFish?.fishType,
                    mostCaughtFishCount = mostCaughtFish?.totalCount,
                    favoriteLocation = favoriteLocation?.location,
                    onClearUser = {
                        viewModel.clearUser()
                        isEditing = true
                    },
                    onEditUser  = { isEditing = true },
                    onNavigateToTheme = onNavigateToTheme
                )
            }
        }
    }
}

@Composable
fun UserInputForm(
    modifier: Modifier = Modifier,
    nameDefault: String = "",
    usernameDefault: String = "",
    imageUriDefault: String? = null,
    onSave: (name: String, username: String, imageUri: String?) -> Unit
) {
    var name by remember { mutableStateOf(nameDefault) }
    var username by remember { mutableStateOf(usernameDefault) }
    val context = LocalContext.current

    var imageUri by remember {
        mutableStateOf(imageUriDefault?.let { Uri.parse(it) })
    }
    var tmpUri by remember { mutableStateOf<Uri?>(null) }

    // launcher for taking a picture with the camera
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = tmpUri
        } else {
            tmpUri = null
            imageUri = null
        }
    }

    // launcher for picking an image from gallery
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val profileImageFile = File(context.filesDir, "profile_image_${System.currentTimeMillis()}.jpg")

                inputStream?.use { input ->
                    profileImageFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                imageUri = Uri.fromFile(profileImageFile)
            } catch (e: Exception) {
                imageUri = uri
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Rediger profil", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        // input for name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Navn") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // input for username
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Kallenavn") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // buttons for selecting profile picture
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    // create temporary file and launch camera
                    val tmpFile = File.createTempFile("profile_", ".jpg", context.cacheDir).apply {
                        createNewFile()
                        deleteOnExit()
                    }
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        tmpFile
                    )
                    tmpUri = uri
                    takePictureLauncher.launch(uri)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Ta bilde")
            }

            Button(
                onClick = {
                    pickImageLauncher.launch("image/*")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Velg fra galleri")
            }
        }

        // preview selected image
        if (imageUri != null) {
            Spacer(Modifier.height(12.dp))
            Text("Valgt bilde:", style = MaterialTheme.typography.titleMedium)
            AsyncImage(
                model = imageUri,
                contentDescription = "Profilbilde",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // remove selected image and delete temp file if it was created
            OutlinedButton(
                onClick = {
                    imageUri?.let { uri ->
                        if (uri.toString().contains("profile_")) {
                            val file = File(uri.path ?: "")
                            if (file.exists()) file.delete()
                        }
                    }
                    imageUri = null
                    tmpUri = null
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Fjern bilde")
            }
        } else {
            Text(
                "Ingen bilde valgt",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(24.dp))

        // save profile data
        Button(
            onClick = {
                onSave(name.trim(), username.trim(), imageUri?.toString())
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lagre endringer")
        }
    }
}
