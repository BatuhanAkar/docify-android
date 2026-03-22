package com.batuscode.docunote.v2.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.batuscode.docunote.v2.ui.component.MessageBubble
import com.batuscode.docunote.v2.ui.component.SourceItem
import com.batuscode.docunote.v2.ui.theme.DocuNoteTheme

// Tablo verisi içeren mock model
data class MockTableData(
    val headers: List<String>,
    val rows: List<List<String>>
)

data class MessageMock(
    val id: String,
    val role: String,
    val content: String,
    val table: MockTableData? = null // Tablo opsiyonel
)
data class SourceMock(val id: String, val title: String, val type: String)

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    windowSizeClass: WindowWidthSizeClass,
    messages: List<MessageMock>,
    sources: List<SourceMock>
) {
    val isExpanded = windowSizeClass == WindowWidthSizeClass.Expanded

    Row(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {

        // EĞER GENİŞ EKRANSA: Sabit Sol Panel (Sources)
        if (isExpanded) {
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF161616))
                    .border(1.dp, color = Color.White.copy(alpha = 0.1f))
                    .padding(16.dp)
            ) {
                Text("SOURCES", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.6f))
                Spacer(Modifier.height(16.dp))
                sources.forEach { SourceItem(it) }
            }
        }

        // ANA CHAT ALANI
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            // Header
            CenterAlignedTopAppBar(
                title = { Text("Research Session #42", style = MaterialTheme.typography.titleMedium) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF121212).copy(alpha = 0.8f),
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = {}) { Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.White) }
                },
                actions = {
                    // Mobilde Kaynakları açmak için buton (Sadece Compact'ta)
                    if (!isExpanded) {
                        IconButton(onClick = { /* Bottom Sheet Aç */ }) {
                            Icon(Icons.Default.Source, contentDescription = null, tint = Color.White)
                        }
                    }
                    IconButton(onClick = { /* Bottom Sheet Aç */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White)
                    }
                }
            )

            // Mesaj Listesi
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(messages) { msg ->
                    MessageBubble(message = msg, isUser = msg.role == "user")
                }
            }

            // Chat Giriş Alanı (Web'deki yüzen bar tasarımı)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1A1A1A))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Ask about your sources...",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.2f)
                    )
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF2563EB))
                    }
                }
            }
        }
    }
}

@Preview(device = Devices.PIXEL_7, showBackground = true)
@Composable
fun PreviewWorkspaceMobile() {
    val tableMessage = MessageMock(
        id = "m4",
        role = "ai",
        content = "Here is the comparison you requested:",
        table = MockTableData(
            headers = listOf("Model", "Latency", "Memory"),
            rows = listOf(
                listOf("GPT-4", "45ms", "12GB"),
                listOf("Claude 3", "12ms", "4.2GB"),
                listOf("Gemini 1.5", "15ms", "3.8GB")
            )
        )
    )
    DocuNoteTheme {
        WorkspaceScreen(
            windowSizeClass = WindowWidthSizeClass.Compact,
            messages = listOf(MessageMock("1", "user", "How does attention work?"), tableMessage),
            sources = listOf(SourceMock("1", "Paper.pdf", "pdf"))
        )
    }
}

@Preview(device = Devices.TABLET, showBackground = true)
@Composable
fun PreviewWorkspaceTablet() {
    val tableMessage = MessageMock(
        id = "m4",
        role = "ai",
        content = "Here is the comparison you requested:",
        table = MockTableData(
            headers = listOf("Model", "Latency", "Memory"),
            rows = listOf(
                listOf("GPT-4", "45ms", "12GB"),
                listOf("Claude 3", "12ms", "4.2GB"),
                listOf("Gemini 1.5", "15ms", "3.8GB")
            )
        )
    )
    DocuNoteTheme {
        WorkspaceScreen(
            windowSizeClass = WindowWidthSizeClass.Expanded,
            messages = listOf(MessageMock("1", "user", "How does attention work?"), tableMessage),
            sources = listOf(SourceMock("1", "Data.pdf", "pdf"), SourceMock("2", "Video", "youtube"))
        )
    }
}