package no.uio.ifi.in2000.team46.presentation.profile.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileUiContract


/**
 * Skjerm for visning og redigering av brukerprofil.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileUiContract,
    onNavigateToHome: () -> Unit,
    onNavigateToAlerts: () -> Unit
) {
    // Hent bruker fra ViewModel
    val user by viewModel.user.collectAsState()
    // Lokal redigeringsflagg
    var isEditing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
                actions = {
                    if (!isEditing && user != null) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Rediger profil")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Hjem") },
                    selected = false,
                    onClick = onNavigateToHome
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Varsler") },
                    selected = false,
                    onClick = onNavigateToAlerts
                )
            }
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(24.dp)
        ) {
            when {
                // Vis skjema ved oppstart eller ved redigering
                user == null || isEditing -> {
                    UserInputForm(
                        nameDefault = user?.name.orEmpty(),
                        usernameDefault = user?.username.orEmpty(),
                        onSave = { name, username, imageUri ->
                            viewModel.saveUser(name, username, imageUri)
                            isEditing = false
                        }
                    )
                }
                else -> {
                    user?.let { u ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(u.profileImageUri)
                                    .crossfade(true)
                                    .build()
                            )
                            Image(
                                painter = painter,
                                contentDescription = "Profilbilde",
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Text(text = u.name, style = MaterialTheme.typography.headlineSmall)
                            Text(text = "@${u.username}", style = MaterialTheme.typography.bodyMedium)
                            Button(
                                onClick = { viewModel.clearUser() }
                            ) {
                                Text("Slett bruker")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserInputForm(
    nameDefault: String,
    usernameDefault: String,
    onSave: (String, String, String?) -> Unit
) {
    var name by remember { mutableStateOf(nameDefault) }
    var username by remember { mutableStateOf(usernameDefault) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Navn") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Brukernavn") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { onSave(name.trim(), username.trim(), null) },
            enabled = name.isNotBlank() && username.isNotBlank(),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Lagre")
        }
    }
}




