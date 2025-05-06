package com.batuscode.docunote.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batuscode.docunote.model.User
import com.batuscode.docunote.utils.Auth
import com.batuscode.docunote.utils.FunctionsUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SignupActivityViewModel @Inject constructor() : ViewModel() {
    private val TAG = "SignInActivityViewModel"

}