package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NexElectricBlue,
    secondary = NexCyan,
    tertiary = NexBrightBlue,
    background = NexBlack,
    surface = NexDarkGray,
    onPrimary = TextWhite,
    onSecondary = NexBlack,
    onTertiary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite,
    surfaceVariant = NexCardGray,
    onSurfaceVariant = TextWhite
)

private val LightColorScheme = darkColorScheme( // For gaming vibe, we default light scheme to dark scheme too!
    primary = NexElectricBlue,
    secondary = NexCyan,
    tertiary = NexBrightBlue,
    background = NexBlack,
    surface = NexDarkGray,
    onPrimary = TextWhite,
    onSecondary = NexBlack,
    onTertiary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite,
    surfaceVariant = NexCardGray,
    onSurfaceVariant = TextWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme by default
    dynamicColor: Boolean = false, // Disable dynamic color to maintain consistent brand gaming colors
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
