package com.batuscode.pdfium

import androidx.compose.ui.geometry.Offset

class OffsetWrapper(public val offset: Offset) {
    fun getX(): Float = offset.x
    fun getY(): Float = offset.y
}