package com.batuscode.docunote

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.batuscode.docunote.manager.ActivityResultLauncherManager
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.utils.PDFUtil
import com.batuscode.docunote.utils.ScanUtil
import com.batuscode.docunote.view.Extensions
import com.batuscode.docunote.view.Folders
import com.batuscode.docunote.view.RecentlyRead
import com.batuscode.docunote.viewmodel.AppViewModel
import com.batuscode.docunote.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        lateinit var mainActivityViewModel: MainActivityViewModel
        lateinit var _appViewModel: AppViewModel
        lateinit var mainActivity: ComponentActivity
        lateinit var context: Context
        lateinit var openDocumentLauncher: ActivityResultLauncher<Intent>
        lateinit var toFolderDocumentLauncher: ActivityResultLauncher<Intent>
        var memUri = mutableStateOf<Uri?>(null)
        var newFolder = mutableStateOf(false)
        var recentlyStat = mutableStateOf(false)
        var folderStat = mutableStateOf(false)
        val snackbarHostState = SnackbarHostState()
        var waitinit = mutableStateOf(false)
        var handlePDFProcessType = mutableStateOf("")
        var splitPDFName = mutableStateOf("")
        var splitRange = mutableStateOf("")
        var mergePDFDocName = mutableStateOf("")

        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }


    }

    private val cameraPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                CoroutineScope(Dispatchers.Default).launch {
                    ScanActivity.snackbarHostState.showSnackbar(
                        message = "Permissin request denied." ,
                        duration = SnackbarDuration.Short
                    )
                }
            } else {
                val intent = Intent(context , ScanActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        }

    @Composable
    fun splitDialog(
        onDismissRequest: () -> Unit,
        onConfirm: (String, String) -> Unit
    ) {


        var textFieldValue by remember { mutableStateOf("") }
        var startRangeValue by remember { mutableStateOf("") }
        var endRangeValue by remember { mutableStateOf("") }

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


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @SuppressLint("Range")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        val appViewModel: AppViewModel by viewModels()
        _appViewModel = appViewModel
        mainActivity = this
        ActivityResultLauncherManager.activity = this@MainActivity
        ActivityResultLauncherManager.init()
        mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        lifecycleScope.launch {
            mainActivityViewModel.inPDFProcess.collect{

                when(handlePDFProcessType.value){
                    "merge" -> {
                        mainActivityViewModel.update_OnScreenMergePDF(false)
                        mainActivityViewModel.update_OnBackOperation(true)
                        PDFUtil.mergePDFS(ActivityResultLauncherManager.MultiplePDFFileUris, context , mergePDFDocName.value){ complated ->
                            if (complated){

                                mainActivityViewModel.update_OnBackOperation(false)
                            }
                        }
                    }

                    "split" -> {
                        mainActivityViewModel.update_OnScreenSplitPDF(false)
                        mainActivityViewModel.update_OnBackOperation(true)
                        PDFUtil.splitPDF(memUri.value!!, context , splitPDFName.value , splitRange.value){ complated ->
                            if (complated){

                                mainActivityViewModel.update_OnBackOperation(false)
                            }
                        }
                    }
                }

            }
        }


        enableEdgeToEdge()
        setContent {
            val _extensionsOpen = appViewModel._extensionsOpen.collectAsState()
            val _isOnScreenMergePDF by mainActivityViewModel.isOnScreenMergePDF.collectAsState()
            val _isOnScreenSplitPDF by mainActivityViewModel.isOnScreenSplitPDF.collectAsState()
            val _isOnBackOperation by mainActivityViewModel.isBackOperation.collectAsState()
            val _BackOperationDescription by mainActivityViewModel.BackOperationDescription.collectAsState()
            val _isNewFolderOnScreen by mainActivityViewModel.isNewFolderOnScreen.collectAsState()

            DocuNoteTheme(darkTheme = true) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize(),
                    bottomBar = {
                        BottomAppBar(
                            actions = {

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {


                                    Row(
                                        modifier = Modifier
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,


                                        ) {


                                        // scan ...
                                        Box(
                                            modifier = Modifier
                                                .clickable {
                                                    ScanUtil.update_FromImageOrganizerActivity(false)
                                                    ScanUtil.update_firstOpen(true)
                                                    cameraPermissionLauncher.launch(
                                                        arrayOf(Manifest.permission.CAMERA)
                                                    )
                                                }
                                        ) {

                                            Image(
                                                painter = painterResource(R.drawable.document_scanner),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop ,
                                                modifier = Modifier
                                                    .size(32.dp)
                                            )
                                        }
                                        // read ...
                                        Box(
                                            modifier = Modifier
                                                .clickable {
                                                    ripple(bounded = true, radius = 48.dp)
                                                    ActivityResultLauncherManager.pickPDF(allowMultiplePick = false) { uri , FileNameWithOutExtension , _ ->

                                                        val intent = Intent(context, PDFViewerActivity::class.java).apply {
                                                            putExtra("fileUri", uri.toString())
                                                            putExtra("fileDisplayName", FileNameWithOutExtension)
                                                        }
                                                        context.startActivity(intent)
                                                    }
                                                }
                                        ) {

                                            Image(
                                                painter = painterResource(R.drawable.read_icon),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop ,
                                                modifier = Modifier
                                                    .size(32.dp)
                                            )
                                        }


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
                                                contentScale = ContentScale.Crop ,
                                                modifier = Modifier
                                                    .size(32.dp)
                                            )
                                        }


                                    }
                                }

                            },
                            floatingActionButton = {
                                FloatingActionButton(
                                    onClick = {
                                        finish()
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

                    MainContent(
                        modifier = Modifier
                            .padding(innerPadding) ,
                        appViewModel
                    )

                    if (_extensionsOpen.value) {
                        Extensions(onDissmis = {
                            appViewModel.update_extensionsOpenState(false)
                        }, appViewModel = appViewModel)
                    }

                    if (_isOnScreenMergePDF){
                        mCustomDialog(
                            onDismissRequest = {
                                handlePDFProcessType.value = "merge"
                                mainActivityViewModel.update_OnScreenMergePDF(false)
                            } ,
                            onConfirm = { docName ->
                                handlePDFProcessType.value = "merge"
                                mergePDFDocName.value = docName
                                mainActivityViewModel.handle_PDFProcess()
                            }
                        )
                    }

                    if (_isOnScreenSplitPDF){
                        splitDialog(
                            onDismissRequest = {
                                mainActivityViewModel.update_OnScreenSplitPDF(false)
                            } ,
                            onConfirm = { fileName , range ->
                                handlePDFProcessType.value = "split"
                                splitPDFName.value = fileName
                                splitRange.value = range
                                memUri.value.let {
                                    mainActivityViewModel.handle_PDFProcess()
                                }
                            }
                        )
                    }

                    if (_isOnBackOperation){
                        onBackOperationDailog(
                            onDismissRequest = {
                                CoroutineScope(Dispatchers.Default).launch {
                                    snackbarHostState.showSnackbar(
                                        message = "$_BackOperationDescription is complated." ,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } ,
                            description = _BackOperationDescription
                        )
                    }

                    if (_isNewFolderOnScreen){
                        mCustomDialog(
                            onDismissRequest = {
                                mainActivityViewModel.update_NewFolderOnScreen(false)
                            } ,
                            onConfirm = { folderName ->
                                mainActivityViewModel.createNewFolder(folderName = folderName)
                                mainActivityViewModel.update_NewFolderOnScreen(false)
                            }
                        )
                    }

                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("mainActivty", "onDestroy")
    }
}

@Composable
fun onBackOperationDailog(
    onDismissRequest: () -> Unit,
    description : String
){

    Dialog(
        onDismissRequest = { onDismissRequest() } ,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true , usePlatformDefaultWidth = true)
    ) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .wrapContentHeight() ,
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp), // Opsiyonel: Butonun etrafında boşluk bırakmak için
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 2.dp ,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Text(
                    text = description
                )
            }
        }
    }
}


@Composable
fun MainContent(modifier: Modifier , appViewModel: AppViewModel){
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Folders(appViewModel)
        RecentlyRead(appViewModel)
    }
}




@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true , showSystemUi = true)
@Composable
fun MainContentPreview() {
    var isDrawerOpen = remember { mutableStateOf(false) }
    DocuNoteTheme(darkTheme = true) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize() ,
            bottomBar = {
                BottomAppBar(
                    modifier = Modifier
                        .background(Color.Transparent) ,
                    actions = {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically ,

                            ) {

                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clickable {
                                            isDrawerOpen.value = isDrawerOpen.value.not()
                                            Log.d("mainss", "clicked side sheet")
                                        }
                                ) {

                                    Image(
                                        painter = painterResource(R.drawable.android_dark_rd_na),
                                        contentDescription = null
                                    )
                                }

                                // premium badge ...
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clickable {
                                        }
                                ) {

                                    Image(
                                        painter = painterResource(R.drawable.workspace_premium),
                                        contentDescription = null
                                    )
                                }
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

          //  MainContent(modifier = Modifier.padding(innerPadding))




        }

    }

}