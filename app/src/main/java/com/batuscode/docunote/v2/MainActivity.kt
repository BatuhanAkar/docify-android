package com.batuscode.docunote.v2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.batuscode.docunote.v2.ui.navigation.DocuNoteNavHost
import com.batuscode.docunote.v2.ui.navigation.NavigationEvent
import com.batuscode.docunote.v2.ui.navigation.NavigationManager
import com.batuscode.docunote.v2.ui.theme.DocuNoteTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigationManager: NavigationManager

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val navController = rememberNavController()

            // Navigation Manager'ı Dinle
            LaunchedEffect(Unit) {
                navigationManager.navigationEvents.collect { event ->
                    when (event) {
                        is NavigationEvent.Navigate -> {
                            navController.navigate(event.destination) {
                                event.popUpTo?.let { route ->
                                    popUpTo(route) { inclusive = event.inclusive }
                                }
                            }
                        }
                        is NavigationEvent.NavigateBack -> {
                            navController.popBackStack()
                        }
                    }
                }
            }

            DocuNoteTheme {
                DocuNoteNavHost(
                    windowSizeClass = windowSizeClass.widthSizeClass,
                    navController = navController
                )
            }
        }
    }
}