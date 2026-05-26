package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StarkDarkColorScheme = darkColorScheme(
    primary = CyberPrimary,
    secondary = CyberSecondary,
    tertiary = CyberTertiary,
    background = CyberBackground,
    surface = CyberSurface,
    onPrimary = Color(0xFF1A1C1E),
    onSecondary = Color(0xFFE2E2E6),
    onTertiary = Color(0xFFE2E2E6),
    onBackground = Color(0xFFE2E2E6),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF2F3033),
    onSurfaceVariant = Color(0xFFC2C6CF)
)

private val StarkLightColorScheme = lightColorScheme(
    primary = CyberPrimary,
    secondary = CyberSecondary,
    tertiary = CyberTertiary,
    background = Color(0xFFE2E2E6),
    surface = Color(0xFF2F3033),
    onPrimary = Color(0xFF1A1C1E),
    onSecondary = Color(0xFF1A1C1E),
    onTertiary = Color(0xFF1A1C1E),
    onBackground = Color(0xFF1A1C1E),
    onSurface = Color(0xFF1A1C1E)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme by default for appropriate J.A.R.V.I.S interface atmosphere
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) StarkDarkColorScheme else StarkLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
