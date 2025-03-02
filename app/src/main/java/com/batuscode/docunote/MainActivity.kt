package com.batuscode.docunote

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ComponentCaller
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Debug
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.systemGesturesPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.batuscode.docunote.model.Folder
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.utils.PDFUtils
import com.batuscode.docunote.view.Extensions
import com.batuscode.docunote.view.Folders
import com.batuscode.docunote.view.HandNotes
import com.batuscode.docunote.viewmodel.AppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.core.app.ActivityCompat
import com.batuscode.docunote.utils.FileManager
import com.batuscode.docunote.view.RecentlyRead

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.batuscode.pdfium.icore
import java.io.File
import java.io.FileOutputStream


class MainActivity : ComponentActivity() {
    companion object {
        init {
            System.loadLibrary("jpdfium");
        }
        lateinit var _appViewModel: AppViewModel
        lateinit var mainActivity: ComponentActivity
        lateinit var context: Context
        lateinit var openDocumentLauncher: ActivityResultLauncher<Intent>
        lateinit var mergeDocumentLauncher: ActivityResultLauncher<Intent>
        lateinit var multiplyselectTofolderDocumentLauncher: ActivityResultLauncher<Intent>
        var newFolder = mutableStateOf(false)
        lateinit var mainicore: icore
        lateinit var fileManager: FileManager
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

    @SuppressLint("Range")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        caller: ComponentCaller
    ) {
         if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            data?.let { it ->
                // Dosya URI'sini işlemek

                // Log.d("newuri" , it.toString())

                if (it.clipData != null){
                    val count = it.clipData?.itemCount ?: 0
                    for (i in 0 until count){
                        val uri = it.clipData?.getItemAt(i)?.uri
                        uri?.let {
                            _appViewModel.add_urlist(it)

                            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                            contentResolver.takePersistableUriPermission(it, takeFlags)
                        }
                        Log.d("newuri" , uri.toString())
                    }

                    newFolder.value = true

                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/pdf"
                       // putExtra(Intent.EXTRA_TITLE, R.string.documentname)

                        // Optionally, specify a URI for the directory that should be opened in
                        // the system file picker before your app creates the document.
                    }
                    mainActivity.startActivityForResult(intent, 0)

                }else {
                    // Tekli dosya seçimi
                    val uri = it.data
                    Log.d("newuri" , uri.toString())

                }

            }
        }
        else if (requestCode == 0 && resultCode == Activity.RESULT_OK){
             data?.let { it ->
                 val uri = it.data
                 Log.d("newuri" , uri.toString())

                 val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                         Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                 contentResolver.takePersistableUriPermission(uri!!, takeFlags)

                 val cursor = contentResolver.query(uri,null,null,null)
                 val nameindex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                 cursor?.moveToFirst()
                 val name = cursor?.getString(nameindex!!)

                 Log.d("tahmin" , " cursor dosya adı ::: ")
                 cursor?.close()

                 lifecycleScope.launch(Dispatchers.Main){
                     val pdfutils = PDFUtils()

                     pdfutils.mergeDocument(_appViewModel.uris , name!! , uri!! , mainActivity.contentResolver)


                 }
             }
         }
         else if (requestCode == 2 && resultCode == Activity.RESULT_OK){

             Log.d("newuri" ,"sonuc döndü")

             data?.let { it ->
                 val uri = it.data
                 val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                         Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                 contentResolver.takePersistableUriPermission(uri!!, takeFlags)

                 Log.d("newuri" , uri.toString())

                 val cursor = contentResolver.query(
                     uri!! ,
                     arrayOf(OpenableColumns.DISPLAY_NAME) ,
                     null ,
                     null ,
                     null
                 )

                 cursor?.use {
                     if (it.moveToFirst()){
                         val displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                         val dotIndex = displayName.lastIndexOf('.')
                         val fileNameWithoutExtension = if (dotIndex != -1) displayName.substring(0, dotIndex) else displayName

                         Log.d("newuri" , displayName)
                         lifecycleScope.launch(Dispatchers.Main){

                             val intent = Intent(context , PDFViewerActivity::class.java).apply {
                                 putExtra("fileUri" , uri.toString())
                                 putExtra("fileDisplayName" , fileNameWithoutExtension)
                             }

                             mainActivity.startActivity(intent)
                         }
                     }
                 }


             }
         }
         else if (requestCode == 22 && resultCode == Activity.RESULT_OK){
             data?.data?.also { uri ->
                 // Perform operations on the document using its URI.
                 val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                         Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                 contentResolver.takePersistableUriPermission(uri, takeFlags)
             }

         }
         else if (requestCode == 55 && resultCode == Activity.RESULT_OK) {
             data?.let { it ->
                 // Dosya URI'sini işlemek

                 // Log.d("newuri" , it.toString())



                 if (it.clipData != null){
                     val count = it.clipData?.itemCount ?: 0
                     for (i in 0 until count){
                         val uri = it.clipData?.getItemAt(i)?.uri
                         uri?.let {
                             _appViewModel.add_urlist(it)

                             val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                     Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                             contentResolver.takePersistableUriPermission(it, takeFlags)
                         }
                         Log.d("newuri" , uri.toString())
                     }

                     newFolder.value = true



                 }else {
                     // Tekli dosya seçimi
                     val uri = it.data
                     Log.d("newuri" , uri.toString())

                 }

             }
         } else {
             Log.d("realdevice" , "sikinti var...")
         }

        super.onActivityResult(requestCode, resultCode, data, caller)
    }

    val requestPermissionLauncher =
        registerForActivityResult(RequestPermission()
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
    @SuppressLint("Range")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val appViewModel : AppViewModel by viewModels()
        _appViewModel = appViewModel
        mainActivity = this
        mainicore = icore(this)
        mainicore.nativeInitLibrary()
        fileManager = FileManager(this)

      //  PDFBoxResourceLoader.init(getApplicationContext());

       /* val folder1 = Folder( 1 , "Matematik" , R.drawable.folder_icon_4_01)
        val folder2 = Folder( 2 , "Coğrafya" , R.drawable.folder_icon_4_01)
        val folder3 = Folder( 3 , "Kimya" , R.drawable.folder_icon_4_01)
        val folder4 = Folder( 4 , "Türk Dili ve Edebiyatı" , R.drawable.folder_icon_4_01)
        appViewModel.loadFolders(folder)
        appViewModel.loadFolders(folder1)
        appViewModel.loadFolders(folder2)
        appViewModel.loadFolders(folder3)
        appViewModel.loadFolders(folder4)

        appViewModel.loadhandNotesFolders(folder)
        appViewModel.loadhandNotesFolders(folder1)
        appViewModel.loadhandNotesFolders(folder2)
        appViewModel.loadhandNotesFolders(folder3)
        appViewModel.loadhandNotesFolders(folder4)*/

        multiplyselectTofolderDocumentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK){
                val data: Intent? = result.data

                data?.let { it ->
                    // Dosya URI'sini işlemek

                    // Log.d("newuri" , it.toString())



                    if (it.clipData != null){
                        val count = it.clipData?.itemCount ?: 0
                        for (i in 0 until count){
                            val uri = it.clipData?.getItemAt(i)?.uri
                            uri?.let {
                                _appViewModel.add_urlist(it)

                                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                                contentResolver.takePersistableUriPermission(it, takeFlags)
                            }
                            Log.d("newuri" , uri.toString())
                        }

                        newFolder.value = true



                    }else {
                        // Tekli dosya seçimi
                        val uri = it.data
                        Log.d("newuri" , uri.toString())

                    }

                }
            }
        }
        mergeDocumentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK){
                val data: Intent? = result.data
                data?.let { it ->
                    // Dosya URI'sini işlemek

                    // Log.d("newuri" , it.toString())

                    if (it.clipData != null){
                        val count = it.clipData?.itemCount ?: 0
                        for (i in 0 until count){
                            val uri = it.clipData?.getItemAt(i)?.uri
                            uri?.let {
                                _appViewModel.add_urlist(it)

                                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                                contentResolver.takePersistableUriPermission(it, takeFlags)
                            }
                            Log.d("newuri" , uri.toString())
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

                    }else {
                        // Tekli dosya seçimi
                        val uri = it.data
                        Log.d("newuri" , uri.toString())

                    }

                }
            }
        }
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

                    Log.d("newuri" , uri.toString())

                    val cursor = contentResolver.query(
                        uri!! ,
                        arrayOf(OpenableColumns.DISPLAY_NAME) ,
                        null ,
                        null ,
                        null
                    )

                    cursor?.use {
                        if (it.moveToFirst()){
                            val displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                            val dotIndex = displayName.lastIndexOf('.')
                            val fileNameWithoutExtension = if (dotIndex != -1) displayName.substring(0, dotIndex) else displayName

                            Log.d("newuri" , displayName)
                            lifecycleScope.launch(Dispatchers.Main){

                                val intent = Intent(context , PDFViewerActivity::class.java).apply {
                                    putExtra("fileUri" , uri.toString())
                                    putExtra("fileDisplayName" , fileNameWithoutExtension)
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

            val scaleFactor = LocalContext.current.resources.displayMetrics.densityDpi / 72f

            context = LocalContext.current

            var filemanager = remember {
                FileManager(context = context)
            }

          /*  var folders = remember {
                mutableStateOf<List<Folder>>(emptyList())
            }*/

            LaunchedEffect(Unit) {
               // folders.value = filemanager.getFoldersForDirectory()

                appViewModel.pushFolders(filemanager.getFoldersForDirectory())

                appViewModel.pushRecentlyList(filemanager.getDocumentList(context))
            }

            val _extensionsOpen = appViewModel._extensionsOpen.collectAsState()
            DocuNoteTheme() {


                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .fillMaxSize()
                        ,
                    floatingActionButton = {
                        FloatingActionButton(
                            containerColor = MaterialTheme.colorScheme.background,
                            onClick = {
                                appViewModel.update_extensionsOpenState(_extensionsOpen.value.not())
                            } ,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_extension_24) ,
                                contentDescription = "extensions")
                        }
                    } ,


                ) { innerPadding ->

                    if (newFolder.value){
                        mCustomDialog(
                            onDismissRequest = { newFolder.value = newFolder.value.not() },
                            onConfirm = {

                                val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ,it)

                                dir.mkdirs()

                                appViewModel.uris.forEach {
                                    uri ->

                                    val cursor = contentResolver.query(
                                        uri!! ,
                                        arrayOf(OpenableColumns.DISPLAY_NAME) ,
                                        null ,
                                        null ,
                                        null
                                    )

                                    cursor?.use {
                                        if (it.moveToFirst()){
                                            val displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                                            val dotIndex = displayName.lastIndexOf('.')
                                            val fileNameWithoutExtension = if (dotIndex != -1) displayName.substring(0, dotIndex) else displayName

                                            val destinationFile = File(dir, fileNameWithoutExtension)

                                            val inputstream = context.contentResolver.openInputStream(uri)

                                            inputstream?.use { input ->

                                                FileOutputStream(destinationFile).use { out ->
                                                    val buffer = ByteArray(1024)
                                                    var length: Int
                                                    while (input.read(buffer).also { length = it } > 0) {
                                                        out.write(buffer, 0, length)
                                                    }
                                                }
                                            }
                                        }
                                    }




                                }


                                val folder = Folder(1 , name = it , R.drawable.folder_icon_4_01)
                                MainActivity._appViewModel.addFolder(folder)

                                newFolder.value = newFolder.value.not() // Dialogu kapat
                            }
                        )
                    }
                    if (_extensionsOpen.value){
                        Extensions(appViewModel = appViewModel)
                    }
                    Flow(innerPadding , appViewModel = appViewModel)
                }
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainicore.nativeDestroyLibrary()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Flow(innerPadding: PaddingValues, appViewModel: AppViewModel) {

    val colors = arrayOf(
        0.50f to Color(0xFF1E1E1E),
        1f to colorResource(R.color.teal_700) ,
    )

    val color = colorResource(R.color.modified)

    val folderCardBrush = Brush.linearGradient(colorStops = colors)


    Column (
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                val width = size.width
                val height = 270.dp.toPx()

                // Köşe radyuslarını tanımlıyoruz
                val normalRadius = 80.dp.toPx()
                val largeRadius = 80.dp.toPx()

                // Çizim için Path oluşturuyoruz
                val path = Path().apply {
                    // Sol üst köşeden başlıyoruz
                    moveTo(0f, 0f)

                    // Sağ üst köşeye gidiyoruz
                    lineTo(width, 0f)

                    // Sağ alt köşeye gidiyoruz, burada dışa doğru büyük bir radyus uyguluyoruz
                    lineTo(width, height - largeRadius)
                    arcTo(
                        rect = Rect(
                            width - largeRadius, // Dışa doğru radyus
                            height - largeRadius + 80.dp.toPx(),
                            width,
                            height + 80.dp.toPx()
                        ),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = -90f,
                        forceMoveTo = false
                    )

                    // Sol alt köşeye gidiyoruz, burada normal bir radyus
                    lineTo(normalRadius, height)
                    arcTo(
                        rect = Rect(
                            0f,
                            height - normalRadius,
                            normalRadius,
                            height
                        ),
                        startAngleDegrees = 90f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )

                    // Sol üst köşeye dönüyoruz
                    lineTo(0f, 0f)

                    close() // Path'i kapatıyoruz
                }

                // Path'i bir renkle çiziyoruz
                drawPath(path, color = color)
            }



        ,

    ) {

        Folders(appViewModel)

        RecentlyRead(appViewModel)


    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DocuNoteTheme {
        Flow(PaddingValues() , AppViewModel())
    }
}


class Permissions(val context: Context) {

}