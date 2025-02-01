package com.batuscode.docunote.model

import androidx.room.Dao
import androidx.room.Query

@Dao
interface RecentlyFileDao {
    @Query("SELECT * FROM recentlyfile")
    fun getAll(): List<RecentlyFile>
}