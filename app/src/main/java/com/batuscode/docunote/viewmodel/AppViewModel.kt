package com.batuscode.docunote.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.batuscode.docunote.model.File
import com.batuscode.docunote.model.Folder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppViewModel : ViewModel(){




    val handNotesFolders : MutableList<Folder> = mutableListOf()

    fun loadhandNotesFolders(folder: Folder){
        handNotesFolders.add(folder)
    }

    
    val extensionsOpen = MutableStateFlow<Boolean>(false)
    val _extensionsOpen : StateFlow<Boolean> = extensionsOpen

    fun update_extensionsOpenState(state:Boolean){
        extensionsOpen.value = state
    }

    val uris : MutableList<Uri> = mutableListOf()

    fun add_urlist(uri: Uri){
        uris.add(uri)
    }

    val _recentlyList = MutableStateFlow<List<File>>(mutableStateListOf())

    val recentlyList : StateFlow<List<File>> get() = _recentlyList

    fun pushRecentlyList(list : List<File>){
        _recentlyList.value = list
    }

    fun addRecentlyFile(file: File){

        val iter = _recentlyList.value.toMutableList()
        val exists = iter.any { it.uri == file.uri }

        if (!exists){

            iter.add(file)
            _recentlyList.value = iter
        }
    }


    val _folders = MutableStateFlow<List<Folder>>(mutableStateListOf())

    val folders : StateFlow<List<Folder>> get() = _folders

    fun pushFolders(folders: List<Folder>){

        _folders.value = folders
      /*  val exists = iter.any { it.uri == file.uri }

        if (!exists){

            iter.add(file)
            _recentlyList.value = iter
        }*/

    }

    fun addFolder(folder:Folder){
        val iter = _folders.value.toMutableList()
        iter.add(folder)
        _folders.value = iter
    }



}