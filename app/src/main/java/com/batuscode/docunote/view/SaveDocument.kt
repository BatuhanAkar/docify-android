package com.batuscode.docunote.view

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.batuscode.docunote.CreatePDFActivity
import com.batuscode.docunote.MainActivity
import com.batuscode.docunote.PDFViewerActivity
import com.batuscode.docunote.R
import com.batuscode.docunote.model.Folder
import com.batuscode.docunote.utils.PDFConverter
import com.batuscode.docunote.utils.PDFCreator
import com.batuscode.pdfium.OffsetWrapper
import com.batuscode.pdfium.PathData
import com.batuscode.pdfium.icore
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveDocument(create:Boolean, onDismissRequest: () -> Unit, textStates: SnapshotStateMap<Int, RichTextState>?){
    val modalSheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        sheetState = modalSheetState,
        onDismissRequest = {
            onDismissRequest()
        }
    ) {
        SaveDocContent(create,textStates)
    }
}

@Composable
fun SaveDocContent(create: Boolean,textStates: SnapshotStateMap<Int, RichTextState>?){

    val context = LocalContext.current

    var text by remember {
        mutableStateOf("")
    }

    var newFolder = remember {
        mutableStateOf(false)
    }

    var FolderName by remember {
        mutableStateOf("")
    }

    var converter = remember {
        PDFConverter(context)
    }

    var streamSaved = remember {
        mutableStateOf(false)
    }

    var fileSaved = remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    if (newFolder.value){
        CustomDialog(
        onDismissRequest = { newFolder.value = newFolder.value.not() },
        onConfirm = {
            FolderName = it // Kullanıcıdan gelen veriyi kaydet
            newFolder.value = newFolder.value.not() // Dialogu kapat
        }
    )
    }

    Box (
        contentAlignment = Alignment.Center ,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()

        ) {
            FilledTonalButton(
                onClick = {
                    newFolder.value = newFolder.value.not()
            } ,
                modifier = Modifier
                    .align(Alignment.Start)
            ) {

                Image(painter = painterResource(R.drawable.baseline_create_new_folder_24), contentDescription = "")
                Text(text = stringResource(R.string.tofolder))
            }
            TextField(
                leadingIcon = {
                    if (!FolderName.isEmpty()){
                        Text(text = "${FolderName}/")
                    }
                },
                value = text ,
                onValueChange = {
                    text = it
                } ,
                placeholder = {
                    Text(text = stringResource(R.string.documentname))
                } ,

                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.LightGray.copy(0.5f) ,
                    focusedContainerColor = Color.LightGray.copy(0.5f),
                    unfocusedIndicatorColor = Color.Transparent ,
                    focusedIndicatorColor = Color.Transparent ,

                ),
                modifier = Modifier
                    .fillMaxWidth()

            )

            Button(
                onClick = {

                    if (!FolderName.isEmpty()){

                        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) , "${FolderName}")

                        dir.mkdirs()
                        val file = File(dir, "${text}.pdf")
                        val filePath = File(dir, "${text}.pdf").absolutePath


                        val folder = Folder(1 , name = FolderName , R.drawable.folder_icon_4_01)
                        MainActivity._appViewModel.addFolder(folder)

                        val creat = PDFCreator(context)
                       // creat.savePDF( text , file = File(dir , "${text}.pdf") , CreatePDFActivity.pagesContainer , CreatePDFActivity.cpageStates , context.contentResolver)

                        if (create){

                            val tempFile = File(context.cacheDir, "temp_file.pdf")
                            val mfilePath = tempFile.absolutePath
                            val contentResolver = context.contentResolver
                            val contentValues = ContentValues().apply {
                                put(MediaStore.MediaColumns.DISPLAY_NAME, "${text}") // Dosya adı
                                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf") // Dosya türü
                                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}") // Documents dizini
                            }
                            val uri = contentResolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues)

                            Log.d("page count" , "in save" + CreatePDFActivity.docptr)
                            var ptr = CreatePDFActivity.docptr

                            Log.d("page count" , "in save" + ptr)
                            scope.launch{
                                textStates?.forEach { (index, state) ->

                                    val text = state.toMarkdown()
                                    val utf16Bytes = text.toByteArray(Charsets.UTF_16LE)
                                    Log.d("new character", "utf-16 :: " + utf16Bytes)
                                    Log.d("new character", "utf-16 :: " + utf16Bytes.joinToString(", ") { it.toString() })
                                    MainActivity.mainicore.addText(CreatePDFActivity.docptr, index, utf16Bytes, 50f, 800f, 12f, 14f)
                                    Log.d("new character" , "index :: " + index + " text :: " + state.toMarkdown())
                                }

                                MainActivity.fileManager.addDocument(context = context , uri = uri.toString(), fileName = text)

                                val file2 = com.batuscode.docunote.utils.File(uri.toString() , text)
                                MainActivity._appViewModel.addRecentlyFile(file2)
                                uri?.let {
                                    val outputStream: OutputStream? = contentResolver.openOutputStream(it)
                                    outputStream?.use { stream ->
                                        MainActivity.mainicore.saveDocumentAsStream(CreatePDFActivity.docptr , stream , context)
                                    }
                                }
                                val saveOk = MainActivity.mainicore.saveDocument(CreatePDFActivity.docptr,filePath)

                                if (saveOk){
                                    CreatePDFActivity.pdfActivity.onBackPressedDispatcher.onBackPressed()
                                }
                            }
                        }
                        else {

                            // save as copy

                            scope.launch{
                                val pdfPageWidth = 595.0f
                                val pdfPageHeight = 842.0f
                                val canvasWidth = 1080.0f
                                val canvasHeight = 1528.0f

                                val scaleX = pdfPageWidth / canvasWidth
                                val scaleY = pdfPageHeight / canvasHeight
                                var PathMap : MutableMap<Int, List<PathData>> = mutableMapOf()
                                scope.launch{

                                    PDFViewerActivity.mpageStates.filter { (index , state) -> state.value.paths.isNotEmpty() }
                                        .map { (index,state)->
                                            if (!state.value.paths.isEmpty()){
                                                Log.d("saveDocument" , "path is not empty to page :: " + index)
                                                val paths = state.value.paths.map { pathData ->
                                                    pathData.copy(
                                                        path = pathData.path.map { point ->

                                                            // Koordinatları tersine çevir, ardından ölçekle
                                                            val transformedPoint = point.transformToBottomLeftOrigin(1528.0f,point.offset)
                                                            val scaledPoint = point.scalePointForPDF(transformedPoint, scaleX, scaleY)
                                                            OffsetWrapper(scaledPoint)
                                                        }
                                                    )
                                                }
                                                PathMap.put(index,paths)
                                                val color = PDFViewerActivity.mpageStates[index]?.value?.selectedColor?.toArgb()
                                                Log.d("saveDocument" , "colorInt :: " + color)
                                                //converter.drawPathToPage(context,PDFViewerActivity.muri,index,paths,color!!)
                                            }
                                        }

                                    var ok = converter.drawPathToPage(context,PDFViewerActivity.muri,PathMap)

                                    if (ok){
                                        val saved = saveTempFileToDocuments(null,false,context,PDFConverter.mfilePath,text)
                                        if (saved){
                                            PDFViewerActivity.activity.onBackPressedDispatcher.onBackPressed()
                                        }
                                    }
                                    // val saveOk = MainActivity.mainicore.saveDocument(PDFConverter.midoc.mNativeDocPtr,filePath)

                                    /*creat.saveDrawingsToPDF(text , file = File(dir , "${text}.pdf") ,
                                        PDFViewerActivity.mpageStates ,context.contentResolver , context)*/
                                }
                            }
                          /*  creat.saveDrawingsToPDF(text,file = File(dir , "${text}.pdf") ,
                                PDFViewerActivity.mrendererPages ,
                                PDFViewerActivity.mpageStates , context.contentResolver)*/
                        }


                    }
                    else {
                        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                        val filePath = File(dir, "${text}.pdf").absolutePath

                        val contentResolver = context.contentResolver
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, "${text}") // Dosya adı
                            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf") // Dosya türü
                            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}") // Documents dizini
                        }
                        val uri = contentResolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues)
                        // create page
                        if (create){
                            val tempFile = File(context.cacheDir, "temp_file.pdf")
                            val mfilePath = tempFile.absolutePath
                            Log.d("page count" , "in save" + CreatePDFActivity.docptr)
                            var ptr = CreatePDFActivity.docptr

                            Log.d("page count" , "in save" + ptr)
                            scope.launch{
                                textStates?.forEach { (index, state) ->

                                    val text = state.toMarkdown()
                                    val cleanedText = text.replace("<br>", "")

                                    val utf16Bytes = cleanedText.toByteArray(Charsets.UTF_16LE)
                                    Log.d("new character", "utf-16 :: " + utf16Bytes)
                                    Log.d("new character", "utf-16 :: " + utf16Bytes.joinToString(", ") { it.toString() })
                                    MainActivity.mainicore.addText(CreatePDFActivity.docptr, index, utf16Bytes, 50f, 800f, 12f, 14f)
                                    Log.d("new character" , "index :: " + index + " text :: " + state.toMarkdown())
                                }

                                MainActivity.fileManager.addDocument(context = context , uri = uri.toString(), fileName = text)

                                val file2 = com.batuscode.docunote.utils.File(uri.toString() , text)
                                MainActivity._appViewModel.addRecentlyFile(file2)
                                uri?.let {

                                    val outputStream: OutputStream? = contentResolver.openOutputStream(it)
                                    outputStream?.use { stream ->
                                       streamSaved.value = MainActivity.mainicore.saveDocumentAsStream(CreatePDFActivity.docptr , stream , context)
                                    }
                                }
                                fileSaved.value = MainActivity.mainicore.saveDocument(CreatePDFActivity.docptr,filePath)

                                if (fileSaved.value && streamSaved.value){
                                    CreatePDFActivity.pdfActivity.onBackPressedDispatcher.onBackPressed()
                                }
                            }
                        }
                        else {

                            // save as copy
                            // edit page
                             /* creat.saveDrawingsToPDF(text , file = File(dir , "${text}.pdf") ,
                                  PDFViewerActivity.mrendererPages ,
                                  PDFViewerActivity.mpageStates , context.contentResolver)*/
                            val dpi = context.getResources().getDisplayMetrics().densityDpi
                            val pdfPageWidth = 595.0f
                            val pdfPageHeight = 842.0f
                            val canvasWidth = 1080.0f
                            val canvasHeight = 1528.0f

                            val scaleX = pdfPageWidth / canvasWidth
                            val scaleY = pdfPageHeight / canvasHeight

                            var PathMap : MutableMap<Int, List<PathData>> = mutableMapOf()
                            scope.launch{

                                val fill = async{
                                    PDFViewerActivity.mpageStates.filter { (index , state) -> state.value.paths.isNotEmpty() }
                                        .map { (index,state)->
                                            if (!state.value.paths.isEmpty()){
                                                Log.d("saveDocument" , "path is not empty to page :: " + index)
                                                val paths = state.value.paths.map { pathData ->
                                                    pathData.copy(
                                                        path = pathData.path.map { point ->

                                                            // Koordinatları tersine çevir, ardından ölçekle
                                                            val transformedPoint = point.transformToBottomLeftOrigin(1528.0f,point.offset)
                                                            val scaledPoint = point.scalePointForPDF(transformedPoint, scaleX, scaleY)
                                                            OffsetWrapper(scaledPoint)
                                                        }
                                                    )
                                                }
                                                PathMap.put(index,paths)
                                                val color = PDFViewerActivity.mpageStates[index]?.value?.selectedColor?.toArgb()
                                                Log.d("saveDocument" , "colorInt :: " + color)
                                                //converter.drawPathToPage(context,PDFViewerActivity.muri,index,paths,color!!)
                                            }
                                        }
                                }

                                fill.await()

                                Log.d("saveDocument" , "map size :: " + PathMap.size)
                                var ok = converter.drawPathToPage(context,PDFViewerActivity.muri,PathMap)

                                Log.d("saveDocument" , "ok :: ${ok}")
                                if (ok){
                                    val saved = saveTempFileToDocuments(null,false,context,PDFConverter.mfilePath,text)

                                    Log.d("saveDocument" , "saved :: ${saved}")
                                    if (saved){
                                        PDFViewerActivity.activity.onBackPressedDispatcher.onBackPressed()
                                    }
                                }

                               // val saveOk = MainActivity.mainicore.saveDocument(PDFConverter.midoc.mNativeDocPtr,filePath)

                                /*creat.saveDrawingsToPDF(text , file = File(dir , "${text}.pdf") ,
                                    PDFViewerActivity.mpageStates ,context.contentResolver , context)*/
                            }

                        }



                    }

                } ,
                modifier = Modifier
                    .width(150.dp)
                    .padding(8.dp)
            ) {
                Text(text = stringResource(R.string.ok))
            }
        }
    }
}


