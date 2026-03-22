package com.batuscode.docunote.v2.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class DocuNoteDimensions(
    val defaultPadding: Dp = 16.dp,
    val smallPadding: Dp = 8.dp,
    val cardRadius: Dp = 10.dp, // CSS: 0.625rem
    val sidebarWidth: Dp = 280.dp,
    val iconSize: Dp = 24.dp,
    val borderThickness: Dp = 1.dp
)

val LocalDimensions = staticCompositionLocalOf { DocuNoteDimensions() }