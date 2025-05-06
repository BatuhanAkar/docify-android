package com.batuscode.docunote.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.batuscode.docunote.AiActivity.Companion.aiActivityViewModel
import com.google.firebase.vertexai.GenerativeModel
import com.google.firebase.vertexai.type.FirebaseVertexAIException
import com.google.firebase.vertexai.type.ServerException
import com.google.firebase.vertexai.type.content
import com.google.firebase.vertexai.vertexAI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AiUtil {
    const val TAG = "AiUtil"
    lateinit var model : GenerativeModel

    suspend fun initGenerativeModel() = withContext(Dispatchers.IO){
        Log.d(TAG , "initializing model...")
        model = com.google.firebase.Firebase.vertexAI.generativeModel("gemini-2.0-flash")
    }

     fun welcoming(){
         var loading = mutableStateOf(true)
        var generatedText = mutableStateOf("")

         aiActivityViewModel.add_welcome_chat(generatedText)

        val prompt = content{
            text("can you summarize pdf file?")
        }
         CoroutineScope(Dispatchers.IO).launch {
             model.generateContentStream(prompt).collect { chunk ->
                 withContext(Dispatchers.Default){

                     Log.d(TAG , chunk.text ?: "")
                     generatedText.value = generatedText.value.plus(chunk.text)

                     if (loading.value){
                         loading.value = loading.value.not()

                         aiActivityViewModel.update_chat_item(generatedText)
                     }
                 }
             }
         }

    }
    fun sumPDF(pdfUri: Uri , context: Context){
        var loading = mutableStateOf(true)

        var generatedText = mutableStateOf("")

        aiActivityViewModel.push_summ_chat_item(generatedText)

        context.contentResolver.openInputStream(pdfUri).use { stream ->
            stream?.let {
                val bytes = stream.readBytes()

                val prompt = content {
                    inlineData(bytes , "application/pdf")
                    text("summarize the document")
                }

                try {
                    CoroutineScope(Dispatchers.IO).launch {
                        model.generateContentStream(prompt).collect { chunk ->
                            withContext(Dispatchers.Default){
                                Log.d(TAG , chunk.text ?: "")
                                generatedText.value = generatedText.value.plus(chunk.text)

                                if (loading.value){
                                    loading.value = loading.value.not()

                                    aiActivityViewModel.update_chat_item(generatedText)
                                }
                            }
                        }
                        aiActivityViewModel.update_chat_index()
                        PDFUtil.createSumPDFfile(generatedText.value , context , pdfUri)

                    }

                } catch (cause : FirebaseVertexAIException) {
                    when(cause){
                        is ServerException -> {
                            Log.e(TAG , cause.message , cause.cause)
                        }

                        else -> {
                            Log.e(TAG , cause.message , cause.cause)
                        }
                    }
                }

            }
        }
    }


}