package com.batuscode.docunote.v2.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.batuscode.docunote.v2.ui.component.IngestionCard
import com.batuscode.docunote.v2.ui.component.RecentActivityItem
import com.batuscode.docunote.v2.ui.theme.DocuNoteTheme

@Composable
fun IngestionScreen(
    windowWidthSizeClass: WindowWidthSizeClass,
    mockActivities: List<String> = listOf("Quantum Physics Paper", "AI Research Note", "Web Archive - 2026"),
    onCardClick: () -> Unit = {},
    onRecentActivityClick: (String) -> Unit = {}
) {
    val isExpanded = windowWidthSizeClass == WindowWidthSizeClass.Expanded
    val isMedium = windowWidthSizeClass == WindowWidthSizeClass.Medium

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { /* Web'deki Header buraya gelecek */ }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // Layout Seçimi
            if (isExpanded || isMedium) {
                // GENİŞ EKRAN: Sol (Recent) -> Sağ (New)
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Sol Panel: Recent Activities
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Recent Activities",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(16.dp))
                        LazyColumn {
                            items(mockActivities) { RecentActivityItem(it, "PDF Source") }
                        }
                    }

                    // Sağ Panel: New Ingestion
                    Column(modifier = Modifier.weight(0.8f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Academic Brain-Sync",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(32.dp))
                        IngestionCard(onClick = { /* Navigate */ })
                    }
                }
            } else {
                // COMPACT (MOBİL): Üst (New) -> Alt (Recent)
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Source AI",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(24.dp))

                    IngestionCard(onClick = { /* Navigate */ })

                    Spacer(Modifier.height(32.dp))

                    Text("Recent Activities",
                        modifier = Modifier.align(Alignment.Start),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground)
                    mockActivities.forEach { RecentActivityItem(it, "PDF") }
                }
            }
        }
    }
}

// 1. Durum: Telefon - Temiz Başlangıç
@Preview(
    name = "Phone Light",
    device = Devices.PIXEL_7,
    showBackground = true
)
@Composable
fun PreviewIngestionCompactAnalysis() {
    DocuNoteTheme(darkTheme = false) {
        // Arka plan beyaz, kartlar ikincil renk mi? Kontrastı görelim.
        IngestionScreen(windowWidthSizeClass = WindowWidthSizeClass.Compact)
    }
}

// 2. Durum: Tablet - Geniş Yüzey Kontrolü
// Geniş ekranlarda LightSecondary (Gri) alanlar çok büyüdüğünde "boş ve kirli" durabilir.
@Preview(
    name = "Tablet Expanded",
    device = Devices.TABLET,
    showBackground = true
)
@Composable
fun PreviewIngestionExpandedAnalysis() {
    DocuNoteTheme(darkTheme = false) {
        IngestionScreen(windowWidthSizeClass = WindowWidthSizeClass.Expanded)
    }
}

// 3. Durum: Telefon - Karanlık Mod (Kontrast Patlaması)
// DarkPrimary (#FAFAFA) çok mu parlak kalıyor?
@Preview(
    name = "Phone Dark",
    device = Devices.PIXEL_7,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewIngestionDarkAnalysis() {
    DocuNoteTheme(darkTheme = true) {
        IngestionScreen(windowWidthSizeClass = WindowWidthSizeClass.Compact)
    }
}