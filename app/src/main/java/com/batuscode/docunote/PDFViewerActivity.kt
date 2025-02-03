package com.batuscode.docunote

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.core.net.toUri
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.utils.PDFConverter
import com.batuscode.docunote.viewmodel.PDFViewerActivityViewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.AsyncImage

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.fastForEach
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.graphics.applyCanvas
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.batuscode.docunote.CreatePDFActivity.Companion.layers
import com.batuscode.docunote.utils.FileManager
import com.batuscode.docunote.utils.PDFCreator
import com.batuscode.docunote.view.DrawingScreen
import com.batuscode.docunote.view.DrawingState
import com.batuscode.docunote.view.RecentlyRead
import com.batuscode.docunote.view.SaveDocument
import com.batuscode.docunote.viewmodel.DrawingAction
import com.batuscode.docunote.viewmodel.allColors
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.getValue
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset


class PDFViewerActivity : ComponentActivity() {
    companion object {
        lateinit var mrendererPages: List<Bitmap>
        lateinit var mpageStates: MutableList<MutableState<DrawingState>>

        lateinit var elayers: MutableList<GraphicsLayer>
        lateinit var activity: PDFViewerActivity
    }
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            elayers = remember {
                mutableStateListOf<GraphicsLayer>()
            }
            val context = LocalContext.current
            activity = this

            val pdfViewerActivityViewModel : PDFViewerActivityViewModel by viewModels()

            var fileManager = remember {
                FileManager(context = context)
            }

            val uri = intent.getStringExtra("fileUri")
            val displayName = intent.getStringExtra("fileDisplayName")
            lateinit var  fileUri: Uri

            uri?.let {
                fileUri = Uri.parse(it)
                fileManager.addDocument(context = context , uri = it , fileName = displayName!!)
                val file = com.batuscode.docunote.utils.File(uri , displayName)
                MainActivity._appViewModel.addRecentlyFile(file)
            }



            var creator = remember {
                PDFCreator()
            }

            val pdfBitmapConverter = remember {
                PDFConverter(context)
            }

            var renderedPages by remember {
                mutableStateOf<List<Bitmap>>(emptyList())
            }

            val scope = rememberCoroutineScope()

            LaunchedEffect(fileUri) {
                fileUri?.let { uri ->
                    renderedPages = pdfBitmapConverter.pdfToBitmaps(uri)

                }
            }


            val edit = pdfViewerActivityViewModel.edit.collectAsState()
            val pageStates = remember { mutableListOf<MutableState<DrawingState>>() } // Birden fazla sayfa için DrawingState listesi


