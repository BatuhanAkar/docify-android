package com.batuscode.docunote.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.content.FileProvider
import com.batuscode.docunote.AiActivity
import com.batuscode.docunote.AiActivity.Companion.aiActivityViewModel
import com.batuscode.docunote.model.AIChatListItem
import com.batuscode.pdfium.icore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

object PDFUtil {
    const val TAG = "PDFUtil"
    lateinit var core : icore
    lateinit var generatedFileUri : Uri

    fun init(context: Context){
        core = icore(context)
    }
    suspend fun createSumPDFfile(sdtext : String , context: Context , uri: Uri) = withContext(Dispatchers.IO){
        val cursor = context.contentResolver.query(
            uri!! ,
            arrayOf(OpenableColumns.DISPLAY_NAME) ,
            null ,
            null ,
            null
        )
        var prefix: String = ""
        var displayName: String = ""
        var dotIndex: Int
        var fileNameWithoutExtension : String
        var fileName : String = ""
        cursor?.use {
            if (it.moveToFirst()){
                prefix = context.getString(com.batuscode.docunote.R.string.summarized_prefix_text)
                displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))



                dotIndex = displayName.lastIndexOf('.')
                fileNameWithoutExtension = if (dotIndex != -1) displayName.substring(0, dotIndex) else displayName
                fileName = "(${prefix}) ${fileNameWithoutExtension}"
            }
        }

        Log.d("newuri" , displayName)

        val utf16byte = sdtext.toByteArray(Charsets.UTF_16LE)

// NULL terminator ekleyin (PDFium bekliyor olabilir)
        val utf16WithNull = utf16byte + byteArrayOf(0x00, 0x00)

        val rr = core.createSummarizedDocument(utf16WithNull, context, fileName)

        rr.thenAccept { it ->
            CoroutineScope(Dispatchers.IO).launch {
                if (it.isNotEmpty()){
                    generatedFileUri = getFileUriFromPath(context,it)
                    if (aiActivityViewModel.chatList.value.isNotEmpty() && aiActivityViewModel.chatList.value[aiActivityViewModel.chatListItemIndex.value] is AIChatListItem.SumItem){
                        // when summarization finish set generating false...
                        (aiActivityViewModel.chatList.value[aiActivityViewModel.chatListItemIndex.value] as AIChatListItem.SumItem).fileName.value = fileName
                        (aiActivityViewModel.chatList.value[aiActivityViewModel.chatListItemIndex.value] as AIChatListItem.SumItem).filePath.value = it
                        (aiActivityViewModel.chatList.value[aiActivityViewModel.chatListItemIndex.value] as AIChatListItem.SumItem).generating.value = true
                    }
                    Log.d("pdfium" , "createSummarizedDocument filePath :: ${rr}")
                }
            }



        }
    }

    suspend fun getFileUriFromPath(context: Context, filePath: String): Uri = withContext(
        Dispatchers.IO) {
        val file = File(filePath)

        // Eğer file mevcutsa ve okunabilir yazılabilir ise
        if (file.exists() && file.canRead()) {
            // FileProvider ile URI'yi alıyoruz
            return@withContext FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider", // Bu, AndroidManifest.xml içinde tanımladığınız authority olmalı
                file
            )
        } else {
            throw Exception("File not accessible")
        }
    }
}