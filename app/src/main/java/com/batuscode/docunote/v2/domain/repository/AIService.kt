package com.batuscode.docunote.v2.domain.repository

import com.batuscode.docunote.v2.domain.model.Resource
import com.batuscode.docunote.v2.domain.model.SourceEntity
import kotlinx.coroutines.flow.Flow

interface AIService {
    fun streamAnalysis(prompt: String, sources: List<SourceEntity>): Flow<Resource<String>>
}