        /*    var graphicsLayers = remember {
                mutableStateOf<List<GraphicsLayer>>(emptyList())
            }
*/
            var graphicsLayersBitmaps = remember {
                mutableStateOf<List<ImageBitmap>>(emptyList())
            }



            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)


            var showSaveDialog = remember {
                mutableStateOf(false)
            }

            var gestureState = remember {
                mutableStateOf(true)
            }
            DocuNoteTheme(darkTheme = true) {

                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(colorResource(R.color.modified).toArgb() , colorResource(R.color.modified).toArgb())
                )
                ModalNavigationDrawer(
                    modifier = Modifier
                        .fillMaxSize(),
                    gesturesEnabled = (if (!edit.value) true else false),
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
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .verticalScroll(state = rememberScrollState())
                            ) {
                                  renderedPages.forEach{
                                      bitmap ->
                                      Image(bitmap = bitmap.asImageBitmap() , contentDescription = "" , modifier = Modifier.padding(8.dp))
                                  }
                            }

                        }
                    }
                ) {
                    Scaffold(

                        containerColor = MaterialTheme.colorScheme.background,
                        topBar = {
                            TopAppBar(
                                title = { Text(text = "$displayName") },
                                navigationIcon = {

                                    if (!edit.value){
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
                                            Icon(Icons.Default.Menu, contentDescription = "Menu" , modifier = Modifier.width(100.dp).height(100.dp))
                                        }
                                    } else {
                                        IconButton(onClick = {
                                            pdfViewerActivityViewModel.update_editState(false)
                                        }) {
                                            Icon(Icons.Default.ArrowBack, contentDescription = "Back" , modifier = Modifier.width(100.dp).height(100.dp))
                                        }
                                    }

                                },
                                actions = {
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



                                                mrendererPages = renderedPages
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
                        floatingActionButton = {
                            if (!edit.value) {

                                FloatingActionButton(
                                    onClick = {
                                        /* val intent = Intent(context , EditPDFActivty::class.java)
                                     context.startActivity(intent)*/
                                        pdfViewerActivityViewModel.update_editState(true)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "extensions"
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()

                    ) { innerPadding ->

                        if (showSaveDialog.value){
                            SaveDocument(create = false, onDismissRequest = {showSaveDialog.value = showSaveDialog.value.not()})
                        }

                        ViewerFlow(
                            innerPadding,
                            gestureState,
                            pageStates,
                            pdfViewerActivityViewModel,
                            pdfBitmapConverter,
                            modifier = Modifier.padding(innerPadding),
                            renderedPages
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PdfPage(
    pdfViewerActivityViewModel: PDFViewerActivityViewModel ,
    page: Bitmap,
    modifier: Modifier = Modifier,
) {


    AsyncImage(
        model = page,
        contentDescription = null,
        modifier = modifier
            .aspectRatio(PDRectangle.A4.width / PDRectangle.A4.height)


    )




}
data class Line(
    val start: Offset,
    val end: Offset,
    val color: Color = Color.Yellow.copy(alpha = 0.5f),
    val strokeWidth: Dp = 10.dp
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerFlow(innerPadding : PaddingValues , gestureState: MutableState<Boolean>,pageStates: MutableList<MutableState<DrawingState>> , pdfViewerActivityViewModel: PDFViewerActivityViewModel ,
               pdfBitmapConverter: PDFConverter , modifier:Modifier = Modifier , renderedPages: List<Bitmap>){
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density


    var isZoom by remember {
        mutableStateOf(false)
    }

    val screenWidthPx = configuration.screenWidthDp * density // Ekran genişliği (px cinsinden)
    val screenHeightPx = configuration.screenHeightDp * density // Ekran yüksekliği (px cinsinden)



    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformablestate = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = maxOf(1f, scale * zoomChange)

        // Görselin boyutunu hesapla
        val contentWidthPx = screenWidthPx * scale
        val contentHeightPx = screenHeightPx * scale

        // Offset'i ekran boyutlarına göre sınırla
        val maxOffsetX = (contentWidthPx - screenWidthPx) / 2f
        val maxOffsetY = (contentHeightPx - screenHeightPx) / 2f

        // Offset değerlerini px cinsinden sınırla
        offset = Offset(
            x = (offset.x + offsetChange.x).coerceIn(-maxOffsetX, maxOffsetX),
            y = (offset.y + offsetChange.y).coerceIn(-maxOffsetY, maxOffsetY)
        )
    }


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

    val scope = rememberCoroutineScope()

    var sliderPosition by remember {
        // ScrollState'in başlangıçta olduğu yerde slider da başlar
        mutableStateOf(0f)
    }
    val scrollState = rememberScrollState()
    var columnHeight by remember { mutableStateOf(0) }

    var scrollbarvisibilty by remember {
        mutableStateOf(true)
    }

    val vectorIcon: Painter = painterResource(id = R.drawable.baseline_unfold_more_24)
    LaunchedEffect(key1 = colorPaletteVisible , key2 = scrollState.value , key3 = scrollbarvisibilty) {
        if (colorPaletteVisible){

            delay(2000L)
            colorPaletteVisible = colorPaletteVisible.not()
        }

        if (scrollbarvisibilty){

            delay(2000L)
            scrollbarvisibilty = scrollbarvisibilty.not()
        }

        val proportion = scrollState.value.toFloat() / scrollState.maxValue.toFloat()
        sliderPosition = proportion
    }

    if (!edit.value){

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {


            Column (

                modifier = modifier
                    .fillMaxWidth()
                    .transformable(state = transformablestate)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .verticalScroll(scrollState , enabled = true)
                    .pointerInput(true){
                        detectTapGestures(
                            onTap = {
                                scrollbarvisibilty = scrollbarvisibilty.not()
                            }
                        )
                    }

            )
            {
                renderedPages.forEachIndexed { index , page ->
                    PdfPage(
                        pdfViewerActivityViewModel ,
                        page = page,
                        modifier = Modifier
                            .padding(8.dp)

                    )
                }

            }

            if (scrollbarvisibilty){
                Slider(

                    value = sliderPosition,
                    onValueChange = { newValue ->
                        sliderPosition = newValue
                        // Sync slider position with scroll state
                        scope.launch {
                            scrollState.scrollTo((scrollState.maxValue * newValue).toInt())
                        }
                    },
                    modifier = Modifier
                        .rotate(90f)
                        .offset(0.dp,-200.dp)
                        .fillMaxHeight(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Transparent, // Thumb rengini şeffaf yapıyoruz çünkü ikonu göstereceğiz
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    ),
                    thumb = {
                        Box(
                            contentAlignment = Alignment.Center ,
                            modifier = Modifier
                                .offset(x = 10.dp, y = (20).dp)
                                .clipToBounds()
                                .clip(CircleShape)
                                .background(Color.Red)
                                .rotate(90f)

                        ) {
                            Image(
                                painter = vectorIcon, // İkonu slider'ın thumb kısmına yerleştiriyoruz
                                contentDescription = "Slider Thumb",
                                modifier = Modifier // İkonun boyutunu ayarlayın
                                    .clipToBounds()
                                    .clip(CircleShape)
                                    .background(colorResource(R.color.modified))

                            )
                        }

                    }
                )
            }
        }

    }
    else {

        Box(
            modifier = Modifier
                .fillMaxSize()
        )
        {

            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(state = transformablestate)
                    .verticalScroll(scrollState , enabled = true)
                    .pointerInput(true){
                        detectTapGestures(
                            onTap = {
                                scrollbarvisibilty = scrollbarvisibilty.not()
                            } ,

                        )


                    }


            )
            {
                renderedPages.forEachIndexed { index , page ->
                    val state = remember { mutableStateOf(DrawingState()) }


                    state.value.selectedColor = selectedcolor.value

                    if (highlight.value){
                        state.value.thickness = 10f
                        state.value.selectedColor = state.value.selectedColor.copy(alpha = 0.2f)
                    } else {
                        state.value.thickness = 5f
                        state.value.selectedColor = state.value.selectedColor
                    }

                    if (undo.value){

                        state.value = state.value.copy(paths = state.value.paths.dropLast(1))
                        undo.value = undo.value.not()
                    }


                    state.value.isErasing = earse.value


                    pageStates.add(state)


                    val graphicsLayer = rememberGraphicsLayer()

                    Box (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .drawWithContent {

                                /*  graphicsLayer.record {
                                    this@drawWithContent.drawContent()
                                }

                                drawLayer(graphicsLayer)*/

                                if (PDFViewerActivity.Companion.elayers.size <= index) {
                                    // val newLayer = GraphicsLayer()
                                    PDFViewerActivity.Companion.elayers.add(graphicsLayer)
                                    graphicsLayer.record {
                                        this@drawWithContent.drawContent()
                                    }
                                }
                                drawLayer(PDFViewerActivity.Companion.elayers[index])


                            }




                    ) {

                      /*  PdfPage(
                            pdfViewerActivityViewModel ,
                            page = page,
                            modifier = Modifier
                                .padding(8.dp)
                                .aspectRatio(page.width.toFloat() / page.height.toFloat())

                        )*/


                        AsyncImage(
                            model = page,
                            contentDescription = null,

                            modifier = Modifier

                                .aspectRatio(page.width.toFloat() / page.height.toFloat())

                        )
                        DrawingScreen(
                            pageStates[index],
                            modifier = Modifier
                                .padding(8.dp)




                        )



                    }
                }

            }

            if (scrollbarvisibilty){
                Slider(

                    value = sliderPosition,
                    onValueChange = { newValue ->
                        sliderPosition = newValue
                        // Sync slider position with scroll state
                        scope.launch {
                            scrollState.scrollTo((scrollState.maxValue * newValue).toInt())
                        }
                    },
                    modifier = Modifier
                        .rotate(90f)
                        .offset(0.dp,-200.dp)
                        .fillMaxHeight(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Transparent, // Thumb rengini şeffaf yapıyoruz çünkü ikonu göstereceğiz
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    ),
                    thumb = {
                        Box(
                            contentAlignment = Alignment.Center ,
                            modifier = Modifier
                                .offset(x = 10.dp, y = (20).dp)
                                .clipToBounds()
                                .clip(CircleShape)
                                .background(Color.Red)
                                .rotate(90f)

                        ) {
                            Image(
                                painter = vectorIcon, // İkonu slider'ın thumb kısmına yerleştiriyoruz
                                contentDescription = "Slider Thumb",
                                modifier = Modifier // İkonun boyutunu ayarlayın
                                    .clipToBounds()
                                    .clip(CircleShape)
                                    .background(colorResource(R.color.modified))

                            )
                        }

                    }
                )
            }



            if (colorPaletteVisible){

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
                            val isSelected = selectedcolor.value == color
                            Box(
                                modifier = Modifier
                                    .graphicsLayer {
                                        val scale = if(isSelected) 1.2f else 1f
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = 2.dp,
                                        color = if(selectedcolor.value == color) {
                                            Color.Black
                                        } else {
                                            Color.Transparent
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        if (highlight.value){

                                            selectedcolor.value = color.copy(0.2f)
                                        } else {
                                            selectedcolor.value = color
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

                            if (!colorPaletteVisible){
                                colorPaletteVisible = colorPaletteVisible.not()
                            }
                            if (highlight.value){
                                highlight.value = highlight.value.not()
                            }
                            if (earse.value){
                                earse.value = earse.value.not()
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

                            if (!colorPaletteVisible){
                                colorPaletteVisible = colorPaletteVisible.not()
                            }

                            if (earse.value){
                                earse.value = earse.value.not()
                            }
                            highlight.value = highlight.value.not()
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
                            undo.value = undo.value.not()
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
                            earse.value = earse.value.not()
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

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun scrollbar(){

    // Vektör ikonunu proje içerisinden yükleyin
    val vectorIcon: Painter = painterResource(id = R.drawable.baseline_unfold_more_24)

    val scope = rememberCoroutineScope()


    var sliderPosition by remember {
        // ScrollState'in başlangıçta olduğu yerde slider da başlar
        mutableStateOf(0f)
    }
    val scrollState = rememberScrollState()
    var columnHeight by remember { mutableStateOf(0) }

   /* Box(
        modifier = Modifier
            .background(Color.Yellow)
            .fillMaxWidth()

    ) {

    }*/
    Slider(

        value = sliderPosition,
        onValueChange = { newValue ->
            sliderPosition = newValue
            // Sync slider position with scroll state
            scope.launch {
                scrollState.scrollTo((scrollState.maxValue * newValue).toInt())
            }
        },
        modifier = Modifier
            .rotate(90f)
            .offset(0.dp,-200.dp)
            .fillMaxHeight(),
        colors = SliderDefaults.colors(
            thumbColor = Color.Transparent, // Thumb rengini şeffaf yapıyoruz çünkü ikonu göstereceğiz
            activeTrackColor = Color.Transparent,
            inactiveTrackColor = Color.Transparent
        ),
        thumb = {
            Box(
                contentAlignment = Alignment.Center ,
                modifier = Modifier
                    .offset(x = 10.dp, y = (20).dp)

                    .graphicsLayer(rotationZ = 90f)

            ) {
                Image(
                    painter = vectorIcon, // İkonu slider'ın thumb kısmına yerleştiriyoruz
                    contentDescription = "Slider Thumb",
                    modifier = Modifier // İkonun boyutunu ayarlayın
                        .clipToBounds()
                        .clip(CircleShape)
                        .background(Color.White)

                )
            }

        }
    )


}

@Preview(showBackground = true , showSystemUi = true)
@Composable
fun GreetingPreview2() {
    DocuNoteTheme {
        //ViewerFlow( graphicsLayers, PDFViewerActivityViewModel() , PDFConverter(LocalContext.current) , PaddingValues() , listOf())
        scrollbar()
    }
}