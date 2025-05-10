package com.batuscode.docunote.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.batuscode.docunote.R
import com.batuscode.docunote.model.Folder
import com.batuscode.docunote.model.File
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = "on_boarding_pref")
val Context.notificationPermissionDataStore : DataStore<Preferences> by preferencesDataStore(name = "noti_perm_pref")
val Context.takedMessagingToken : DataStore<Preferences> by preferencesDataStore(name = "taked_msgToken")
val Context.recentlyReads : DataStore<Preferences> by preferencesDataStore(name = "recentlyReads")
val Context.folders : DataStore<Preferences> by preferencesDataStore(name = "folders_pref")

class PrefRepository(context: Context){

    companion object{
        const val TAG = "PrefRepository"
    }

    private object PreferencesKey {
        val onBoardingKey = booleanPreferencesKey(name = "on_boarding_completed")
        val notificationPermissionKey = booleanPreferencesKey(name = "showNotiPermDia")
        val messagingKey = booleanPreferencesKey(name = "taked_msg")
        val recentlyReadsKey = stringPreferencesKey(name = "docs")
        val foldersKey = stringPreferencesKey(name = "folders")
    }

    private val dataStore = context.dataStore
    private val notificationPermissionDataStore = context.notificationPermissionDataStore
    private val messagingDataStore = context.takedMessagingToken
    private val recentlyReadDataStore = context.recentlyReads
    private val foldersDataStore = context.folders



    suspend fun newFolder(folderName : String){
        foldersDataStore.edit { preferences ->
            val currentJson = preferences[PreferencesKey.foldersKey] ?: "[]"
            val type = object : TypeToken<MutableList<Folder>>() {}.type
            val gson = Gson()
            val currentList : MutableList<Folder> = gson.fromJson(currentJson , type)

            val icon = R.drawable.folder_icon_4_01
            val folder = Folder(
                id = (currentList.size - 1) ,
                name = folderName ,
                icon = icon ,
                emptyList<File>()
            )

            currentList.add(folder)

            val updatedJson = gson.toJson(currentList)
            preferences[PreferencesKey.foldersKey] = updatedJson
        }
    }

    suspend fun addDocument_Folder(folderId : Int , docsMap : Map<String , String>){
        foldersDataStore.edit { preferences ->
            val list = docsMap.entries.map { File(it.value, it.key) }
            Log.d(TAG, "new document list ::: " + Gson().toJson(list))

            val folderList = getFoldersForAddDocument()

            val updatedFolderList = folderList.map { folder ->
                if (folder.id == folderId) {
                    val updatedDocs = folder.docs.toMutableList().apply {
                        addAll(list)
                    }
                    folder.copy(docs = updatedDocs)
                } else {
                    folder
                }
            }

            Log.d(TAG, "updated folder list: ${Gson().toJson(updatedFolderList)}")

            val updatedFolderListJson = Gson().toJson(updatedFolderList)
            preferences[PreferencesKey.foldersKey] = updatedFolderListJson
        }

    }


    suspend fun getFoldersForAddDocument() : List<Folder>{
        return foldersDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val json = preferences[PreferencesKey.foldersKey] ?: "[]"
                val type = object : TypeToken<List<Folder>>() {}.type
                val folderList = Gson().fromJson<List<Folder>>(json , type)
                Log.d(TAG , "folder list for add document :: " + Gson().toJson(folderList))
                folderList
            }
            .first()
    }

    fun getDocumentsToFolder(folderId : Int) : Flow<List<File>>{
        return foldersDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val json = preferences[PreferencesKey.foldersKey] ?: "[]"
                val folderList = Gson().fromJson<List<Folder>>(json, object : TypeToken<List<Folder>>() {}.type)
                Log.d(TAG , "folder list for document :: " + Gson().toJson(folderList))
                folderList.find { it.id == folderId }?.docs ?: emptyList()
            }

    }
    fun getFolders() : Flow<List<Folder>> {
        return foldersDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val json = preferences[PreferencesKey.foldersKey] ?: "[]"
                val folderList = Gson().fromJson<List<Folder>>(json , object : TypeToken<List<Folder>>() {}.type)
                Log.d(TAG , Gson().toJson(folderList))
                folderList
            }
    }

    suspend fun saveRecentlyReadDoc(uri: String, fileName: String){
        recentlyReadDataStore.edit { preferences ->
            val currentJson = preferences[PreferencesKey.recentlyReadsKey] ?: "[]"
            val type = object : TypeToken<MutableList<File>>() {}.type
            val gson = Gson()
            val currentList : MutableList<File> = gson.fromJson(currentJson,type) ?: mutableListOf()

            val isDocumentAlreadyExists = currentList.any { it.uri == uri }
            if (!isDocumentAlreadyExists) {
                val newDocument = File(uri, fileName)
                currentList.add(newDocument)

                val updatedJson = Gson().toJson(currentList)
                preferences[PreferencesKey.recentlyReadsKey] = updatedJson
            }
        }
    }

    fun getRecentlyReadDocs() : Flow<List<File>>{
        return recentlyReadDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
            val json = preferences[PreferencesKey.recentlyReadsKey] ?: "[]"
            Gson().fromJson(json,object : TypeToken<List<File>>() {}.type)
        }
    }

    suspend fun saveTakedMessageState(taked : Boolean){
        messagingDataStore.edit { preferences ->
            preferences[PreferencesKey.messagingKey] = taked
        }
    }

    fun readTakedMSGToken() : Flow<Boolean> {
        return messagingDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val takedMSGState = preferences[PreferencesKey.messagingKey] ?: false
                takedMSGState
            }
    }

    suspend fun saveOnBoardingState(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKey.onBoardingKey] = completed
        }
    }


    fun readOnBoardingState(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val onBoardingState = preferences[PreferencesKey.onBoardingKey] ?: false
                onBoardingState
            }
    }

    suspend fun saveNotificationPermissionState(isGranted : Boolean){
        notificationPermissionDataStore.edit { preferences ->
            preferences[PreferencesKey.notificationPermissionKey] = isGranted
        }
    }

    fun readNotificationState(): Flow<Boolean> {
        Log.d("isNeedAskNotificationPermission" , "reading")

        return notificationPermissionDataStore.data
            .catch { exception ->
                if (exception is IOException){
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val notificationPermissionState = preferences[PreferencesKey.notificationPermissionKey] ?: true
                Log.d("isNeedAskNotificationPermission" , "read in " + notificationPermissionState.toString())

                notificationPermissionState
            }
    }
}