package com.batuscode.docunote.v2.ui.navigation

sealed class NavigationEvent {
    data class Navigate(
        val destination: String,
        val popUpTo: String? = null,
        val inclusive: Boolean = false
    ) : NavigationEvent()

    object NavigateBack : NavigationEvent()
}