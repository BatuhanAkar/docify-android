package com.batuscode.docunote

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.batuscode.docunote.DocifyAI.Companion.chatList
import com.batuscode.docunote.DocifyAI.Companion.chatListItemIndex
import com.batuscode.docunote.DocifyAI.Companion.showselectdocumentbutton
import com.batuscode.docunote.MainActivity.Companion.llmInference
import com.batuscode.docunote.model.AIChatListItem
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.ProgressListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SystemPrompts {
    suspend fun initInference(newModelPath: String , context: Context): LlmInference{
        return withContext(Dispatchers.IO){
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(newModelPath) // Use the path we just got
                .setPreferredBackend(LlmInference.Backend.CPU)
                .setMaxTokens(1000)
                .setMaxTopK(40)
                .build()

            return@withContext LlmInference.createFromOptions(context, options)
        }
    }
    fun welcoming(){
        var generatedText = mutableStateOf("")
        chatList.add(
            chatListItemIndex.value , AIChatListItem.TextItem(generating = mutableStateOf(true) ,
                generatedText
            ))

        val welcomeText = "Write exactly this text: 'Send the document, get the summary – how does that sound?'"
        val prompt = welcomeText
        llmInference.generateResponseAsync(prompt, object : ProgressListener<String>{
            override fun run(partialResult: String?, done: Boolean) {
                Log.d("summarized" , "value :: ${done}")
                generatedText.value = generatedText.value.plus(partialResult)

                if (chatList.isNotEmpty() && chatList[chatListItemIndex.value] is AIChatListItem.TextItem){
                    if (!done){
                        (chatList[chatListItemIndex.value] as AIChatListItem.TextItem).generating.value = done
                    }
                    (chatList[chatListItemIndex.value] as AIChatListItem.TextItem).text.value = generatedText.value
                }
                if (done){
                    showselectdocumentbutton.value = done
                }
            }

        })
    }
}