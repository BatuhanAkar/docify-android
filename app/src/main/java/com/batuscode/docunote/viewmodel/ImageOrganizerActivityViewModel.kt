package com.batuscode.docunote.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batuscode.docunote.data.PrefRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageOrganizerActivityViewModel @Inject constructor(
    private val repository: PrefRepository,
    @ApplicationContext private val context: Context
) : ViewModel(){


    fun addRecentlyReadedDoc(fileUri: String , fileName : String){
        viewModelScope.launch {
            repository.saveRecentlyReadDoc(uri = fileUri , fileName = fileName)
        }
    }
}