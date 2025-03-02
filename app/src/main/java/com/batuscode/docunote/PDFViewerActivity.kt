package com.batuscode.docunote

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.utils.PDFConverter
import com.batuscode.docunote.viewmodel.PDFViewerActivityViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.batuscode.docunote.utils.FileManager
import com.batuscode.docunote.utils.PDFCreator
import com.batuscode.docunote.view.DrawingScreen
import com.batuscode.docunote.view.DrawingState
import com.batuscode.docunote.view.SaveDocument
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.getValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import com.batuscode.docunote.model.mColor
import com.batuscode.pdfium.PdfDocument
import com.batuscode.pdfium.icore
import com.smarttoolfactory.zoom.enhancedZoom
import com.smarttoolfactory.zoom.rememberEnhancedZoomState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors


class PDFViewerActivity : ComponentActivity() {
    companion object {
        lateinit var mrendererPages: List<Bitmap>
        lateinit var mpageStates: MutableMap<Int , MutableState<DrawingState>>

        lateinit var elayers: MutableList<GraphicsLayer>
        lateinit var activity: PDFViewerActivity
        lateinit var gLView: MyGLSurfaceView
        var wdocptr = mutableStateOf<Long>(0)
        var pdfDocument = mutableStateOf<PdfDocument?>(null)
    }


    init {

    }

    val mPreLoadPageWorker = Executors.newSingleThreadExecutor()
    val mRenderPageWorker = Executors.newSingleThreadExecutor()
    var renderRunnable: Runnable? = null

    @SuppressLint("UnusedBoxWithConstraintsScope")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            elayers = remember {
                mutableStateListOf<GraphicsLayer>()
            }
            val context = LocalContext.current
            activity = this

            val pdfViewerActivityViewModel: PDFViewerActivityViewModel by viewModels()

            var fileManager = remember {
                FileManager(context = context)
            }

            val uri = intent.getStringExtra("fileUri")
            val displayName = intent.getStringExtra("fileDisplayName")
            lateinit var fileUri: Uri

            uri?.let {
                fileUri = Uri.parse(it)
                fileManager.addDocument(context = context, uri = it, fileName = displayName!!)
                val file = com.batuscode.docunote.utils.File(uri, displayName)
                MainActivity._appViewModel.addRecentlyFile(file)
            }


            var creator = remember {
                PDFCreator(context)
            }

            val pdfBitmapConverter = remember {
                PDFConverter(context)
            }

            var renderedPages by remember {
                mutableStateOf<List<Bitmap>>(emptyList())
            }

            val scope = rememberCoroutineScope()

            var colorlist = remember {
                mutableStateOf<List<mColor>>(emptyList())
            }



            colorlist.value = getColorsFromResources()

            var ok = remember {
                mutableStateOf(false)
            }

            //  gLView = MyGLSurfaceView(this, pdfBitmapConverter, Uri.parse(uri))

            /*LaunchedEffect(fileUri) {
                fileUri?.let { uri ->
                   // renderedPages = pdfBitmapConverter.dfr(uri)

                }

                lifecycleScope.launch(Dispatchers.IO) {
                    Log.d("bg" , "started...")
                    renderedPages = pdfBitmapConverter.dff(uri = Uri.parse(uri)) // PDF sayfalarını render et
                    withContext(Dispatchers.Main) {

                        Log.d("bg" , "finished...")
                        ok.value = true
                      //  gLView.onPdfRenderCompleted(renderedBitmapList) // OpenGL'i güncelle
                    }
                }
            }*/


            val pageCount = remember { mutableStateOf(0) }
            val scaleFactor = 0.5f

