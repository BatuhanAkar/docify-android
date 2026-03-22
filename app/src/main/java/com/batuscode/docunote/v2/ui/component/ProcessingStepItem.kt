package com.batuscode.docunote.v2.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.batuscode.docunote.v2.ui.screen.ProcessingStatus

@Composable
fun ProcessingStepItem(
    label: String,
    status: ProcessingStatus // Enum: PENDING, ACTIVE, COMPLETE
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        val containerColor by animateColorAsState(
            targetValue = when (status) {
                ProcessingStatus.COMPLETE -> Color(0xFF22C55E)
                ProcessingStatus.ACTIVE -> Color.Transparent
                else -> Color.Transparent
            }, label = "color"
        )

        val borderColor by animateColorAsState(
            targetValue = when (status) {
                ProcessingStatus.COMPLETE -> Color(0xFF22C55E)
                ProcessingStatus.ACTIVE -> Color(0xFF3B82F6)
                else -> Color.White.copy(alpha = 0.1f)
            }, label = "border"
        )

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(containerColor)
                .border(1.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            when (status) {
                ProcessingStatus.COMPLETE -> Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp), tint = Color.Black)
                ProcessingStatus.ACTIVE -> CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF3B82F6)
                )
                else -> {}
            }
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (status == ProcessingStatus.PENDING) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.8f)
        )
    }
}