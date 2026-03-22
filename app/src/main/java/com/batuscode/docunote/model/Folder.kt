package com.batuscode.docunote.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Folder(
    var id: Int ,
    var name:String ,
    var icon:Int ,
    var docs : List<File>
) : Parcelable

typealias FolderStructer = List<Folder>
