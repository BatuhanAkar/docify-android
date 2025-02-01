package com.batuscode.docunote.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RecentlyFile(
    @PrimaryKey val id: Int , 
    @ColumnInfo(name = "file_name") val filename: String? ,
    @ColumnInfo(name = "file_path") val filepath: String?
)
