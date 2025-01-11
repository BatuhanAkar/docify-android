package com.batuscode.docunote.viewmodel

import androidx.collection.MutableObjectList
import androidx.lifecycle.ViewModel
import com.batuscode.docunote.model.Folder
import kotlinx.coroutines.flow.MutableStateFlow

class AppViewModel : ViewModel(){
    val folders : MutableList<Folder> = mutableListOf()

    fun loadFolders(folder: Folder){
        folders.add(folder)
    }

}