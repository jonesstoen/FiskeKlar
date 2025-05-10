package no.uio.ifi.in2000.team46.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import no.uio.ifi.in2000.team46.presentation.profile.viewmodel.ProfileViewModel

@Composable
fun AppTheme(
    viewModel: ProfileViewModel,
    content: @Composable () -> Unit
) {
    val theme by viewModel.theme.collectAsState()
    val isDarkTheme = when (theme) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (isDarkTheme) darkScheme else lightScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
} 