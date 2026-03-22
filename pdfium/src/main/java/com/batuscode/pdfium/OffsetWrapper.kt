package com.batuscode.pdfium

import androidx.compose.ui.geometry.Offset

class OffsetWrapper(val offset: Offset) {

    fun getX(): Float = offset.x
    fun getY(): Float = offset.y
   //fun getY(canvasHeight: Float): Float = canvasHeight - offset.y

    fun scalePointForPDF(point: Offset, scaleX: Float, scaleY: Float): Offset {
        return Offset(point.x * scaleX, point.y * scaleY)
    }
    fun transformToBottomLeftOrigin(canvasHeight: Float, point: Offset): Offset {
        return Offset(point.x, canvasHeight - point.y)
    }


}