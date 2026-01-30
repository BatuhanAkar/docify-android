package com.batuscode.docunote.model

import android.graphics.Bitmap
import androidx.compose.runtime.MutableIntState
import com.mohamedrejeb.richeditor.model.RichTextState

data class Document(
    var index: Int ,
    var page: MutableIntState ,
    var text: RichTextState ,
    var bitmap: Bitmap
)
