package com.batuscode.docunote.model

import androidx.compose.runtime.MutableState

sealed class AIChatListItem {
    data class TextItem(var generating: MutableState<Boolean>, var text: MutableState<String>) : AIChatListItem()
    data class SumItem(val generating: MutableState<Boolean>, val fileName: MutableState<String>, val filePath: MutableState<String>) : AIChatListItem()
}