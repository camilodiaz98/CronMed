package com.example.cronmed.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.cronmed.ui.navigation.CronMedTypography

private val LightColorScheme = lightColorScheme(
    primary = CronMedBlue,
    onPrimary = CronMedSurface,
    primaryContainer = CronMedCardBlue,
    onPrimaryContainer = CronMedBlueDark,
    secondary = CronMedBlueLight,
    onSecondary = CronMedSurface,
    secondaryContainer = CronMedBackground,
    onSecondaryContainer = CronMedTextPrimary,
    background = CronMedBackground,
    onBackground = CronMedTextPrimary,
    surface = CronMedSurface,
    onSurface = CronMedTextPrimary,
    surfaceVariant = CronMedBackground,
    onSurfaceVariant = CronMedTextSecondary,
    error = CronMedError,
    onError = CronMedSurface,
    outline = CronMedDivider,
    outlineVariant = CronMedTextHint
)

@Composable
fun CronMedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // Always light to match design

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CronMedBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CronMedTypography,
        content = content
    )
}