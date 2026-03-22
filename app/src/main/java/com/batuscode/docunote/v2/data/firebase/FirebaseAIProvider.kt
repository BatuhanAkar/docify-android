package com.batuscode.docunote.v2.data.firebase

import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.HarmBlockThreshold
import com.google.firebase.ai.type.HarmCategory
import com.google.firebase.ai.type.SafetySetting
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAIProvider @Inject constructor(
    private val remoteConfigManager: RemoteConfigManager
) {
    // Firebase AI instance'ı
    private val firebaseAI = Firebase.ai

    fun getModel(): GenerativeModel {
        // Remote Config'den model adını ve configleri çekiyoruz
        val modelName = remoteConfigManager.getModelName()
        val systemInstruction = remoteConfigManager.getSystemInstruction()
        val generationConfig = remoteConfigManager.getGenerationConfig()

        return firebaseAI.generativeModel(
            modelName = modelName,
            generationConfig = generationConfig,
            // Sunucu tarafında yönetilen sistem talimatı
            systemInstruction = content { text(systemInstruction) },
            tools = getTools(),
            // Firebase AI SDK ile gelen güvenlik ayarları
            safetySettings = listOf(
                SafetySetting(HarmCategory.HATE_SPEECH, HarmBlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.HARASSMENT, HarmBlockThreshold.MEDIUM_AND_ABOVE)
            )
        )
    }

    // Server-side Prompt Template erişimi
    fun getPromptTemplate(templateKey: String): String {
        return remoteConfigManager.getTemplate(templateKey)
    }

    private fun getTools(): List<Tool> {
        return listOf(
            Tool.googleSearch(),
            Tool.urlContext()
        )
    }
}