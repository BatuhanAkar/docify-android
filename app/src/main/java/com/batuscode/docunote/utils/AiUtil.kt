package com.batuscode.docunote.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.mutableStateOf
import com.batuscode.docunote.AiActivity
import com.batuscode.docunote.AiActivity.Companion.aiActivityViewModel
import com.google.firebase.vertexai.GenerativeModel
import com.google.firebase.vertexai.type.FirebaseVertexAIException
import com.google.firebase.vertexai.type.ServerException
import com.google.firebase.vertexai.type.content
import com.google.firebase.vertexai.vertexAI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AiUtil {
    const val TAG = "AiUtil"
    lateinit var model : GenerativeModel

    suspend fun initGenerativeModel() = withContext(Dispatchers.IO){
        Log.d(TAG , "initializing model...")
        model = com.google.firebase.Firebase.vertexAI.generativeModel("gemini-2.0-flash-001")
    }

    fun welcomeKnowledge(){

        var loading = mutableStateOf(true)
        var generatedText = mutableStateOf("")

        aiActivityViewModel.start_knowledge_chat(generatedText)

        val prompt = content(
            role = "model" ,
            {
                text("Greet the user and request them to upload a PDF file to initiate a knowledge-based chat. Do not include any acknowledgments like \"Okay\" or \"I understand.\" Only provide the result of the task.")
            }
        )
        CoroutineScope(Dispatchers.IO).launch {
            model.generateContentStream(prompt).collect { chunk ->
                withContext(Dispatchers.Default){

                    Log.d(TAG , chunk.text ?: "")
                    generatedText.value = generatedText.value.plus(chunk.text)

                    if (loading.value){
                        loading.value = loading.value.not()

                        aiActivityViewModel.update_knowledge_chat_item(generatedText)
                    }
                }
            }
        }
    }

     fun welcoming(){
         var loading = mutableStateOf(true)
        var generatedText = mutableStateOf("")

         aiActivityViewModel.add_welcome_chat(generatedText)

        val prompt = content(
            role = "model" ,
            {
                text("You are a multilingual summarization assistant.Say hello the user, and want to pdf file upload directly for summarize.Do not include any acknowledgments like \"Okay\" or \"I understand.\" Only provide the result of the task.")
            }
        )
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


        aiActivityViewModel.push_summ_chat_item(generatedText , "model")

        val inputStream = context.contentResolver.openInputStream(pdfUri)

        if (inputStream != null){

            val prompt = content{
                inlineData(
                    bytes = inputStream.readBytes() ,
                    mimeType = "application/pdf"
                )
                text(" Summarize the following PDF content **in the same language** it is written in. Do not add explanations, confirmations, or any extra comments. Only return a clear, concise, and informative summary.")

            }

            try {
                CoroutineScope(Dispatchers.IO).launch {
                    model.generateContentStream(prompt).collect { chunk ->
                        Log.d(TAG , chunk.text ?: "")
                        generatedText.value = generatedText.value.plus(chunk.text)

                        if (loading.value){
                            loading.value = loading.value.not()

                            aiActivityViewModel.update_chat_item(generatedText)
                        }
                    }

                    PDFUtil.createSumPDFfile(generatedText.value , context , pdfUri)

                }

            }
            catch (cause : FirebaseVertexAIException) {
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

    fun knowledgeChat(pdfUri: Uri , context: Context , usergeneratedText: String){
        val chat = model.startChat()
        aiActivityViewModel.push_knowledge_chat_item(mutableStateOf(usergeneratedText) , "user")
        aiActivityViewModel.update_knowledge_chat_item(mutableStateOf(usergeneratedText))

        var loading = mutableStateOf(true)

        var generatedText = mutableStateOf("")

        aiActivityViewModel.push_knowledge_chat_item(generatedText , "model")

        context.contentResolver.openInputStream(pdfUri).use { stream ->
            stream?.let {
                val bytes = stream.readBytes()

                val prompt = content(
                    role = "user" ,
                    {
                        text(usergeneratedText)
                        inlineData(bytes , "application/pdf")
                    }
                )

                try {
                    CoroutineScope(Dispatchers.IO).launch {

                        chat.sendMessageStream(prompt).collectIndexed { index, value ->
                            generatedText.value = generatedText.value.plus(value.text)

                            if (loading.value){
                                loading.value = loading.value.not()
                                aiActivityViewModel.update_knowledge_chat_item(generatedText)
                            }
                        }

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
                } catch (e : Exception){
                    if (e.message?.contains("413") == true || e.message?.contains("Payload Too Large") == true){
                        CoroutineScope(Dispatchers.Default).launch{
                            AiActivity.snackbarHostState.showSnackbar(
                                message = "File size limit 20MB. Please select small file" ,
                                duration = SnackbarDuration.Short
                            )
                        }
                    } else {
                        CoroutineScope(Dispatchers.Default).launch{
                            AiActivity.snackbarHostState.showSnackbar(
                                message = "Please try again." ,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }

            }
        }

    }


}