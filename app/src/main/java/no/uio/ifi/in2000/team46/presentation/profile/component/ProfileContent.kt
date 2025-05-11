package no.uio.ifi.in2000.team46.presentation.profile.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import no.uio.ifi.in2000.team46.R
import no.uio.ifi.in2000.team46.data.local.database.entities.User

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    user: User,
    mostCaughtFish: String?,
    mostCaughtFishCount: Int?,
    favoriteLocation: String?,
    onClearUser: () -> Unit,
    onEditUser: () -> Unit,
    onNavigateToTheme: () -> Unit
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
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            value = favoriteLocation ?: "Ingen data", 
                            label = "Favorittområde",
                            icon = IconType.Vector(Icons.Default.Place)
                        )

                        StatItem(
                            value = if (mostCaughtFish != null && mostCaughtFishCount != null) 
                                    "$mostCaughtFish (${mostCaughtFishCount}stk)" 
                                   else "Ingen data", 
                            label = "Flest fanget fisk",
                            icon = IconType.Resource(painterResource(id = R.drawable.fish))
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        // 4) Innstillinger
        item {
            SettingsItem(
                title = "Temainnstillinger",
                icon = Icons.Default.Palette,
                onClick = onNavigateToTheme
            )
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Label at the top
        Text(
            text = label, 
            style = MaterialTheme.typography.bodyMedium, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Icon in the middle
        when (icon) {
            is IconType.Vector -> Icon(
                imageVector = icon.imageVector,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(28.dp).padding(vertical = 4.dp)
            )
            is IconType.Resource -> Icon(
                painter = icon.painter,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(28.dp).padding(vertical = 4.dp)
            )
        }
        
        // Value at the bottom
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SettingsItem(
    title: String, 
    icon: ImageVector,
    onClick: () -> Unit = { /* Default empty click handler */ }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title)
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge)
    }
}
