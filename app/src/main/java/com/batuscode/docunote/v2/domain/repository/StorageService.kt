package com.batuscode.docunote.v2.domain.repository

import com.batuscode.docunote.v2.domain.model.SourceEntity
import com.batuscode.docunote.v2.domain.model.UploadState
import kotlinx.coroutines.flow.Flow

interface StorageService {
    // Tek bir dosyayı Storage'a yükler ve ilerlemeyi (0-100) raporlar
    fun uploadSource(source: SourceEntity): Flow<UploadState>
}