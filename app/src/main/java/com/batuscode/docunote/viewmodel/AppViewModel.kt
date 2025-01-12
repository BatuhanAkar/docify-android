package com.batuscode.docunote.viewmodel

import androidx.collection.MutableObjectList
import androidx.lifecycle.ViewModel
import com.batuscode.docunote.model.ExtensionButton
import com.batuscode.docunote.model.Folder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppViewModel : ViewModel(){
    val folders : MutableList<Folder> = mutableListOf()

    fun loadFolders(folder: Folder){
        folders.add(folder)
    }
    
    val extensionsOpen = MutableStateFlow<Boolean>(false)
    val _extensionsOpen : StateFlow<Boolean> = extensionsOpen

    fun update_extensionsOpenState(state:Boolean){
        extensionsOpen.value = state
    }

}