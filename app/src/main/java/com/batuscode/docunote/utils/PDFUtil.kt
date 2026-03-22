package com.batuscode.docunote.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.batuscode.docunote.AiActivity
import com.batuscode.docunote.AiActivity.Companion.aiActivityViewModel
import com.batuscode.docunote.ImageOrganizerActivity
import com.batuscode.docunote.ImageOrganizerActivity.Companion.imageOrganizerActivityViewModel
import com.batuscode.docunote.MainActivity
import com.batuscode.docunote.PDFViewerActivity
import com.batuscode.docunote.R
import com.batuscode.docunote.utils.PDFConverter.Companion.mfilePath
import com.batuscode.pdfium.OffsetWrapper
import com.batuscode.pdfium.PathData
import com.batuscode.pdfium.icore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream

object PDFUtil {
    const val TAG = "PDFUtil"
    const val FILE_NAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    lateinit var core : icore

    fun init(context: Context){
        core = icore(context)
        core.nativeInitLibrary()
    }
    fun createSumPDFfile(sdtext : String , context: Context , uri: Uri){

        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
        context.contentResolver.takePersistableUriPermission(uri!!, takeFlags)
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
                prefix = context.getString(R.string.summarized_prefix_text)
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

        aiActivityViewModel.add_summed_item()

        val rr = core.createSummarizedDocument(utf16WithNull, context, fileName)

        rr.thenAccept { ip ->
            CoroutineScope(Dispatchers.Default).launch {
                if (ip.isNotEmpty()){
                    val uristr = getFileUriFromPath(context,ip).toString()
                    aiActivityViewModel.update_summed_item(fileName,uristr)
                    Log.d("pdfium" , "createSummarizedDocument filePath :: ${rr}")
                }
            }



        }
    }

    suspend fun saveSumPDFfile(pdfFileUri : String , fileName : String , context: Context) = withContext(Dispatchers.IO) {

        Log.d("summarized", "filePath in uri value :: ${pdfFileUri}")
        val contentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${fileName}") // Dosya adı
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf") // Dosya türü
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}") // Documents dizini
        }

// Dosyayı MediaStore'a kaydediyoruz
        val uri = contentResolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues)

        uri?.let { uri ->
            // Uri ile dosyayı açıyoruz
            val outputStream: OutputStream? = contentResolver.openOutputStream(uri)

            outputStream?.use { stream ->
                // Bu noktada, filePath'deki içeriği açıp yeni dosyaya yazabiliriz
                val inputStream = contentResolver.openInputStream(pdfFileUri.toUri()) // Kaynak dosya (summedItem.filePath)

                inputStream?.use { input ->
                    // Burada verileri bir dosyadan diğerine kopyalıyoruz
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (input.read(buffer).also { length = it } > 0) {
                        stream.write(buffer, 0, length)
                    }
                    stream.flush() // Verileri diske yazıyoruz

                    CoroutineScope(Dispatchers.Main).launch {
                        AiActivity.snackbarHostState.showSnackbar(
                            message = context.getString(R.string.saved_summed_doc_explain),
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                Log.d("summarized", "Dosya başarıyla kaydedildi!")
            }
        } ?: run {
            Log.e("summarized", "Dosya kaydedilemedi!")
        }
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

    suspend fun createPDFFromJPEG(imageUris : List<Uri> , context: Context , fileName: String) = withContext(Dispatchers.IO){
        val allBytes = mutableListOf<Byte>()
        val offsets = mutableListOf<Int>()
        val lengths = mutableListOf<Int>()

        imageUris.forEach { uri ->
            val bytes = setRotatedImageBytes(context, uri)
            offsets.add(allBytes.size)
            lengths.add(bytes!!.size)
            allBytes.addAll(bytes.toList())
        }

        val combinedByteArray = allBytes.toByteArray()
        val offsetArray = offsets.toIntArray()
        val lengthArray = lengths.toIntArray()

        val filepath = core.CreatePDFFromJPEG(combinedByteArray,offsetArray,lengthArray,context, fileName)

        filepath.thenAccept {
            CoroutineScope(Dispatchers.IO).launch {
                saveCreatedPDFFromJPEG(it , fileName, context)
            }
        }
        Log.d(TAG , "created file path ::: $filepath")
    }

    suspend fun setRotatedImageBytes(context: Context , uri: Uri) : ByteArray = withContext(Dispatchers.IO){
        val bytes = context.contentResolver.openInputStream(uri)?.readBytes()

        val exif = ExifInterface(bytes!!.inputStream())
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        val rotationMatrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotationMatrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotationMatrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotationMatrix.postRotate(270f)
            else -> return@withContext bytes // Düzgünse direkt dön
        }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, rotationMatrix, true
        )

        // Döndürülmüş bitmap'i yeniden JPEG'e encode et
        val outputStream = ByteArrayOutputStream()
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

        return@withContext outputStream.toByteArray()
    }

    suspend fun saveCreatedPDFFromJPEG(pdfFilePath : String , fileName : String , context: Context) = withContext(Dispatchers.IO){
        val pdfFileUri = getFileUriFromPath(context = context , filePath = pdfFilePath)

        Log.d(TAG, "JPEG-PDF filePath :: ${pdfFilePath}")
        Log.d(TAG, "JPEG-PDF fileUri :: ${pdfFileUri}")

        val contentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${fileName}") // Dosya adı
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf") // Dosya türü
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}") // Documents dizini
        }

