package com.batuscode.docunote.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batuscode.docunote.AiActivity.Companion.aiActivityViewModel
import com.batuscode.docunote.WelcomeActivity
import com.batuscode.docunote.data.PrefRepository
import com.batuscode.docunote.model.AIChatListItem
import com.batuscode.docunote.utils.Auth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class AiActivityViewModel@Inject constructor(
    private val repository: PrefRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val TAG = "AiActivtyViewModel"

    private val _summarizeWelcome = MutableStateFlow(false)
    val summarizeWelcome : StateFlow<Boolean> = _summarizeWelcome.asStateFlow()

    fun update_SummarizeWelcome(newValue: Boolean){
        _summarizeWelcome.value = newValue
    }

    private val _knowledgeWelcome = MutableStateFlow(false)
    val knowledge : StateFlow<Boolean> = _knowledgeWelcome.asStateFlow()

    fun update_KnowledgeWelcome(newValue: Boolean){
        _knowledgeWelcome.value = newValue
    }

    private val _isSelectedKnowledgePDFfile = MutableStateFlow<Boolean>(false)
    val isSelectedKnowledgePDFfile : StateFlow<Boolean> = _isSelectedKnowledgePDFfile.asStateFlow()

    fun update_isSelectedKnowledgePDFfile(newValue : Boolean){
        _isSelectedKnowledgePDFfile.value = newValue
    }

    private val _knowledgePdfFileUri = MutableStateFlow<Uri?>(null)
    val knowledgePdfFileUri : StateFlow<Uri?> = _knowledgePdfFileUri.asStateFlow()

    fun update_knowledgePdfFileUri(uri: Uri){
        _knowledgePdfFileUri.value = uri
        update_isSelectedKnowledgePDFfile(true)
    }

    private val _openKnowledgePDFfilePickerActivity = MutableSharedFlow<Unit>()
    val openKnowledgePDFfilePickerActivity = _openKnowledgePDFfilePickerActivity.asSharedFlow()

    fun handleSelectKnowledgePdfFile(){
        viewModelScope.launch {
            _openKnowledgePDFfilePickerActivity.emit(Unit)
        }
    }

    var knowledgeListItemIndex = mutableStateOf(0)
    private val _knowledgeList = MutableStateFlow<List<AIChatListItem>>(emptyList())
    val knowledgeList : StateFlow<List<AIChatListItem>> = _knowledgeList.asStateFlow()

    fun start_knowledge_chat(generatedText: MutableState<String>){
        _knowledgeList.value = _knowledgeList.value + AIChatListItem.TextItem(generating = mutableStateOf(true) , generatedText , "model")
    }

    fun push_knowledge_chat_item(generatedText: MutableState<String> , role : String){
        knowledgeListItemIndex.value += 1
        _knowledgeList.value = _knowledgeList.value + AIChatListItem.TextItem(generating = mutableStateOf(true) , generatedText , role)
    }

    fun update_knowledge_chat_item(generatedText: MutableState<String>){
        if (knowledgeListItemIndex.value == _knowledgeList.value.size){
            if (_knowledgeList.value.isNotEmpty() && _knowledgeList.value[(_knowledgeList.value.size - 1)] is AIChatListItem.TextItem){
                (_knowledgeList.value[(_knowledgeList.value.size - 1)] as AIChatListItem.TextItem).generating.value = false
                (_knowledgeList.value[(_knowledgeList.value.size - 1)] as AIChatListItem.TextItem).text.value = generatedText.value
            }
        } else {
            if (_knowledgeList.value.isNotEmpty() && _knowledgeList.value[knowledgeListItemIndex.value] is AIChatListItem.TextItem){
                (_knowledgeList.value[knowledgeListItemIndex.value] as AIChatListItem.TextItem).generating.value = false
                (_knowledgeList.value[knowledgeListItemIndex.value] as AIChatListItem.TextItem).text.value = generatedText.value
            }
        }
    }

    var chatListItemIndex = mutableStateOf(0)
    private val _chatList = MutableStateFlow<List<AIChatListItem>>(emptyList())
    val chatList : StateFlow<List<AIChatListItem>> = _chatList.asStateFlow()

    fun update_chat_index(){
        chatListItemIndex.value += 1
    }
    fun add_welcome_chat(generatedText: MutableState<String>){
        _chatList.value = _chatList.value + AIChatListItem.TextItem(generating = mutableStateOf(true) , generatedText , "model")
    }

    fun push_summ_chat_item(generatedText: MutableState<String> , role: String){
        chatListItemIndex.value += 1
        _chatList.value = _chatList.value + AIChatListItem.TextItem(generating = mutableStateOf(true) , generatedText , role)
    }

    fun update_chat_item(generatedText: MutableState<String>){
        if (chatListItemIndex.value == aiActivityViewModel.chatList.value.size){
            if (_chatList.value.isNotEmpty() && _chatList.value[(aiActivityViewModel.chatList.value.size - 1)] is AIChatListItem.TextItem){
                (_chatList.value[(aiActivityViewModel.chatList.value.size - 1)] as AIChatListItem.TextItem).generating.value = false
                (_chatList.value[(aiActivityViewModel.chatList.value.size - 1)] as AIChatListItem.TextItem).text.value = generatedText.value
            }
        } else {
            if (_chatList.value.isNotEmpty() && _chatList.value[chatListItemIndex.value] is AIChatListItem.TextItem){
                (_chatList.value[chatListItemIndex.value] as AIChatListItem.TextItem).generating.value = false
                (_chatList.value[chatListItemIndex.value] as AIChatListItem.TextItem).text.value = generatedText.value

            }
        }

    }


    fun add_summed_item(){
        _chatList.value = _chatList.value + AIChatListItem.SumItem(generating = mutableStateOf(false) , mutableStateOf("") , mutableStateOf(""))
    }

    fun update_summed_item(fileName : String , filePath : String){

        if (chatListItemIndex.value == aiActivityViewModel.chatList.value.size){
            if (aiActivityViewModel.chatList.value.isNotEmpty() && aiActivityViewModel.chatList.value[(aiActivityViewModel.chatList.value.size - 1)] is AIChatListItem.SumItem){
                // when summarization finish set generating false...
                (aiActivityViewModel.chatList.value[(aiActivityViewModel.chatList.value.size - 1)] as AIChatListItem.SumItem).fileName.value = fileName
                (aiActivityViewModel.chatList.value[(aiActivityViewModel.chatList.value.size - 1)] as AIChatListItem.SumItem).filePath.value = filePath
                (aiActivityViewModel.chatList.value[(aiActivityViewModel.chatList.value.size - 1)] as AIChatListItem.SumItem).generating.value = true
            }
        } else {
            if (aiActivityViewModel.chatList.value.isNotEmpty() && aiActivityViewModel.chatList.value[aiActivityViewModel.chatListItemIndex.value] is AIChatListItem.SumItem){
                // when summarization finish set generating false...
                (aiActivityViewModel.chatList.value[aiActivityViewModel.chatListItemIndex.value] as AIChatListItem.SumItem).fileName.value = fileName
                (aiActivityViewModel.chatList.value[aiActivityViewModel.chatListItemIndex.value] as AIChatListItem.SumItem).filePath.value = filePath
                (aiActivityViewModel.chatList.value[aiActivityViewModel.chatListItemIndex.value] as AIChatListItem.SumItem).generating.value = true
            }
        }
    }











    val _isGrantedNotificationPermission = mutableStateOf<Boolean>(false)
    suspend fun saveNotificationState(isGranted : Boolean) = withContext(Dispatchers.IO){
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveNotificationPermissionState(isGranted)
        }
    }
    init {

        viewModelScope.launch {
            repository.readOnBoardingState().collect { completed ->
                if (!completed) {
                    val intent = Intent(context , WelcomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    ( context as? Activity)?.finish()
                }
            }
        }
        viewModelScope.launch {
            repository.readNotificationState().collect { isGranted ->
                Log.d("isNeedAskNotificationPermission" , isGranted.toString())
                _isGrantedNotificationPermission.value = isGranted
            }
        }
        viewModelScope.launch {
            // read msg token status .
            if (Auth.auth.currentUser != null){
                repository.readTakedMSGToken().collect { isTaked ->
                    Log.d(TAG , "isTaked " + isTaked)
                    if (!isTaked){
                        Log.d(TAG , "msg token not taked")
                        requestMSGtoken()
                    }
                }
            }
        }

    }


    private fun requestMSGtoken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful){
                Log.w(TAG , "messaging token failed " , task.exception)
                return@addOnCompleteListener
            }
            Log.d(TAG , "msg token is taked")
            CoroutineScope(Dispatchers.IO).launch {
                repository.saveTakedMessageState(true)
            }
        }
    }
}