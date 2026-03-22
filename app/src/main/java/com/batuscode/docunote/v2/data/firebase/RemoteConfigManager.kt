package com.batuscode.docunote.v2.data.firebase

import com.google.firebase.ai.type.generationConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigManager @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) {
    init {
        val settings = remoteConfigSettings { minimumFetchIntervalInSeconds = 3600 }
        remoteConfig.setConfigSettingsAsync(settings)
        remoteConfig.setDefaultsAsync(mapOf(
            "ai_model_name" to "gemini-1.5-flash",
            "ai_temp" to 0.7,
            "ai_system_instruction" to "Sen profesyonel bir asistansın...",
            "analysis_template_json" to "{\"prompt\": \"Analyze this: {{context}}\", \"safety\": \"high\"}"
        ))
    }

    fun getModelName(): String = remoteConfig.getString("ai_model_name")
    fun getSystemInstruction(): String = remoteConfig.getString("ai_system_instruction")
    fun getTemplate(key: String): String = remoteConfig.getString(key)

    fun getGenerationConfig() = generationConfig {
        temperature = remoteConfig.getDouble("ai_temp").toFloat().takeIf { it > 0 } ?: 0.7f
        topK = remoteConfig.getLong("ai_top_k").toInt()
        topP = remoteConfig.getDouble("ai_top_p").toFloat()
        maxOutputTokens = remoteConfig.getLong("ai_max_tokens").toInt()
        // Structured Output için responseMimeType ayarı da buradan yönetilebilir
        responseMimeType = remoteConfig.getString("ai_response_type").ifEmpty { "text/plain" }
    }
}