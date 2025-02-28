package com.batuscode.docunote

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.opengl.GLSurfaceView
import android.util.Log
import com.batuscode.docunote.utils.PDFConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyGLSurfaceView(
    context: Context,
    val pdfConverter: PDFConverter,
    val uri: Uri,
    val bitmaps: List<Bitmap>?
) : GLSurfaceView(context) {

    private val renderer: MyGLRenderer

    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        // Set the Renderer for drawing on the GLSurfaceView
        renderer = MyGLRenderer(context, pdfConverter, uri, bitmaps)
        setRenderer(renderer)

        // Render continuously
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onPause() {
        super.onPause()
        // Handle pause (e.g., release resources)
    }

    override fun onResume() {
        super.onResume()
        // Handle resume (e.g., reinitialize resources)
    }
}