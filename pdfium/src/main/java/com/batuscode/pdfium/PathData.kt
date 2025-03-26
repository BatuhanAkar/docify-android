package com.batuscode.pdfium

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

data class PathData(
    val id: String,
    val mcolor: Color,
    val path: List<OffsetWrapper>,
    val thickness: Float = 10f
){
    fun getMcolorValue(): ULong {
        return mcolor.value // Örnek: Color'ın temel değeri
    }
    public fun toArgb(): Int {
        return mcolor.toArgb()
    }
}