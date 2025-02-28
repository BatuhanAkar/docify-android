package com.batuscode.pdfium

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class PathData(
    val id: String,
    var color: Color,
    val path: List<OffsetWrapper>,
    val thickness: Float = 10f
)