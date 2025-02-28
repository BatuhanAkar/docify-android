package com.batuscode.docunote

import android.app.Activity
import android.app.ComponentCaller
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.viewmodel.CreatePDFActivityViewModel
import kotlinx.coroutines.launch
import kotlin.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.batuscode.docunote.model.Document
import com.batuscode.docunote.model.mColor
import com.batuscode.docunote.utils.FileManager
import com.batuscode.docunote.utils.PDFConverter
import com.batuscode.docunote.utils.PDFCreator
import com.batuscode.docunote.view.DrawingState
import com.batuscode.docunote.view.SaveDocument
import com.batuscode.pdfium.PDFPage
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import com.batuscode.pdfium.PdfDocument
import com.batuscode.pdfium.icore
import kotlin.properties.Delegates

class CreatePDFActivity : ComponentActivity() {
    companion object {
        lateinit var mcreatePDFActivityViewModel: CreatePDFActivityViewModel
        lateinit var pdfActivity: CreatePDFActivity
        lateinit var pagesContainer: List<Document>
        lateinit var cpageStates: MutableList<MutableState<DrawingState>>
        lateinit var contentResolver: ContentResolver
        lateinit var context: Context
        lateinit var layers: MutableList<GraphicsLayer>
        var docptr: Long = 0

    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        caller: ComponentCaller
    ) {
        super.onActivityResult(requestCode, resultCode, data, caller)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            data?.let { it ->
                val uri = it.data
                Log.d("newuri" , uri.toString())

                lifecycleScope.launch(Dispatchers.Main){
                    mcreatePDFActivityViewModel.update_fileUri(uri!!)
                    mcreatePDFActivityViewModel.update_saveFlag(true)
                }
            }
        }
        else if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            data?.let { it ->
                val uri = it.data
                Log.d("newuri" , uri.toString())

                lifecycleScope.launch(Dispatchers.Main){
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                    contentResolver.takePersistableUriPermission(uri!!, takeFlags)
                  //  val creator = PDFCreator()
                   // creator.createPage(uri = uri!! , docList = pagesContainer , Companion.contentResolver)

/*  mcreatePDFActivityViewModel.update_fileUri(uri!!)
                    mcreatePDFActivityViewModel.update_write(true)*/

                }
            }
        }

    }



    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val createPDFActivityViewModel: CreatePDFActivityViewModel by viewModels()
        mcreatePDFActivityViewModel = createPDFActivityViewModel
        pdfActivity = this
        Companion.contentResolver = contentResolver

        cpageStates = mutableStateListOf()

        val creator = PDFCreator(this)
        val filemanager = FileManager(this)
        val converter = PDFConverter(this)



        setContent {
            context = LocalContext.current
            DocuNoteTheme() {
                enableEdgeToEdge()
                val textStates = remember { mutableStateMapOf<Int, RichTextState>() }
                var documentPtr: Long = 0
                var pages = remember {
                    mutableStateListOf<PDFPage>()
                }
                documentPtr = MainActivity.mainicore.createDocument()

                LaunchedEffect(Unit) {

                 /*   var draftReady = creator.saveDraft()

                    if (draftReady){
                        val uri = filemanager.getDraftUri(context)
                        val page = converter.renderDraftDoc(context, Uri.parse(uri))
                        if (page!= null){
                            pages.add(page) // Add the page to the list
                            Log.d("ownCreator" , "page is created")
                        } else {
                            Log.e("ownCreator" , "page is null")
                        }
                    } else {

                        Log.d("ownCreator" , "draft not ready")
                    }*/


                    Log.d("page count" , pages.size.toString())
                    if (documentPtr != 0L) {
                        val page = PDFPage(595f, 842f) // A4 size
                        val ok = MainActivity.mainicore.addPage(documentPtr, page)
                        if (ok != 0L){

                            Log.d("page count" , "first" + documentPtr)
                            documentPtr=ok

                            Log.d("page count" , "after" + documentPtr)
                            docptr=ok

                            Log.d("page count" , "after main" + docptr)
                            pages.add(page) // Add the page to the list
                            Log.d("page count" , pages.size.toString())
                        }
                    }

                }

                val scope = rememberCoroutineScope()

                var index = remember {
                    mutableIntStateOf(0)
                }

                var pageNumber = remember {
                    mutableIntStateOf(1)
                }
               /* var pages = remember {
                    mutableStateOf<MutableList<Document>>(mutableStateListOf())
                }*/


                var showSaveDialog = remember {
                    mutableStateOf(false)
                }

                var componentList = remember {
                    mutableStateListOf<@Composable () -> Unit>()
                }


                val layer = rememberGraphicsLayer()

                var pageBitmaps = remember {
                    mutableStateOf<MutableList<ImageBitmap>>(mutableStateListOf())
                }

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

                lateinit var pageBitmap: Bitmap

                val pageStates = remember { mutableListOf<MutableState<DrawingState>>() }
                val scaleFactor = resources.displayMetrics.densityDpi / 72f

                 layers = remember {
                    mutableStateListOf<GraphicsLayer>()
                }



                val configuration = LocalConfiguration.current
                val density = LocalDensity.current.density
                val screenWidthPx = configuration.screenWidthDp * density // Ekran genişliği (px cinsinden)
                val screenHeightPx = configuration.screenHeightDp * density // Ekran yüksekliği (px cinsinden)

                var chighlight = remember {
                    mutableStateOf(false)
                }
                var ccolorPaletteVisible by remember {
                    mutableStateOf(true)
                }
                var cundo = remember {
                    mutableStateOf(false)
                }
                var cearse = remember {
                    mutableStateOf(false)
                }
                var addPage = remember {
                    mutableStateOf(true)
                }
                var pdfDocument = remember {
                    mutableStateOf(PdfDocument())
                }

                LaunchedEffect(key1 = addPage.value , key2 = ccolorPaletteVisible) {



                    if (ccolorPaletteVisible){

                        delay(2000L)
                        ccolorPaletteVisible = ccolorPaletteVisible.not()
                    }
                }

                val frList = remember { mutableStateListOf<FocusRequester>() }
                val scrollState = rememberScrollState()
                var cpaletteVisible by remember {
                    mutableStateOf(false)
                }
                var cthicknessTextfield = remember {
                    mutableStateOf(5)
                }
                var ccolorlist = remember {
                    mutableStateOf<List<mColor>>(emptyList())
                }
               // ccolorlist.value = getcolor()
                var cselectedcolor = remember {
                    mutableStateOf<Color>(Color.Black)
                }
                var highlight = remember {
                    mutableStateOf(false)
                }
                var cdraw = remember {
                    mutableStateOf(true)
                }








                ModalNavigationDrawer(
                    modifier = Modifier
                        .fillMaxSize(),
                    gesturesEnabled = false,
                    drawerState = drawerState,
                    scrimColor = Color.Transparent,
                    drawerContent = {
                        ModalDrawerSheet (

                            drawerContainerColor = Color(0xFF121212) ,
                            drawerShape = RectangleShape ,
                            modifier = Modifier
                                .displayCutoutPadding()
                                .statusBarsPadding()


                        ) {

                            Column(
                                modifier = Modifier
                                    .verticalScroll(state = rememberScrollState())
                            ) {
                                pageBitmaps.value.forEach{
                                    bitmap ->
                                    Image(
                                        bitmap = bitmap ,
                                        contentDescription = "" ,
                                        modifier = Modifier
                                            .scale(0.25f))
                                }

/*  if (pageBitmaps.value != null){

                                    Image(bitmap = pageBitmaps.value!! , contentDescription = "")
                                }*/

                            }

                        }
                    }
                ) {
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background,
                        topBar = {
                            TopAppBar(
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                ),
                                title = { Text(stringResource(R.string.newfile)) },
                                actions = {
                                    FilledTonalButton(
                                        onClick = {
                                            showSaveDialog.value = showSaveDialog.value.not()
                                        },

                                        ) {
                                        Text(text = stringResource(R.string.save))
                                    }
                                } ,
                                navigationIcon = {
                                    IconButton(onClick = {
                                        scope.launch {
                                            if (drawerState.isClosed) {

                                                layers.forEachIndexed { index , layer ->
                                                    pageBitmaps.value.add(index , layer.toImageBitmap())
                                                }
                                                drawerState.open()
                                            } else {
                                                drawerState.close()
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menu" , modifier = Modifier
                                            .width(100.dp)
                                            .height(100.dp))
                                    }
                                }

                            )
                        },
                        floatingActionButton = {
                            FloatingActionButton(
                                containerColor = MaterialTheme.colorScheme.background,
                                onClick = {
                                   /* if (documentPtr != 0L) {
                                        val page = PDFPage(595f, 842f) // A4 size
                                        icore.addPage(documentPtr, page)
                                        pages.add(page) // Add the page to the list
                                        Log.d("page count" , pages.size.toString())
                                    }*/
                                } ,
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.note_add_20px) ,
                                    contentDescription = "extensions")
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                    ) { innerPadding ->

                        if (showSaveDialog.value){
                            SaveDocument(create = true, onDismissRequest = {showSaveDialog.value = showSaveDialog.value.not()} , textStates)
                        }

                        Box (
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                        ){
                            LazyColumn(
                                modifier = Modifier
                                    .matchParentSize()
                            ) {
                                if (pages.isNotEmpty()){
                                    itemsIndexed(pages){
                                            index , page ->
                                        val state = textStates.getOrPut(index) { rememberRichTextState() }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(page.width / page.height)
                                                .background(Color.White)
                                                .border(1.dp, Color.Gray)
                                        ) {

                                            // Simulate page content (e.g., text, images)
                                            RichTextEditor(
                                                singleLine = false,
                                                modifier = Modifier
                                                    .aspectRatio(page.width / page.height),
                                                state = state ,
                                                colors = RichTextEditorDefaults.richTextEditorColors(
                                                    disabledIndicatorColor = Color.Transparent ,
                                                    unfocusedIndicatorColor = Color.Transparent ,
                                                    cursorColor = Color.Black ,
                                                    focusedIndicatorColor = Color.Transparent ,
                                                    containerColor = Color.White ,
                                                    textColor = Color.Black

                                                )
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


/*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageScreen(cpageStates: MutableList<MutableState<DrawingState>> , modifier: Modifier = Modifier, pages: List<Document>){




 LaunchedEffect(Unit) {
        createPDFActivityViewModel.initializePage()
    }

    val docsState = rememberLazyListState()


    val docs = createPDFActivityViewModel._page.collectAsState()


    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density
    val screenWidthPx = configuration.screenWidthDp * density // Ekran genişliği (px cinsinden)
    val screenHeightPx = configuration.screenHeightDp * density // Ekran yüksekliği (px cinsinden)



    var chighlight = remember {
        mutableStateOf(false)
    }


    var ccolorPaletteVisible by remember {
        mutableStateOf(true)
    }

    var cundo = remember {
        mutableStateOf(false)
    }

    var cearse = remember {
        mutableStateOf(false)
    }


    LaunchedEffect(key1 = ccolorPaletteVisible) {
        if (ccolorPaletteVisible){

            delay(2000L)
            ccolorPaletteVisible = ccolorPaletteVisible.not()
        }
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val frList = remember { mutableStateListOf<FocusRequester>() }


    val scope = rememberCoroutineScope()

    val scaleFactor = LocalContext.current.resources.displayMetrics.densityDpi / 72f

    val width = (PDRectangle.A4.width * scaleFactor)
    val height = (PDRectangle.A4.height * scaleFactor)

    val scrollState = rememberScrollState()


    var scale = remember { mutableStateOf(1f) }
    var offset = remember { mutableStateOf(Offset.Zero) }


    var cpaletteVisible by remember {
        mutableStateOf(false)
    }


    var cthicknessTextfield = remember {
        mutableStateOf(5)
    }

    var ccolorlist = remember {
        mutableStateOf<List<mColor>>(emptyList())
    }

    var cselectedcolor = remember {
        mutableStateOf<Color>(Color.Black)
    }

    var highlight = remember {
        mutableStateOf(false)
    }

    var cdraw = remember {
        mutableStateOf(true)
    }


    Box (
        modifier = Modifier
            .fillMaxSize()
    ){

        Column (
            //state = docsState,
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(scrollState, enabled = true)
                .pointerInput(Unit) {
                    detectPointerTransformGestures(
                        onGestureStart = {
                        },
                        onGesture = { centroid: Offset,
                                      offsetChange: Offset,
                                      zoomChange: Float,
                                      gestureRotate: Float,
                                      mainPointerInputChange: PointerInputChange,
                                      pointerList: List<PointerInputChange> ->


                            val isMultiTouch = pointerList.size > 1

                            // Ölçeği güncelle, %75'e kadar küçülme ve maksimum 6x büyütme


                            if (isMultiTouch) {
                                pointerList.forEach {
                                    it.consume()
                                }

                                val oldScale = scale.value
                                var newScale =
                                    (scale.value * zoomChange).coerceIn(
                                        0.75f..6f
                                    )
                                // Görselin boyutunu hesapla
                                val contentWidthPx =
                                    screenWidthPx * newScale
                                val contentHeightPx =
                                    screenHeightPx * newScale


                                // Offset'i ekran boyutlarına göre sınırla
                                val maxOffsetX =
                                    (contentWidthPx - screenWidthPx).coerceAtLeast(
                                        0f
                                    ) / 2f
                                val maxOffsetY =
                                    (contentHeightPx - screenHeightPx).coerceAtLeast(
                                        0f
                                    ) / 2f


                                offset.value =
                                    (offset.value + centroid / oldScale) -
                                            (centroid / newScale + offsetChange / oldScale)


*/
/*  offset.value = Offset(
                                      x = offset.value.x.coerceIn(
                                          -maxOffsetX,
                                          maxOffsetX
                                      ),
                                      y = offset.value.y.coerceIn(
                                          -maxOffsetY,
                                          maxOffsetY
                                      )
                                  )*//*



                                scale.value = newScale




                            } else {
                                cdraw.value = true
                            }

                        },
                        onGestureEnd = {
                        }
                    )
                }
                .pointerInput(true) {

                    detectTapGestures(
                        onTap = {
                            if (cpaletteVisible) {
                                cpaletteVisible = cpaletteVisible.not()
                            }
                        },

                        )


                }
                .graphicsLayer(

                    translationX = -offset.value.x * scale.value,
                    translationY = -offset.value.y * scale.value,
                    scaleX = scale.value,
                    scaleY = scale.value,

                    transformOrigin = TransformOrigin(0.5f, 0f)
                )

        )
        {
            pages.forEachIndexed {
                    index , document ->

                val focusRequester = remember { FocusRequester() }
                frList.add(index , focusRequester)

                val cstate = remember { mutableStateOf(DrawingState()) }


                cstate.value.selectedColor = cselectedcolor.value

                if (chighlight.value){
                    cstate.value.thickness = 50f
                    cstate.value.selectedColor = cstate.value.selectedColor.copy(alpha = 0.2f)
                } else {
                    cstate.value.thickness = 5f
                    cstate.value.selectedColor = cstate.value.selectedColor
                }

                if (cundo.value){

                    cstate.value = cstate.value.copy(paths = cstate.value.paths.dropLast(1))
                    cundo.value = cundo.value.not()
                }


                cstate.value.isErasing = cearse.value


                cpageStates.add(index,cstate)



                val graphicsLayer = rememberGraphicsLayer()

                val rtstate = remember {
                    mutableStateOf(RichTextState())
                }
                document.text = rtstate.value


                // CreatePDFActivity.Companion.layers.add(graphicsLayer)
                Box (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .drawWithContent {



                            if (CreatePDFActivity.Companion.layers.size <= index) {
                                // val newLayer = GraphicsLayer()
                                CreatePDFActivity.Companion.layers.add(graphicsLayer)
                                graphicsLayer.record {
                                    this@drawWithContent.drawContent()
                                }
                            }
                            drawLayer(CreatePDFActivity.Companion.layers[index])


                        }

                ){


*/
/*Image(
                        bitmap = document.bitmap.asImageBitmap() ,
                        ""
                    )*//*





                    RichTextEditor(

                        minLines = 1 ,
                        maxLength = 1820,
                        state = rtstate.value ,
                        colors = RichTextEditorDefaults.richTextEditorColors(
                            disabledIndicatorColor = Color.Transparent ,
                            unfocusedIndicatorColor = Color.Transparent ,
                            cursorColor = Color.Black ,
                            focusedIndicatorColor = Color.Transparent ,
                            containerColor = Color.White ,
                            textColor = Color.Black

                        ),
                        modifier = Modifier
                            .focusRequester(frList.get(index))
                            .aspectRatio(PDRectangle.A4.width / PDRectangle.A4.height)

                    )

                    DrawingScreen(
                        cdraw,
                        scale,
                        offset,
                        document.bitmap,
                        cpageStates[index],
                        modifier = Modifier
                            .padding(8.dp)
                            .aspectRatio(PDRectangle.A4.width / PDRectangle.A4.height)
                            .pointerInput(true) {
                                detectTapGestures(
                                    onTap = {

                                        val fr = frList.get(index)

                                        fr.requestFocus()
                                    }
                                )
                            }


                    )

                }




*/
/*  DocumentView(
                      document ,
                      modifier = Modifier
                          .drawWithContent {
                              layer.record{
                                  this@drawWithContent.drawContent()
                              }
                              drawLayer(layer)
                              layers.add(layer)
                          }
                  )*//*



            }

        }


        if (cpaletteVisible) {
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
                    .offset(0.dp, -60.dp)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {

                        Text(text = stringResource(R.string.thickness))
                        BasicTextField(
                            value = cthicknessTextfield.value.toString(),
                            onValueChange = { it ->

                                cthicknessTextfield.value = it.toInt()

                            },
                            modifier = Modifier
                                .width(50.dp)
                                .height(20.dp)
                                .background(Color.LightGray)
                        )


                    }



                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                    ) {

                        ccolorlist.value.forEachIndexed { index, clist ->
                            Column {
                                clist.palet.forEachIndexed { index, color ->

                                    val isSelected =
                                        cselectedcolor.value == color

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
                                                color = if (cselectedcolor.value == color) {
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
                                                    if (highlight.value) {

                                                        cselectedcolor.value =
                                                            color.copy(0.2f)
                                                    } else {
                                                        cselectedcolor.value =
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

                        if (!ccolorPaletteVisible) {
                            ccolorPaletteVisible = ccolorPaletteVisible.not()
                        }
                        if (highlight.value) {
                            highlight.value = highlight.value.not()
                        }
                        if (cearse.value) {
                            cearse.value = cearse.value.not()
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

                        if (!ccolorPaletteVisible) {
                            ccolorPaletteVisible = ccolorPaletteVisible.not()
                        }

                        if (cearse.value) {
                            cearse.value = cearse.value.not()
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
                        .background(cselectedcolor.value)
                        .clickable {
                            cpaletteVisible = cpaletteVisible.not()

                        }

                        .size(16.dp)
                )
                OutlinedIconButton(
                    onClick = {
                        cundo.value = cundo.value.not()
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
                        cearse.value = cearse.value.not()
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


            }

        }
    }
}
*/

@Preview(showBackground = true , showSystemUi = true)
@Composable
fun PreviewPDFActivity() {
    DocuNoteTheme(darkTheme = true) {
       // PageScreen(createPDFActivityViewModel = CreatePDFActivityViewModel())
    }
}
