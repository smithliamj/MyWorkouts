package com.example.myworkouts.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF003366),
    onPrimaryContainer = Color(0xFFB3D9FF),
    secondary = ElectricBlueVariant,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1A365D),
    onSecondaryContainer = Color(0xFFE2E8F0),
    background = DarkBackground,
    onBackground = WhiteText,
    surface = DarkSurface,
    onSurface = WhiteText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = MutedText,
    outline = Color(0xFF475569)
)

@Composable
fun MyWorkoutsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}