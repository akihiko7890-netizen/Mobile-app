package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WarmNestOrange,
    onPrimary = Color(0xFF2C1600),
    primaryContainer = Color(0xFF653300),
    onPrimaryContainer = SoftPeach,
    secondary = SoftSageGreen,
    onSecondary = Color(0xFF142917),
    secondaryContainer = Color(0xFF2B442E),
    onSecondaryContainer = Color(0xFFD4E8CF),
    tertiary = SoftBlueAccent,
    background = NestDarkBg,
    onBackground = NestDarkTextMain,
    surface = NestDarkCard,
    onSurface = NestDarkTextMain,
    surfaceVariant = Color(0xFF333B49),
    onSurfaceVariant = NestDarkTextSecondary,
    outline = NestDarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = WarmNestOrange,
    onPrimary = Color.White,
    primaryContainer = SoftPeach,
    onPrimaryContainer = Color(0xFF5A2A00),
    secondary = SoftSageGreen,
    onSecondary = Color(0xFF1B381F),
    secondaryContainer = Color(0xFFE2EFE0),
    onSecondaryContainer = Color(0xFF1A381E),
    tertiary = SoftBlueAccent,
    background = WarmCream,
    onBackground = NestMainText,
    surface = NestCardBg,
    onSurface = NestMainText,
    surfaceVariant = WarmCreamDarker,
    onSurfaceVariant = NestSecondaryText,
    outline = NestCardBorder
)

@Composable
fun NestNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    NestNoteTheme(darkTheme = darkTheme, content = content)
}

