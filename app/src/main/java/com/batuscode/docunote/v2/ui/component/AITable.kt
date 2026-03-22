package com.batuscode.docunote.v2.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AITable(
    headers: List<String>,
    rows: List<List<String>>
) {
    // Web'deki border-white/10 ve yuvarlatılmış köşeler
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.03f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        // Tablo geniş olabilir, bu yüzden yatay kaydırma ekliyoruz
        Column(
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            // Header Bölümü
            Row(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                headers.forEach { header ->
                    Text(
                        text = header,
                        modifier = Modifier.width(120.dp), // Sabit genişlik sütun hizalamasını sağlar
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Satırlar
            rows.forEachIndexed { index, row ->
                val isLast = index == rows.size - 1
                Row(
                    modifier = Modifier
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                        .drawBehind { // Satır aralarına ince çizgi çekiyoruz
                            if (!isLast) {
                                val strokeWidth = 1.dp.toPx()
                                drawLine(
                                    color = Color.White.copy(alpha = 0.05f),
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = strokeWidth
                                )
                            }
                        }
                ) {
                    row.forEach { cell ->
                        Text(
                            text = cell,
                            modifier = Modifier.width(120.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}