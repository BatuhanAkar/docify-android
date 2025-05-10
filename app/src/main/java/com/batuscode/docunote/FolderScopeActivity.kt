package com.batuscode.docunote

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModelProvider
import com.batuscode.docunote.MainActivity.Companion.context
import com.batuscode.docunote.manager.ActivityResultLauncherManager
import com.batuscode.docunote.model.Folder
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.viewmodel.FolderScopeActivityViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.batuscode.docunote.model.File

@AndroidEntryPoint
class FolderScopeActivity : ComponentActivity() {
    companion object {
        lateinit var folderScopeActivityViewModel: FolderScopeActivityViewModel
        const val TAG = "drpfldr"
        var folderName = mutableStateOf("")
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityResultLauncherManager.folderScopeActivity = this@FolderScopeActivity
        ActivityResultLauncherManager.init_docsToFolder()
        val folder = intent.getParcelableExtra("folder" , Folder::class.java)
        folderName.value = intent.getStringExtra("folderName").toString()
        Log.d("FolderQuery" , "clicked index " + folderName.value)
        Log.d("FolderQuery" , "folder id :: ${folder?.id!!}")

        folderScopeActivityViewModel = ViewModelProvider(this).get(FolderScopeActivityViewModel::class.java)
        folderScopeActivityViewModel.getDocuments(folder?.id!!)

        CoroutineScope(Dispatchers.IO).launch {
            folderScopeActivityViewModel.addNewDocuments.collect{
                val docsMap = ActivityResultLauncherManager.parseDocsMapUris(
                    folderScopeActivityViewModel.UrisMap.value)
                Log.d(TAG , "activity result launcher docsMap siez :: ${docsMap.size}")
                folderScopeActivityViewModel.addDocumentToFolder(folder?.id!! , docsMap)
            }
        }
        setContent {
            val context = LocalContext.current




            val color = colorResource(R.color.modified)

            val _documents = folderScopeActivityViewModel.documents.collectAsState()



            enableEdgeToEdge()
            DocuNoteTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            modifier = Modifier
                                .statusBarsPadding(),
                            title = {
                                folderName.let {
                                    Text(text = folderName.value , color = Color.White)
                                }
                            } ,
                            actions = {
                                IconButton(
                                    onClick = {
                                        Log.d(TAG , "folder id :: ${folder?.id!!}")

                                        ActivityResultLauncherManager.pickPDFToFolder(true){ UrisMap ->
                                            Log.d(TAG , "activity result launcher UrisMap siez :: ${UrisMap?.size}")
                                            Log.d(TAG , "activity result launcher UrisMap :: " + Gson().toJson(UrisMap))
                                            folderScopeActivityViewModel.handle_addNewDocuments(UrisMap)
                                        }
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.doc_add) ,
                                        contentDescription = null ,
                                        modifier = Modifier
                                            .size(24.dp)
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = Color.White
                                    ),
                                    onClick = {
                                        onBackPressedDispatcher.onBackPressed()
                                    }
                                ) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack , contentDescription = "" , )
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()


                ) { innerPadding ->

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ){
                        LazyColumn (
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 40.dp , vertical = 40.dp)


                        ) {
                            items(_documents.value){
                                    file -> FileView(file,  )
                            }
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun FileView(file:File ,modifier: Modifier = Modifier){

    val color = colorResource(R.color.modified)
    ElevatedCard(
        onClick = {
            ripple(bounded = true)

            val intent = Intent(context , PDFViewerActivity::class.java).apply {
                putExtra("fileUri" , file.uri)
                putExtra("fileDisplayName" , file.name)
            }
            context.startActivity(intent)
        },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp , pressedElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        modifier = Modifier
            .padding(vertical = 8.dp)


    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp , vertical = 16.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.open_pdf_01) ,
                contentDescription = "icon" ,
                alignment = Alignment.Center ,
                modifier = Modifier
                    .size(32.dp)
                    .zIndex(1f)
            )
            Text(
                color = MaterialTheme.colorScheme.onPrimary,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                text = file.name ,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview4() {
    DocuNoteTheme {
       // FileView(File())
    }
}