package com.example.myapplication.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigo,
    onPrimary = Color.White,
    primaryContainer = DarkBlue,
    onPrimaryContainer = LightBlue,
    secondary = SecondaryTeal,
    onSecondary = Color.White,
    tertiary = AccentAmber,
    onTertiary = Grey900,
    background = SurfaceDark,
    onBackground = Grey100,
    surface = SurfaceDark,
    onSurface = Grey100,
    surfaceVariant = Grey900,
    onSurfaceVariant = Grey400,
    outline = Grey700,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    onPrimary = Color.White,
    primaryContainer = LightBlue,
    onPrimaryContainer = DarkBlue,
    secondary = SecondaryTeal,
    onSecondary = Color.White,
    tertiary = AccentAmber,
    onTertiary = Grey900,
    background = SurfaceLight,
    onBackground = Grey900,
    surface = SurfaceLight,
    onSurface = Grey900,
    surfaceVariant = Color.White,
    onSurfaceVariant = Grey700,
    outline = Grey400,
    error = ErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
