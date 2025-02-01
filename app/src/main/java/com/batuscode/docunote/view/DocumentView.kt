package com.batuscode.docunote.view

import android.graphics.Bitmap
import android.graphics.Picture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.text.style.TextAlign
import com.batuscode.docunote.model.Document
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults

private fun createBitmapFromPicture(picture: Picture): Bitmap {
    val bitmap = Bitmap.createBitmap(
        picture.width,
        picture.height,
        Bitmap.Config.ARGB_8888
    )

    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    canvas.drawPicture(picture)
    return bitmap
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentView(document: Document,/*createPDFActivityViewModel: CreatePDFActivityViewModel*/  modifier: Modifier = Modifier ){
    val state = rememberRichTextState()


   /* val docs = createPDFActivityViewModel._page.collectAsState()

    val write = createPDFActivityViewModel._write.collectAsState()

   // val newDocName = createPDFActivityViewModel._newDocName.collectAsState()

    val fileUri = createPDFActivityViewModel._fileUri.collectAsState()

    val list = mutableListOf<Document>()
    val creator = PDFCreator()
    val counter = CountDownLatch(docs.value.size)
    if (write.value){
        for (doc in docs.value){
            doc.text = state.toText()
            list.add(doc)
            counter.countDown()
        }

        counter.await()
        creator.createPage(fileUri.value!! , /*newDocName.value!!*/  list , createPDFActivityViewModel)
        Log.d("writesome" , state.toText())
        createPDFActivityViewModel.update_write(false)
    }*/

   // document.text = state.toText()


    Box (
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(1280.dp)
    ){

        RichTextEditor(

            minLines = 1 ,
            maxLength = 1820,
            state = state ,
            colors = RichTextEditorDefaults.richTextEditorColors(
                disabledIndicatorColor = Color.Transparent ,
                unfocusedIndicatorColor = Color.Transparent ,
                cursorColor = Color.Black ,
                focusedIndicatorColor = Color.Transparent ,
                containerColor = Color.White ,
                textColor = Color.Black

            ),
            modifier = Modifier.fillMaxWidth().height(1280.dp))

    }


}

@Preview(showBackground = true)
@Composable
fun PreviewDocumentPage(){
    DocuNoteTheme {
       // DocumentPage(Document(0,"",1) , createPDFActivityViewModel = CreatePDFActivityViewModel())
    }
}