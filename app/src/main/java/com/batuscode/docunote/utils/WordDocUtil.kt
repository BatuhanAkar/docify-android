package com.batuscode.docunote.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
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
        Thread {
            try {
                val wordMLPackage = WordprocessingMLPackage.load(wrodTempFile)
                val mainDocumentPart = wordMLPackage.mainDocumentPart
                val text = mainDocumentPart.content.joinToString(" ") { it.toString() }

                Log.d("WordDocument" , "main document part :: ${text}")

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}