// Dosyayı MediaStore'a kaydediyoruz
        val uri = contentResolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues)

        uri?.let { uri ->
            // Uri ile dosyayı açıyoruz
            val outputStream: OutputStream? = contentResolver.openOutputStream(uri)

            outputStream?.use { stream ->
                // Bu noktada, filePath'deki içeriği açıp yeni dosyaya yazabiliriz
                val inputStream = contentResolver.openInputStream(pdfFileUri) // Kaynak dosya (summedItem.filePath)

                inputStream?.use { input ->
                    // Burada verileri bir dosyadan diğerine kopyalıyoruz
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (input.read(buffer).also { length = it } > 0) {
                        stream.write(buffer, 0, length)
                    }
                    stream.flush() // Verileri diske yazıyoruz

                    CoroutineScope(Dispatchers.Main).launch {
                        ImageOrganizerActivity.snackbarHostState.showSnackbar(
                            message = MainActivity.context.getString(R.string.saved_summed_doc_explain),
                            duration = SnackbarDuration.Short
                        )
                    }

                    imageOrganizerActivityViewModel.addRecentlyReadedDoc(uri.toString(),fileName)

                }

                Log.d("summarized", "Dosya başarıyla kaydedildi!")
            }
        } ?: run {
            Log.e("summarized", "Dosya kaydedilemedi!")
        }
    }

    private var _PDFprocess : ((Boolean) -> Unit)? = null
    suspend fun mergePDFS(
        uriMap: MutableMap<Int, Uri> ,
        context: Context,
        fileName: String ,
        callback : (Boolean) -> Unit
    ) = withContext(Dispatchers.IO){
        _PDFprocess = callback
        var filePathMap : MutableMap<Int, String> = mutableMapOf()
        uriMap.map {
            val filePath = getFilePathFromUri(context,it.value,"temp_file${it.key}.pdf")
            filePathMap.put(it.key,filePath!!)
        }
        val contentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${fileName}.pdf") // Dosya adı
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf") // Dosya türü
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}") // Documents dizini
        }
        val uri = contentResolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues)
        uri?.let { mediaFileUri ->
            val fileUri = mediaFileUri.toString()

            val outputStream: OutputStream? = contentResolver.openOutputStream(mediaFileUri)

            val result = core.mergeDocument(filePathMap , outputStream , context)

            result.thenAccept {
                outputStream?.close()
                MainActivity.mainActivityViewModel.addRecentlyReadedDoc(fileUri,fileName)
                _PDFprocess?.invoke(it)

            }.exceptionally {
                it.printStackTrace()
                outputStream?.close() // hata durumunda da kapat
                null
            }
        }
    }

    suspend fun splitPDF(
        uri: Uri ,
        context: Context ,
        displayName: String,
        range: String ,
        callback: (Boolean) -> Unit
    ) = withContext(Dispatchers.IO){
        _PDFprocess = callback
        val filePath = getFilePathFromUri(context,uri,"temp_file.pdf")
        val contentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${displayName}.pdf") // Dosya adı
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf") // Dosya türü
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}") // Documents dizini
        }
        val uri = contentResolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues)
        uri?.let { it ->

            val outputStream: OutputStream? = contentResolver.openOutputStream(it)

            val result = core.splitDocument(filePath,outputStream,context , range)

            result.thenAccept { bool ->
                if (bool){
                    val fileUri = it.toString()
                    outputStream?.close()
                    MainActivity.mainActivityViewModel.addRecentlyReadedDoc(fileUri,displayName)
                    _PDFprocess?.invoke(bool)
                }
            }.exceptionally {
                it.printStackTrace()
                outputStream?.close() // hata durumunda da kapat
                null
            }
        }
    }

    suspend fun saveAsCopy(
        context: Context ,
        fileName: String ,
        uri: Uri ,
    ) = withContext(Dispatchers.IO){

        val pdfPageWidth = 595.0f
        val pdfPageHeight = 842.0f
        val canvasWidth = 1080.0f
        val canvasHeight = 1528.0f

        val scaleX = pdfPageWidth / canvasWidth
        val scaleY = pdfPageHeight / canvasHeight


        var PathMap: MutableMap<Int, List<PathData>> = mutableMapOf()

        CoroutineScope(Dispatchers.IO).launch {


            val fill = async {
                PDFViewerActivity.mpageStates.filter { (_, state) -> state.value.paths.isNotEmpty() }
                    .map { (index, state) ->
                        if (!state.value.paths.isEmpty()) {
                            Log.d(
                                "saveDocument",
                                "path is not empty to page :: " + index
                            )
                            val paths = state.value.paths.map { pathData ->
                                pathData.copy(
                                    path = pathData.path.map { point ->

                                        // Koordinatları tersine çevir, ardından ölçekle
                                        val transformedPoint =
                                            point.transformToBottomLeftOrigin(
                                                1528.0f,
                                                point.offset
                                            )
                                        val scaledPoint =
                                            point.scalePointForPDF(
                                                transformedPoint,
                                                scaleX,
                                                scaleY
                                            )
                                        OffsetWrapper(scaledPoint)
                                    }
                                )
                            }
                            PathMap.put(index, paths)
                            val color =
                                PDFViewerActivity.mpageStates[index]?.value?.selectedColor?.toArgb()
                            Log.d("saveDocument", "colorInt :: " + color)
                            //converter.drawPathToPage(context,PDFViewerActivity.muri,index,paths,color!!)
                        }
                    }
            }

            fill.await()

            Log.d("saveDocument", "map size :: " + PathMap.size)

            val filePath = getFilePathFromUri(context,uri,"temp_file.pdf")

            val result = core.drawPath(filePath , PathMap)

            result.thenAccept {

                CoroutineScope(Dispatchers.IO).launch {
                    val sourceFileUri = getFileUriFromPath(context,filePath!!)

                    val contentResolver = context.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, "(Copy) ${fileName}.pdf") // Dosya adı
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf") // Dosya türü
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}") // Documents dizini
                    }
                    val uri = contentResolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues)
                    uri.let { mediaFileUri ->
                        val inputStream = context.contentResolver.openInputStream(sourceFileUri)

                        inputStream.use { stream ->
                            val outputStream = context.contentResolver.openOutputStream(mediaFileUri!!)

                            outputStream.use { out ->
                                stream?.copyTo(out!!)
                            }
                        }
                    }

                    MainActivity.mainActivityViewModel.addRecentlyReadedDoc(uri.toString() ,"(Copy) ${fileName}")
                    PDFViewerActivity.snackbarHostState.showSnackbar(
                        message = "Copy saved." ,
                        duration = SnackbarDuration.Short
                    )
                }

            }

        }


    }


    suspend fun drawPathToEachPage(
        context: Context,
        uri: Uri,
        PathMap: MutableMap<Int, List<PathData>>
    ) = withContext(Dispatchers.IO){

        val filePath = getFilePathFromUri(context,uri,"temp_file.pdf")
        mfilePath = filePath!!

        //return core.drawPath(filePath ,PathMap)
    }


}
