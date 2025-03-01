package com.batuscode.docunote.view

import android.content.Intent
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.batuscode.docunote.CreatePDFActivity
import com.batuscode.docunote.MainActivity
import com.batuscode.docunote.PDFViewerActivity
import com.batuscode.docunote.R
import com.batuscode.docunote.model.Folder
import com.batuscode.docunote.utils.PDFCreator
import com.batuscode.docunote.viewmodel.CreatePDFActivityViewModel
import com.batuscode.pdfium.icore
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlinx.coroutines.launch
import java.io.File
import java.nio.charset.Charset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveDocument(create:Boolean , onDismissRequest: () -> Unit,  textStates: SnapshotStateMap<Int, RichTextState>){
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
fun SaveDocContent(create: Boolean,textStates: SnapshotStateMap<Int, RichTextState>){

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


                        val folder = Folder(1 , name = FolderName , R.drawable.folder_icon_4_01)
                        MainActivity._appViewModel.addFolder(folder)

                        val creat = PDFCreator(context)
                       // creat.savePDF( text , file = File(dir , "${text}.pdf") , CreatePDFActivity.pagesContainer , CreatePDFActivity.cpageStates , context.contentResolver)

                        if (create){
                            val icore = icore(context)

                            scope.launch{
                                scope.launch{

                                    val fontInputStream = context.resources.openRawResource(com.batuscode.docunote.R.font.helvetica)
                                    val fontData = fontInputStream.readBytes()
                                    textStates.forEach { (index, state) ->

                                        Log.d("new character" , "index :: " + index + " text :: " + state.toMarkdown())
                                       // icore.nativeAddTextToPage(CreatePDFActivity.docptr,index,state.toMarkdown(),50f,800f,12f,14f,fontData)
                                    }
                                }

                                //creat.savePDF(text , file = File(dir , "${text}.pdf") , CreatePDFActivity.layers ,context.contentResolver , context)
                            }
                        } else {

                            scope.launch{
                                creat.saveDrawingsToPDF(text , file = File(dir , "${text}.pdf") ,
                                    PDFViewerActivity.mpageStates ,context.contentResolver , context)
                            }
                          /*  creat.saveDrawingsToPDF(text,file = File(dir , "${text}.pdf") ,
                                PDFViewerActivity.mrendererPages ,
                                PDFViewerActivity.mpageStates , context.contentResolver)*/
                        }


                    }
                    else {
                        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                        val filePath = File(dir, "${text}.pdf").absolutePath

                        // create page

                        if (create){
                            val fontInputStream = context.resources.openRawResource(com.batuscode.docunote.R.font.helvetica)
                            val fontData = fontInputStream.readBytes()

                            Log.d("page count" , "in save" + CreatePDFActivity.docptr)
                            var ptr = CreatePDFActivity.docptr

                            Log.d("page count" , "in save" + ptr)
                            scope.launch{
                                textStates.forEach { (index, state) ->

                                    val text = state.toMarkdown()
                                    val utf16Bytes = text.toByteArray(Charsets.UTF_16LE)
                                    Log.d("new character", "utf-16 :: " + utf16Bytes)
                                    Log.d("new character", "utf-16 :: " + utf16Bytes.joinToString(", ") { it.toString() })
                                    MainActivity.mainicore.addText(CreatePDFActivity.docptr, index, utf16Bytes, 50f, 800f, 12f, 14f, fontData)

                                    // Metni satırlara böl
                                   /* val lines = text.split("\\s{2,}")

                                    System.out.println(lines)

                                    // Başlangıç Y koordinatını ayarla
                                    var currentY = 800f

                                    // Her satırı PDF'e ekle
                                    lines.forEach { line ->
                                        Log.d("new character", "index :: $index text :: $line")

                                        // Her satırı PDF'e ekle ve satır yüksekliğine göre Y koordinatını güncelle
                                        MainActivity.mainicore.addText(CreatePDFActivity.docptr, index, utf16Bytes, 50f, currentY, 12f, 14f, fontData)
                                        currentY -= 14f  // Line height kadar Y'yi düşür
                                    }*/

                                    Log.d("new character" , "index :: " + index + " text :: " + state.toMarkdown())
                                   // MainActivity.mainicore.addText(ptr,0,state.toText(),50f,800f,12f,14f,fontData)
                                }
                                MainActivity.mainicore.saveDocument(CreatePDFActivity.docptr,filePath)
                            }
                        }
                        else {
                            // edit page
                             /* creat.saveDrawingsToPDF(text , file = File(dir , "${text}.pdf") ,
                                  PDFViewerActivity.mrendererPages ,
                                  PDFViewerActivity.mpageStates , context.contentResolver)*/

                            scope.launch{
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