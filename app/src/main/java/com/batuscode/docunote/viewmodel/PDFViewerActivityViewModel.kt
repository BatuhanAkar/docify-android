package com.batuscode.docunote.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PDFViewerActivityViewModel : ViewModel() {
    val _PDFpages : MutableList<Bitmap> = mutableListOf()

    fun set_pdfPage(bitmap:Bitmap){
        _PDFpages.add(bitmap)
    }

    val _edit = MutableStateFlow<Boolean>(false)

    val edit : StateFlow<Boolean> get() = _edit

    fun update_editState(state: Boolean){
        _edit.value = state
    }


}