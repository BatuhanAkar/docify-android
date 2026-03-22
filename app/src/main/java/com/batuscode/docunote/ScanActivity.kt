package com.batuscode.docunote

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.batuscode.docunote.ScanActivity.Companion.TAG
import com.batuscode.docunote.ScanActivity.Companion.snackbarHostState
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.utils.ScanUtil
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class ScanActivity : ComponentActivity() {
    companion object {
        const val TAG = "ScanActivity"
        val snackbarHostState = SnackbarHostState()
        lateinit var cameraExecutor : ExecutorService
        val imageCapture = ImageCapture.Builder().build()
        lateinit var context : Context

    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        context = this
        lifecycleScope.launch {
            ScanUtil.openImageOrganizerActivity.collect{
                val intent = Intent(context , ImageOrganizerActivity::class.java)
                context.startActivity(intent)
                finish()
            }
        }
        enableEdgeToEdge()
        setContent {
            val isTakedOnePhoto = ScanUtil.takedOnePhoto.collectAsState()
            val isFirstOpen = ScanUtil.firstOpen.collectAsState()
            val isFromImageOrganizerActivity = ScanUtil.FromImageOrganizerActivity.collectAsState()

            if (isFirstOpen.value && !isFromImageOrganizerActivity.value){
                ScanUtil.refresh_photo_uris()
                ScanUtil.update_taked_one_photo(false)
            }
            DocuNoteTheme(darkTheme = true) {
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState
                        )
                    },
                    bottomBar = {
                        BottomAppBar(
                            containerColor = Color.Transparent
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                IconButton(
                                    modifier = Modifier
                                        .align(Alignment.Center),
                                    onClick = {
                                        ScanUtil.takePhoto(context = context)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Camera ,
                                        contentDescription = "" ,
                                        modifier = Modifier
                                            .size(32.dp)
                                    )
                                }

                                if (isTakedOnePhoto.value){
                                    IconButton(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd),
                                        onClick = {
                                            val intent = Intent(this@ScanActivity , ImageOrganizerActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward ,
                                            contentDescription = ""
                                        )
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        CameraScreen()
                    }
                }
            }

        }
    }
}

@Composable
fun CameraScreen(){
    val context = LocalContext.current
    val cameraView = remember { PreviewView(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageUris = ScanUtil.photoUris.collectAsState()
    val isTakedOnePhoto = ScanUtil.takedOnePhoto.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize() ,
        verticalArrangement = Arrangement.Top ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AndroidView(
            factory = {cameraView} ,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        if (isTakedOnePhoto.value){
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
                    )
                }
            }
        }

    }
    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider : ProcessCameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = cameraView.surfaceProvider
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    ScanActivity.imageCapture
                )
            } catch (e : Exception) {
                Log.e(TAG , "Use case binding failed :: " , e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

}



@Preview(showBackground = true)
@Composable
fun GreetingPreview3() {
    DocuNoteTheme(darkTheme = true) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = Color.Transparent
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically ,
                        horizontalArrangement = Arrangement.Center ,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {

                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera ,
                                contentDescription = "" ,
                                modifier = Modifier
                                    .size(32.dp)
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                CameraScreen()
            }
        }
    }

}