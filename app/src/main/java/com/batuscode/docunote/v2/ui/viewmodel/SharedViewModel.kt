package com.batuscode.docunote.v2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batuscode.docunote.v2.data.firebase.FirebaseAnalyticsManager
import com.batuscode.docunote.v2.data.repository.StorageServiceImpl
import com.batuscode.docunote.v2.domain.model.Resource
import com.batuscode.docunote.v2.domain.model.SourceEntity
import com.batuscode.docunote.v2.domain.model.UploadState
import com.batuscode.docunote.v2.domain.repository.AIService
import com.batuscode.docunote.v2.domain.repository.StorageService
import com.batuscode.docunote.v2.ui.navigation.Destination
import com.batuscode.docunote.v2.ui.navigation.NavigationEvent
import com.batuscode.docunote.v2.ui.navigation.NavigationManager
import com.batuscode.docunote.v2.ui.screen.MessageMock
import com.batuscode.docunote.v2.ui.util.WorkspaceViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val aiService: AIService,
    private val storageService: StorageServiceImpl,
    private val navManager: NavigationManager,
    private val analyticsManager: FirebaseAnalyticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkspaceViewState())
    val uiState = _uiState.asStateFlow()

    // UI'daki Progress bar'ı besleyecek state (0.0f - 1.0f arası)
    private val _processingProgress = MutableStateFlow(0f)
    val processingProgress = _processingProgress.asStateFlow()

    fun startProcessing(userPrompt: String, selectedSources: List<SourceEntity>) {
        analyticsManager.setUserAttributes("Free", "TR", "Turkey")

        viewModelScope.launch {
            navManager.navigate(NavigationEvent.Navigate(Destination.Processing.route))

            val updatedSources = mutableListOf<SourceEntity>()
            val totalSources = selectedSources.size

            // 1. AŞAMA: Dosyaları Storage'a Yükleme (Progress: %0 - %80)
            selectedSources.forEachIndexed { index, source ->
                storageService.uploadSource(source).collect { state ->
                    when (state) {
                        is UploadState.Progress -> {
                            // Genel ilerlemeyi hesapla. Örn: 2 dosya varsa her biri %40'lık (0.4f) dilim kaplar.
                            val baseProgress = (index.toFloat() / totalSources) * 0.8f
                            val currentFileProgress = (state.percentage / 100f) * (1f / totalSources) * 0.8f
                            _processingProgress.value = baseProgress + currentFileProgress
                        }
                        is UploadState.Success -> {
                            updatedSources.add(state.updatedSource)
                            // Firestore'a metadata kaydetme işlemi burada yapılabilir (Repository üzerinden)
                        }
                        is UploadState.Error -> {
                            _uiState.update { it.copy(error = state.exception.message) }
                            return@collect // Hata varsa akışı kes
                        }
                    }
                }
            }

            // 2. AŞAMA: Yapay Zeka Analizi (Progress: %80 -> %100)
            _processingProgress.value = 0.85f // AI isteği atılıyor

            aiService.streamAnalysis(userPrompt, updatedSources).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _processingProgress.value = 0.90f // AI Model yanıtı bekleniyor
                    }
                    is Resource.Success -> {
                        _processingProgress.value = 1.0f // İşlem bitti!

                        _uiState.update { currentState ->
                            val aiMessage = MessageMock(id = UUID.randomUUID().toString(), role = "ai", content = resource.data)
                            currentState.copy(
                                isLoading = false,
                                messages = currentState.messages + aiMessage
                            )
                        }
                        navManager.navigate(NavigationEvent.Navigate(Destination.Workspace.route))
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(error = resource.exception.message) }
                    }
                }
            }
        }
    }
}