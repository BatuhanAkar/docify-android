package com.batuscode.docunote.viewmodel

import androidx.lifecycle.ViewModel
import com.batuscode.docunote.model.Subscription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StoreActivityViewModel : ViewModel() {
    private val _subs = MutableStateFlow<List<Subscription>>(emptyList())
    val subs : StateFlow<List<Subscription>> = _subs.asStateFlow()
    fun addSub(sub : Subscription) {
        _subs.value = _subs.value + sub
    }
    fun Clear_list(){
        _subs.value = emptyList()
    }
}