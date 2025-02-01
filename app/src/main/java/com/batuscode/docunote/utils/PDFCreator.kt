package com.batuscode.docunote.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.batuscode.docunote.CreatePDFActivity
import com.batuscode.docunote.MainActivity
import com.batuscode.docunote.PDFViewerActivity
import com.batuscode.docunote.model.Document
import com.batuscode.docunote.view.DrawingState
import com.batuscode.docunote.view.PathData
import com.batuscode.docunote.viewmodel.CreatePDFActivityViewModel
import com.tom_roush.harmony.awt.geom.AffineTransform
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState
import com.tom_roush.pdfbox.pdmodel.graphics.state.PDGraphicsState
import com.tom_roush.pdfbox.rendering.PDFRenderer
import com.tom_roush.pdfbox.util.Matrix
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.CountDownLatch
import kotlin.math.abs

class PDFCreator{

    val pdfDocument = PdfDocument()
    val paint = Paint()




    val pageInfo = PageInfo.Builder(595, 842, 1).create()

    suspend fun saveDrawingsToPDF (fileName:String, file: File, /*newDocName:String*/  GrapList: List<GraphicsLayer>, contentResolver: ContentResolver, context:Context){
        val filemanager = FileManager(context)
        val document = PDDocument()

        for ((index,grap) in GrapList.withIndex()){
            val page = PDPage()
            document.addPage(page)

            val contentStream = PDPageContentStream(document, page , true ,true ,true)

            val bitmap = grap.toImageBitmap().asAndroidBitmap()
            val bb = bitmap.copy(Bitmap.Config.ARGB_8888 , true)
            val image = JPEGFactory.createFromImage(document, bb)
            contentStream.drawImage(image, 0f, 0f, page.mediaBox.width, page.mediaBox.height)

            contentStream.close()
        }


        val contentResolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName) // Dosya adı
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf") // Dosya türü
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}") // Documents dizini
        }
        val uri = contentResolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues)

        filemanager.addDocument(context = context , uri = uri.toString(), fileName = fileName)

        val file2 = com.batuscode.docunote.utils.File(uri.toString() , fileName)
        MainActivity._appViewModel.addRecentlyFile(file2)
        uri?.let {
            val outputStream: OutputStream? = contentResolver.openOutputStream(it)
            outputStream?.use { stream ->
                document.save(stream) // PDF belgesini stream'e yaz

            }
        }

        document.save(file)
        document.close()

        PDFViewerActivity.activity.onBackPressedDispatcher.onBackPressed()

    }

    fun abc(fileName:String , file: File, bitmapList: List<Bitmap>, pageStates: MutableList<MutableState<DrawingState>> , contentResolver: ContentResolver) {
        val document = PDDocument()

        for ((index, bitmap) in bitmapList.withIndex()) {
            val page = PDPage()
            document.addPage(page)

            // Bitmap'i PDF'ye eklemek için canvas kullanıyoruz
            val contentStream = PDPageContentStream(document, page)

            // PdfBox-Android'de bitmap eklemek için JPEGFactory kullanılır
            val image = JPEGFactory.createFromImage(document, bitmap)
            contentStream.drawImage(image, 0f, 0f, page.mediaBox.width, page.mediaBox.height)

            // Kullanıcının çizdiği çizimleri PDF'ye ekle
            val canvasBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(canvasBitmap)

            // Her çizim için renk ve kalınlık ayarları
            for (pathData in pageStates[index].value.paths) {
                val paint = Paint().apply {
                    isAntiAlias = true
                    color = pathData.color.toArgb() // PathData'daki renk
                    style = Paint.Style.STROKE
                    strokeJoin = Paint.Join.ROUND
                    strokeWidth = pathData.thickness // PathData'daki kalınlık
                }

                // Her bir path'i canvas'a çiz
                val drawPath = android.graphics.Path().apply {
                    if (pathData.path.isNotEmpty()) {
                        moveTo(pathData.path.first().x, pathData.path.first().y)
                        for (offset in pathData.path.drop(1)) {
                            lineTo(offset.x, offset.y)
                        }
                    }
                }
                canvas.drawPath(drawPath, paint)
            }

            // PDF'ye kullanıcı çizimlerini ekle
            val drawingImage = JPEGFactory.createFromImage(document, canvasBitmap)
            contentStream.drawImage(drawingImage, 0f, 0f, page.mediaBox.width, page.mediaBox.height)

            contentStream.close()
        }
        val contentResolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${fileName}") // Dosya adı
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf") // Dosya türü
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}") // Documents dizini
        }
        val uri = contentResolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues)
        uri?.let {
            val outputStream: OutputStream? = contentResolver.openOutputStream(it)
            outputStream?.use { stream ->
                document.save(stream) // PDF belgesini stream'e yaz

            }
        }

        // PDF dosyasını kaydet
        document.save(file)
        document.close()



    }

  /*  fun createPDFPage(document: PDDocument): Bitmap{

        val page = PDPage(PDRectangle.A4)
        document.addPage(page)

        document.pages

        val renderer = PDFRenderer(document)

        renderer
    }*/

     suspend fun savePDF (fileName:String, file: File, /*newDocName:String*/  GrapList: List<GraphicsLayer>, contentResolver: ContentResolver, context:Context){
         val filemanager = FileManager(context)

         val document = PDDocument()
        for ((index,grap) in GrapList.withIndex()){
            val page = PDPage()
            document.addPage(page)

            val contentStream = PDPageContentStream(document, page , true ,true ,true)

            val bitmap = grap.toImageBitmap().asAndroidBitmap()
            val bb = bitmap.copy(Bitmap.Config.ARGB_8888 , true)
            val image = JPEGFactory.createFromImage(document, bb)
            contentStream.drawImage(image, 0f, 0f, page.mediaBox.width, page.mediaBox.height)

            contentStream.close()
        }


         val contentResolver = contentResolver
         val contentValues = ContentValues().apply {
             put(MediaStore.MediaColumns.DISPLAY_NAME, "${fileName}") // Dosya adı
             put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf") // Dosya türü
             put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}") // Documents dizini
         }
         val uri = contentResolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues)

         filemanager.addDocument(context = context , uri = uri.toString(), fileName = fileName)

         val file2 = com.batuscode.docunote.utils.File(uri.toString() , fileName)
         MainActivity._appViewModel.addRecentlyFile(file2)
         uri?.let {
             val outputStream: OutputStream? = contentResolver.openOutputStream(it)
             outputStream?.use { stream ->
                 document.save(stream) // PDF belgesini stream'e yaz

             }
         }

        document.save(file)
        document.close()

        CreatePDFActivity.pdfActivity.onBackPressedDispatcher.onBackPressed()

    }
    fun asd(fileName:String , file: File, /*newDocName:String*/  docList: List<Document> , pageStates: MutableList<MutableState<DrawingState>> , contentResolver: ContentResolver){
      val font = PDType1Font.HELVETICA;
      val document = PDDocument()

      for ((index, doc) in docList.withIndex()) {
          val page = PDPage()
          document.addPage(page)

          // Bitmap'i PDF'ye eklemek için canvas kullanıyoruz
          val contentStream = PDPageContentStream(document, page , true ,true ,true)
          contentStream.beginText();
          contentStream.setFont(font, 12f);
          contentStream.newLineAtOffset(100f, 700f);
          contentStream.showText(doc.text.toText());
          contentStream.endText();


          // PdfBox-Android'de bitmap eklemek için JPEGFactory kullanılır
        /*  val image = JPEGFactory.createFromImage(document, doc.bitmap)
          contentStream.drawImage(image, 0f, 0f, page.mediaBox.width, page.mediaBox.height)*/

          // Kullanıcının çizdiği çizimleri PDF'ye ekle
          val canvasBitmap = Bitmap.createBitmap(doc.bitmap.width, doc.bitmap.height, Bitmap.Config.ARGB_8888)
          val canvas = Canvas(canvasBitmap)


          // Her çizim için renk ve kalınlık ayarları

          for (pathData in pageStates[index].value.paths) {
              val paint = Paint().apply {
                  isAntiAlias = true
                  color = pathData.color.toArgb() // PathData'daki renk
                  style = Paint.Style.STROKE
                  strokeJoin = Paint.Join.ROUND
                  strokeWidth = pathData.thickness // PathData'daki kalınlık
              }

              // Her bir path'i canvas'a çiz
              val drawPath = android.graphics.Path().apply {
                  if (pathData.path.isNotEmpty()) {
                      contentStream.moveTo(pathData.path.first().x , page.mediaBox.height- pathData.path.first().y)
                    /*  for (offset in pathData.path.drop(1)) {
                          contentStream.lineTo(offset.x , page.mediaBox.height- offset.y)
                      }*/

                      val smoothness = 5
                      for(i in 1..pathData.path.lastIndex) {
                          val from = pathData.path[i - 1]
                          val to = pathData.path[i]
                          val dx = abs(from.x - to.x)
                          val dy = abs(from.y - to.y)
                          if(dx >= smoothness || dy >= smoothness) {

                             /* quadraticTo(
                                  x1 = (from.x + to.x) / 2f,
                                  y1 = (from.y + to.y) / 2f,
                                  x2 = to.x,
                                  y2 = to.y
                              )*/
                          }
                      }
                  }
              }
            //  canvas.clipOutPath(drawPath)
            //  canvas.drawPath(drawPath, paint)

          }

          // PDF'ye kullanıcı çizimlerini ekle
        //  val drawingImage = JPEGFactory.createFromImage(document, canvasBitmap)
         // contentStream.drawImage(drawingImage, 0f, 0f, page.mediaBox.width, page.mediaBox.height)

          contentStream.stroke()
          contentStream.close()
      }


      val contentResolver = contentResolver
      val contentValues = ContentValues().apply {
          put(MediaStore.MediaColumns.DISPLAY_NAME, "${fileName}") // Dosya adı
          put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf") // Dosya türü
          put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}") // Documents dizini
      }
      val uri = contentResolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues)
      uri?.let {
          val outputStream: OutputStream? = contentResolver.openOutputStream(it)
          outputStream?.use { stream ->
              document.save(stream) // PDF belgesini stream'e yaz

          }
      }

      // PDF dosyasını kaydet
      document.save(file)
      document.close()

      /*  val counterr = CountDownLatch(docList.size)

        for (doc in docList){
            val page = pdfDocument.startPage(pageInfo)

            val canvas = page.canvas

            canvas.drawText(doc.text.toText() , 40F, 50F, paint)

            Log.d("writesome" ,"in creator :: " + doc.text)
            pdfDocument.finishPage(page)
            counterr.countDown()
        }

        counterr.await()

        val contentResolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${fileName}") // Dosya adı
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf") // Dosya türü
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}") // Documents dizini
        }




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.d("saveway" , "new")

            val uri = contentResolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues)
            uri?.let {
                val outputStream: OutputStream? = contentResolver.openOutputStream(it)
                outputStream?.use { stream ->
                    pdfDocument.writeTo(stream) // PDF belgesini stream'e yaz
                }
            }


            val outputStream: OutputStream? = file.outputStream()
            outputStream?.use { stream ->
                pdfDocument.writeTo(stream) // PDF belgesini stream'e yaz
            }
        } else {
            Log.d("saveway" , "old")
            // Android 9 ve öncesi için klasik dosya yolu ile kayıt
           // val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "$newDocName.pdf")
          //  pdfDocument.writeTo(FileOutputStream(file))
        }

        pdfDocument.close()*/

       // createPDFActivityViewModel.update_saveFlag(false)

        CreatePDFActivity.pdfActivity.onBackPressedDispatcher.onBackPressed()

    }
}