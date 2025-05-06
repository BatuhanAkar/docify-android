package com.batuscode.docunote.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batuscode.docunote.data.PrefRepository
import com.batuscode.docunote.utils.OnBoardingPage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
open class WelcomeViewModel @Inject constructor(
    private val repository: PrefRepository
) : ViewModel() {
    val _pages = mutableStateListOf<OnBoardingPage>()
    val pages : List<OnBoardingPage> = _pages

    suspend fun saveOnBoardingState(completed: Boolean) = withContext(Dispatchers.IO) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveOnBoardingState(completed = completed)
        }
    }

    init {
        viewModelScope.launch {
            _pages += OnBoardingPage.First
            _pages += OnBoardingPage.Second
            _pages += OnBoardingPage.Third
            _pages += OnBoardingPage.Fourth
        }
    }
}