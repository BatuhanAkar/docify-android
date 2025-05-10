package com.batuscode.docunote.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import com.batuscode.docunote.ScanActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

object ScanUtil {
    private const val TAG = "ScanUtil"
    private const val FILE_NAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    private val _firstOpen = MutableStateFlow(false)
    val firstOpen : StateFlow<Boolean> = _firstOpen.asStateFlow()

    fun update_firstOpen(newValue: Boolean){
        _firstOpen.value = newValue
    }

    private val _takedOnePhoto = MutableStateFlow<Boolean>(false)
    val takedOnePhoto : StateFlow<Boolean> = _takedOnePhoto.asStateFlow()

    fun update_taked_one_photo(newValue : Boolean){
        _takedOnePhoto.value = newValue
    }

    private val _openImageOrganizerActivity = MutableSharedFlow<Unit>()
    val openImageOrganizerActivity = _openImageOrganizerActivity.asSharedFlow()

    fun handleStartImageOrganizerActivity(){
        CoroutineScope(Dispatchers.IO).launch{
            update_taked_one_photo(true)
            _openImageOrganizerActivity.emit(Unit)
        }
    }

    private val _photoUris = MutableStateFlow<List<Uri>>(emptyList())
    val photoUris : StateFlow<List<Uri>> = _photoUris.asStateFlow()

    fun refresh_photo_uris(){
        _photoUris.value = emptyList()
    }
    fun add_photo_uri(uri: Uri){
        _photoUris.value = _photoUris.value + uri
    }

    fun takePhoto(context: Context){
        val mImageCapture = ScanActivity.imageCapture ?: return

        val name = SimpleDateFormat(FILE_NAME_FORMAT , Locale.getDefault()).format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME , name)
            put(MediaStore.MediaColumns.MIME_TYPE , "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                put(MediaStore.Images.Media.RELATIVE_PATH , "Pictures/CameraX-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver ,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI ,
                contentValues
            )
            .build()

        mImageCapture.takePicture(
            outputOptions ,
            ContextCompat.getMainExecutor(context) ,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val uri = outputFileResults.savedUri
                    Log.d(TAG , "saved photo uri ::: $uri")
                    add_photo_uri(uri = uri!!)
                    if (!takedOnePhoto.value){
                        handleStartImageOrganizerActivity()
                    }
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

            }
        )
    }
}