package com.batuscode.docunote

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.ripple
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.batuscode.docunote.model.Folder
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.view.Extensions
import com.batuscode.docunote.view.Folders
import com.batuscode.docunote.viewmodel.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import com.batuscode.docunote.utils.FileManager
import com.batuscode.docunote.view.RecentlyRead
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.batuscode.docunote.utils.Auth
import com.batuscode.docunote.utils.DrawerSide
import com.batuscode.docunote.utils.PDFConverter
import com.batuscode.docunote.utils.WordDocUtil
import com.batuscode.pdfium.icore
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.CoroutineScope
import kotlin.math.roundToInt


class MainActivity : ComponentActivity() {
    companion object {
        init {
            // System.loadLibrary("jpdfium");
        }

        lateinit var _appViewModel: AppViewModel
        lateinit var mainActivity: ComponentActivity
        lateinit var context: Context
        lateinit var openDocumentLauncher: ActivityResultLauncher<Intent>
        lateinit var openwordDocumentLauncher: ActivityResultLauncher<Intent>
        lateinit var openpptDocumentLauncher: ActivityResultLauncher<Intent>
        lateinit var toFolderDocumentLauncher: ActivityResultLauncher<Intent>
        lateinit var mergeDocumentLauncher: ActivityResultLauncher<Intent>
        lateinit var splitDocumentLauncher: ActivityResultLauncher<Intent>
        lateinit var multiplyselectTofolderDocumentLauncher: ActivityResultLauncher<Intent>
        lateinit var dynamicFeatureLauncher: ActivityResultLauncher<IntentSenderRequest>
        var memUri = mutableStateOf<Uri?>(null)
        var newFolder = mutableStateOf(false)
        var pdfsplitOP = mutableStateOf(false)
        var pdfmergeOP = mutableStateOf(false)
        lateinit var mainicore: icore
        lateinit var fileManager: FileManager
        var uriMap: MutableMap<Int, Uri> = mutableMapOf()
        var recentlyStat = mutableStateOf(false)
        var folderStat = mutableStateOf(false)
        var showDFRDialog = mutableStateOf(false)
        var showDownloadProg = mutableStateOf(false)
        var progress = mutableStateOf(0f)
        lateinit var llmInference: LlmInference
        var expdwstatus = mutableStateOf("")
        val snackbarHostState = SnackbarHostState()
        var waitinit = mutableStateOf(false)
    }


