package no.uio.ifi.in2000.team46.presentation.profile.component

import androidx.compose.foundation.border
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

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    user: User,
    onClearUser: () -> Unit,
    onEditUser: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier            = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1) Header: tittel + knapper
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment   = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = "Min profil",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Row {
                    IconButton(onClick = onEditUser) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Rediger profil",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Slett profil",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // 2) Profilbilde + info
        item {
            if (user.profileImageUri != null) {
                AsyncImage(
                    model             = user.profileImageUri,
                    contentDescription= "Profilbilde",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)

                )
            } else {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = "Profilbilde",
                    modifier = Modifier
                        .size(100.dp)
                        //FIXME: FIX THIS AFTER THEME IS IMPLEMENTED IN THE APP
                        // .border( ),
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(user.name,     style = MaterialTheme.typography.titleLarge)
            Text(user.username, style = MaterialTheme.typography.bodyMedium)
            Text(
                "Medlem siden ${user.memberSince}",
                style = MaterialTheme.typography.bodySmall
            )
        }

        // 3) Fiskestatistikk
        item {
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor   = MaterialTheme.colorScheme.onTertiaryContainer
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Fiskestatistikk",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatItem("28", "Antall fisketurer",
                            IconType.Vector(Icons.Default.Schedule),

                        )
                        StatItem("Nesodden", "Favorittområde",
                            IconType.Vector(Icons.Default.Place),

                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatItem("Torsk", "Flest fanget fisk",
                            IconType.Resource(painterResource(id = R.drawable.fish)),

                        )
                        StatItem("134", "Timer på sjøen",
                            IconType.Vector(Icons.Default.Timer),

                        )
                    }
                }
            }
        }

        // 4) Mine enheter
        item {
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor   = MaterialTheme.colorScheme.onTertiaryContainer
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Mine enheter", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    Text("Havørna",    style = MaterialTheme.typography.bodyLarge)
                    Text("LJ2023  |  AIS-ID: 123456789",
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // 5) Innstillinger
        item {
            SettingsItem("Endre passord", Icons.Default.Lock)
            SettingsItem("Varslingsinnstillinger", Icons.Default.Notifications)
        }
    }

    // Slett dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title            = { Text("Slett profil") },
            text             = { Text("Er du sikker? Dette kan ikke angres.") },
            confirmButton    = {
                TextButton(onClick = {
                    showDialog = false
                    onClearUser()
                }) {
                    Text("Ja, slett",
                        color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton    = {
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
