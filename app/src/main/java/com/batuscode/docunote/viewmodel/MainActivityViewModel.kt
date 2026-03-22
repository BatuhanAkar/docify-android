package com.batuscode.docunote.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batuscode.docunote.MainActivity
import com.batuscode.docunote.data.PrefRepository
import com.batuscode.docunote.model.File
import com.batuscode.docunote.model.Folder
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
class MainActivityViewModel@Inject constructor(
    private val repository: PrefRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _BackOperationDescription = MutableStateFlow("")
    val BackOperationDescription : StateFlow<String> = _BackOperationDescription


    fun update_onBackOperationDescription(newValue : String){
        _BackOperationDescription.value = newValue
    }

    private val _isBackOperation = MutableStateFlow(false)
    val isBackOperation : StateFlow<Boolean> = _isBackOperation

    fun update_OnBackOperation(newValue : Boolean){
        _isBackOperation.value = newValue
    }

    private val _isOnScreenMergePDF = MutableStateFlow(false)
    val isOnScreenMergePDF : StateFlow<Boolean> = _isOnScreenMergePDF

    fun update_OnScreenMergePDF(newValue : Boolean){
        _isOnScreenMergePDF.value = newValue
    }

    private val _isOnScreenSplitPDF = MutableStateFlow(false)
    val isOnScreenSplitPDF : StateFlow<Boolean> = _isOnScreenSplitPDF

    fun update_OnScreenSplitPDF(newValue : Boolean){
        _isOnScreenSplitPDF.value = newValue
    }

    private val _isNewFolderOnScreen = MutableStateFlow(false)
    val isNewFolderOnScreen : StateFlow<Boolean> = _isNewFolderOnScreen

    fun update_NewFolderOnScreen(newValue: Boolean){
        _isNewFolderOnScreen.value = newValue
    }

    private val _recentlyReadedDocs = MutableStateFlow<List<File>>(emptyList())
    val recentlyReadedDocs : StateFlow<List<File>> = _recentlyReadedDocs.asStateFlow()

    fun update_recentlyReadedDocs(file: List<File>){
        _recentlyReadedDocs.value = file
    }

    fun addRecentlyReadedDoc(fileUri: String , fileName : String){
        viewModelScope.launch {
            repository.saveRecentlyReadDoc(uri = fileUri , fileName = fileName)
        }
    }

    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    val folders : StateFlow<List<Folder>> = _folders.asStateFlow()

    fun update_folders(folders : List<Folder>){
        _folders.value = folders
    }

    fun createNewFolder(folderName : String){
        viewModelScope.launch {
            repository.newFolder(folderName = folderName)
        }
    }


    private var _inPDFProcess = MutableSharedFlow<Unit>()
    val inPDFProcess = _inPDFProcess.asSharedFlow()

    fun handle_PDFProcess(){
        viewModelScope.launch {
            _inPDFProcess.emit(Unit)
        }
    }

    init {
        viewModelScope.launch {
            repository.getRecentlyReadDocs().collect{
                if (it.isNotEmpty()){
                    MainActivity.recentlyStat.value = true
                }
                update_recentlyReadedDocs(it)
            }
        }

        viewModelScope.launch {
            repository.getFolders().collect{
                if (it.isNotEmpty()){
                    MainActivity.folderStat.value = true
                }
                update_folders(it)
            }
        }
    }
}