package com.batuscode.docunote

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.batuscode.docunote.ui.theme.DocuNoteTheme

import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.batuscode.docunote.view.DrawingCanvas
import com.batuscode.docunote.view.DrawingScreen
import com.batuscode.docunote.view.DrawingState
import com.batuscode.docunote.view.ListItem
import com.batuscode.docunote.viewmodel.DrawingViewModel
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource

class EditPDFActivty : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DocuNoteTheme {

                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(colorResource(R.color.modified).toArgb() , colorResource(R.color.modified).toArgb())
                )
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Area(modifier = Modifier.fillMaxSize().padding(innerPadding))
                }
            }
        }
    }
}



@Composable
fun Area(modifier: Modifier = Modifier){

    val scaleFactor = LocalContext.current.resources.displayMetrics.densityDpi / 72f
    val width = (PDRectangle.A4.width * scaleFactor)
    val height = (PDRectangle.A4.height * scaleFactor)

    val start = Offset(0f,0f)
    val animate = remember {
        Animatable(0f)
    }

    var visible by remember {
        mutableStateOf(true)
    }

    var y = remember {
        mutableStateOf(50f)
    }

    var rowLenght = 0f
    Canvas(
        modifier = Modifier.fillMaxSize()
            .background(Color.Yellow)
    ) {
       // drawLine(Color.Black , start = Offset(40f,40f), end = Offset(40f,90f) , 5f)

        rowLenght = size.width - 20

        drawRect(color = Color.White , size = Size(width=width , height = height))
        val row = Path().apply {

            moveTo(20f , y.value)
            lineTo(size.width -20 , y.value)
        }

        val row2 = Path().apply {

            moveTo(20f , 120f)
            lineTo(size.width -20 , 120f)
        }



        drawPath(path = row , color = Color.Black , style = Stroke(width = 5f))

        drawPath(path = row2 , color = Color.Black , style = Stroke(width = 5f))


    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview3() {
    DocuNoteTheme {
        Area()
    }
}