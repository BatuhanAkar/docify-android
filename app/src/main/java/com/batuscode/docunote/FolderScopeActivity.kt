package com.batuscode.docunote

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.batuscode.docunote.MainActivity.Companion.context
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.utils.File
import com.batuscode.docunote.utils.FileManager

class FolderScopeActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current

            var folderName = remember {
                mutableStateOf("")
            }

            var filemanager = remember {
                FileManager(context = context)
            }

            var files = remember {
                mutableStateOf<List<File>>(emptyList())
            }

            val color = colorResource(R.color.modified)

            LaunchedEffect(Unit) {
                folderName.value = intent.getStringExtra("folderName").toString()
                Log.d("FolderQuery" , "clicked index " + folderName.value)

                files.value = filemanager.getFilesForDirectory(folderName.value)
            }


            enableEdgeToEdge()
            DocuNoteTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            modifier = Modifier
                                .statusBarsPadding(),
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = color
                            ),
                            title = {
                                folderName.let {
                                    Text(text = folderName.value , color = Color.White)
                                }
                            } ,
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
                            .drawBehind {
                                val width = size.width
                                val height =  35.dp.toPx()

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
                    ){
                        LazyColumn (
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 40.dp , vertical = 40.dp)


                        ) {
                            items(files.value){
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