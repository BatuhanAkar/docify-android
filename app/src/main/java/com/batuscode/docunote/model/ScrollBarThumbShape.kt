package com.batuscode.docunote.model

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset

class ScrollBarThumbShape : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(path = createArrowPath(size))
    }

    private fun createArrowPath(size: Size): Path {

        val path = Path()

        // Rectangle dimensions
        val rectWidth = size.width // Use the full width of the shape
        val rectHeight = size.height // Use the full height of the shape
        val rectRadius = 20f // Corner radius for the rectangle

        // Arrow dimensions
        val arrowWidth = 40f // Width of the arrow
        val arrowHeight = 30f // Height of the arrow
        val arrowThickness = 10f // Thickness of the arrow line

        // Draw the rounded rectangle behind the arrow
        val rect = RoundRect(
            left = 0f,
            top = 0f,
            right = rectWidth,
            bottom = rectHeight,
            CornerRadius(rectRadius, rectRadius)
        )
        path.addRoundRect(rect)

        // Draw the back arrow
        val arrowPath = Path().apply {
            // Start at the tip of the arrow
            moveTo(arrowWidth, arrowHeight / 2f) // Tip of the arrow
            // Draw the arrowhead
            lineTo(0f, arrowHeight / 2f) // Base of the arrowhead
            lineTo(arrowWidth / 2f, 0f) // Top of the arrowhead
            lineTo(arrowWidth / 2f, arrowHeight) // Bottom of the arrowhead
            close() // Close the arrowhead
        }

        // Position the arrow in the center of the rectangle
        val arrowLeft = (rectWidth - arrowWidth) / 2f
        val arrowTop = (rectHeight - arrowHeight) / 2f
        path.addPath(arrowPath, Offset(arrowLeft, arrowTop))
        return path
    }

}