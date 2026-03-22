package com.batuscode.docunote.v2.ui.util

import com.batuscode.docunote.v2.ui.screen.MessageMock

interface ViewState

data class WorkspaceViewState(
    val isLoading: Boolean = false,
    val messages: List<MessageMock> = emptyList(),
    val error: String? = null
) : ViewState