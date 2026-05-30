package com.batuscode.docunote.view

import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.batuscode.docunote.CreatePDFActivity
import com.batuscode.docunote.MainActivity
import com.batuscode.docunote.R
import com.batuscode.docunote.model.Folder
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.utils.PDFConverter
import com.batuscode.docunote.utils.PDFUtil
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlinx.coroutines.launch
import java.io.File
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveDocument(
    create: Boolean,
    onDismissRequest: () -> Unit,
    textStates: SnapshotStateMap<Int, RichTextState>?
) {
    val modalSheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        sheetState = modalSheetState,
        onDismissRequest = {
            onDismissRequest()
        }
    ) {
        SaveDocContent(create, textStates)
    }
}

@Composable
fun SaveDocContent(create: Boolean, textStates: SnapshotStateMap<Int, RichTextState>?) {

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

    remember {
        PDFConverter(context)
    }

    var streamSaved = remember {
        mutableStateOf(false)
    }

    var fileSaved = remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    if (newFolder.value) {
        CustomDialog(
            onDismissRequest = { newFolder.value = newFolder.value.not() },
            onConfirm = {
                FolderName = it // Kullanıcıdan gelen veriyi kaydet
                newFolder.value = newFolder.value.not() // Dialogu kapat
            }
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()

        ) {
           /* FilledTonalButton(
                onClick = {
                    newFolder.value = newFolder.value.not()
                },
                modifier = Modifier
                    .align(Alignment.Start)
            ) {

                Image(
                    painter = painterResource(R.drawable.baseline_create_new_folder_24),
                    contentDescription = ""
                )
                Text(text = stringResource(R.string.tofolder))
            } */
            TextField(
               /* leadingIcon = {
                    if (!FolderName.isEmpty()) {
                        Text(text = "${FolderName}/")
                    }
                },*/
                value = text,
                onValueChange = {
                    text = it
                },
                placeholder = {
                    Text(text = stringResource(R.string.documentname))
                },

                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.LightGray.copy(0.5f),
                    focusedContainerColor = Color.LightGray.copy(0.5f),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,

                    ),
                modifier = Modifier
                    .fillMaxWidth()

            )

            Button(
                onClick = {

                    if (!FolderName.isEmpty()) {

                        val dir = File(
                            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                            "${FolderName}"
                        )

                        dir.mkdirs()
                        File(dir, "${text}.pdf")
                        val filePath = File(dir, "${text}.pdf").absolutePath


                        val folder = Folder(1, name = FolderName, R.drawable.folder_icon_4_01 , emptyList())
                        MainActivity._appViewModel.addFolder(folder)

                        // creat.savePDF( text , file = File(dir , "${text}.pdf") , CreatePDFActivity.pagesContainer , CreatePDFActivity.cpageStates , context.contentResolver)

                        if (create) {

                            val tempFile = File(context.cacheDir, "temp_file.pdf")
                            tempFile.absolutePath
                            val contentResolver = context.contentResolver
                            val contentValues = ContentValues().apply {
                                put(MediaStore.MediaColumns.DISPLAY_NAME, "${text}") // Dosya adı
                                put(
                                    MediaStore.MediaColumns.MIME_TYPE,
                                    "application/pdf"
                                ) // Dosya türü
                                put(
                                    MediaStore.MediaColumns.RELATIVE_PATH,
                                    "${Environment.DIRECTORY_DOCUMENTS}"
                                ) // Documents dizini
                            }
                            val uri = contentResolver.insert(
                                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                                contentValues
                            )

                            Log.d("page count", "in save" + CreatePDFActivity.docptr)
                            var ptr = CreatePDFActivity.docptr

                            Log.d("page count", "in save" + ptr)
                            scope.launch {
                                textStates?.forEach { (index, state) ->

                                    val text = state.toMarkdown()
                                    val utf16Bytes = text.toByteArray(Charsets.UTF_16LE)
                                    Log.d("new character", "utf-16 :: " + utf16Bytes)
                                    Log.d(
                                        "new character",
                                        "utf-16 :: " + utf16Bytes.joinToString(", ") { it.toString() })
                                    PDFUtil.core.addText(
                                        CreatePDFActivity.docptr,
                                        index,
                                        utf16Bytes,
                                        50f,
                                        800f,
                                        12f,
                                        14f
                                    )
                                    Log.d(
                                        "new character",
                                        "index :: " + index + " text :: " + state.toMarkdown()
                                    )
                                }

                                /*MainActivity.fileManager.addDocument(context = context , uri = uri.toString(), fileName = text)
                                val file2 = com.batuscode.docunote.utils.File(uri.toString() , text)
                                MainActivity._appViewModel.addRecentlyFile(file2)*/
                                MainActivity.mainActivityViewModel.addRecentlyReadedDoc(
                                    fileUri = uri.toString(),
                                    fileName = text
                                )

                                uri?.let {
                                    val outputStream: OutputStream? =
                                        contentResolver.openOutputStream(it)
                                    outputStream?.use { stream ->
                                        PDFUtil.core.saveDocumentAsStream(
                                            CreatePDFActivity.docptr,
                                            stream,
                                            context
                                        )
                                    }
                                }
                                val saveOk =
                                    PDFUtil.core.saveDocument(CreatePDFActivity.docptr, filePath)

                                if (saveOk) {
                                    CreatePDFActivity.pdfActivity.onBackPressedDispatcher.onBackPressed()
                                }
                            }
                        }


                    }
                    else {
                        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                        val filePath = File(dir, "${text}.pdf").absolutePath

                        val contentResolver = context.contentResolver
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, "${text}") // Dosya adı
                            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf") // Dosya türü
                            put(
                                MediaStore.MediaColumns.RELATIVE_PATH,
                                "${Environment.DIRECTORY_DOCUMENTS}"
                            ) // Documents dizini
                        }
                        val uri = contentResolver.insert(
                            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                            contentValues
                        )
                        // create page
                        if (create) {
                            val tempFile = File(context.cacheDir, "temp_file.pdf")
                            tempFile.absolutePath
                            Log.d("page count", "in save" + CreatePDFActivity.docptr)
                            var ptr = CreatePDFActivity.docptr

                            Log.d("page count", "in save" + ptr)
                            scope.launch {
                                textStates?.forEach { (index, state) ->

                                    val text = state.toMarkdown()
                                    val cleanedText = text.replace("<br>", "")

                                    val utf16Bytes = cleanedText.toByteArray(Charsets.UTF_16LE)
                                    Log.d("new character", "utf-16 :: " + utf16Bytes)
                                    Log.d(
                                        "new character",
                                        "utf-16 :: " + utf16Bytes.joinToString(", ") { it.toString() })
                                    PDFUtil.core.addText(
                                        CreatePDFActivity.docptr,
                                        index,
                                        utf16Bytes,
                                        50f,
                                        800f,
                                        12f,
                                        14f
                                    )
                                    Log.d(
                                        "new character",
                                        "index :: " + index + " text :: " + state.toMarkdown()
                                    )
                                }

                                /*MainActivity.fileManager.addDocument(context = context , uri = uri.toString(), fileName = text)

                                val file2 = com.batuscode.docunote.utils.File(uri.toString() , text)
                                MainActivity._appViewModel.addRecentlyFile(file2)*/

                                MainActivity.mainActivityViewModel.addRecentlyReadedDoc(
                                    fileUri = uri.toString(),
                                    fileName = text
                                )
                                uri?.let {

                                    val outputStream: OutputStream? =
                                        contentResolver.openOutputStream(it)
                                    outputStream?.use { stream ->
                                        streamSaved.value = PDFUtil.core.saveDocumentAsStream(
                                            CreatePDFActivity.docptr,
                                            stream,
                                            context
                                        )
                                    }
                                }
                                fileSaved.value =
                                    PDFUtil.core.saveDocument(CreatePDFActivity.docptr, filePath)

                                if (fileSaved.value && streamSaved.value) {
                                    CreatePDFActivity.pdfActivity.onBackPressedDispatcher.onBackPressed()
                                }
                            }
                        }


                    }

                },
                modifier = Modifier
                    .width(150.dp)
                    .padding(8.dp)
            ) {
                Text(text = stringResource(R.string.ok))
            }
        }
    }
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
fun PreviewSaveDocument() {
    DocuNoteTheme {
        // SaveDocContent(false)
    }
}