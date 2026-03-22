package com.batuscode.docunote.v2.domain.model

import java.util.UUID

enum class SourceType {
    PDF, AUDIO, WEBSITE, YOUTUBE, TEXT
}

// Kaynağın veritabanı (Firestore) ve uygulama içi temsil modeli
data class SourceEntity(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: SourceType,
    val localUri: String? = null, // Cihazdaki dosyanın yolu veya Paste Text'in kendisi
    val remoteUrl: String? = null, // Sadece WEBSITE ve YOUTUBE için
    val storagePath: String? = null, // Sadece PDF, AUDIO, TEXT (.txt olarak) için
    val sizeBytes: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

// Yükleme (Upload) aşamasının durumlarını yöneteceğimiz sınıf
sealed class UploadState {
    data class Progress(val percentage: Int) : UploadState()
    data class Success(val updatedSource: SourceEntity) : UploadState()
    data class Error(val exception: Exception) : UploadState()
}