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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import com.batuscode.docunote.utils.FileManager
import com.batuscode.docunote.view.RecentlyRead
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.batuscode.docunote.MainActivity.Companion.snackbarHostState
import com.batuscode.docunote.utils.AssetPacksUtil
import com.batuscode.docunote.utils.DrawerSide
import com.batuscode.docunote.utils.PDFConverter
import com.batuscode.docunote.utils.WordDocUtil
import com.batuscode.docunote.view.CustomSideDrawerContent
import com.batuscode.docunote.view.CustomSideDrawerOverlay
import com.batuscode.docunote.view.DFDownloadProgressView
import com.batuscode.docunote.view.DynamicFeatureRequestDialog
import com.batuscode.pdfium.icore
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
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

                        if (!isDrawerOpen.value) {
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
// scan ...
                                Box(
                                    modifier = Modifier
                                        .clickable {

                                        }
                                ) {

                                    Image(
                                        painter = painterResource(R.drawable.expand_content),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop
                                    )
                                }
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
            }
        }
    }
}