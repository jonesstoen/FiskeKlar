package no.uio.ifi.in2000.team46.presentation.profile.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
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
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileUiContract
import androidx.compose.ui.platform.LocalContext
import no.uio.ifi.in2000.team46.data.local.database.entities.User
import java.io.File

@Composable
fun ProfileScreen(
    viewModel: ProfileUiContract,
    onNavigateToHome: () -> Unit,
    onNavigateToAlerts: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    var isEditingProfile by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {},
    ) { paddingValues ->
        ProfileContentWrapper(
            paddingValues = paddingValues,
            user = user,
            isEditingProfile = isEditingProfile,
            onEditToggle = { isEditingProfile = !isEditingProfile },
            onUserSaved = { name, username, imageUri ->
                viewModel.saveUser(name, username, imageUri)
                isEditingProfile = false
            },
            onClearUser = { viewModel.clearUser() }
        )
    }
}

@Composable
fun ProfileContentWrapper(
    paddingValues: PaddingValues,
    user: User?, // Assuming there's a User data class
    isEditingProfile: Boolean,
    onEditToggle: () -> Unit,
    onUserSaved: (String, String, String?) -> Unit,
    onClearUser: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (user == null || isEditingProfile) {
            UserProfileInputForm(
                nameDefault = user?.name.orEmpty(),
                usernameDefault = user?.username.orEmpty(),
                onSaveUser = onUserSaved
            )
        } else {
            ProfileContent(
                user = user,
                onClearUser = onClearUser,
                onEditUser = onEditToggle
            )
        }
    }
}

@Composable
fun UserProfileInputForm(
    nameDefault: String,
    usernameDefault: String,
    imageUriDefault: String? = null,
    onSaveUser: (String, String, String?) -> Unit
) {
    var userName by remember { mutableStateOf(nameDefault) }
    var userAlias by remember { mutableStateOf(usernameDefault) }
    var profileImageUri by remember { mutableStateOf(imageUriDefault) }
    val context = LocalContext.current

    val imageCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) profileImageUri = null
    }

    UserProfileInputFormUI(
        userNameState = userName,
        userAliasState = userAlias,
        profileImageUri = profileImageUri,
        onSaveUser = onSaveUser,
        onImageUriChange = { uriString -> profileImageUri = uriString },
        context = context,
        imageCaptureLauncher = imageCaptureLauncher,
        onUserNameChange = { userName = it },
        onUserAliasChange = { userAlias = it }
    )
}

@Composable
fun UserProfileInputFormUI(
    userNameState: String,
    userAliasState: String,
    profileImageUri: String?,
    onSaveUser: (String, String, String?) -> Unit,
    onImageUriChange: (String?) -> Unit,
    context: Context,
    imageCaptureLauncher: ActivityResultLauncher<Uri>,
    onUserNameChange: (String) -> Unit,
    onUserAliasChange: (String) -> Unit
) {
    Text("Rediger profil", style = MaterialTheme.typography.titleLarge)

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = userNameState,
        onValueChange = { onUserNameChange(it) },
        label = { Text("Navn") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = userAliasState,
        onValueChange = { onUserAliasChange(it) },
        label = { Text("Kallenavn") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(24.dp))

    CaptureProfileImageButton(
        context = context,
        onImageUriChange = onImageUriChange,
        imageCaptureLauncher = imageCaptureLauncher
    )

    profileImageUri?.let { uri ->
        Spacer(modifier = Modifier.height(12.dp))
        AsyncImage(
            model = uri,
            contentDescription = "Profilbilde",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(onClick = { onSaveUser(userNameState, userAliasState, profileImageUri) }) {
        Text("Lagre endringer")
    }
}

@Composable
fun CaptureProfileImageButton(
    context: Context,
    onImageUriChange: (String?) -> Unit,
    imageCaptureLauncher: ActivityResultLauncher<Uri>
) {
    Button(
        onClick = {
            val tempFile = File.createTempFile("profile_", ".jpg", context.cacheDir).apply {
                createNewFile()
                deleteOnExit()
            }
            val tempUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                tempFile
            )
            onImageUriChange(tempUri.toString())
            imageCaptureLauncher.launch(tempUri)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Ta profilbilde")
    }
}
