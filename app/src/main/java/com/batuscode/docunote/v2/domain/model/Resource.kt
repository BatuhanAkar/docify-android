package com.batuscode.docunote.v2.domain.model

import com.batuscode.docunote.v2.domain.util.DocuNoteException

sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val exception: DocuNoteException) : Resource<Nothing>()
}