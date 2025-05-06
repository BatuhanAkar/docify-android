package com.batuscode.docunote.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.batuscode.docunote.model.Subscription

class StoreActivityViewModel : ViewModel() {
    private val _subs = mutableStateListOf<Subscription>()
    val subs: List<Subscription> = _subs
    fun addSub(sub : Subscription) {
        _subs.add(sub)
    }
}