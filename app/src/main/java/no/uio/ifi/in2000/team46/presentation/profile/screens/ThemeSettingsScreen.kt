package no.uio.ifi.in2000.team46.presentation.profile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileViewModel

// this file defines the ThemeSettingsScreen composable which lets the user select the app theme
// user can choose between system default, light mode, or dark mode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val currentTheme by viewModel.theme.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Temainnstillinger") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tilbake")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .selectableGroup()
        ) {
            Text(
                "Velg tema",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            ThemeOption(
                text = "Følg systeminnstillinger",
                icon = Icons.Default.PhoneAndroid,
                selected = currentTheme == "system",
                onClick = { viewModel.setTheme("system") }
            )

            ThemeOption(
                text = "Alltid lys",
                icon = Icons.Default.LightMode,
                selected = currentTheme == "light",
                onClick = { viewModel.setTheme("light") }
            )

            ThemeOption(
                text = "Alltid mørk",
                icon = Icons.Default.DarkMode,
                selected = currentTheme == "dark",
                onClick = { viewModel.setTheme("dark") }
            )
        }
    }
}

@Composable
private fun ThemeOption(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text)
    }
} 