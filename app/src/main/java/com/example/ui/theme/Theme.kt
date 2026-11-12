package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NordicLunarDarkColorScheme = darkColorScheme(
    primary = ImperialGold,
    onPrimary = Color(0xFF07111E),
    primaryContainer = Color(0xFF173152),
    onPrimaryContainer = ImperialGoldLight,
    secondary = AmethystWisdom,
    onSecondary = Color(0xFF130B2E),
    secondaryContainer = ArcaneSurfaceVariant,
    onSecondaryContainer = ParchmentLight,
    tertiary = TorchAmber,
    onTertiary = Color(0xFF261800),
    background = ObsidianBackground,
    onBackground = ParchmentLight,
    surface = ArcaneSurface,
    onSurface = ParchmentLight,
    surfaceVariant = ArcaneSurfaceVariant,
    onSurfaceVariant = ParchmentMuted,
    outline = ParchmentBorder,
    error = CrimsonChaos
)

private val NordicFrostLightColorScheme = lightColorScheme(
    primary = RoyalGoldPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E8FF),
    onPrimaryContainer = Color(0xFF091E3A),
    secondary = Color(0xFF4F46E5),
    onSecondary = Color.White,
    secondaryContainer = LightParchmentVariant,
    onSecondaryContainer = DeepInkText,
    tertiary = Color(0xFFB45309),
    background = LightParchmentBg,
    onBackground = DeepInkText,
    surface = LightParchmentSurface,
    onSurface = DeepInkText,
    surfaceVariant = LightParchmentVariant,
    onSurfaceVariant = Color(0xFF334E68),
    outline = Color(0xFF829AB1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to Nordic Night & Lunar Silver RPG ambiance
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) NordicLunarDarkColorScheme else NordicFrostLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
