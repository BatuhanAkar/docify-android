package com.batuscode.docunote.utils

import android.content.Context
import java.io.FileOutputStream
import java.io.IOException
import java.io.File

object ModelUtil {
    fun copyAssetToAppFiles(context: Context, assetFileName: String): String {
        // Hedef dosya yolunu oluştur (izin gerekmez)
        val targetFile = File(context.filesDir, assetFileName)

        // Dosya zaten varsa kopyalamayı atla (isteğe bağlı)
        if (targetFile.exists() && targetFile.length() > 0) {
            return targetFile.absolutePath
        }

        try {
            // Assets'ten okuma ve hedefe yazma
            context.assets.open(assetFileName).use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                    outputStream.flush()
                }
            }
            return targetFile.absolutePath
        } catch (e: IOException) {
            throw RuntimeException("Dosya kopyalanamadı: $assetFileName", e)
        }
    }
}