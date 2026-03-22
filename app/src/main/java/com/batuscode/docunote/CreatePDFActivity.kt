package com.batuscode.docunote

import android.app.ComponentCaller
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.utils.PDFUtil
import com.batuscode.docunote.view.DrawingState
import com.batuscode.docunote.view.SaveDocument
import com.batuscode.docunote.viewmodel.CreatePDFActivityViewModel
import com.batuscode.pdfium.PDFPage
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CreatePDFActivity : ComponentActivity() {
    companion object {
        lateinit var mcreatePDFActivityViewModel: CreatePDFActivityViewModel
        lateinit var pdfActivity: CreatePDFActivity
        lateinit var cpageStates: MutableList<MutableState<DrawingState>>
        lateinit var contentResolver: ContentResolver
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

        if (requestCode == 0 && resultCode == RESULT_OK) {
            data?.let {
                val uri = it.data
                Log.d("newuri" , uri.toString())

                lifecycleScope.launch(Dispatchers.Main){
                    mcreatePDFActivityViewModel.update_fileUri(uri!!)
                    mcreatePDFActivityViewModel.update_saveFlag(true)
                }
            }
        }
        else if (requestCode == 1 && resultCode == RESULT_OK) {
            data?.let {
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

        val context : Context = this

        val createPDFActivityViewModel: CreatePDFActivityViewModel by viewModels()
        mcreatePDFActivityViewModel = createPDFActivityViewModel
        pdfActivity = this
        Companion.contentResolver = contentResolver

        cpageStates = mutableStateListOf()


        val page = PDFPage(595f, 842f) // A4 size


        setContent {
            DocuNoteTheme {
                enableEdgeToEdge()
                val textStates = remember { mutableStateMapOf<Int, RichTextState>() }
                val documentPtr = remember {
                    mutableStateOf<Long>(PDFUtil.core.createDocument())
                }
                val pages = remember {
                    mutableStateListOf<PDFPage>()
                }

                LaunchedEffect(Unit) {

                    Log.d("page count" , pages.size.toString())
                    if (documentPtr.value != 0L) {
                        val ok = PDFUtil.core.addPage(documentPtr.value, page)
                        if (ok != 0L){

                            Log.d("page count" , "first" + documentPtr)
                           // documentPtr.value=ok

                            Log.d("page count" , "after" + documentPtr)
                            docptr=documentPtr.value

                            Log.d("page count" , "after main" + docptr)
                            pages.add(page) // Add the page to the list
                            Log.d("page count" , pages.size.toString())
                        }
                    }

                }

                rememberCoroutineScope()

                remember {
                    mutableIntStateOf(0)
                }


                val showSaveDialog = remember {
                    mutableStateOf(false)
                }

                val pageBitmaps = remember {
                    mutableStateOf<MutableList<ImageBitmap>>(mutableStateListOf())
                }

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

                 layers = remember {
                    mutableStateListOf<GraphicsLayer>()
                }



                var ccolorPaletteVisible by remember {
                    mutableStateOf(true)
                }
                var addPage = remember {
                    mutableStateOf(true)
                }
                LaunchedEffect(key1 = addPage.value , key2 = ccolorPaletteVisible) {



                    if (ccolorPaletteVisible){

                        delay(2000L)
                        ccolorPaletteVisible = ccolorPaletteVisible.not()
                    }
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
                            )
                        },
                        floatingActionButton = {
                            FloatingActionButton(
                                containerColor = MaterialTheme.colorScheme.background,
                                onClick = {
                                    if (documentPtr.value != 0L) {
                                        val ok = PDFUtil.core.addPage(documentPtr.value, page)
                                        if (ok != 0L){

                                            Log.d("page count" , "add method first" + documentPtr)
                                            //documentPtr.value=ok

                                            Log.d("page count" , "add method after" + documentPtr)
                                            docptr=documentPtr.value

                                            Log.d("page count" , "add method after main" + docptr)
                                            pages.add(page) // Add the page to the list
                                            Log.d("add method page count" , pages.size.toString())
                                        }
                                    }
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
                        val mCurrentDpi = context.resources.displayMetrics.densityDpi

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
                                                textStyle = TextStyle(
                                                    fontFamily = FontFamily(Font(R.font.notosans_regular)),
                                                    fontSize = 12f.sp,
                                                    lineHeight = 14f.sp,
                                                    lineBreak = LineBreak(
                                                        strategy = LineBreak.Strategy.Simple,
                                                        strictness = LineBreak.Strictness.Strict,
                                                        wordBreak = LineBreak.WordBreak.Phrase
                                                    ),
                                                    textAlign = TextAlign.Left,


                                                ),
                                                modifier = Modifier
                                                    .width(((page.width*mCurrentDpi)/72).dp)
                                                    .height(((page.height*mCurrentDpi)/72).dp),
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


@Preview(showBackground = true , showSystemUi = true)
@Composable
fun PreviewPDFActivity() {
    DocuNoteTheme(darkTheme = true) {
       // PageScreen(createPDFActivityViewModel = CreatePDFActivityViewModel())
    }
}
