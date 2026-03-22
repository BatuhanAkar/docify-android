package com.batuscode.docunote.v2.data.repository

import com.batuscode.docunote.v2.data.firebase.FirebaseAIProvider
import com.batuscode.docunote.v2.domain.repository.AIService
import com.batuscode.docunote.v2.domain.util.DocuNoteException
import com.batuscode.docunote.v2.domain.model.Resource
import com.batuscode.docunote.v2.domain.model.SourceEntity
import com.batuscode.docunote.v2.domain.model.SourceType
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class AIServiceImpl @Inject constructor(
    private val firebaseAIProvider: FirebaseAIProvider,
    private val functions: FirebaseFunctions
) : AIService {

    override fun streamAnalysis(prompt: String, sources: List<SourceEntity>): Flow<Resource<String>> = flow {
        emit(Resource.Loading)
        try {
            // 1. Backend'den onay ve sessionId al (Gemini API üzerinden sayım yapıldı)
            val sessionResponse = functions.getHttpsCallable("requestAiSession")
                .call(mapOf("prompt" to prompt)).await()
            val sessionId = (sessionResponse.data as Map<*, *>)["sessionId"] as String

            // 2. Client-side: Firebase AI Logic SDK ile akışı başlat
            val model = firebaseAIProvider.getModel()
            var resultText = ""

            model.generateContentStream(prompt).collect { chunk ->
                resultText += chunk.text ?: ""
                emit(Resource.Success(resultText))

                // Son parça geldiğinde metadata'yı yakala ve doğrula
                chunk.usageMetadata?.let { metadata ->
                    functions.getHttpsCallable("commitAiSession").call(mapOf(
                        "sessionId" to sessionId,
                        "actualOutputTokens" to metadata.candidatesTokenCount,
                        "requestId" to "gen-id-${UUID.randomUUID()}"
                    )).await()
                }
            }

        } catch (e: Exception) {
            emit(Resource.Error(DocuNoteException.UnknownException(e.message ?: "AI Flow Error")))
        }
    }

    private fun prepareContent(prompt: String, sources: List<SourceEntity>): Content {
        val inputContent = content {
            sources.forEach { source ->
                if (source.storagePath != null) {
                    // PDF, Audio veya .txt olarak Storage'a yüklenen metinler
                    fileData(source.storagePath, getMimeType(source.type))
                } else if (source.remoteUrl != null) {
                    // WEBSITE ve YOUTUBE: Model yapılandırmasındaki Tool.urlContext() ile
                    // bu URL'i bir "bağlam" olarak okuyacak. Burada metin olarak veriyoruz.
                    text("Context URL: ${source.remoteUrl}\n")
                }
            }
            text(prompt)
        }
        return inputContent
    }
    private fun getMimeType(type: SourceType) = when (type) {
        SourceType.PDF -> "application/pdf"
        SourceType.AUDIO -> "audio/mpeg"
        SourceType.TEXT -> "text/plain" // Paste text artık bir .txt dosyası
        else -> "text/plain"
    }
}