fun saveTempFileToDocuments(mfile: File? , toFolder:Boolean , context: Context, tempFilePath: String, text:String): Boolean {
    try {
        var filename = text
        val tempFile = File(tempFilePath)
        if (!tempFile.exists()) {
            return false
        }


        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        var file = File(dir, "${filename}.pdf")

        // Yeni hedef konumu (Documents dizini)
       // val documentsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "temp_file.pdf")

        // Eğer hedef dosya mevcutsa sil
        if (file.exists()) {
            filename = filename + "(1)"
            file = File(dir, "${filename}.pdf")
        }



        val contentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${filename}") // Dosya adı
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf") // Dosya türü
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}") // Documents dizini
        }
        val uri = contentResolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues)

        uri?.let {

            MainActivity.fileManager.addDocument(context = context, uri = it.toString(), fileName = filename!!)
            val file = com.batuscode.docunote.utils.File(it.toString(), filename)
            MainActivity._appViewModel.addRecentlyFile(file)
            val outputStream: OutputStream? = contentResolver.openOutputStream(it)

            tempFile.inputStream().use { input ->
                outputStream?.use { stream ->
                    input.copyTo(stream)
                }
            }
        }
        // Dosyayı yeni dizine taşımak
        if (toFolder){
            tempFile.copyTo(mfile!!, overwrite = true)
        } else {
            tempFile.copyTo(file, overwrite = true)
        }

        // Yeni dosyanın yolu
        if (file.isAbsolute){
            return true
        } else {
            return false
        }

    } catch (e: IOException) {
        e.printStackTrace()
    }
    return false
}


@Composable
fun CustomDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var textFieldValue by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnClickOutside = false) // Dışına tıklayınca kapanmasın
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                // TextField for user input
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    label = { Text(text = stringResource(R.string.foldername)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons row
                Row {
                    Button(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onConfirm(textFieldValue) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.ok))
                    }
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewSaveDocument(){
    DocuNoteTheme {
       // SaveDocContent(false)
    }
}