package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = ElectricBlueLight,
    onPrimaryContainer = ElectricBlueDark,
    secondary = StatusSuccess,
    onSecondary = Color.White,
    secondaryContainer = StatusSuccessBg,
    onSecondaryContainer = StatusSuccess,
    tertiary = ElectricBlueGlow,
    onTertiary = TextPrimary,
    error = StatusDanger,
    onError = Color.White,
    errorContainer = StatusDangerBg,
    onErrorContainer = StatusDanger,
    background = CleanBg,
    onBackground = TextPrimary,
    surface = CleanSurface,
    onSurface = TextPrimary,
    surfaceVariant = ElectricBlueLight,
    onSurfaceVariant = TextSecondary,
    outline = CleanCardBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlueGlow,
    onPrimary = Color(0xFF001A4D),
    primaryContainer = Color(0xFF003399),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFF34D399),
    onSecondary = Color.Black,
    background = Color(0xFF0B132B),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF131D3B),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E294B),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF273860)
)

@Composable
fun KerupukKuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
