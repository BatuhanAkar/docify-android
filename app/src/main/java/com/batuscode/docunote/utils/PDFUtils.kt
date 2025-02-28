package com.batuscode.docunote.utils

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import com.batuscode.docunote.MainActivity.Companion.context
import com.batuscode.docunote.MainActivity.Companion.mainActivity
import com.batuscode.docunote.PDFViewerActivity

class PDFUtils {


    fun mergeDocument(uris: MutableList<Uri>, outputfilename: String ,  outputUri: Uri, contentResolver: ContentResolver){
       /* val merge = PDDocument()
        uris.forEach { uri ->
            val inputStrem = contentResolver.openInputStream(uri)

            inputStrem?.let {
                val doc = PDDocument.load(it)
                doc.pages.forEach{
                    page ->
                    merge.importPage(page)
                }
            }
        }

        val outputStream = contentResolver.openOutputStream(outputUri)
        outputStream?.let {
            merge.save(it)
        }

        val intent = Intent(context , PDFViewerActivity::class.java).apply {
            putExtra("fileUri" , outputUri.toString())
            putExtra("fileDisplayName" , outputfilename)
        }
        mainActivity.startActivity(intent)
*/
    }

   /* fun toBitmap(pdfViewerActivityViewModel: PDFViewerActivityViewModel , uri: Uri , contentResolver: ContentResolver){
        val stream = contentResolver.openInputStream(uri)

        var doc = PDDocument()
        stream?.let {
            doc = PDDocument.load(it)

        }

        val renderer = PDFRenderer(doc)

        for (i in 0 until doc.pages.count){

            val bitmap = renderer.renderImage(i)

            pdfViewerActivityViewModel.set_pdfPage(bitmap)
        }
    }*/



}