    @Composable
    fun splitDialog(
        onDismissRequest: () -> Unit,
        onConfirm: (String, String) -> Unit
    ) {


        var textFieldValue by remember { mutableStateOf("") }
        var startRangeValue by remember { mutableStateOf("") }
        var endRangeValue by remember { mutableStateOf("") }
        var range by remember { mutableStateOf("") }

        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(dismissOnClickOutside = false) // Dışına tıklayınca kapanmasın
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    // TextField for user input
                    OutlinedTextField(
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        label = { Text(text = stringResource(R.string.documentname)) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    // TextField for user input
                    OutlinedTextField(
                        value = startRangeValue,
                        onValueChange = { startRangeValue = it },
                        label = { Text(text = stringResource(R.string.startRange)) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    // TextField for user input
                    OutlinedTextField(
                        value = endRangeValue,
                        onValueChange = { endRangeValue = it },
                        label = { Text(text = stringResource(R.string.endRange)) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons row
                    Row {
                        Button(
                            onClick = { onDismissRequest() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = stringResource(R.string.cancel))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {

                                onConfirm(textFieldValue, "${startRangeValue}-${endRangeValue}")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = stringResource(R.string.ok))
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun mCustomDialog(
        onDismissRequest: () -> Unit,
        onConfirm: (String) -> Unit
    ) {
        var textFieldValue by remember { mutableStateOf("") }

        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(dismissOnClickOutside = false) // Dışına tıklayınca kapanmasın
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    // TextField for user input
                    OutlinedTextField(
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        label = { Text(text = stringResource(R.string.foldername)) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons row
                    Row {
                        Button(
                            onClick = { onDismissRequest() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = stringResource(R.string.cancel))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { onConfirm(textFieldValue) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = stringResource(R.string.ok))
                        }
                    }
                }
            }
        }
    }


    val requestPermissionLauncher =
        registerForActivityResult(
            RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @SuppressLint("Range")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this

        val appViewModel: AppViewModel by viewModels()
        _appViewModel = appViewModel
        mainActivity = this
        mainicore = icore(this)
        mainicore.nativeInitLibrary()
        fileManager = FileManager(this)
        var converter = PDFConverter(this)
        folderStat.value = fileManager.getFoldersStat(this)
        recentlyStat.value = fileManager.getRecentlyStat(this)

        dynamicFeatureLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Kullanıcı onayladı, indirme devam eder
            } else {
                // Kullanıcı iptal etti, senaryoya göre işlem yap
            }
        }

        multiplyselectTofolderDocumentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data

                data?.let { it ->
                    // Dosya URI'sini işlemek

                    // Log.d("newuri" , it.toString())


                    if (it.clipData != null) {
                        val count = it.clipData?.itemCount ?: 0
                        for (i in 0 until count) {
                            val uri = it.clipData?.getItemAt(i)?.uri
                            uri?.let {
                                _appViewModel.add_urlist(it)

                                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                                contentResolver.takePersistableUriPermission(it, takeFlags)
                            }
                            Log.d("newuri", uri.toString())
                        }

                        if (!folderStat.value) {
                            folderStat.value = folderStat.value.not()
                            fileManager.saveFoldersStat(context, true)
                        }
                        newFolder.value = true


                    } else {
                        // Tekli dosya seçimi
                        val uri = it.data
                        Log.d("newuri", uri.toString())

                    }

                }
            }
        }

        // merge PDF document

        mergeDocumentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                data?.let { it ->
                    if (it.clipData != null) {
                        if (uriMap.isNotEmpty()) {
                            uriMap.clear()
                        }
                        val count = it.clipData?.itemCount ?: 0
                        for (i in 0 until count) {
                            val uri = it.clipData?.getItemAt(i)?.uri
                            uri?.let {
                                uriMap.put(i, uri)
                                // _appViewModel.add_urlist(it)

                                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                                contentResolver.takePersistableUriPermission(it, takeFlags)
                            }
                            Log.d("newuri", uri.toString())

                        }
                        pdfmergeOP.value = pdfmergeOP.value.not()

                    }
                }

            }
        }

        // split PDF document

        splitDocumentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                var uriMap: MutableMap<Int, Uri> = mutableMapOf()
                val data: Intent? = result.data
                data?.let { it ->
                    val uri = it.data
                    uri?.let {
                        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                        contentResolver.takePersistableUriPermission(it, takeFlags)
                        memUri.value = uri
                        pdfsplitOP.value = pdfsplitOP.value.not()
                    }
                    Log.d("newuri", uri.toString())
                }
            }
        }

        // to folder

        toFolderDocumentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                data?.let { it ->
                    // Dosya URI'sini işlemek

                    // Log.d("newuri" , it.toString())

                    if (it.clipData != null) {
                        val count = it.clipData?.itemCount ?: 0
                        for (i in 0 until count) {
                            val uri = it.clipData?.getItemAt(i)?.uri
                            uri?.let {
                                _appViewModel.add_urlist(it)

                                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                                contentResolver.takePersistableUriPermission(it, takeFlags)
                            }
                            Log.d("newuri", uri.toString())
                        }

                        newFolder.value = true

                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "application/pdf"
                            //  putExtra(Intent.EXTRA_TITLE, R.string.documentname)

                            // Optionally, specify a URI for the directory that should be opened in
                            // the system file picker before your app creates the document.
                        }
                        mainActivity.startActivityForResult(intent, 0)

                    } else {
                        // Tekli dosya seçimi
                        val uri = it.data
                        Log.d("newuri", uri.toString())

                    }

                }
            }
        }

        // open ppt

        openpptDocumentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                /*  val uri: Uri? = data?.data
                  // Seçilen dosya URI'ını burada kullanabilirsin
                  if (uri != null) {
                      // Örneğin, URI'yi bir dosyaya yazdırabilir veya okuyabilirsin
                      Log.d("FileSelector", "Seçilen dosya: $uri")


                  }*/

                data?.let { it ->
                    val uri = it.data
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                    contentResolver.takePersistableUriPermission(uri!!, takeFlags)

                    Log.d("newuri", uri.toString())

                    val cursor = contentResolver.query(
                        uri!!,
                        arrayOf(OpenableColumns.DISPLAY_NAME),
                        null,
                        null,
                        null
                    )

                    cursor?.use {
                        if (it.moveToFirst()) {
                            val displayName =
                                it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                            val dotIndex = displayName.lastIndexOf('.')
                            val fileNameWithoutExtension =
                                if (dotIndex != -1) displayName.substring(
                                    0,
                                    dotIndex
                                ) else displayName

                            Log.d("newuri", displayName)
                            lifecycleScope.launch(Dispatchers.Main) {

                                if (!recentlyStat.value) {
                                    recentlyStat.value = recentlyStat.value.not()
                                    fileManager.saveRecentlyStat(context, true)
                                }

                                /* val intent = Intent(context , PDFViewerActivity::class.java).apply {
                                     putExtra("fileUri" , uri.toString())
                                     putExtra("fileDisplayName" , fileNameWithoutExtension)
                                 }

                                 mainActivity.startActivity(intent)*/
                            }
                        }
                    }


                }
            }
        }

        // open word
        openwordDocumentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                /*  val uri: Uri? = data?.data
                  // Seçilen dosya URI'ını burada kullanabilirsin
                  if (uri != null) {
                      // Örneğin, URI'yi bir dosyaya yazdırabilir veya okuyabilirsin
                      Log.d("FileSelector", "Seçilen dosya: $uri")


                  }*/

                data?.let { it ->
                    val uri = it.data
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                    contentResolver.takePersistableUriPermission(uri!!, takeFlags)

                    Log.d("newuri", uri.toString())

                    val cursor = contentResolver.query(
                        uri!!,
                        arrayOf(OpenableColumns.DISPLAY_NAME),
                        null,
                        null,
                        null
                    )

                    cursor?.use {
                        if (it.moveToFirst()) {
                            val displayName =
                                it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                            val dotIndex = displayName.lastIndexOf('.')
                            val fileNameWithoutExtension =
                                if (dotIndex != -1) displayName.substring(
                                    0,
                                    dotIndex
                                ) else displayName

                            Log.d("newuri", displayName)
                            lifecycleScope.launch(Dispatchers.Main) {

                                if (!recentlyStat.value) {
                                    recentlyStat.value = recentlyStat.value.not()
                                    fileManager.saveRecentlyStat(context, true)
                                }

                                WordDocUtil.loadDocument(
                                    WordDocUtil.getWordDocFromUri(
                                        context,
                                        uri!!,
                                        fileNameWithoutExtension
                                    )
                                )
                                /* val intent = Intent(context , PDFViewerActivity::class.java).apply {
                                     putExtra("fileUri" , uri.toString())
                                     putExtra("fileDisplayName" , fileNameWithoutExtension)
                                 }

                                 mainActivity.startActivity(intent)*/
                            }
                        }
                    }


                }
            }
        }

        // open pdf

        openDocumentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                /*  val uri: Uri? = data?.data
                  // Seçilen dosya URI'ını burada kullanabilirsin
                  if (uri != null) {
                      // Örneğin, URI'yi bir dosyaya yazdırabilir veya okuyabilirsin
                      Log.d("FileSelector", "Seçilen dosya: $uri")


                  }*/

                data?.let { it ->
                    val uri = it.data
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                    contentResolver.takePersistableUriPermission(uri!!, takeFlags)

                    Log.d("newuri", uri.toString())

                    val cursor = contentResolver.query(
                        uri!!,
                        arrayOf(OpenableColumns.DISPLAY_NAME),
                        null,
                        null,
                        null
                    )

                    cursor?.use {
                        if (it.moveToFirst()) {
                            val displayName =
                                it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                            val dotIndex = displayName.lastIndexOf('.')
                            val fileNameWithoutExtension =
                                if (dotIndex != -1) displayName.substring(
                                    0,
                                    dotIndex
                                ) else displayName

                            Log.d("newuri", displayName)
                            lifecycleScope.launch(Dispatchers.Main) {

                                if (!recentlyStat.value) {
                                    recentlyStat.value = recentlyStat.value.not()
                                    fileManager.saveRecentlyStat(context, true)
                                }

                                val intent = Intent(context, PDFViewerActivity::class.java).apply {
                                    putExtra("fileUri", uri.toString())
                                    putExtra("fileDisplayName", fileNameWithoutExtension)
                                }

                                mainActivity.startActivity(intent)
                            }
                        }
                    }


                }
            }
        }


        enableEdgeToEdge()
        setContent {
            var filemanager = remember {
                FileManager(context = context)
            }

            LaunchedEffect(Unit) {
                // folders.value = filemanager.getFoldersForDirectory()

                appViewModel.pushFolders(filemanager.getFoldersForDirectory())

                appViewModel.pushRecentlyList(filemanager.getDocumentList(context))
            }

            val _extensionsOpen = appViewModel._extensionsOpen.collectAsState()

            var isDrawerOpen = remember { mutableStateOf(false) }
            DocuNoteTheme(darkTheme = true) {
                Scaffold(
                    bottomBar = {
                        BottomAppBar(
                            actions = {

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .clickable {
                                                isDrawerOpen.value = true
                                                Log.d("mainss", "clicked side sheet")
                                            }
                                    ) {

                                        Image(
                                            painter = painterResource(R.drawable.android_dark_rd_na),
                                            contentDescription = null
                                        )
                                    }


                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,


                                        ) {

                                        // extensions ...
                                        Box(
                                            modifier = Modifier
                                                .clickable {
                                                    appViewModel.update_extensionsOpenState(true)
                                                }
                                        ) {

                                            Image(
                                                painter = if (!_extensionsOpen.value) painterResource(
                                                    R.drawable.collapse_content
                                                ) else painterResource(R.drawable.expand_content),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        // scan ...
                                        Box(
                                            modifier = Modifier
                                                .clickable {

                                                }
                                        ) {

                                            Image(
                                                painter = painterResource(R.drawable.document_scanner),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        // read ...
                                        Box(
                                            modifier = Modifier
                                                .clickable {
                                                    ripple(bounded = true, radius = 48.dp)

                                                    val intent =
                                                        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                                            addCategory(Intent.CATEGORY_OPENABLE)
                                                            type = "application/pdf"
                                                        }
                                                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                                                    // MainActivity.mainActivity.startActivityForResult(intent, 2)
                                                    MainActivity.openDocumentLauncher.launch(intent)
                                                }
                                        ) {

                                            Image(
                                                painter = painterResource(R.drawable.read_icon),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop
                                            )
                                        }


                                    }
                                }

                            },
                            floatingActionButton = {
                                FloatingActionButton(
                                    onClick = {

                                    },
                                    elevation = FloatingActionButtonDefaults.loweredElevation(
                                        defaultElevation = 20.dp
                                    ),
                                    containerColor = Color.LightGray
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.ic_launcher_foreground),
                                        contentDescription = null,
                                    )
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                    ) {
                        Folders(appViewModel)
                        RecentlyRead(appViewModel)

                        if (_extensionsOpen.value) {
                            Extensions(onDissmis = {
                                appViewModel.update_extensionsOpenState(false)
                            }, appViewModel = appViewModel)
                        }

                        if (isDrawerOpen.value) {
                            CustomSideDrawerOverlay(
                                isDrawerOpen = isDrawerOpen.value,
                                onDismiss = {
                                    isDrawerOpen.value = isDrawerOpen.value.not()
                                },
                                drawerContent = {
                                    CustomSideDrawerContent()
                                },
                                // No need to pass content here since it's handled separately
                                drawerWidth = 300.dp,  // Customize the drawer width
                                showMask = true,  // Optional: if you want to show the mask when drawer is open
                                drawerSide = DrawerSide.RIGHT,  // Drawer from left, or RIGHT
                                animationDuration = 300  // Animation duration for opening/closing the drawer
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("mainActivty", "onDestroy")
        mainicore.nativeDestroyLibrary()
    }
}


@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    var isDrawerOpen = remember { mutableStateOf(false) }
    DocuNoteTheme(darkTheme = true) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize() ,
            bottomBar = {
                BottomAppBar(
                    actions = {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable(
                                        enabled = true ,
                                        onClick = {
                                            isDrawerOpen.value = isDrawerOpen.value.not()
                                        }
                                    )
                            ) {

                                Image(
                                    painter = painterResource(R.drawable.android_dark_rd_na),
                                    contentDescription = null
                                )
                            }


                            Row(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                ) {

                                // extensions ...
                                Box(
                                    modifier = Modifier
                                        .clickable {
                                          //  appViewModel.update_extensionsOpenState(true)
                                        }
                                ) {

                                  /*  Image(
                                        painter = if (!_extensionsOpen.value) painterResource(
                                            R.drawable.collapse_content
                                        ) else painterResource(R.drawable.expand_content),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop
                                    )*/
                                }

                                // scan ...
                                Box(
                                    modifier = Modifier
                                        .clickable {

                                        }
                                ) {

                                    Image(
                                        painter = painterResource(R.drawable.document_scanner),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                // read ...
                                Box(
                                    modifier = Modifier
                                        .clickable {
                                            ripple(bounded = true, radius = 48.dp)

                                            val intent =
                                                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                                    addCategory(Intent.CATEGORY_OPENABLE)
                                                    type = "application/pdf"
                                                }
                                            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                                            // MainActivity.mainActivity.startActivityForResult(intent, 2)
                                            MainActivity.openDocumentLauncher.launch(intent)
                                        }
                                ) {

                                    Image(
                                        painter = painterResource(R.drawable.read_icon),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop
                                    )
                                }


                            }
                        }

                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {

                            },
                            elevation = FloatingActionButtonDefaults.loweredElevation(
                                defaultElevation = 20.dp
                            ),
                            containerColor = Color.LightGray
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_launcher_foreground),
                                contentDescription = null,
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->

            MainContent(modifier = Modifier.padding(innerPadding))
            if (isDrawerOpen.value){
                CustomSideDrawerOverlay(
                    isDrawerOpen = isDrawerOpen.value,
                    onDismiss = {
                        isDrawerOpen.value = isDrawerOpen.value.not()
                    },
                    drawerContent = {
                        CustomSideDrawerContent()
                    },
                    // No need to pass content here since it's handled separately
                    drawerWidth = 300.dp,  // Customize the drawer width
                    showMask = true,  // Optional: if you want to show the mask when drawer is open
                    drawerSide = DrawerSide.RIGHT,  // Drawer from left, or RIGHT
                    animationDuration = 300  // Animation duration for opening/closing the drawer
                )
            }
        }
    }

}

@Composable
fun MainContent(modifier: Modifier){
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {

    }
}


@Composable
fun CustomSideDrawerOverlay(
    isDrawerOpen: Boolean,
    onDismiss: () -> Unit,
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    drawerWidth: Dp = 300.dp,
    animationDuration: Int = 300,
    maskColor: Color = Color.Black.copy(alpha = 0.5f),
    showMask: Boolean = false,
    drawerSide: DrawerSide = DrawerSide.RIGHT,
    cornerRadius: Dp = 32.dp,
    dragThresholdFraction: Float = 0.5f,
    enableSwipe: Boolean = true
) {
    // Coroutine scope for managing animations
    val scope = rememberCoroutineScope()

    val density = LocalDensity.current

    // Width of the drawer in pixels
    val drawerWidthPx = with(density) { drawerWidth.toPx() }

    // Offset for the drawer animation
    val offsetX = remember { Animatable(if (isDrawerOpen) 0f else drawerWidthPx * (if (drawerSide == DrawerSide.LEFT) -1 else 1)) }

    // Launch animation when the drawer state changes
    LaunchedEffect(isDrawerOpen) {
        val targetOffsetX = if (isDrawerOpen) 0f else drawerWidthPx * (if (drawerSide == DrawerSide.LEFT) -1 else 1)
        offsetX.animateTo(
            targetValue = targetOffsetX,
            animationSpec = tween(durationMillis = animationDuration)
        )
    }

    if (isDrawerOpen) {
        BackHandler {
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1f)
    ) {

        // Mask overlay when the drawer is open
        if (isDrawerOpen && showMask) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(maskColor)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onDismiss() })
                    }
            )
        }

        // Drawer content
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(drawerWidth)
                .offset { IntOffset(x = 2 * offsetX.value.roundToInt(), y = 0) }
                .align(if (drawerSide == DrawerSide.LEFT) Alignment.CenterStart else Alignment.CenterEnd)
                .systemBarsPadding()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = if (cornerRadius > 0.dp) {
                        if (drawerSide == DrawerSide.LEFT) {
                            RoundedCornerShape(topEnd = cornerRadius, bottomEnd = cornerRadius)
                        } else {
                            RoundedCornerShape(topStart = cornerRadius, bottomStart = cornerRadius)
                        }
                    } else {
                        RectangleShape
                    }
                )
                .pointerInput(Unit) {
                    if (enableSwipe) {
                        detectDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    val shouldClose = when (drawerSide) {
                                        DrawerSide.LEFT -> offsetX.value < -drawerWidthPx * dragThresholdFraction
                                        DrawerSide.RIGHT -> offsetX.value > drawerWidthPx * dragThresholdFraction
                                    }

                                    val finalTarget = if (shouldClose) {
                                        drawerWidthPx * (if (drawerSide == DrawerSide.LEFT) -1 else 1)
                                    } else {
                                        0f
                                    }

                                    offsetX.animateTo(
                                        targetValue = finalTarget,
                                        animationSpec = tween(durationMillis = animationDuration)
                                    )

                                    if (shouldClose) {
                                        onDismiss()
                                    }
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()

                            scope.launch {
                                val newOffset = offsetX.value + dragAmount.x

                                val clampedOffset = when (drawerSide) {
                                    DrawerSide.LEFT -> newOffset.coerceIn(-drawerWidthPx, 0f)
                                    DrawerSide.RIGHT -> newOffset.coerceIn(0f, drawerWidthPx)
                                }

                                offsetX.snapTo(clampedOffset)
                            }
                        }
                    }
                }
        ) {
            // Content inside the drawer
            drawerContent()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun CustomSideDrawerContent(
    drawerWidth: Dp = 300.dp ,
    cornerRadius: Dp = 32.dp,
){
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(drawerWidth)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(topEnd = cornerRadius, bottomEnd = cornerRadius)

            ) ,
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(64.dp))
        Box(

        ) {
            AsyncImage(
                model = Auth.auth.currentUser?.photoUrl ,
                contentDescription = "stringResource(R.string.profile_photo)" ,
                contentScale = ContentScale.Crop ,
                modifier = Modifier
                    .size(172.dp)
                    .clip(CircleShape)
            )


        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = Auth.auth.currentUser?.displayName!! ,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(32.dp))

        Column {


            OutlinedButton(
                onClick = {
                    val intent = Intent()
                    intent.setAction(Intent.ACTION_SEND)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.setType("*/*")
                    intent.putExtra(Intent.EXTRA_TEXT,"merhaba")
                    context.startActivity(Intent.createChooser(intent,"share"))
                } ,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.share_with_others)
                )
            }

            Text(
                text = "stringResource(R.string.earn_token)" ,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) ,
                style = MaterialTheme.typography.labelSmall ,
            )


            OutlinedButton(
                onClick = {
                    // InAppReview.requestReview(context)
                } ,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "stringResource(R.string.rate_review)"
                )
            }
        }


        Spacer(modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    Auth.signOut(context)
                }
            } ,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = "stringResource(R.string.sign_out)" ,
            )
        }
    }
}