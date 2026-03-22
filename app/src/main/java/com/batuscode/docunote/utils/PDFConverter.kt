package com.batuscode.docunote.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Debug
import android.util.Log
import androidx.core.content.FileProvider
import com.batuscode.pdfium.PathData
import com.batuscode.pdfium.PdfDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PDFConverter(private val context: Context) {
    companion object{
        lateinit var midoc: PdfDocument
        lateinit var mfilePath : String
    }

    fun logMemoryUsage() {
        val heapSize = Debug.getNativeHeapAllocatedSize()
        Log.d("MemoryUsage", "Native heap size: $heapSize bytes")
    }
    suspend fun renderPage(
        context: Context,
        uri: Uri,
        pageIndex: Int,
        scaleFactor: Float = 1.0f // Add a scale factor for downscaling
    ): Bitmap? = withContext(Dispatchers.IO) {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->

            val idoc = PDFUtil.core.newDocument(descriptor)
            midoc = idoc

            PDFUtil.core.openPage(idoc, pageIndex)
            Log.d("PDFConverter", "Document pointer: ${idoc.mNativePagesPtr}")

            val pageWidth = PDFUtil.core.getPageWidth(idoc, pageIndex)
            val pageHeight = PDFUtil.core.getPageHeight(idoc, pageIndex)

            // Downscale the bitmap to match the screen size
            val scaledWidth = (pageWidth * scaleFactor).toInt()
            val scaledHeight = (pageHeight * scaleFactor).toInt()

            val bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.RGB_565)

            PDFUtil.core.renderPageBitmap(
                idoc,
                bitmap,
                pageIndex,
                0, 0,
                scaledWidth, scaledHeight
            )

            logMemoryUsage()
            return@withContext bitmap
        }
        return@withContext null
    }
    suspend fun getFileUriFromPath(context: Context, filePath: String): Uri = withContext(
        Dispatchers.IO)
    {
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
    fun getFilePathFromUri(context: Context, uri: Uri , fileName:String): String? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(context.cacheDir, fileName)
            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return tempFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    suspend fun drawPathToPage(
        context: Context,
        uri: Uri,
        PathMap: MutableMap<Int, List<PathData>>
    ): Boolean{
        val filePath = getFilePathFromUri(context,uri,"temp_file.pdf")
        mfilePath = filePath!!

        return false

        //return PDFUtil.core.drawPath(filePath ,PathMap)

        /*context.contentResolver.openFileDescriptor(uri , "r")?.use { descriptor ->
            Log.d("drawPathToPage" , "uri :: " + uri)



            val idoc = MainActivity.mainicore.newDocument(descriptor)
            Log.d("drawPathToPage" , "docPtr :: " + idoc.mNativeDocPtr + " pageIndex :: " + pageIndex)
            MainActivity.mainicore.openPage(idoc, pageIndex)

            MainActivity.mainicore.drawPath(idoc.mNativeDocPtr ,pageIndex , pathData)

        }*/
    }




}