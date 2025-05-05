package no.uio.ifi.in2000.team46.presentation.profile.screens
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import no.uio.ifi.in2000.team46.presentation.profile.component.ProfileContent
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileViewModel
import java.io.File
import androidx.compose.ui.platform.LocalContext
import android.net.Uri



@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToAlerts: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                ProfileContent(
                    modifier    = Modifier
                        .fillMaxSize(),

                    user        = user!!,
                    onClearUser = {
                        viewModel.clearUser()
                        isEditing = true
                    },
                    onEditUser  = { isEditing = true }
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
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) imageUri = null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Rediger profil", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Navn") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Kallenavn") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),

            )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                // Opprett midlertidig fil og ta bilde
                val tmpFile = File.createTempFile("profile_", ".jpg", context.cacheDir).apply {
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
            Text("Ta profilbilde")
        }

        imageUri?.let {
            Spacer(Modifier.height(12.dp))
            AsyncImage(
                model = it,
                contentDescription = "Profilbilde",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

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


