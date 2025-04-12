package com.batuscode.docunote.utils

import android.content.Context
import android.net.Uri
import java.io.File
import kotlin.io.outputStream

object WordDocUtil {
    fun getWordDocFromUri(context: Context, uri: Uri , fileName:String): File? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("temp" , ".docx" , context.cacheDir)
            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return tempFile
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

     fun loadDocument(wrodTempFile: File?){

    }
}