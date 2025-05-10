package com.batuscode.docunote.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class File(
    val uri : String ,
    val name : String
) : Parcelable
