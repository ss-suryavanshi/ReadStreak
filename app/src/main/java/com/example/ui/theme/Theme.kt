package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ReadStreakLightColorScheme = lightColorScheme(
    primary = FirePrimary,
    onPrimary = OnFirePrimary,
    primaryContainer = FirePrimaryContainer,
    onPrimaryContainer = OnFirePrimaryContainer,
    secondary = SuccessGreen,
    onSecondary = OnFirePrimary,
    secondaryContainer = SuccessGreenContainer,
    onSecondaryContainer = OnSuccessGreenContainer,
    background = FireBackground,
    onBackground = OnSurface,
    surface = FireSurface,
    onSurface = OnSurface,
    surfaceVariant = FireSurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    error = ErrorRed,
    errorContainer = ErrorContainer
)

private val ReadStreakDarkColorScheme = darkColorScheme(
    primary = FirePrimaryContainer,
    onPrimary = OnFirePrimary,
    primaryContainer = FirePrimary,
    onPrimaryContainer = OnFirePrimary,
    secondary = SuccessGreenContainer,
    onSecondary = OnSuccessGreenContainer,
    secondaryContainer = SuccessGreen,
    onSecondaryContainer = OnFirePrimary,
    background = Color(0xFF181716),
    onBackground = Color(0xFFF3F0EF),
    surface = Color(0xFF1F1D1C),
    onSurface = Color(0xFFF3F0EF),
    surfaceVariant = Color(0xFF312E2C),
    onSurfaceVariant = Color(0xFFD5C3BC),
    outline = Color(0xFF9E857C),
    outlineVariant = Color(0xFF53433D),
    inverseSurface = Color(0xFFF3F0EF),
    inverseOnSurface = Color(0xFF1F1D1C),
    error = Color(0xFFFFB4AB),
    errorContainer = ErrorRed
)

@Composable
fun ReadStreakTheme(
    themeMode: String = "Light", // "Light", "Dark", or "System"
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemInDarkTheme()
    }
    
    val colorScheme = if (darkTheme) ReadStreakDarkColorScheme else ReadStreakLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep backwards-compatible alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ReadStreakTheme(
        themeMode = if (darkTheme) "Dark" else "Light",
        content = content
    )
}

