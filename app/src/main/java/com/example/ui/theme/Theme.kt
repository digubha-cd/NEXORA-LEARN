package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NexoraLightColorScheme = lightColorScheme(
    primary = NexoraCyan,
    onPrimary = Color.White,
    primaryContainer = NexoraSurfaceVariant,
    onPrimaryContainer = NexoraCyan,
    secondary = NexoraMagenta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFDF2F8),
    onSecondaryContainer = NexoraPink,
    tertiary = NexoraPurple,
    onTertiary = Color.White,
    background = NexoraBackground,
    onBackground = NexoraTextPrimary,
    surface = NexoraSurface,
    onSurface = NexoraTextPrimary,
    surfaceVariant = NexoraSurfaceVariant,
    onSurfaceVariant = NexoraTextSecondary,
    outline = NexoraBorder,
    error = NexoraError,
    onError = Color.White
)

@Composable
fun NexoraTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = NexoraBackground.toArgb()
                window.navigationBarColor = NexoraBackground.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = true
                controller.isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = NexoraLightColorScheme,
        typography = Typography,
        content = content
    )
}

// Retain alias for any existing test references
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    NexoraTheme(content = content)
}

