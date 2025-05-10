package com.batuscode.docunote.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batuscode.docunote.data.PrefRepository
import com.batuscode.docunote.model.File
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderScopeActivityViewModel @Inject constructor(
    private val repository: PrefRepository,
    @ApplicationContext private val context: Context ,
) : ViewModel() {

    private val _UrisMap = MutableStateFlow<MutableMap<Int,Uri>>(mutableMapOf())
    val UrisMap : StateFlow<MutableMap<Int,Uri>> = _UrisMap.asStateFlow()

    fun update_UrisMap(UrisMap: MutableMap<Int, Uri>?){
        _UrisMap.value = UrisMap!!
    }
    private val _addNewDocuments = MutableSharedFlow<Unit>()
    val addNewDocuments = _addNewDocuments.asSharedFlow()

    fun handle_addNewDocuments(UrisMap: MutableMap<Int, Uri>?){
        viewModelScope.launch {
            update_UrisMap(UrisMap)
            _addNewDocuments.emit(Unit)
        }
    }
    private val _documents = MutableStateFlow<List<File>>(emptyList())
    val documents : StateFlow<List<File>> = _documents.asStateFlow()

    fun update_documentsList(docs : List<File>){
        _documents.value = docs
    }
    fun addDocumentToFolder(folderId : Int , docsMap : Map<String , String>){
        viewModelScope.launch {
            repository.addDocument_Folder(folderId,docsMap)
        }
    }

    fun getDocuments(folderId : Int){

        viewModelScope.launch {
            repository.getDocumentsToFolder(folderId).collect{
                _documents.value = emptyList()
                update_documentsList(it)
            }
        }
    }


}