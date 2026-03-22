package com.batuscode.docunote.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Dark theme color scheme
private val DarkColorScheme = darkColorScheme(
    primary = ModernDarkPrimary,
    secondary = ModernDarkSecondary,
    tertiary = ModernDarkTertiary,

    background = background, // Dark background
    surface = surface, // Dark surface
    onPrimary = onPrimary, // Text on primary color
    onSecondary = onSecondary, // Text on secondary color
    onTertiary = onTertiary, // Text on tertiary color
    onBackground = onBackground, // Text on background
    onSurface = onSurface // Text on surface


)

// Light theme color scheme
private val LightColorScheme = lightColorScheme(
    primary = ModernLightPrimary,
    secondary = ModernLightSecondary,
    tertiary = ModernLightTertiary,

    background = lonBackground, // Light background
    surface = lsurface, // Light surface
    onPrimary = lonPrimary, // Text on primary color
    onSecondary = lonSecondary, // Text on secondary color
    onTertiary = lonTertiary, // Text on tertiary color
    onBackground = lonBackground, // Text on background
    onSurface = lonSurface // Text on surface
)



@Composable
fun DocuNoteTheme(
    darkTheme: Boolean = true,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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