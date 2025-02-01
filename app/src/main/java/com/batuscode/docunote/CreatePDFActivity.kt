package com.batuscode.docunote

import android.app.Activity
import android.app.ComponentCaller
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.inputmethodservice.InputMethodService
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
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.core.view.WindowCompat
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.viewmodel.CreatePDFActivityViewModel
import kotlinx.coroutines.launch
import kotlin.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.batuscode.docunote.model.Document
import com.batuscode.docunote.view.DrawingCanvas
import com.batuscode.docunote.view.DrawingScreen
import com.batuscode.docunote.view.DrawingState
import com.batuscode.docunote.view.SaveDocument
import com.batuscode.docunote.viewmodel.allColors
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

class CreatePDFActivity : ComponentActivity() {
    companion object {
        lateinit var mcreatePDFActivityViewModel: CreatePDFActivityViewModel
        lateinit var pdfActivity: CreatePDFActivity
        lateinit var pagesContainer: List<Document>
        lateinit var cpageStates: MutableList<MutableState<DrawingState>>
        lateinit var contentResolver: ContentResolver
        lateinit var context: Context
        lateinit var layers: MutableList<GraphicsLayer>

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
       // focusrequesters = mutableStateListOf()

        setContent {
            context = LocalContext.current
            DocuNoteTheme(darkTheme = true) {

                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(colorResource(R.color.modified).toArgb() , colorResource(R.color.modified).toArgb())
                )
                val scope = rememberCoroutineScope()

                var index = remember {
                    mutableIntStateOf(0)
                }

                var pageNumber = remember {
                    mutableIntStateOf(1)
                }
                var pages = remember {
                    mutableStateOf<MutableList<Document>>(mutableStateListOf())
                }


                var showSaveDialog = remember {
                    mutableStateOf(false)
                }

                var componentList = remember {
                    mutableStateListOf<@Composable () -> Unit>()
                }


                val layer = rememberGraphicsLayer()

                var pageBitmaps = remember {
                    mutableStateOf<ImageBitmap?>(null)
                }

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

                var document = remember {
                    PDDocument()
                }

                lateinit var pageBitmap: Bitmap

                val pageStates = remember { mutableListOf<MutableState<DrawingState>>() }
                val scaleFactor = resources.displayMetrics.densityDpi / 72f

                 layers = remember {
                    mutableStateListOf<GraphicsLayer>()
                }

                LaunchedEffect(key1 = Unit) {



                    val page = PDPage(PDRectangle.A4)

                    val width = (page.mediaBox.width * scaleFactor).toInt()
                    val height = (page.mediaBox.height * scaleFactor).toInt()
                    val bitmap = Bitmap.createBitmap(width , height ,
                        Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bitmap).apply {
                        drawColor(android.graphics.Color.WHITE)
                        drawBitmap(bitmap, 0f, 0f, null)
                    }

                    pageBitmap = bitmap



                    val doc = Document(index = 0 ,
                        page = pageNumber ,
                        text = RichTextState(),
                        bitmap = bitmap ,)


                    val iter = pages.value.toMutableList()
                    iter.add(index.value , doc)
                    index.value += index.value

                    pages.value = iter
                }




                ModalNavigationDrawer(
                    modifier = Modifier
                        .fillMaxSize(),
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
                              /*  pageBitmaps.forEach{
                                    bitmap ->
                                    Image(bitmap = bitmap , contentDescription = "")
                                }*/
                                if (pageBitmaps.value != null){

                                    Image(bitmap = pageBitmaps.value!! , contentDescription = "")
                                }
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

                                            /*  val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                                          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                                      }
                                      activity?.startActivityForResult(intent , 0)*/


                                           /* pagesContainer = pages

                                            val intent =
                                                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                                    addCategory(Intent.CATEGORY_OPENABLE)
                                                    type = "application/pdf"
                                                    putExtra(
                                                        Intent.EXTRA_TITLE,
                                                        R.string.documentname
                                                    )

                                                    // Optionally, specify a URI for the directory that should be opened in
                                                    // the system file picker before your app creates the document.
                                                }
                                            pdfActivity.startActivityForResult(intent, 1)*/
                                            pagesContainer = pages.value
                                            cpageStates = pageStates
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
                                               // Log.d("images" , pageBitmaps.size.toString())
                                              /*  layers.forEach{
                                                        grap ->

                                                    pageBitmaps.add(grap.toImageBitmap())
                                                }*/
                                                pageBitmaps.value = layer.toImageBitmap()
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
                                onClick = {
                                    scope.launch {



                                      /*  val doc = Document(index, RichTextState(), pageNumber)
                                        pages.add(doc)
                                        index.value = ++index.value
                                        pageNumber.value = ++pageNumber.value*/

                                        // createPDFActivityViewModel.setNewPage()
                                        // docsState.animateScrollToItem(1)



                                      /*  val page = PDPage(PDRectangle.A4)

                                        val page2 = document.pages.get(0)

                                        val width = (page2.mediaBox.width * scaleFactor).toInt()
                                        val height = (page2.mediaBox.height * scaleFactor).toInt()
                                        val bitmap = Bitmap.createBitmap(width , height ,
                                            Bitmap.Config.ARGB_8888)
                                        val canvas = android.graphics.Canvas(bitmap).apply {
                                            drawColor(android.graphics.Color.WHITE)
                                            drawBitmap(bitmap, 0f, 0f, null)
                                        }

                                        val doc = Document(index = 1 , page = pageNumber , RichTextState() , bitmap = bitmap)
                                        val iter = pages.value.toMutableList()
                                        iter.add(doc)
                                        pages.value = iter*/


                                        val doc2 = Document(index = 0 ,
                                            page = pageNumber ,
                                            RichTextState() ,
                                            bitmap = pageBitmap ,
                                            )
                                        val iter = pages.value.toMutableList()
                                        iter.add(index.value , doc2)
                                        index.value += index.value

                                        pages.value = iter
                                    }
                                },
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "extensions"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->

                        if (showSaveDialog.value){
                            SaveDocument(create = true , onDismissRequest = {showSaveDialog.value = showSaveDialog.value.not()})
                        }

                        PageScreen(cpageStates  , modifier = Modifier.padding(innerPadding), pages.value)


                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun klkl(){
    val state = rememberRichTextState()


    Box (
        modifier = Modifier
            .fillMaxWidth()
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
            modifier = Modifier
                .fillMaxWidth()
                .height(1280.dp)
        )

    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageScreen(cpageStates: MutableList<MutableState<DrawingState>> , modifier: Modifier = Modifier, pages: List<Document>){



   /* LaunchedEffect(Unit) {
        createPDFActivityViewModel.initializePage()
    }

    val docsState = rememberLazyListState()


    val docs = createPDFActivityViewModel._page.collectAsState()*/


    var graphic = remember {
        mutableStateListOf<GraphicsLayer>()
    }


    var drawTextfield = remember {
        mutableStateOf(false)
    }

    var chighlight = remember {
        mutableStateOf(false)
    }

    var cselectedcolor = remember {
        mutableStateOf<Color>(Color.Black)
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
    Box (
        modifier = Modifier
            .fillMaxSize()
    ){

        Column (
            //state = docsState,
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(scrollState, enabled = true)

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

                            /*  graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }

                            drawLayer(graphicsLayer)*/

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

                    /*Image(
                        bitmap = document.bitmap.asImageBitmap() ,
                        ""
                    )*/



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
                  )*/

            }

        }

        if (ccolorPaletteVisible){

            Surface(
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 15.dp,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .rotate(90f)
                    .graphicsLayer {
                        translationY = -300f
                    }

            )
            {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(10.dp)
                )
                {
                    allColors.fastForEach { color ->
                        val isSelected = cselectedcolor.value == color
                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    val scale = if (isSelected) 1.2f else 1f
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = 2.dp,
                                    color = if (cselectedcolor.value == color) {
                                        Color.Black
                                    } else {
                                        Color.Transparent
                                    },
                                    shape = CircleShape
                                )
                                .clickable {
                                    if (chighlight.value) {

                                        cselectedcolor.value = color.copy(0.2f)
                                    } else {
                                        cselectedcolor.value = color
                                    }
                                }
                        )
                    }

                }

            }
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 15.dp,
            // color = colorResource(id = R.color.e),
            modifier = Modifier
                .padding(50.dp)

                .align(Alignment.BottomCenter)

        )
        {

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(5.dp)
            )
            {
                OutlinedIconButton(
                    onClick = {

                        scope.launch{

                            val fr = frList.get(scrollState.value)

                            fr.requestFocus()
                        }

                            if (!ccolorPaletteVisible){
                                ccolorPaletteVisible = ccolorPaletteVisible.not()
                            }
                            if (chighlight.value){
                                chighlight.value = chighlight.value.not()
                            }
                            if (cearse.value){
                                cearse.value = cearse.value.not()
                            }
                    },
                    border = null,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier
                )
                {
                    Icon(
                        painter = painterResource(R.drawable.baseline_text_fields_24),
                        contentDescription = "",
                        modifier = Modifier
                    )
                }
                OutlinedIconButton(
                    onClick = {


                        if (!ccolorPaletteVisible){
                                ccolorPaletteVisible = ccolorPaletteVisible.not()
                            }
                            if (chighlight.value){
                                chighlight.value = chighlight.value.not()
                            }
                            if (cearse.value){
                                cearse.value = cearse.value.not()
                            }
                    },
                    border = null,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier
                )
                {
                    Icon(
                        painter = painterResource(R.drawable.pen_icon),
                        contentDescription = "",
                        modifier = Modifier
                    )
                }
                OutlinedIconButton(
                    onClick = {

                         if (!ccolorPaletteVisible){
                             ccolorPaletteVisible = ccolorPaletteVisible.not()
                         }

                         if (cearse.value){
                             cearse.value = cearse.value.not()
                         }
                         chighlight.value = chighlight.value.not()
                    },
                    border = null,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier
                )
                {
                    Icon(
                        painter = painterResource(R.drawable.brush_icon),
                        contentDescription = "",
                        modifier = Modifier
                    )
                }
                OutlinedIconButton(
                    onClick = {
                        cundo.value = cundo.value.not()
                    },
                    border = null,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier
                )
                {
                    Icon(
                        painter = painterResource(R.drawable.undo_icon),
                        contentDescription = "",
                        modifier = Modifier
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
                )
                {
                    Icon(
                        painter = painterResource(R.drawable.erase_icon),
                        contentDescription = "",
                        modifier = Modifier
                    )
                }


            }

        }
    }

}

@Preview(showBackground = true , showSystemUi = true)
@Composable
fun PreviewPDFActivity() {
    DocuNoteTheme(darkTheme = true) {
       // PageScreen(createPDFActivityViewModel = CreatePDFActivityViewModel())
    }
}