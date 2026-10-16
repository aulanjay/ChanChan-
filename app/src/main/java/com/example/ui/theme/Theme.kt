package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ChanOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = ChanOrangeContainer,
    onPrimaryContainer = ChanOnOrangeContainer,
    secondary = ChanCoffeeAmber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0EAE1),
    onSecondaryContainer = ChanCoffeeBrown,
    tertiary = ChanGoldPoints,
    onTertiary = Color.White,
    background = ChanWarmCream,
    onBackground = TextPrimary,
    surface = ChanWarmSurface,
    onSurface = TextPrimary,
    surfaceVariant = ChanSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = ChanBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = ChanOrangeLight,
    onPrimary = Color.White,
    primaryContainer = ChanOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFBCAAA4),
    onSecondary = Color.Black,
    tertiary = ChanGoldPoints,
    background = Color(0xFF1A1615),
    surface = Color(0xFF241F1E),
    onBackground = Color(0xFFF5EBE6),
    onSurface = Color(0xFFF5EBE6)
)

@Composable
fun ChanChanTheme(
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
