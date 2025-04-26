package no.uio.ifi.in2000.team46.presentation.profile.screens
/*
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
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileUiContract
import no.uio.ifi.in2000.team46.presentation.ui.screens.Background


@Composable
fun ProfileScreen(
    viewModel: ProfileUiContract,
    onNavigateToHome: () -> Unit,
    onNavigateToAlerts: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {

        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (user == null || isEditing) {
                UserInputForm(
                    nameDefault = user?.name ?: "",
                    usernameDefault = user?.username ?: "",
                    onSave = { name, username, imageUri ->
                        viewModel.saveUser(name, username, imageUri)
                        isEditing = false
                    }
                )
            }  else {
                ProfileContent(
                    user = user!!,
                    onClearUser = { viewModel.clearUser() },
                    onEditUser = { isEditing = true } // 👈 Dette trigger visning av skjema
                )
        }
        }
    }
}

@Composable
fun UserInputForm(
    nameDefault: String = "",
    usernameDefault: String = "",
    imageUriDefault: String? = null,
    onSave: (String, String, String?) -> Unit
) {
    var name by remember { mutableStateOf(nameDefault) }
    var username by remember { mutableStateOf(usernameDefault) }
    //for taking pictures
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf(imageUriDefault?.let { android.net.Uri.parse(it) }) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) imageUri = null
    }

    Text("Rediger profil", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Navn") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = username,
        onValueChange = { username = it },
        label = { Text("Kallenavn") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = {
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
        Spacer(modifier = Modifier.height(12.dp))
        AsyncImage(
            model = it,
            contentDescription = "Profilbilde",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(onClick = {
        onSave(name, username, imageUri?.toString())
    }) {
        Text("Lagre endringer")
    }
}*/


