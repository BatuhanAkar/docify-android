package com.batuscode.docunote.v2.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    background = DarkBackground,
    surface = DarkBackground,
    onBackground = DarkForeground,
    onSurface = DarkForeground,
    error = DarkDestructive,
    outline = DarkMutedForeground
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    background = LightBackground,
    surface = LightBackground,
    onBackground = LightForeground,
    onSurface = LightForeground,
    error = LightDestructive,
    outline = LightBorder
)

@Composable
fun DocuNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // CSS odaklı tasarımda Dynamic Color genelde kapatılır ki markanın renkleri korunsun
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalDimensions provides DocuNoteDimensions()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }

}