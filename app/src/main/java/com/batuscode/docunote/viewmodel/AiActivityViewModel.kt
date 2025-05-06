package com.batuscode.docunote.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batuscode.docunote.WelcomeActivity
import com.batuscode.docunote.data.PrefRepository
import com.batuscode.docunote.model.AIChatListItem
import com.batuscode.docunote.model.User
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    var chatListItemIndex = mutableStateOf(0)
    private val _chatList = MutableStateFlow<List<AIChatListItem>>(emptyList())
    val chatList : StateFlow<List<AIChatListItem>> = _chatList.asStateFlow()

    fun update_chat_index(){
        chatListItemIndex.value += 1
    }
    fun add_welcome_chat(generatedText: MutableState<String>){
        _chatList.value = _chatList.value + AIChatListItem.TextItem(generating = mutableStateOf(true) , generatedText)
    }

    fun push_summ_chat_item(generatedText: MutableState<String>){
        chatListItemIndex.value += 1
        _chatList.value = _chatList.value + AIChatListItem.TextItem(generating = mutableStateOf(true) , generatedText)
    }

    fun update_chat_item(generatedText: MutableState<String>){
        if (_chatList.value.isNotEmpty() && _chatList.value[chatListItemIndex.value] is AIChatListItem.TextItem){
            (_chatList.value[chatListItemIndex.value] as AIChatListItem.TextItem).generating.value = false
            (_chatList.value[chatListItemIndex.value] as AIChatListItem.TextItem).text.value = generatedText.value

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
            repository.readTakedMSGToken().collect { isTaked ->
                Log.d(TAG , "isTaked " + isTaked)
                if (!isTaked){
                    Log.d(TAG , "msg token not taked")
                    requestMSGtoken()
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