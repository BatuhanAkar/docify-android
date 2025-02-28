package com.batuscode.docunote

import android.net.Uri
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.utils.PDFConverter

class ViewerActivity : ComponentActivity() {
    private lateinit var gLView: GLSurfaceView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val converter : PDFConverter = PDFConverter(this)
        val uri = intent.getStringExtra("fileUri")
        val displayName = intent.getStringExtra("fileDisplayName")

      //  gLView = MyGLSurfaceView(this,converter , Uri.parse(uri))
        enableEdgeToEdge()
        setContentView(gLView)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview3() {
    DocuNoteTheme {
        Greeting("Android")
    }
}