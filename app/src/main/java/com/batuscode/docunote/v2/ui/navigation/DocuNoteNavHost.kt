package com.batuscode.docunote.v2.ui.navigation

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.batuscode.docunote.v2.ui.screen.IngestionScreen
import com.batuscode.docunote.v2.ui.screen.ProcessingScreen
import com.batuscode.docunote.v2.ui.screen.SourceManagerScreen
import com.batuscode.docunote.v2.ui.screen.WorkspaceScreen

@Composable
fun DocuNoteNavHost(
    windowSizeClass: WindowWidthSizeClass,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Ingestion.route
    ) {
        // 1. EKRAN: Ingestion
        composable(Destination.Ingestion.route) {
            IngestionScreen(
                windowWidthSizeClass = windowSizeClass,
                onCardClick = {
                    // Senaryo A: Yeni Kaynak Ekleme
                    navController.navigate(Destination.SourceManager.route)
                },
                onRecentActivityClick = { activityId ->
                    // Senaryo B: Geçmişe Dönüş (Direkt işleme gider)
                    navController.navigate(Destination.Processing.route)
                }
            )
        }

        // 2. EKRAN: Source Manager
        composable(Destination.SourceManager.route) {
            SourceManagerScreen(
                windowSizeClass = windowSizeClass,
                onBack = { navController.popBackStack() },
                onAnalyzeStarted = { sources ->
                    // Kaynaklar eklendi, işleme başla
                    navController.navigate(Destination.Processing.route)
                }
            )
        }

        // 3. EKRAN: Processing (Geçiş Ekranı)
        composable(Destination.Processing.route) {
            // Burada gerçek bir ViewModel state'i progress'i kontrol etmeli
            ProcessingScreen(
                progress = 0.5f, // Mock
                currentStep = 2,  // Mock
                remainingCredits = 95,
                onFinished = {
                    // İşlem bittiğinde Workspace'e uçuyoruz
                    navController.navigate(Destination.Workspace.route) {
                        // Geri tuşuna basınca tekrar Processing'e dönmesin diye stack'i temizliyoruz
                        popUpTo(Destination.Ingestion.route) { inclusive = false }
                    }
                }
            )
        }

        // 4. EKRAN: Workspace
        /*composable(Destination.Workspace.route) {
            WorkspaceScreen(
                windowSizeClass = windowSizeClass,
                // Workspace verileri burada ViewModel'den akacak
            )
        }*/
    }
}