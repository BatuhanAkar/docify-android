package com.batuscode.docunote.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Debug
import android.util.Log
import com.batuscode.docunote.MainActivity
import com.batuscode.docunote.PDFViewerActivity
import com.batuscode.pdfium.PDFPage
import com.batuscode.pdfium.PdfDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.batuscode.pdfium.icore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlin.use

class PDFConverter(private val context: Context) {


    private var renderer: PdfRenderer? = null
    companion object{
        lateinit var midoc: PdfDocument
    }



    fun logMemoryUsage() {
        val heapSize = Debug.getNativeHeapAllocatedSize()
        Log.d("MemoryUsage", "Native heap size: $heapSize bytes")
    }

    /*suspend fun renderDraftDoc(
        context: Context,
        uri: Uri,): PDFPage? = withContext(Dispatchers.IO)
    {
        context.contentResolver.openFileDescriptor(uri,"r")?.use { descriptor ->
            val icore = icore(context)
            val idoc = icore.newDocument(descriptor)
            val page = PDFPage(595f, 842f)
            var ok = icore.addPage(idoc,page)
            if (ok){
                return@withContext page

                Log.d("ownCreator" , "page")
            } else {
                return@withContext null
                Log.d("ownCreator" , "null")

            }
        }
        return@withContext null
    }*/



    suspend fun internalRenderPage(
        pdfDocument: PdfDocument,
        pageIndex: Int,
        scaleFactor: Float = 1.0f // Add a scale factor for downscaling
    ): Bitmap? = withContext(Dispatchers.IO) {

        Log.d("PDFConverter", "Document pages pointer: ${pdfDocument.mNativeDocPtr}")
        MainActivity.mainicore.memPage(pdfDocument, pageIndex)


        val pageWidth = MainActivity.mainicore.getPageWidth(pdfDocument, pageIndex)
        val pageHeight = MainActivity.mainicore.getPageHeight(pdfDocument, pageIndex)

        // Downscale the bitmap to match the screen size
        val scaledWidth = (pageWidth * scaleFactor).toInt()
        val scaledHeight = (pageHeight * scaleFactor).toInt()

        val bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.RGB_565)

        MainActivity.mainicore.renderPageBitmap(
            pdfDocument,
            bitmap,
            pageIndex,
            0, 0,
            scaledWidth, scaledHeight
        )

