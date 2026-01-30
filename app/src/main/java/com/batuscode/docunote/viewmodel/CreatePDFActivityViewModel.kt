package com.batuscode.docunote.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import com.batuscode.docunote.model.Document
import com.batuscode.docunote.model.DocumentThumbnail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CreatePDFActivityViewModel : ViewModel(){

    val fileUri = MutableStateFlow<Uri?>(null)

    val _fileUri: StateFlow<Uri?> get() = fileUri

    fun update_fileUri(uri: Uri){
        fileUri.value = uri
    }

    val saveFlag = MutableStateFlow<Boolean>(false)

    val _saveFlag : StateFlow<Boolean> get() = saveFlag

    fun update_saveFlag(state: Boolean){
        saveFlag.value = state
    }

    val page = MutableStateFlow<MutableList<Document>>(mutableStateListOf())

    val _page: StateFlow<MutableList<Document>> get() = page

   /* fun initializePage(){
        val doc = Document(id = 0 , "" , 1)
        val iter = page.value.toMutableList()
        iter.add(doc)
        page.value = iter
    }

    fun setNewPage(){
        val doc = Document(id = 1 , "" , 2)

        val iter = page.value.toMutableList()
        iter.add(doc)
        page.value = iter
    }*/


    val write = MutableStateFlow<Boolean>(false)
    val _write : StateFlow<Boolean> get() = write

    fun update_write(state:Boolean){
        write.value = state
    }

    val newDocName = MutableStateFlow<String?>(null)

    val _newDocName : StateFlow<String?> get() = newDocName

    fun update_newDocName(value: String){
        newDocName.value = value
    }
}