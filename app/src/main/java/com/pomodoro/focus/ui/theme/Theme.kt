package com.pomodoro.focus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ForestPrimary,
    onPrimary = ForestBackground,
    primaryContainer = ForestPrimaryVariant,
    secondary = ForestSecondary,
    background = ForestBackground,
    surface = ForestSurface,
    surfaceVariant = ForestSurfaceVariant,
    onBackground = ForestTextPrimary,
    onSurface = ForestTextPrimary,
    onSurfaceVariant = ForestTextSecondary,
    outline = ForestDivider,
    error = ForestAccentRed,
    tertiary = ForestAccentGold
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightBackground,
    primaryContainer = LightPrimaryVariant,
    secondary = LightSecondary,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    outline = LightDivider,
    error = LightAccentRed,
    tertiary = LightAccentGold
)

@Composable
fun FocusForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
