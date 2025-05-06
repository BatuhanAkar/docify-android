package com.batuscode.docunote.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map


val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = "on_boarding_pref")
val Context.notificationPermissionDataStore : DataStore<Preferences> by preferencesDataStore(name = "noti_perm_pref")
val Context.takedMessagingToken : DataStore<Preferences> by preferencesDataStore(name = "taked_msgToken")
class PrefRepository(context: Context){

    private object PreferencesKey {
        val onBoardingKey = booleanPreferencesKey(name = "on_boarding_completed")
        val notificationPermissionKey = booleanPreferencesKey(name = "showNotiPermDia")
        val messagingKey = booleanPreferencesKey(name = "taked_msg")
    }

    private val dataStore = context.dataStore
    private val notificationPermissionDataStore = context.notificationPermissionDataStore
    private val messagingDataStore = context.takedMessagingToken

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