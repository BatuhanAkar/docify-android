package com.batuscode.docunote.manager

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ActivityResultLauncherManager {
    const val TAG = "ActivityResultLauncherManager"
    lateinit var activity : ComponentActivity
    lateinit var folderScopeActivity : ComponentActivity

    private lateinit var pdfPickerLauncher : ActivityResultLauncher<Intent>
    private lateinit var pdfDocPickerToFolderLauncher : ActivityResultLauncher<Intent>

    var MultiplePDFFileUris: MutableMap<Int, Uri> = mutableMapOf()
    private var _onPDFPicked : ((Uri? , String? , MutableMap<Int, Uri>?) -> Unit)? = null
    private var _onPDFDocPickedToFolder : ((MutableMap<Int, Uri>?) -> Unit)? = null

    fun init_docsToFolder(){

        pdfDocPickerToFolderLauncher = folderScopeActivity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){ result ->
            if (result.resultCode == RESULT_OK){
                val data: Intent? = result.data
                data.let {

                    if (it?.clipData != null){
                        Log.d(TAG , "clip data not null")
                        if (MultiplePDFFileUris.isNotEmpty()){
                            MultiplePDFFileUris.clear()
                        }
                        val count = it.clipData?.itemCount ?: 0
                        for (i in 0 until count) {
                            val uri = it.clipData?.getItemAt(i)?.uri
                            uri?.let {
                                MultiplePDFFileUris.put(i , it)

                                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                                activity.contentResolver.takePersistableUriPermission(it, takeFlags)

                            }
                        }
                        _onPDFDocPickedToFolder?.invoke(MultiplePDFFileUris)

                    } else {

                        if (MultiplePDFFileUris.isNotEmpty()){
                            MultiplePDFFileUris.clear()
                        }

                        val uri = it?.data
                        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                        activity.contentResolver.takePersistableUriPermission(uri!!, takeFlags)
                        MultiplePDFFileUris.put(0 , uri)

                        _onPDFDocPickedToFolder?.invoke(MultiplePDFFileUris)

                    }

                }
            }
        }
    }
    fun init(){
        pdfPickerLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){ result ->
            if (result.resultCode == RESULT_OK){
                val data: Intent? = result.data
                data.let {

                    if (it?.clipData != null){
                        Log.d(TAG , "clip data not null")
                        if (MultiplePDFFileUris.isNotEmpty()){
                            MultiplePDFFileUris.clear()
                        }
                        val count = it.clipData?.itemCount ?: 0
                        for (i in 0 until count) {
                            val uri = it.clipData?.getItemAt(i)?.uri
                            uri?.let {
                                MultiplePDFFileUris.put(i , it)

                                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                                activity.contentResolver.takePersistableUriPermission(it, takeFlags)

                            }
                        }
                        _onPDFPicked?.invoke(null,null, MultiplePDFFileUris)

                    } else {
                        val uri = it?.data
                        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                        activity.contentResolver.takePersistableUriPermission(uri!!, takeFlags)

                        CoroutineScope(Dispatchers.IO).launch {
                            val fileNameWithoutExtension = getPDFFileNameWithOutExtension(uri)
                            _onPDFPicked?.invoke(uri,fileNameWithoutExtension,null)
                        }
                    }

                }
            }
        }
    }

    fun pickPDF(allowMultiplePick : Boolean , callback : (Uri? , String? , MutableMap<Int, Uri>?) -> Unit){
        _onPDFPicked = callback
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT ).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE , allowMultiplePick)
        }
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        pdfPickerLauncher.launch(intent)
    }

    fun pickPDFToFolder(allowMultiplePick : Boolean , callback : (MutableMap<Int, Uri>?) -> Unit){
        _onPDFDocPickedToFolder = callback
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT ).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE , allowMultiplePick)
        }
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        pdfDocPickerToFolderLauncher.launch(intent)
    }

    suspend fun getPDFFileNameWithOutExtension(uri: Uri) : String = withContext(Dispatchers.IO){


        val cursor = activity.contentResolver.query(
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
                return@withContext fileNameWithoutExtension
            }
        }
        return@withContext ""
    }

    suspend fun parseDocsMapUris(urisMap : MutableMap<Int , Uri>) : Map<String , String> = withContext(Dispatchers.IO){
        Log.d(TAG , "urisMap ::: " + Gson().toJson(urisMap))
        val newMap = urisMap.map { (_ , value) ->
            getPDFFileNameWithOutExtension(value) to value.toString()
        }.toMap()
        Log.d(TAG , "newMap ::: " + Gson().toJson(newMap))
        return@withContext newMap
    }


}