            LaunchedEffect(uri) {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openFileDescriptor(Uri.parse(uri), "r")
                        ?.use { descriptor ->
                             pdfDocument.value = MainActivity.mainicore.newDocument(descriptor)

                           // wdocptr.value = idoc.mNativeDocPtr
                            pageCount.value = MainActivity.mainicore.getPageCount(pdfDocument.value)
                        }
                }
            }


            renderRunnable = Runnable {

            }


            val pageStates =
                remember { mutableStateMapOf<Int, MutableState<DrawingState>>() } // Birden fazla sayfa için DrawingState listesi

            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)


            var showSaveDialog = remember {
                mutableStateOf(false)
            }

            var paletteVisible by remember {
                mutableStateOf(false)
            }

            val configuration = LocalConfiguration.current
            val density = LocalDensity.current.density
            val screenWidthPx =
                configuration.screenWidthDp * density // Ekran genişliği (px cinsinden)
            val screenHeightPx =
                configuration.screenHeightDp * density // Ekran yüksekliği (px cinsinden)


            var scale = remember { mutableStateOf(1f) }
            var offset = remember { mutableStateOf(Offset.Zero) }


            val edit = pdfViewerActivityViewModel.edit.collectAsState()

            var highlight = remember {
                mutableStateOf(false)
            }

            var selectedcolor = remember {
                mutableStateOf<Color>(Color.Black)
            }

            var colorPaletteVisible by remember {
                mutableStateOf(true)
            }

            var undo = remember {
                mutableStateOf(false)
            }

            var earse = remember {
                mutableStateOf(false)
            }


            var sliderPosition = remember {
                // ScrollState'in başlangıçta olduğu yerde slider da başlar
                mutableStateOf(0f)
            }
            val scrollState = rememberScrollState()
            var columnHeight by remember { mutableStateOf(0) }

            var scrollbarvisibilty by remember {
                mutableStateOf(true)
            }

            var draw = remember {
                mutableStateOf(false)
            }

            var pan = remember {
                mutableStateOf(false)
            }

            var canScroll = remember {
                mutableStateOf(true)
            }
            var mthickness = remember {
                mutableStateOf(5f)
            }
            var thickness by remember { mutableStateOf(10f) }

            var barheight = remember {
                mutableStateOf(screenHeightPx * scale.value)
            }

            if (draw.value) {
                Log.d("drawww", "ok")
            } else {
                Log.d("drawww", "no")

            }

            var mcolor = remember {
                mutableStateOf<Color>(Color.Black)
            }

            LaunchedEffect(
                key1 = colorPaletteVisible,
                key2 = scrollState.value,
                key3 = scrollbarvisibilty
            ) {
                if (colorPaletteVisible) {

                    delay(2000L)
                    colorPaletteVisible = colorPaletteVisible.not()
                }

                if (scrollbarvisibilty) {

                    delay(2000L)
                    scrollbarvisibilty = scrollbarvisibilty.not()
                }

                val proportion = scrollState.value.toFloat() / scrollState.maxValue.toFloat()
                sliderPosition.value = proportion
            }

            DocuNoteTheme() {

                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        colorResource(R.color.modified).toArgb(),
                        colorResource(R.color.modified).toArgb()
                    )
                )
                ModalNavigationDrawer(
                    modifier = Modifier
                        .fillMaxSize(),
                    gesturesEnabled = false,
                    drawerState = drawerState,
                    scrimColor = Color.Transparent,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerContainerColor = Color(0xFF121212),
                            drawerShape = RectangleShape,
                            modifier = Modifier
                                .displayCutoutPadding()
                                .statusBarsPadding()


                        ) {

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .verticalScroll(state = rememberScrollState())
                            ) {
                                renderedPages.forEach { bitmap ->
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "",
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                        }
                    }
                ) {
                    Scaffold(

                        containerColor = MaterialTheme.colorScheme.background,
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = "$displayName",
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1
                                    )
                                },
                                navigationIcon = {

                                    if (!edit.value) {
                                        IconButton(onClick = {
                                            scope.launch {
                                                if (drawerState.isClosed) {
                                                    drawerState.open()
                                                } else {
                                                    drawerState.close()
                                                }
                                            }
                                        }

                                        ) {
                                            Icon(
                                                Icons.Default.Menu,
                                                contentDescription = "Menu",
                                                modifier = Modifier.width(100.dp).height(100.dp)
                                            )
                                        }
                                    } else {
                                        IconButton(onClick = {
                                            pdfViewerActivityViewModel.update_editState(false)
                                        }) {
                                            Icon(
                                                Icons.Default.ArrowBack,
                                                contentDescription = "Back",
                                                modifier = Modifier.width(100.dp).height(100.dp)
                                            )
                                        }
                                    }

                                },
                                actions = {

                                    OutlinedIconButton(onClick = {

                                        if (!edit.value) {
                                            pdfViewerActivityViewModel.update_editState(true)
                                        } else {

                                            pdfViewerActivityViewModel.update_editState(false)
                                        }
                                    }, border = null) {
                                        if (!edit.value) {
                                            Icon(
                                                painter = painterResource(R.drawable.edit_square_40px),
                                                "",
                                                modifier = Modifier
                                                    .size(24.dp)
                                            )
                                        } else {
                                            Icon(
                                                painter = painterResource(R.drawable.import_contacts_40px),
                                                "",
                                                modifier = Modifier
                                                    .size(24.dp)
                                            )
                                        }
                                    }
                                    if (edit.value) {
                                        FilledTonalButton(onClick = {
                                            scope.launch {
                                                // val bitmap = graphicsLayer.toImageBitmap()
                                                // do something with the newly acquired bitmap


                                                /*  Log.d("drawbitmap" , "bitmap list size in save button " + graphicsLayers.value.size)
                                                graphicsLayers.value.forEach {
                                                    val bitmap = it.toImageBitmap()
                                                    graphicsLayersBitmaps.value.toMutableList().add(bitmap)
                                                }*/

                                                /*  graphicLayersImageBitmaps = graphicsLayersBitmaps.value
                                                */



                                                //mrendererPages = renderedPages
                                                mpageStates = pageStates

                                                /*  val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                                                Log.d("pathslist" , "list size :: " + pageStates.size)
                                                creator.saveDrawingsToPDF(file = File(dir , "firstedited.pdf") , renderedPages ,
                                                    pageStates)*/

                                                showSaveDialog.value = showSaveDialog.value.not()

                                            }
                                        }) {
                                            Text(text = stringResource(R.string.save_copy))
                                        }
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxSize()

                    ) { innerPadding ->

                        if (showSaveDialog.value) {
                            SaveDocument(
                                create = false,
                                onDismissRequest = {
                                    showSaveDialog.value = showSaveDialog.value.not()
                                },
                                null
                            )
                        }





                        BoxWithConstraints(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()


                        )
                        {


                            LazyColumn(
                                userScrollEnabled = canScroll.value,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clipToBounds() // clip modifikatörünü doğru sırada kullanın
                                    .enhancedZoom(
                                        clip = true,
                                        enhancedZoomState = rememberEnhancedZoomState(
                                            IntSize(screenWidthPx.toInt(), screenHeightPx.toInt()),
                                            minZoom = 0.75f,
                                            maxZoom = 6f,
                                            pannable = if (draw.value) false else true,
                                            initialZoom = 1f,
                                            moveToBounds = true,

                                            )
                                    )


                            ) {
                                items(pageCount.value) { pageIndex ->
                                    val bitmapState = remember { mutableStateOf<Bitmap?>(null) }

                                    // Render the page when it comes into view
                                    LaunchedEffect(pageIndex) {
                                        if (bitmapState.value == null ) {
                                            if (pdfDocument.value != null){
                                                val bitmap = pdfBitmapConverter.internalRenderPage(
                                                    pdfDocument.value!!,
                                                    pageIndex,
                                                    scaleFactor
                                                )
                                                bitmapState.value = bitmap
                                            }
                                        }
                                    }

                                    val state = remember { mutableStateOf(DrawingState()) }



                                    if (!highlight.value) {
                                        selectedcolor.value = mcolor.value
                                        pageStates[pageIndex]?.value?.selectedColor =
                                            selectedcolor.value

                                    } else {
                                        selectedcolor.value = mcolor.value.copy(0.2f)
                                        pageStates[pageIndex]?.value?.selectedColor =
                                            selectedcolor.value.copy(0.2f)
                                    }


                                    pageStates[pageIndex]?.value?.thickness =
                                        thickness

                                    if (undo.value) {

                                        pageStates[pageIndex]?.value =
                                            pageStates[pageIndex]?.value!!.copy(
                                                paths = pageStates[pageIndex]?.value?.paths!!.dropLast(
                                                    1
                                                )
                                            )
                                        undo.value = undo.value.not()
                                    }


                                    state.value.isErasing = earse.value

                                    if (!pageStates.contains(pageIndex)) {
                                        pageStates[pageIndex] = state
                                    }


                                    // Display the rendered page
                                    bitmapState.value?.let { bitmap ->

                                        if (!edit.value) {

                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "Page $pageIndex",
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        } else {

                                            DrawingScreen(
                                                draw,
                                                scale,
                                                offset,
                                                // transformablestate ,
                                                bitmap,
                                                state = pageStates[pageIndex] ?: state,
                                                modifier = Modifier
                                                //  .transformable(state = transformablestate)
                                                //  .aspectRatio(PDRectangle.A4.width / PDRectangle.A4.height)


                                            )
                                        }
                                    }

                                    // Add a placeholder while the page is being rendered
                                    if (bitmapState.value == null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp) // Adjust height as needed
                                                .background(Color.LightGray)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.align(
                                                    Alignment.Center
                                                )
                                            )
                                        }
                                    }
                                }
                                /* itemsIndexed(renderedPages){ pageIndex , page ->




                                     val state = remember { mutableStateOf(DrawingState()) }


                                     state.value.selectedColor = selectedcolor.value

                                     state.value.thickness = thicknessTextfield.value.toFloat()

                                     if (undo.value) {

                                         state.value = state.value.copy(
                                             paths = state.value.paths.dropLast(1)
                                         )
                                         undo.value = undo.value.not()
                                     }


                                     state.value.isErasing = earse.value


                                     pageStates.add(state)


                                     val graphicsLayer = rememberGraphicsLayer()

                                     AsyncImage(model = page , contentDescription = "")
                                     /* Box(
                                          modifier = Modifier
                                              .padding(bottom = 16.dp)
                                              .drawWithContent {


                                                  if (PDFViewerActivity.Companion.elayers.size <= index) {
                                                      PDFViewerActivity.Companion.elayers.add(
                                                          graphicsLayer
                                                      )
                                                      graphicsLayer.record {
                                                          this@drawWithContent.drawContent()
                                                      }
                                                  }
                                                  drawLayer(PDFViewerActivity.Companion.elayers[index])


                                              }


                                      ) {

                                          if (ok.value){

                                              /*  AndroidView(

                                                    factory = { context ->
                                                        MyGLSurfaceView(context,pdfBitmapConverter, Uri.parse(uri) , renderedPages)
                                                    },
                                                    modifier = Modifier
                                                        .aspectRatio(PDRectangle.A4.width / PDRectangle.A4.height)// Bu kısmı istediğiniz gibi ayarlayın
                                                )*/

                                              DrawingScreen(
                                                  draw,
                                                  scale,
                                                  offset,
                                                  // transformablestate ,
                                                  page,
                                                  pageStates[index],
                                                  modifier = Modifier
                                                  //  .transformable(state = transformablestate)
                                                  //  .aspectRatio(PDRectangle.A4.width / PDRectangle.A4.height)


                                              )
                                          }
                                          /*  DrawingScreen(
                                                draw,
                                                scale,
                                                offset,
                                                // transformablestate ,
                                                page,
                                                pageStates[index],
                                                modifier = Modifier
                                                    //  .transformable(state = transformablestate)
                                                  //  .aspectRatio(PDRectangle.A4.width / PDRectangle.A4.height)


                                            )*/
                                      }*/
                                 }*/
                            }





                            if (paletteVisible) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    shadowElevation = 8.dp,
                                    color = Color.White,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                        .height(300.dp)
                                        .align(Alignment.BottomCenter)
                                        .navigationBarsPadding()
                                    //.offset(0.dp, -60.dp)
                                ) {


                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                    ) {


                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {

                                            Canvas(
                                                modifier = Modifier
                                                    .width(100.dp)
                                                    .height(100.dp)
                                            ) {
                                                val path = Path().apply {
                                                    moveTo(0f, size.height / 2)
                                                    quadraticBezierTo(
                                                        size.width / 4,
                                                        size.height / 4,
                                                        size.width / 2,
                                                        size.height / 2
                                                    )
                                                    quadraticBezierTo(
                                                        size.width * 3 / 4,
                                                        size.height * 3 / 4,
                                                        size.width,
                                                        size.height / 2
                                                    )
                                                }

                                                drawPath(
                                                    path = path,
                                                    color = Color.Green,
                                                    style = Stroke(
                                                        width = thickness,
                                                        cap = StrokeCap.Round
                                                    )
                                                )
                                            }
                                            Slider(
                                                value = thickness,
                                                onValueChange = { thickness = it },
                                                valueRange = 5f..50f,
                                                modifier = Modifier.fillMaxWidth()
                                            )


                                        }


                                        Row(
                                            modifier = Modifier
                                                .horizontalScroll(rememberScrollState())
                                        ) {

                                            colorlist.value.forEachIndexed { index, clist ->
                                                Column {
                                                    clist.palet.forEachIndexed { index, color ->

                                                        val isSelected =
                                                            selectedcolor.value == color

                                                        Box(
                                                            modifier = Modifier

                                                                .background(color) // Set the background color
                                                                .size(24.dp) // Set the size of the box
                                                                .graphicsLayer {
                                                                    val scale =
                                                                        if (isSelected) 0.5f else 1f
                                                                    scaleX = scale
                                                                    scaleY = scale
                                                                }
                                                                .border(
                                                                    width = 1.dp,
                                                                    color = if (selectedcolor.value == color) {
                                                                        Color.Black
                                                                    } else {
                                                                        Color.Transparent
                                                                    },
                                                                    shape = RectangleShape
                                                                )
                                                                .clickable(
                                                                    enabled = true,
                                                                    role = Role.Checkbox,
                                                                    onClick = {

                                                                        mcolor.value = color
                                                                        if (highlight.value) {

                                                                            selectedcolor.value =
                                                                                color.copy(0.2f)
                                                                        } else {
                                                                            selectedcolor.value =
                                                                                color
                                                                        }
                                                                    }
                                                                )


                                                        )
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }


                            }

                            if (edit.value) {

                                Surface(
                                    shape = RectangleShape,
                                    // color = colorResource(id = R.color.e),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp)
                                        .align(Alignment.BottomCenter)


                                )
                                {

                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .padding(5.dp)
                                    )
                                    {
                                        OutlinedIconButton(
                                            onClick = {

                                                if (!colorPaletteVisible) {
                                                    colorPaletteVisible =
                                                        colorPaletteVisible.not()
                                                }
                                                if (highlight.value) {
                                                    highlight.value = highlight.value.not()
                                                }
                                                if (earse.value) {
                                                    earse.value = earse.value.not()
                                                }
                                            },
                                            border = null,
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = Color.Transparent
                                            ),
                                            modifier = Modifier
                                                .size(24.dp)
                                        )
                                        {
                                            Icon(
                                                painter = painterResource(R.drawable.pen_icon),
                                                contentDescription = "",
                                                modifier = Modifier
                                                    .size(16.dp)
                                            )
                                        }
                                        OutlinedIconButton(
                                            onClick = {

                                                if (!colorPaletteVisible) {
                                                    colorPaletteVisible =
                                                        colorPaletteVisible.not()
                                                }

                                                if (earse.value) {
                                                    earse.value = earse.value.not()
                                                }
                                                highlight.value = highlight.value.not()
                                            },
                                            border = null,
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = Color.Transparent
                                            ),
                                            modifier = Modifier
                                                .size(24.dp)

                                        )
                                        {
                                            Icon(
                                                painter = painterResource(R.drawable.brush_icon),
                                                contentDescription = "",
                                                modifier = Modifier

                                                    .size(24.dp)
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .background(selectedcolor.value)
                                                .clickable {
                                                    paletteVisible = paletteVisible.not()

                                                }

                                                .size(16.dp)
                                        )
                                        OutlinedIconButton(
                                            onClick = {
                                                undo.value = undo.value.not()
                                            },
                                            border = null,
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = Color.Transparent
                                            ),
                                            modifier = Modifier
                                                .size(24.dp)

                                        )
                                        {
                                            Icon(
                                                painter = painterResource(R.drawable.undo_icon),
                                                contentDescription = "",
                                                modifier = Modifier

                                                    .size(24.dp)
                                            )
                                        }
                                        OutlinedIconButton(
                                            onClick = {
                                                earse.value = earse.value.not()
                                            },
                                            border = null,
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = Color.Transparent
                                            ),
                                            modifier = Modifier
                                                .size(24.dp)


                                        )
                                        {
                                            Icon(
                                                painter = painterResource(R.drawable.erase_icon),
                                                contentDescription = "",
                                                modifier = Modifier

                                                    .size(24.dp)
                                            )
                                        }
                                        OutlinedIconButton(
                                            onClick = {
                                                draw.value = draw.value.not()
                                            },
                                            border = null,
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = Color.Transparent
                                            ),
                                            modifier = Modifier
                                                .size(24.dp)


                                        )
                                        {
                                            if (draw.value) {
                                                Icon(
                                                    painter = painterResource(R.drawable.lock_open_24px),
                                                    contentDescription = "",
                                                    modifier = Modifier

                                                        .size(24.dp)
                                                )

                                            } else {
                                                Icon(
                                                    painter = painterResource(R.drawable.lock_24px),
                                                    contentDescription = "",
                                                    modifier = Modifier

                                                        .size(24.dp)
                                                )

                                            }
                                        }


                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }
        @Composable
        fun getColorsFromResources(): List<mColor> {
            var mclist: List<mColor> = emptyList()
            val iter = mclist.toMutableList()

            val redColors = listOf(

                // Red

                colorResource(id = R.color.red50),
                colorResource(id = R.color.red100),
                colorResource(id = R.color.red200),
                colorResource(id = R.color.red300),
                colorResource(id = R.color.red400),
                colorResource(id = R.color.red500),
                colorResource(id = R.color.red600),
                colorResource(id = R.color.red700),
                colorResource(id = R.color.red800),
                colorResource(id = R.color.red900),
                colorResource(id = R.color.reda100),
                colorResource(id = R.color.reda200),
                colorResource(id = R.color.reda400),
                colorResource(id = R.color.reda700),
            )

            val mcred = mColor(0, redColors)
            iter.add(mcred)

            val pinkColors = listOf(

                // Pink
                colorResource(id = R.color.pink50),
                colorResource(id = R.color.pink100),
                colorResource(id = R.color.pink200),
                colorResource(id = R.color.pink300),
                colorResource(id = R.color.pink400),
                colorResource(id = R.color.pink500),
                colorResource(id = R.color.pink600),
                colorResource(id = R.color.pink700),
                colorResource(id = R.color.pink800),
                colorResource(id = R.color.pink900),
                colorResource(id = R.color.pinka100),
                colorResource(id = R.color.pinka200),
                colorResource(id = R.color.pinka400),
                colorResource(id = R.color.pinka700),
            )

            val mcpink = mColor(1, pinkColors)

            iter.add(mcpink)
            val purpleColors = listOf(
                // Purple
                colorResource(id = R.color.purple50),
                colorResource(id = R.color.purple100),
                colorResource(id = R.color.purple200),
                colorResource(id = R.color.purple300),
                colorResource(id = R.color.purple400),
                colorResource(id = R.color.purple500),
                colorResource(id = R.color.purple600),
                colorResource(id = R.color.purple700),
                colorResource(id = R.color.purple800),
                colorResource(id = R.color.purple900),
                colorResource(id = R.color.purplea100),
                colorResource(id = R.color.purplea200),
                colorResource(id = R.color.purplea400),
                colorResource(id = R.color.purplea700),
            )

            val mcpurple = mColor(2, purpleColors)

            iter.add(mcpurple)
            val deeppurpleColors = listOf(

                // Deep Purple
                colorResource(id = R.color.deeppurple50),
                colorResource(id = R.color.deeppurple100),
                colorResource(id = R.color.deeppurple200),
                colorResource(id = R.color.deeppurple300),
                colorResource(id = R.color.deeppurple400),
                colorResource(id = R.color.deeppurple500),
                colorResource(id = R.color.deeppurple600),
                colorResource(id = R.color.deeppurple700),
                colorResource(id = R.color.deeppurple800),
                colorResource(id = R.color.deeppurple900),
                colorResource(id = R.color.deeppurpleA100),
                colorResource(id = R.color.deeppurpleA200),
                colorResource(id = R.color.deeppurpleA400),
                colorResource(id = R.color.deeppurpleA700),
            )

            val mcdeeppurpleColors = mColor(3, deeppurpleColors)

            iter.add(mcdeeppurpleColors)
            val indigoColors = listOf(

                // Indigo
                colorResource(id = R.color.indigo50),
                colorResource(id = R.color.indigo100),
                colorResource(id = R.color.indigo200),
                colorResource(id = R.color.indigo300),
                colorResource(id = R.color.indigo400),
                colorResource(id = R.color.indigo500),
                colorResource(id = R.color.indigo600),
                colorResource(id = R.color.indigo700),
                colorResource(id = R.color.indigo800),
                colorResource(id = R.color.indigo900),
                colorResource(id = R.color.indigoA100),
                colorResource(id = R.color.indigoA200),
                colorResource(id = R.color.indigoA400),
                colorResource(id = R.color.indigoA700),
            )

            val mcindigoColors = mColor(4, indigoColors)

            iter.add(mcindigoColors)
            val blueColors = listOf(

                // Blue
                colorResource(id = R.color.blue50),
                colorResource(id = R.color.blue100),
                colorResource(id = R.color.blue200),
                colorResource(id = R.color.blue300),
                colorResource(id = R.color.blue400),
                colorResource(id = R.color.blue500),
                colorResource(id = R.color.blue600),
                colorResource(id = R.color.blue700),
                colorResource(id = R.color.blue800),
                colorResource(id = R.color.blue900),
                colorResource(id = R.color.blueA100),
                colorResource(id = R.color.blueA200),
                colorResource(id = R.color.blueA400),
                colorResource(id = R.color.blueA700),
            )

            val mcblueColors = mColor(5, blueColors)
            iter.add(mcblueColors)
            val lightblueColors = listOf(

                // Light Blue
                colorResource(id = R.color.lightblue50),
                colorResource(id = R.color.lightblue100),
                colorResource(id = R.color.lightblue200),
                colorResource(id = R.color.lightblue300),
                colorResource(id = R.color.lightblue400),
                colorResource(id = R.color.lightblue500),
                colorResource(id = R.color.lightblue600),
                colorResource(id = R.color.lightblue700),
                colorResource(id = R.color.lightblue800),
                colorResource(id = R.color.lightblue900),
                colorResource(id = R.color.lightblueA100),
                colorResource(id = R.color.lightblueA200),
                colorResource(id = R.color.lightblueA400),
                colorResource(id = R.color.lightblueA700),
            )

            val mclightblueColors = mColor(6, lightblueColors)
            iter.add(mclightblueColors)
            val cyanColors = listOf(

                // Cyan
                colorResource(id = R.color.cyan50),
                colorResource(id = R.color.cyan100),
                colorResource(id = R.color.cyan200),
                colorResource(id = R.color.cyan300),
                colorResource(id = R.color.cyan400),
                colorResource(id = R.color.cyan500),
                colorResource(id = R.color.cyan600),
                colorResource(id = R.color.cyan700),
                colorResource(id = R.color.cyan800),
                colorResource(id = R.color.cyan900),
                colorResource(id = R.color.cyanA100),
                colorResource(id = R.color.cyanA200),
                colorResource(id = R.color.cyanA400),
                colorResource(id = R.color.cyanA700),
            )

            val mccyanColors = mColor(7, cyanColors)
            iter.add(mccyanColors)
            val tealColors = listOf(

                // Teal
                colorResource(id = R.color.teal50),
                colorResource(id = R.color.teal100),
                colorResource(id = R.color.teal200),
                colorResource(id = R.color.teal300),
                colorResource(id = R.color.teal400),
                colorResource(id = R.color.teal500),
                colorResource(id = R.color.teal600),
                colorResource(id = R.color.teal700),
                colorResource(id = R.color.teal800),
                colorResource(id = R.color.teal900),
                colorResource(id = R.color.tealA100),
                colorResource(id = R.color.tealA200),
                colorResource(id = R.color.tealA400),
                colorResource(id = R.color.tealA700),
            )

            val mctealColors = mColor(8, tealColors)
            iter.add(mctealColors)
            val greenColors = listOf(

                // Green
                colorResource(id = R.color.green50),
                colorResource(id = R.color.green100),
                colorResource(id = R.color.green200),
                colorResource(id = R.color.green300),
                colorResource(id = R.color.green400),
                colorResource(id = R.color.green500),
                colorResource(id = R.color.green600),
                colorResource(id = R.color.green700),
                colorResource(id = R.color.green800),
                colorResource(id = R.color.green900),
                colorResource(id = R.color.greenA100),
                colorResource(id = R.color.greenA200),
                colorResource(id = R.color.greenA400),
                colorResource(id = R.color.greenA700),
            )

            val mcgreenColors = mColor(9, greenColors)
            iter.add(mcgreenColors)
            val lightgreenColors = listOf(

                // Light Green
                colorResource(id = R.color.lightgreen50),
                colorResource(id = R.color.lightgreen100),
                colorResource(id = R.color.lightgreen200),
                colorResource(id = R.color.lightgreen300),
                colorResource(id = R.color.lightgreen400),
                colorResource(id = R.color.lightgreen500),
                colorResource(id = R.color.lightgreen600),
                colorResource(id = R.color.lightgreen700),
                colorResource(id = R.color.lightgreen800),
                colorResource(id = R.color.lightgreen900),
                colorResource(id = R.color.lightgreenA100),
                colorResource(id = R.color.lightgreenA200),
                colorResource(id = R.color.lightgreenA400),
                colorResource(id = R.color.lightgreenA700),
            )

            val mclightgreenColors = mColor(10, lightgreenColors)
            iter.add(mclightgreenColors)
            val limeColors = listOf(

                // Lime
                colorResource(id = R.color.lime50),
                colorResource(id = R.color.lime100),
                colorResource(id = R.color.lime200),
                colorResource(id = R.color.lime300),
                colorResource(id = R.color.lime400),
                colorResource(id = R.color.lime500),
                colorResource(id = R.color.lime600),
                colorResource(id = R.color.lime700),
                colorResource(id = R.color.lime800),
                colorResource(id = R.color.lime900),
                colorResource(id = R.color.limeA100),
                colorResource(id = R.color.limeA200),
                colorResource(id = R.color.limeA400),
                colorResource(id = R.color.limeA700),
            )

            val mclimeColors = mColor(11, limeColors)
            iter.add(mclimeColors)
            val yellowColors = listOf(

                // Yellow
                colorResource(id = R.color.yellow50),
                colorResource(id = R.color.yellow100),
                colorResource(id = R.color.yellow200),
                colorResource(id = R.color.yellow300),
                colorResource(id = R.color.yellow400),
                colorResource(id = R.color.yellow500),
                colorResource(id = R.color.yellow600),
                colorResource(id = R.color.yellow700),
                colorResource(id = R.color.yellow800),
                colorResource(id = R.color.yellow900),
                colorResource(id = R.color.yellowA100),
                colorResource(id = R.color.yellowA200),
                colorResource(id = R.color.yellowA400),
                colorResource(id = R.color.yellowA700),
            )

            val mcyellowColors = mColor(12, yellowColors)
            iter.add(mcyellowColors)
            val amberColors = listOf(

                // Amber
                colorResource(id = R.color.amber50),
                colorResource(id = R.color.amber100),
                colorResource(id = R.color.amber200),
                colorResource(id = R.color.amber300),
                colorResource(id = R.color.amber400),
                colorResource(id = R.color.amber500),
                colorResource(id = R.color.amber600),
                colorResource(id = R.color.amber700),
                colorResource(id = R.color.amber800),
                colorResource(id = R.color.amber900),
                colorResource(id = R.color.amberA100),
                colorResource(id = R.color.amberA200),
                colorResource(id = R.color.amberA400),
                colorResource(id = R.color.amberA700),
            )

            val mcamberColors = mColor(13, amberColors)
            iter.add(mcamberColors)
            val orangeColors = listOf(

                // Orange
                colorResource(id = R.color.orange50),
                colorResource(id = R.color.orange100),
                colorResource(id = R.color.orange200),
                colorResource(id = R.color.orange300),
                colorResource(id = R.color.orange400),
                colorResource(id = R.color.orange500),
                colorResource(id = R.color.orange600),
                colorResource(id = R.color.orange700),
                colorResource(id = R.color.orange800),
                colorResource(id = R.color.orange900),
                colorResource(id = R.color.orangeA100),
                colorResource(id = R.color.orangeA200),
                colorResource(id = R.color.orangeA400),
                colorResource(id = R.color.orangeA700),
            )

            val mcorangeColors = mColor(14, orangeColors)
            iter.add(mcorangeColors)
            val deeporangecolors = listOf(

                // Deep Orange
                colorResource(id = R.color.deeporange50),
                colorResource(id = R.color.deeporange100),
                colorResource(id = R.color.deeporange200),
                colorResource(id = R.color.deeporange300),
                colorResource(id = R.color.deeporange400),
                colorResource(id = R.color.deeporange500),
                colorResource(id = R.color.deeporange600),
                colorResource(id = R.color.deeporange700),
                colorResource(id = R.color.deeporange800),
                colorResource(id = R.color.deeporange900),
                colorResource(id = R.color.deeporangeA100),
                colorResource(id = R.color.deeporangeA200),
                colorResource(id = R.color.deeporangeA400),
                colorResource(id = R.color.deeporangeA700),
            )

            val mcdeeporangecolors = mColor(15, deeporangecolors)
            iter.add(mcdeeporangecolors)
            val brownColors = listOf(

                // Brown
                colorResource(id = R.color.brown50),
                colorResource(id = R.color.brown100),
                colorResource(id = R.color.brown200),
                colorResource(id = R.color.brown300),
                colorResource(id = R.color.brown400),
                colorResource(id = R.color.brown500),
                colorResource(id = R.color.brown600),
                colorResource(id = R.color.brown700),
                colorResource(id = R.color.brown800),
                colorResource(id = R.color.brown900),
            )

            val mcbrownColors = mColor(16, brownColors)
            iter.add(mcbrownColors)
            val greyColors = listOf(

                // Grey
                colorResource(id = R.color.grey50),
                colorResource(id = R.color.grey100),
                colorResource(id = R.color.grey200),
                colorResource(id = R.color.grey300),
                colorResource(id = R.color.grey400),
                colorResource(id = R.color.grey500),
                colorResource(id = R.color.grey600),
                colorResource(id = R.color.grey700),
                colorResource(id = R.color.grey800),
                colorResource(id = R.color.grey900),
            )

            val mcgreyColors = mColor(17, greyColors)
            iter.add(mcgreyColors)
            val bluegrayColors = listOf(

                // Blue Gray
                colorResource(id = R.color.bluegray50),
                colorResource(id = R.color.bluegray100),
                colorResource(id = R.color.bluegray200),
                colorResource(id = R.color.bluegray300),
                colorResource(id = R.color.bluegray400),
                colorResource(id = R.color.bluegray500),
                colorResource(id = R.color.bluegray600),
                colorResource(id = R.color.bluegray700),
                colorResource(id = R.color.bluegray800),
                colorResource(id = R.color.bluegray900),
            )

            val mcbluegrayColors = mColor(18, bluegrayColors)
            iter.add(mcbluegrayColors)
            val stoneColors = listOf(

                // Stone
                colorResource(id = R.color.stone950),
                colorResource(id = R.color.stone900),
                colorResource(id = R.color.stone800),
                colorResource(id = R.color.stone700),
                colorResource(id = R.color.stone600),
                colorResource(id = R.color.stone500),
                colorResource(id = R.color.stone400),
                colorResource(id = R.color.stone300),
                colorResource(id = R.color.stone200),
                colorResource(id = R.color.stone100),
                colorResource(id = R.color.stone50),
            )

            val mcstoneColors = mColor(19, stoneColors)
            iter.add(mcstoneColors)
            val neutralColors = listOf(

                // Neutral
                colorResource(id = R.color.neutral950),
                colorResource(id = R.color.neutral900),
                colorResource(id = R.color.neutral800),
                colorResource(id = R.color.neutral700),
                colorResource(id = R.color.neutral600),
                colorResource(id = R.color.neutral500),
                colorResource(id = R.color.neutral400),
                colorResource(id = R.color.neutral300),
                colorResource(id = R.color.neutral200),
                colorResource(id = R.color.neutral100),
                colorResource(id = R.color.neutral50),
            )

            val mcneutralColors = mColor(20, neutralColors)
            iter.add(mcneutralColors)
            val zincColors = listOf(

                // Zinc
                colorResource(id = R.color.zinc950),
                colorResource(id = R.color.zinc900),
                colorResource(id = R.color.zinc800),
                colorResource(id = R.color.zinc700),
                colorResource(id = R.color.zinc600),
                colorResource(id = R.color.zinc500),
                colorResource(id = R.color.zinc400),
                colorResource(id = R.color.zinc300),
                colorResource(id = R.color.zinc200),
                colorResource(id = R.color.zinc100),
                colorResource(id = R.color.zinc50),
            )

            val mczincColors = mColor(21, zincColors)
            iter.add(mczincColors)
            val slateColors = listOf(

                // Slate
                colorResource(id = R.color.slate950),
                colorResource(id = R.color.slate900),
                colorResource(id = R.color.slate800),
                colorResource(id = R.color.slate700),
                colorResource(id = R.color.slate600),
                colorResource(id = R.color.slate500),
                colorResource(id = R.color.slate400),
                colorResource(id = R.color.slate300),
                colorResource(id = R.color.slate200),
                colorResource(id = R.color.slate100),
                colorResource(id = R.color.slate50),
            )

            val mcslateColors = mColor(22, slateColors)
            iter.add(mcslateColors)
            val emeraldColors = listOf(

                // Emerald
                colorResource(id = R.color.emerald950),
                colorResource(id = R.color.emerald900),
                colorResource(id = R.color.emerald800),
                colorResource(id = R.color.emerald700),
                colorResource(id = R.color.emerald600),
                colorResource(id = R.color.emerald500),
                colorResource(id = R.color.emerald400),
                colorResource(id = R.color.emerald300),
                colorResource(id = R.color.emerald200),
                colorResource(id = R.color.emerald100),
                colorResource(id = R.color.emerald50),
            )

            val mcemeraldColors = mColor(23, emeraldColors)
            iter.add(mcemeraldColors)
            val roseColors = listOf(

                // Rose
                colorResource(id = R.color.rose950),
                colorResource(id = R.color.rose900),
                colorResource(id = R.color.rose800),
                colorResource(id = R.color.rose700),
                colorResource(id = R.color.rose600),
                colorResource(id = R.color.rose500),
                colorResource(id = R.color.rose400),
                colorResource(id = R.color.rose300),
                colorResource(id = R.color.rose200),
                colorResource(id = R.color.rose100),
                colorResource(id = R.color.rose50)
            )

            val mcroseColors = mColor(24, roseColors)
            iter.add(mcroseColors)

            mclist = iter

            return mclist

        }

        @Preview(showBackground = true)
        @Composable
        fun GreetingPreview2() {
            DocuNoteTheme {
            }
        }
}