        logMemoryUsage()
        return@withContext bitmap
    }

    suspend fun renderPage(
        context: Context,
        uri: Uri,
        pageIndex: Int,
        scaleFactor: Float = 1.0f // Add a scale factor for downscaling
    ): Bitmap? = withContext(Dispatchers.IO) {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->

            val idoc = MainActivity.mainicore.newDocument(descriptor)
            midoc = idoc

            MainActivity.mainicore.openPage(idoc, pageIndex)
            Log.d("PDFConverter", "Document pointer: ${idoc.mNativePagesPtr}")

            val pageWidth = MainActivity.mainicore.getPageWidth(idoc, pageIndex)
            val pageHeight = MainActivity.mainicore.getPageHeight(idoc, pageIndex)

            // Downscale the bitmap to match the screen size
            val scaledWidth = (pageWidth * scaleFactor).toInt()
            val scaledHeight = (pageHeight * scaleFactor).toInt()

            val bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.RGB_565)

            MainActivity.mainicore.renderPageBitmap(
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


    suspend fun dff(uri: Uri, ): List<Bitmap> = withContext(Dispatchers.IO) {

        context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->

            val icore = icore(context)
            val idoc = icore.newDocument(descriptor)
            with(icore.newDocument(descriptor)){
                val pageCount = icore.getPageCount(idoc)

                return@withContext (0 until 10).map { pageIndex ->
                    async {


                        icore.openPage(idoc, pageIndex)

                        val pageWidth = icore.getPageWidth(idoc, pageIndex)
                        val pageHeight = icore.getPageHeight(idoc, pageIndex)
                       // val scaledWidth = (pageWidth * scaleFactor).toInt()
                       // val scaledHeight = (pageHeight * scaleFactor).toInt()

                        val bitmap =
                            Bitmap.createBitmap(pageWidth, pageHeight, Bitmap.Config.RGB_565)

                        icore.renderPageBitmap(
                            idoc,
                            bitmap,
                            pageIndex,
                            0, 0,
                            pageWidth, pageHeight
                        )

                        logMemoryUsage()
                        bitmap
                    }
                }.awaitAll()
            }
        }
        return@withContext emptyList()
    }
   /* fun dfrs(uri: Uri, scaleFactor: Float = 1.5f): List<Bitmap>{
        val bitmaps = mutableListOf<Bitmap>()


        context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
            val core = PdfiumCore(context)
            val doc = core.newDocument(descriptor)

            val pageCount = core.getPageCount(doc)

            val pagesPerBatch = 10

            for (batchStart in 0 until pageCount step pagesPerBatch) {
                val batchEnd = minOf(batchStart + pagesPerBatch, pageCount)

                for (pageIndex in batchStart until batchEnd) {
                    core.openPage(doc, pageIndex)

                    val pageWidth = core.getPageWidth(doc, pageIndex)
                    val pageHeight = core.getPageHeight(doc, pageIndex)

                    if (pageWidth > 0 && pageHeight > 0) {
                        val scaledWidth = (pageWidth * scaleFactor).toInt()
                        val scaledHeight = (pageHeight * scaleFactor).toInt()

                        val bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.RGB_565)

                        // Render the page
                        //core.renderPage(doc,holder.surface,pageIndex,0,0,scaledWidth,scaledHeight)
                        core.renderPageBitmap(
                            doc,
                            bitmap,
                            pageIndex,
                            0, 0,
                            scaledWidth, scaledHeight
                        )

                        bitmaps.add(bitmap)

                    } else {
                        Log.e("PDFError", "Invalid page width or height: width=$pageWidth, height=$pageHeight for page $pageIndex")
                    }
                }

            }

            // Close the document to release resources
            core.closeDocument(doc)
        }

        return bitmaps
    }

    suspend fun dfr(uri: Uri, scaleFactor: Float = 1.5f): List<Bitmap> = withContext(Dispatchers.IO) {
        val bitmaps = mutableListOf<Bitmap>()


        context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
            val core = PdfiumCore(context)
            val doc = core.newDocument(descriptor)

            val pageCount = core.getPageCount(doc)

            val pagesPerBatch = 10

            for (batchStart in 0 until pageCount step pagesPerBatch) {
                val batchEnd = minOf(batchStart + pagesPerBatch, pageCount)

                for (pageIndex in batchStart until batchEnd) {
                    core.openPage(doc, pageIndex)

                    val pageWidth = core.getPageWidth(doc, pageIndex)
                    val pageHeight = core.getPageHeight(doc, pageIndex)

                    if (pageWidth > 0 && pageHeight > 0) {
                        val scaledWidth = (pageWidth * scaleFactor).toInt()
                        val scaledHeight = (pageHeight * scaleFactor).toInt()

                        val bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.RGB_565)



                        // Render the page
                        core.renderPageBitmap(
                            doc,
                            bitmap,
                            pageIndex,
                            0, 0,
                            scaledWidth, scaledHeight
                        )

                        bitmaps.add(bitmap)
                    } else {
                        Log.e("PDFError", "Invalid page width or height: width=$pageWidth, height=$pageHeight for page $pageIndex")
                    }
                }

            }

            // Close the document to release resources
            core.closeDocument(doc)
        }

         bitmaps
    }*/

    /*suspend fun dfr(uri: Uri): List<Bitmap> = withContext(Dispatchers.IO) {
        val bitmaps = mutableListOf<Bitmap>()
        context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
            val core = PdfiumCore(context)
            val doc = core.newDocument(descriptor)

            val pageCount = core.getPageCount(doc)

            if (pageCount > 0) {
                core.openPage(doc, 0)

                val pageWidth = core.getPageWidth(doc, 0)
                val pageHeight = core.getPageHeight(doc, 0)

                if (pageWidth > 0 && pageHeight > 0) {
                      // Senkron çalışır ama Coroutine'de arka planda çalıştırılır

                    val scaleFactor = 1.5f
                    val bitmap = Bitmap.createBitmap(
                        (pageWidth * scaleFactor).toInt(),
                        (pageHeight * scaleFactor).toInt(),
                        Bitmap.Config.ARGB_8888
                    )

                    core.renderPageBitmap(
                        doc,
                        bitmap,
                        0,
                        0,
                        0,
                        (pageWidth * scaleFactor).toInt(),
                        (pageHeight * scaleFactor).toInt()
                    )

                    bitmaps.add(bitmap)
                } else {
                    Log.e("PDFError", "Geçersiz genişlik veya yükseklik: width=$pageWidth, height=$pageHeight")
                }
            }
        }
        bitmaps
    }*/


    /*fun dfr (uri: Uri): List<Bitmap>{

        val bitmaps = mutableListOf<Bitmap>()
        context.contentResolver.openFileDescriptor(uri , "r")?.use { descriptor ->
            val core = PdfiumCore(context)
            val doc = core.newDocument(descriptor)

            val pageCount = core.getPageCount(doc)

            val pagewidth = core.getPageWidth(doc,0)
            val pageheight = core.getPageHeight(doc , 0)

            core.openPage(doc,0)

            val scaleFactor = 1.5f // Yüksek çözünürlük yerine düşük çözünürlük kullanabilirsiniz
            val bitmap = Bitmap.createBitmap(
                (PDRectangle.A4.width * scaleFactor).toInt(),
                (PDRectangle.A4.height * scaleFactor).toInt(),
                Bitmap.Config.ARGB_8888
            )

            core.renderPageBitmap(doc,bitmap,1,0,0,(pagewidth * scaleFactor).toInt(),(pageheight * scaleFactor).toInt())

            bitmaps.add(bitmap)

            return bitmaps
        }
        return emptyList()
    }*/

    suspend fun pdfToBitmaps(contentUri: Uri): List<Bitmap> {
        return withContext(Dispatchers.IO) {
            renderer?.close()

            context.contentResolver.openFileDescriptor(contentUri, "r")?.use { descriptor ->
                val pdfRenderer = PdfRenderer(descriptor)
                renderer = pdfRenderer

                val bitmaps = mutableListOf<Bitmap>()

                // Bellek tüketimini kontrol altında tutmak için her seferinde 10 sayfa işliyoruz
                val pagesPerBatch = 10

                for (batchStart in 0 until pdfRenderer.pageCount step pagesPerBatch) {
                    val batchEnd = minOf(batchStart + pagesPerBatch, pdfRenderer.pageCount)

                    for (index in batchStart until batchEnd) {
                        val page = pdfRenderer.openPage(index)
                        val bitmap = renderPageToBitmap(page)
                        bitmaps.add(bitmap)

                        // Belleği serbest bırakma
                        page.close()
                        System.gc()  // Çöp toplama işlemini zorla tetiklemek
                    }

                    // İşlem sonrası bellek temizliği ve profilin izlenmesi
                    // Gereksiz yükü engellemek için `System.gc()` sürekli çalıştırılabilir.
                }

                return@withContext bitmaps
            }
        }?:emptyList()
    }

    private fun renderPageToBitmap(page: PdfRenderer.Page): Bitmap {
        val scaleFactor = 1.5f // Yüksek çözünürlük yerine düşük çözünürlük kullanabilirsiniz
        val bitmap = Bitmap.createBitmap(
            (page.width * scaleFactor).toInt(),
            (page.height * scaleFactor).toInt(),
            Bitmap.Config.ARGB_8888
        )

        val matrix = android.graphics.Matrix().apply {
            postScale(scaleFactor, scaleFactor)
        }

        val canvas = Canvas(bitmap).apply {
            drawColor(Color.WHITE)
        }

        // Sayfayı render et
        page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        return bitmap
    }
   /* private var renderer: PdfRenderer? = null

    suspend fun pdfToBitmaps(contentUri: Uri): List<Bitmap> {
        return withContext(Dispatchers.IO) {
            renderer?.close()

            context.contentResolver.openFileDescriptor(contentUri, "r")?.use { descriptor ->
                val pdfRenderer = PdfRenderer(descriptor)
                renderer = pdfRenderer

                val bitmaps = mutableListOf<Bitmap>()

                // Bellek yönetimi için her seferinde sayfa işleniyor.
                for (index in 0 until pdfRenderer.pageCount) {
                    val page = pdfRenderer.openPage(index)
                    val bitmap = renderPageToBitmap(page)
                    bitmaps.add(bitmap)

                    // Bellek serbest bırakma
                    page.close()
                    System.gc() // Çöp toplama işlemini zorla tetiklemek

                    // Her sayfayı işlemeye devam et
                }

                return@withContext bitmaps
            }

            // Eğer hiç sayfa işlenmediyse boş liste döndür
            emptyList<Bitmap>()
        }
    }

    // Sayfa render etme işlemi
    private fun renderPageToBitmap(page: PdfRenderer.Page): Bitmap {
        val scaleFactor = 2.0f // Ölçekleme faktörü
        val bitmap = Bitmap.createBitmap(
            (page.width * scaleFactor).toInt(),
            (page.height * scaleFactor).toInt(),
            Bitmap.Config.ARGB_8888
        )

        val matrix = android.graphics.Matrix().apply {
            postScale(scaleFactor, scaleFactor)
        }

        val canvas = Canvas(bitmap).apply {
            drawColor(Color.WHITE)
        }

        // Sayfayı render et
        page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        return bitmap
    }*/



   /* suspend fun pdfToBitmaps(contentUri: Uri): List<Bitmap> {
        return withContext(Dispatchers.IO) {
            // Render edilmemişse kapatıyoruz
            renderer?.close()

            context
                .contentResolver
                .openFileDescriptor(contentUri, "r")
                ?.use { descriptor ->

                    with(PdfRenderer(descriptor)) {
                        renderer = this
                        val bitmaps = mutableListOf<Bitmap>()

                        // Sayfaları sırayla işleme
                        for (index in 0 until pageCount) {
                            // Sayfayı aç
                            openPage(index).use { page ->
                                val scaleFactor = 2.0f // Ölçekleme faktörü

                                val bitmap = Bitmap.createBitmap(
                                    (page.width * scaleFactor).toInt(),
                                    (page.height * scaleFactor).toInt(),
                                    Bitmap.Config.ARGB_8888
                                )

                                val matrix = android.graphics.Matrix().apply {
                                    postScale(scaleFactor, scaleFactor)
                                }

                                val canvas = Canvas(bitmap).apply {
                                    drawColor(Color.WHITE)
                                }

                                // Sayfayı render et
                                page.render(
                                    bitmap,
                                    null,
                                    matrix,
                                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                )

                                bitmaps.add(bitmap)
                            }
                        }
                        return@withContext bitmaps
                    }
                }
            emptyList<Bitmap>()
        }
    }*/

    /*suspend fun pdfToBitmaps(contentUri: Uri): List<Bitmap> {
        return withContext(Dispatchers.IO) {
            renderer?.close()

            context
                .contentResolver
                .openFileDescriptor(contentUri, "r")
                ?.use { descriptor ->
                    with(PdfRenderer(descriptor)) {
                        renderer = this

                        return@withContext (0 until pageCount).map { index ->
                            async {
                                var page: PdfRenderer.Page?= null
                                try {

                                     page = openPage(index)
                                    Log.d("sayfa" , "açıldı...")

                                    // Bitmap'i oluşturma ve işleme
                                    val scaleFactor = 2.0f // Ölçekleme faktörü

                                    val bitmap = Bitmap.createBitmap(
                                        (page.width * scaleFactor).toInt(),
                                        (page.height * scaleFactor).toInt(),
                                        Bitmap.Config.ARGB_8888
                                    )

                                    val matrix = android.graphics.Matrix().apply {
                                        postScale(scaleFactor, scaleFactor)
                                    }

                                    val canvas = Canvas(bitmap).apply {
                                        drawColor(Color.WHITE)
                                    }

                                    // Sayfayı render et
                                    page.render(
                                        bitmap,
                                        null,
                                        matrix,
                                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                    )

                                    bitmap
                                } finally {
                                    // Sayfayı her zaman kapat
                                    Log.d("sayfa" , "kapatıldı...")
                                    page?.close()
                                }
                               /* openPage(index).use { page ->
                                    val scaleFactor = 2.0f // Adjust this scale factor based on desired quality




                                    val bitmap = Bitmap.createBitmap(
                                        (page.width * scaleFactor).toInt(), // Scale the width
                                        (page.height * scaleFactor).toInt(), // Scale the height
                                        Bitmap.Config.ARGB_8888
                                    )
                                    // Apply scaling and render the page onto the bitmap
                                    val matrix = android.graphics.Matrix().apply {
                                        postScale(scaleFactor, scaleFactor)
                                    }
                                    val canvas = Canvas(bitmap).apply {
                                        drawColor(Color.WHITE)
                                        drawBitmap(bitmap, 0f, 0f, null)
                                    }

                                    page.render(
                                        bitmap,
                                        null,
                                        matrix,
                                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                    )

                                    bitmap
                                }*/
                            }
                        }.awaitAll()
                    }
                }
            return@withContext emptyList()
        }
    }*/
   /* suspend fun ads(uri: Uri): List<Bitmap>{

        return withContext(Dispatchers.IO) {
            val list: MutableList<Bitmap> = mutableListOf()

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bufferedStream = BufferedInputStream(inputStream, 8192) // 8KB buffer size

                // PDF dosyasını yükle
                val doc = PDDocument.load(bufferedStream)
                val renderer = PDFRenderer(doc)

                // Sayfaları render et
                for (index in 0 until doc.numberOfPages) {
                    val bitmap = renderer.renderImage(index, 2.0f, ImageType.RGB, RenderDestination.VIEW) // Skalalama faktörü
                    list.add(bitmap)
                }

                // PDF dosyasını kapat
                doc.close()
            }

            list
        }

       /* val list: MutableList<Bitmap> = mutableListOf()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            // PDF dosyasını yükle

            val doc = PDDocument.load(inputStream)
            val renderer = PDFRenderer(doc)

            val st = CountDownLatch(doc.numberOfPages)
            (0 until doc.numberOfPages).map { index ->

                val page = doc.getPage(index)
                val pageResources = page.resources.cosObject
                // Her sayfa için Bitmap render et
                // Sayfa bitmapini render et

                val bitmap = renderer.renderImage(index, 2.0f, ImageType.RGB, RenderDestination.VIEW) // Skalalama faktörü
                list.add(index,bitmap)
                st.countDown()
            }
            st.await()
            doc.close()

        }

        return list*/
    }
    suspend fun pdfToBitmaps(contentUri: Uri): List<Bitmap> {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver
                    .openFileDescriptor(contentUri, "r")
                    ?.use { descriptor ->
                        PdfRenderer(descriptor).use { renderer ->
                            // Render each page concurrently
                            (0 until renderer.pageCount).map { index ->
                                async {
                                    renderer.openPage(index).use { page ->
                                        val scaleFactor = 3.0f // Adjust this scale factor based on desired quality

                                        // Create a scaled bitmap
                                        val bitmap = Bitmap.createBitmap(
                                            (page.width * scaleFactor).toInt(), // Scale the width
                                            (page.height * scaleFactor).toInt(), // Scale the height
                                            Bitmap.Config.ARGB_8888
                                        )

                                        // Apply scaling and render the page onto the bitmap
                                        val matrix = android.graphics.Matrix().apply {
                                            postScale(scaleFactor, scaleFactor)
                                        }

                                        page.render(
                                            bitmap,
                                            null,
                                            matrix,
                                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                        )

                                        bitmap // Return the rendered bitmap
                                    }
                                }
                            }.awaitAll() // Wait for all pages to be rendered
                        }
                    } ?: emptyList() // Return an empty list if the file descriptor is null
            } catch (e: Exception) {
                // Handle exceptions (e.g., invalid PDF file, insufficient memory)
                e.printStackTrace()
                emptyList() // Return an empty list in case of errors
            }
        }
    }*/
}