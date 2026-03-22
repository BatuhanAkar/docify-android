package com.batuscode.docunote.v2.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.batuscode.docunote.v2.domain.model.SourceEntity
import com.batuscode.docunote.v2.domain.model.SourceType
import com.batuscode.docunote.v2.ui.theme.DocuNoteTheme
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceManagerTopBar(
    onBack: () -> Unit,
    isExpanded: Boolean
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Add Sources",
                style = MaterialTheme.typography.titleMedium
            )
        },
        navigationIcon = {
            // Tablette genellikle modal veya ayrı ekran olduğu için kapatma ikonu iyidir
            // Ancak tasarım tercihinize göre isExpanded ise simgeyi değiştirebilirsiniz
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.White
        )
    )
}

@Composable
fun SourceManagerBottomBar(
    addedSourcesCount: Int,
    onAnalyzeStarted: () -> Unit,
) {
    // Surface ile butonun arkasına hafif bir katman ve sınır ekliyoruz
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF161616),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Button(
            onClick = onAnalyzeStarted,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2563EB),
                disabledContainerColor = Color.White.copy(alpha = 0.05f)
            ),
            enabled = addedSourcesCount > 0
        ) {
            Icon(Icons.Default.AutoAwesome, null)
            Spacer(Modifier.width(8.dp))
            Text("Analyze $addedSourcesCount Sources")
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceManagerScreen(
    windowSizeClass: WindowWidthSizeClass,
    onAnalyzeStarted: (List<SourceEntity>) -> Unit,
    onBack: () -> Unit
) {
    val addedSources = remember {
        mutableStateListOf(
            SourceEntity(name = "Q4_Report.pdf", type = SourceType.PDF, remoteUrl = "2.4 MB"),
            SourceEntity(name = "Interview_Recording.mp3", type = SourceType.AUDIO, remoteUrl = "15:20")
        )
    }

    val isExpanded = windowSizeClass == WindowWidthSizeClass.Expanded

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            SourceManagerTopBar(onBack = onBack, isExpanded = isExpanded)
        },
        bottomBar = {
            SourceManagerBottomBar(addedSources.size) { onAnalyzeStarted(addedSources) }
        }
    ) { padding ->
        if (isExpanded) {
            // TABLET / DESKTOP: Yan yana iki panel
            Row(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Sol Panel: Ekleme Seçenekleri
                Column(modifier = Modifier.weight(0.4f)) {
                    Text("ADD NEW SOURCE", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.5f))
                    Spacer(Modifier.height(16.dp))
                    SourceTypeGrid() // Grid yapısında büyük butonlar
                }

                // Sağ Panel: Liste
                Column(modifier = Modifier.weight(0.6f)) {
                    Text("SELECTED SOURCES (${addedSources.size})", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.5f))
                    Spacer(Modifier.height(16.dp))
                    SourceList(addedSources)
                }
            }
        } else {
            // TELEFON: Dikey akış
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("QUICK ADD", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                Spacer(Modifier.height(12.dp))
                SourceTypeRow() // Yatay kaydırılabilir bar

                Spacer(Modifier.height(32.dp))

                Text("UPLOADED", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                Spacer(Modifier.height(12.dp))
                SourceList(addedSources)
            }
        }
    }
}

@Composable
fun SourceTypeGrid() {
    val items = listOf(
        Triple(Icons.Default.PictureAsPdf, "PDF Document", Color(0xFFEF4444)),
        Triple(Icons.Default.Language, "Web Article", Color(0xFF3B82F6)),
        Triple(Icons.Default.VideoFile, "YouTube Video", Color(0xFFFF0000)),
        Triple(Icons.Default.Mic, "Audio File", Color(0xFFF97316)),
        Triple(Icons.Default.TextFields, "Paste Text", Color(0xFF10B981))
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { (icon, label, color) ->
            Surface(
                onClick = { /* File Picker */ },
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(16.dp))
                    Text(label, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun SourceTypeRow() {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        QuickAddAction(Icons.Default.PictureAsPdf, "PDF", Color(0xFFEF4444))
        QuickAddAction(Icons.Default.Language, "Link", Color(0xFF3B82F6))
        QuickAddAction(Icons.Default.VideoFile, "Video", Color(0xFFFF0000))
        QuickAddAction(Icons.Default.Mic, "Audio", Color(0xFFF97316))
        QuickAddAction(Icons.Default.TextFields, "Text", Color(0xFF10B981))
    }
}
@Composable
fun QuickAddAction(icon: ImageVector, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(end = 16.dp).clickable { /* Dosya Seçiciyi Aç */ }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.1f))
                .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
    }
}

@Composable
fun SourceListCard(source: SourceEntity, onRemove: () -> Unit) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when(source.type) {
                    SourceType.PDF -> Icons.Default.Description
                    SourceType.WEBSITE -> Icons.Default.Link
                    else -> Icons.Default.Article
                },
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(source.name, style = MaterialTheme.typography.bodyMedium, color = Color.White, maxLines = 1)
                Text(source.remoteUrl!!, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.3f))
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.DeleteOutline, null, tint = Color.Red.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun SourceList(sources: SnapshotStateList<SourceEntity>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(sources) { source ->
            SourceListCard(source = source, onRemove = { sources.remove(source) })
        }
    }
}

@Preview(name = "Mobile - Source Manager", device = Devices.PIXEL_7)
@Composable
fun PreviewSourceManagerMobile() {
    DocuNoteTheme {
        SourceManagerScreen(WindowWidthSizeClass.Compact, {}, {})
    }
}

@Preview(name = "Tablet - Source Manager", device = Devices.TABLET, widthDp = 1280, heightDp = 800)
@Composable
fun PreviewSourceManagerTablet() {
    DocuNoteTheme {
        SourceManagerScreen(WindowWidthSizeClass.Expanded, {}, {})
    }
}