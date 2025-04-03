package com.batuscode.docunote.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.batuscode.docunote.MainActivity
import com.batuscode.docunote.R
import com.batuscode.docunote.model.Folder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class File (
        val uri: String ,
        val name: String
        )

class FileManager(val context: Context) {

    fun saveFoldersStat(context: Context,boolean: Boolean){
        val sharedPref = context.getSharedPreferences("folders", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        editor.putBoolean("folders" , boolean)
        editor.apply()
    }

    fun getFoldersStat(context: Context): Boolean{
        val sharedPref = context.getSharedPreferences("folders" , Context.MODE_PRIVATE)
        val value = sharedPref.getBoolean("folders" , false)

        return value
    }

    fun saveRecentlyStat(context: Context,boolean: Boolean){
        val sharedPref = context.getSharedPreferences("recently", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        editor.putBoolean("recently" , boolean)
        editor.apply()
    }

    fun getRecentlyStat(context: Context): Boolean{
        val sharedPref = context.getSharedPreferences("recently" , Context.MODE_PRIVATE)
        val value = sharedPref.getBoolean("recently" , false)

        return value
    }

    fun saveModelPath(context: Context , modelPath: String){
        val sharedPref = context.getSharedPreferences("model_path" , Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        editor.putString("path" , modelPath)
        editor.apply()
    }

    fun getModelPath(context: Context): String?{
        val sharedPref = context.getSharedPreferences("model_path" , Context.MODE_PRIVATE)
        return sharedPref.getString("path" , null)
    }


    fun saveDraftUri(context: Context,uri: Uri){
        val sharedPref = context.getSharedPreferences("draft_file" , Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        editor.putString("duri",uri.toString())
        editor.apply()
    }
    fun getDraftUri(context: Context): String{
        val sharedPref = context.getSharedPreferences("draft_file" , Context.MODE_PRIVATE)

        val uriString = sharedPref.getString("duri" , "")
        return uriString!!
    }
    fun saveDocumentList(context: Context, documentList: List<File>) {
        val sharedPref = context.getSharedPreferences("recent_documents", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // List'i JSON formatına dönüştür
        val gson = Gson()
        val json = gson.toJson(documentList)

        // JSON'u SharedPreferences'a kaydet
        editor.putString("document_list", json)
        editor.apply()
    }

    fun getDocumentList(context: Context): List<File> {
        val sharedPref = context.getSharedPreferences("recent_documents", Context.MODE_PRIVATE)

        // JSON string'ini al
        val json = sharedPref.getString("document_list", null)

        // JSON'u DocumentModel listesine dönüştür
        val gson = Gson()
        val type = object : TypeToken<List<File>>() {}.type

        return if (json != null) {
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }
    fun addDocument(context: Context, uri: String, fileName: String) {
        // Mevcut listeyi al
        val documentList = getDocumentList(context).toMutableList()

        val isDocumentAlreadyExists = documentList.any { it.uri == uri }

        // Eğer URI zaten listede varsa, ekleme
        if (!isDocumentAlreadyExists) {
            val newDocument = File(uri, fileName)
            documentList.add(newDocument)

            // Güncellenmiş listeyi kaydet
            saveDocumentList(context, documentList)
        }
    }

    fun getFoldersForDirectory(): List<Folder>{
        val folders = mutableListOf<Folder>()

       /* val folder1 = Folder( 0 ,"Downloads" , R.drawable.folder_icon_4_01)
        folders.add(folder1)*/

        val docFolder = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

        if (docFolder!!.exists() && docFolder.isDirectory) {
            val files = docFolder.listFiles()
            files?.forEach { file ->
                if (file.isDirectory){
                    Log.d("FolderQuery", "Dizin Adı: ${file.name}")

                    val folder = Folder(1 , name = file.name , R.drawable.folder_icon_4_01)

                    folders.add(folder)
                } else {
                    Log.d("FolderQuery", "Dosya Adı: ${file.name}")

                }
            }
            return folders
        } else {
            Log.d("FolderQuery", "Dizin bulunamadı: ${docFolder.path}")
        }
        return emptyList()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getFilesForDirectory(directoryName: String): List<File>{


        val recentFiles = mutableListOf<File>()

        val docFolder = context.getExternalFilesDir("${Environment.DIRECTORY_DOCUMENTS}/${directoryName}")


        Log.d("FolderQuery", "index name in fun  ${directoryName}")
        Log.d("FolderQuery", "index name in fun  ${docFolder!!.toUri()}")

        if (docFolder.exists() ) {

            val files = docFolder.listFiles()


            Log.d("FolderQuery", "index name in files count   ${files?.size}")
            files?.forEach { file ->

                if (file.isDirectory){
                    Log.d("FolderQuery", "Dizin Adı: ${file.name}")

                } else {
                    Log.d("FolderQuery", "Dosya Adı: ${file.name}")

                    val dotIndex = file.name.lastIndexOf('.')
                    val fileNameWithoutExtension = if (dotIndex != -1) file.name.substring(0, dotIndex) else file.name

                    val file = File(file.toUri().toString() , fileNameWithoutExtension)
                    recentFiles.add(file)
                }
            }
            return recentFiles
        } else {
            Log.d("FolderQuery", "Dizin bulunamadı: ${docFolder.path}")
        }

        return recentFiles
    }
}