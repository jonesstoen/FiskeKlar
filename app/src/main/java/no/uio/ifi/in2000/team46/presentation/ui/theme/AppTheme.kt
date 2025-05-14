package no.uio.ifi.in2000.team46.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileViewModel

// this file defines a custom app theme wrapper that responds to user-selected theme settings
// the theme is controlled by profileviewmodel and can be "light", "dark", or follow system preference

@Composable
fun AppTheme(
    viewModel: ProfileViewModel,
    content: @Composable () -> Unit
) {
    val theme by viewModel.theme.collectAsState() // observe theme setting from viewmodel
    val isDarkTheme = when (theme) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme() // fallback to system setting if value is unknown
    }

    MaterialTheme(
        colorScheme = if (isDarkTheme) darkScheme else lightScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
} 