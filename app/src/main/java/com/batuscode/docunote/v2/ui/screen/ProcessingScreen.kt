package com.batuscode.docunote.v2.ui.screen

import androidx.compose.runtime.Composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.batuscode.docunote.v2.ui.component.DocuNoteCard
import com.batuscode.docunote.v2.ui.component.ProcessingStepItem
import com.batuscode.docunote.v2.ui.theme.DocuNoteTheme

enum class ProcessingStatus {
    PENDING,ACTIVE,COMPLETE
}
@Composable
fun ProcessingScreen(
    progress: Float, // 0.0f - 1.0f arası
    currentStep: Int,
    remainingCredits: Int,
    onFinished: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        // Arkaplan Glow Efekti
        Box(
            modifier = Modifier
                .size(400.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF1E3A8A).copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // İkon ve Başlık
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF4F46E5)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Psychology, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }

            Spacer(Modifier.height(24.dp))
            Text("Analyzing Source", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Text("Please wait while we process your content", color = Color.White.copy(alpha = 0.4f), style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(48.dp))

            // Progress Circle
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(180.dp),
                    color = Color(0xFF3B82F6),
                    strokeWidth = 6.dp,
                    trackColor = Color.White.copy(alpha = 0.05f),
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(Modifier.height(48.dp))

            // Adımlar
            val steps = listOf("Initializing environment", "Transcribing audio", "Generating embeddings", "Syncing knowledge base")
            Column {
                steps.forEachIndexed { index, label ->
                    val status = when {
                        currentStep > index -> ProcessingStatus.COMPLETE
                        currentStep == index -> ProcessingStatus.ACTIVE
                        else -> ProcessingStatus.PENDING
                    }
                    ProcessingStepItem(label, status)
                }
            }

            Spacer(Modifier.height(32.dp))

            // Credits Card
            DocuNoteCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFF97316).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFFF97316), modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Credits Remaining", style = MaterialTheme.typography.labelMedium, color = Color.White)
                            Text("Premium Plan", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                        }
                    }
                    Text(
                        text = "$remainingCredits",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

// 1. Senaryo: İşlem yeni başlamış (%15)
@Preview(name = "Initial Phase", showBackground = true, device = Devices.PIXEL_7)
@Composable
fun PreviewProcessingInitial() {
    DocuNoteTheme {
        ProcessingScreen(
            progress = 0.15f,
            currentStep = 0, // İlk adımda (Initializing environment)
            remainingCredits = 99
        )
    }
}

// 2. Senaryo: İşlem yarıda (%55)
@Preview(name = "Mid Phase", showBackground = true, device = Devices.PIXEL_7)
@Composable
fun PreviewProcessingMid() {
    DocuNoteTheme {
        ProcessingScreen(
            progress = 0.55f,
            currentStep = 2, // Üçüncü adımda (Generating embeddings)
            remainingCredits = 97
        )
    }
}

// 3. Senaryo: Tablet Ekranında Bitiş (%100)
@Preview(name = "Final Phase Tablet", showBackground = true, device = Devices.TABLET)
@Composable
fun PreviewProcessingFinal() {
    DocuNoteTheme {
        // Geniş ekranlarda (Tablet) merkezi yapının nasıl durduğunu görelim
        ProcessingScreen(
            progress = 1.0f,
            currentStep = 4, // Tüm adımlar tamamlandı
            remainingCredits = 95
        )
    }
}