package no.uio.ifi.in2000.team46.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*

import androidx.compose.runtime.SideEffect

import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightScheme = lightColorScheme(
    primary            = Blue700,
    onPrimary          = Ivory100,
    secondary          = Coral500,
    onSecondary        = Ivory100,
    secondaryContainer = Coral100,
    tertiary           = Cyan300,
    onTertiary         = Blue700,
    tertiaryContainer  = Cyan100,
    background         = Ivory50,
    onBackground       = Blue900,
    surface            = Ivory100,
    onSurface          = Blue900,
    error              = Error,
    onError            = Ivory100
)

private val DarkScheme = darkColorScheme(
    primary            = Cyan300,
    onPrimary          = Blue900,
    secondary          = Coral500,
    onSecondary        = Ivory100,
    secondaryContainer = Coral100,
    tertiary           = Cyan300,
    onTertiary         = Blue900,
    tertiaryContainer  = Cyan100,
    background         = Blue900,
    onBackground       = Ivory100,
    surface            = Blue700,
    onSurface          = Ivory100,
    error              = Color(0xFFCF6679),
    onError            = Blue900
)


@Composable
fun TEAM46Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkScheme
        else      -> LightScheme
    }

    // Synk systembars med tema
    val view = LocalView.current
    SideEffect {
        (view.context as Activity).window.statusBarColor = colors.primary.toArgb()
        WindowCompat.getInsetsController(
            (view.context as Activity).window, view
        ).isAppearanceLightStatusBars = !darkTheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content
    )
}