package com.batuscode.docunote.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext// Modern and Sophisticated Color Palette
private val ModernDarkPrimary = Color(0xFF1E88E5) // Vivid Dark Blue
private val ModernDarkSecondary = Color(0xFF43A047) // Deep Green
private val ModernDarkTertiary = Color(0xFFEF5350) // Soft Vibrant Red

private val ModernLightPrimary = Color(0xFFF1F8E9) // Light Sage Green
private val ModernLightSecondary = Color(0xFF81D4FA) // Soft Sky Blue
private val ModernLightTertiary = Color(0xFFFFCC80) // Peach Orange

// Dark theme color scheme
private val DarkColorScheme = darkColorScheme(
    primary = ModernDarkPrimary,
    secondary = ModernDarkSecondary,
    tertiary = ModernDarkTertiary,

    background = Color(0xFF121212), // Dark background
    surface = Color(0xFF1E1E1E), // Dark surface
    onPrimary = Color.Black, // Text on primary color
    onSecondary = Color.White, // Text on secondary color
    onTertiary = Color.White, // Text on tertiary color
    onBackground = Color.White, // Text on background
    onSurface = Color.White // Text on surface


)

// Light theme color scheme
private val LightColorScheme = lightColorScheme(
    primary = ModernLightPrimary,
    secondary = ModernLightSecondary,
    tertiary = ModernLightTertiary,

    background = Color(0xFFFFFFFF), // Light background
    surface = Color(0xFFF0F0F0), // Light surface
    onPrimary = Color.Black, // Text on primary color
    onSecondary = Color.Black, // Text on secondary color
    onTertiary = Color.Black, // Text on tertiary color
    onBackground = Color.Black, // Text on background
    onSurface = Color.Black // Text on surface


)

@Composable
fun DocuNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}