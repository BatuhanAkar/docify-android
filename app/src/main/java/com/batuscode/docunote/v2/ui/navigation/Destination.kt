package com.batuscode.docunote.v2.ui.navigation

sealed class Destination (val route: String) {
    object Ingestion : Destination("ingestion")
    object SourceManager : Destination("source_manager")
    object Processing : Destination("processing")
    object Workspace : Destination("workspace")
}