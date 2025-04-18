package no.uio.ifi.in2000.team46.presentation.profile.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import no.uio.ifi.in2000.team46.R
import no.uio.ifi.in2000.team46.data.local.database.entities.User
import no.uio.ifi.in2000.team46.presentation.ui.screens.Background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    user: User,
    onClearUser: () -> Unit,
    onEditUser: () -> Unit = {} // placeholder for edit user action
) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Min profil") },
                actions = {
                    IconButton(onClick = { onEditUser() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Rediger profil")
                    }
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Slett profil", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )

            )
        },
        containerColor = Background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                if (user.profileImageUri != null) {
                    AsyncImage(
                        model = user.profileImageUri,
                        contentDescription = "Profilbilde",
                        modifier = Modifier
                            .size(100.dp)
                            .aspectRatio(1f)
                            .clip(CircleShape),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profilbilde",
                        modifier = Modifier.size(100.dp),
                        tint = Color(0xFF2A475E)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(user.name, style = MaterialTheme.typography.titleLarge)
                Text(user.username, style = MaterialTheme.typography.bodyMedium)
                Text("Medlem siden ${user.memberSince}", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Fiskestatistikk", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatItem("28", "Antall fisketurer", IconType.Vector(Icons.Default.Schedule))
                            StatItem("Nesodden", "Favorittområde", IconType.Vector(Icons.Default.Place))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            StatItem(
                                "Torsk",
                                "Flest fanget fisk",
                                IconType.Resource(painterResource(id = R.drawable.fish))
                            )
                            StatItem("134", "Timer på sjøen", IconType.Vector(Icons.Default.Schedule))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Mine enheter", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Havørna", style = MaterialTheme.typography.bodyLarge)
                        Text("LJ2023  |  AIS-ID: 123456789", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                SettingsItem("Endre passord", Icons.Default.Lock)
                SettingsItem("Varslingsinnstillinger", Icons.Default.Notifications)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Slett profil") },
            text = { Text("Er du sikker på at du vil slette profilen din? Dette kan ikke angres.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onClearUser()
                    }
                ) {
                    Text("Ja, slett", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Avbryt")
                }
            }
        )
    }
}

// to be able to use both vector and resource icons in stat item
sealed class IconType {
    data class Vector(val imageVector: ImageVector) : IconType()
    data class Resource(val painter: Painter) : IconType()
}
@Composable
fun StatItem(value: String, label: String, icon: IconType) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when (icon) {
            is IconType.Vector -> Icon(
                imageVector = icon.imageVector,
                contentDescription = label,
                tint = Color(0xFF2A475E)
            )
            is IconType.Resource -> Icon(
                painter = icon.painter,
                contentDescription = label,
                tint = Color(0xFF2A475E),
                modifier = Modifier.size(24.dp)
            )
        }
        Text(value, style = MaterialTheme.typography.bodyLarge)
        Text(label, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
    }
}

@Composable
fun SettingsItem(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ } //TODO: Handle click action
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title)
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge)
    }
}
