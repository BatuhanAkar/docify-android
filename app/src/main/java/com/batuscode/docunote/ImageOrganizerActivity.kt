package com.batuscode.docunote

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.utils.PDFUtil
import com.batuscode.docunote.utils.PDFUtil.FILE_NAME_FORMAT
import com.batuscode.docunote.utils.ScanUtil
import com.batuscode.docunote.viewmodel.ImageOrganizerActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class ImageOrganizerActivity : ComponentActivity() {
    companion object {
        val snackbarHostState = SnackbarHostState()
        lateinit var imageOrganizerActivityViewModel: ImageOrganizerActivityViewModel
    }

    override fun onDestroy() {
        super.onDestroy()
        CoroutineScope(Dispatchers.IO).launch {
            ScanUtil.update_firstOpen(false)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageOrganizerActivityViewModel = ViewModelProvider(this).get(ImageOrganizerActivityViewModel::class.java)
        enableEdgeToEdge()
        setContent {
            var fileName = remember { mutableStateOf("") }
            val imageUris = ScanUtil.photoUris.collectAsState()

            DocuNoteTheme(darkTheme = true) {
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize(),
                    bottomBar = {
                        BottomAppBar(
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth() ,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                IconButton(
                                    onClick = {
                                        ScanUtil.update_FromImageOrganizerActivity(true)
                                        val intent = Intent(this@ImageOrganizerActivity , ScanActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.add) ,
                                        contentDescription = ""
                                    )
                                }
                                BasicTextField(
                                    modifier = Modifier
                                        .weight(1f)
                                        .requiredHeight(42.dp),
                                    singleLine = true,
                                    value = fileName.value ,
                                    onValueChange = { newFileName ->
                                        fileName.value = newFileName
                                    } ,
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier
                                                .shadow(
                                                    elevation = 8.dp,
                                                    shape = RoundedCornerShape(32.dp)
                                                )
                                                .background(
                                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(
                                                    horizontal = 32.dp,
                                                    vertical = 8.dp
                                                )

                                        ) {
                                            if (fileName.value.isEmpty()){
                                                Text(
                                                    text = stringResource(R.string.pdffilename) ,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            innerTextField()
                                        }

                                    }
                                )

                                IconButton(
                                    onClick = {
                                        var fname = SimpleDateFormat(FILE_NAME_FORMAT , Locale.getDefault()).format(System.currentTimeMillis())
                                        if (fileName.value.isNotEmpty()){
                                            fname = fileName.value
                                        }

                                        CoroutineScope(Dispatchers.IO).launch {
                                            PDFUtil.createPDFFromJPEG(imageUris = imageUris.value , this@ImageOrganizerActivity , fname)
                                        }
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.picture_as_pdf) ,
                                        contentDescription = ""
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        ImageScope()
                    }
                }
            }
        }
    }
}

@Composable
fun ImageScope(){
    val images = listOf(
        R.drawable.folder_icon_4_01,
        R.drawable.android_dark_rd_na ,
        R.drawable.ic_launcher_foreground
    )


    val imageUris = ScanUtil.photoUris.collectAsState()
    var selectedImage = remember { mutableStateOf(imageUris.value.get(0)) }

    Column(
        modifier = Modifier
            .fillMaxSize() ,
        verticalArrangement = Arrangement.Top ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = selectedImage.value ,
            contentDescription = "" ,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.1f)
                .fillMaxWidth()
        ) {
            items(imageUris.value){ image ->
                AsyncImage(
                    model = image ,
                    contentDescription = "" ,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clickable { 
                            selectedImage.value = image
                        }
                )
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun ImageScopePreview() {
    var fileName = remember { mutableStateOf("") }
    DocuNoteTheme(darkTheme = true) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            bottomBar = {
                BottomAppBar(
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth() ,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        IconButton(
                            onClick = {

                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.add) ,
                                contentDescription = ""
                            )
                        }
                        BasicTextField(
                            modifier = Modifier
                                .weight(1f)
                                .requiredHeight(42.dp),
                            singleLine = true,
                            value = fileName.value ,
                            onValueChange = { newFileName ->
                                fileName.value = newFileName
                            } ,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier
                                        .shadow(
                                            elevation = 8.dp,
                                            shape = RoundedCornerShape(32.dp)
                                        )
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(
                                            horizontal = 32.dp,
                                            vertical = 8.dp
                                        )

                                ) {
                                    if (fileName.value.isEmpty()){
                                        Text(
                                            text = stringResource(R.string.pdffilename) ,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    innerTextField()
                                }

                            }
                        )

                        IconButton(
                            onClick = {

                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.picture_as_pdf) ,
                                contentDescription = ""
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                ImageScope()
            }
        }
    }
}