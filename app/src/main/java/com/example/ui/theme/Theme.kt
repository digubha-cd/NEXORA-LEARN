package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NexoraDarkColorScheme = darkColorScheme(
    primary = NexoraCyan,
    onPrimary = NexoraBackground,
    primaryContainer = NexoraSurfaceVariant,
    onPrimaryContainer = NexoraCyan,
    secondary = NexoraMagenta,
    onSecondary = NexoraBackground,
    secondaryContainer = NexoraSurfaceElevated,
    onSecondaryContainer = NexoraPink,
    tertiary = NexoraPurple,
    onTertiary = NexoraBackground,
    background = NexoraBackground,
    onBackground = NexoraTextPrimary,
    surface = NexoraSurface,
    onSurface = NexoraTextPrimary,
    surfaceVariant = NexoraSurfaceVariant,
    onSurfaceVariant = NexoraTextSecondary,
    outline = NexoraBorder,
    error = NexoraError,
    onError = NexoraTextPrimary
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
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = NexoraDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Retain alias for any existing test references
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    NexoraTheme(content = content)
}

