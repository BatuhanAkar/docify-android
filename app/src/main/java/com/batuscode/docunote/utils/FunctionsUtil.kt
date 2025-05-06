package com.batuscode.docunote.utils

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FunctionsUtil {

    const val TAG = "FunctionsUtil"
    lateinit var functions : FirebaseFunctions
    suspend fun sendMSGtoken(token : String) = withContext(Dispatchers.IO) {
        val data = hashMapOf("token" to token)

        functions
            .getHttpsCallable("communication")
            .call(data)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful){
                    Log.d(TAG , "saved msg token")
                    return@addOnCompleteListener
                }
            }

    }

    suspend fun validateAuth() : Boolean = withContext(Dispatchers.IO){

        try {

            val result = functions
                .getHttpsCallable("validateAuth")
                .call()
                .await()

            val resultData = result.data as Map<*, *>
            val success = resultData["success"] as? Boolean ?: false
            Log.d(TAG , "validate ::: ${success}")
            return@withContext success

        } catch (e : FirebaseFunctionsException){
            val code = e.code
            val detail = e.details

            when(code){
                FirebaseFunctionsException.Code.PERMISSION_DENIED -> {
                    Log.e(TAG , "Permission denied")
                    return@withContext false
                }
                FirebaseFunctionsException.Code.UNAUTHENTICATED -> {
                    return@withContext false
                }
                else -> {
                    return@withContext false
                }
            }
        } catch (e : Exception) {
            Log.e(TAG , "Error ::: ${e.message}")
            return@withContext false
        }
    }

    suspend fun vnp(tkn : String) : Boolean = withContext(Dispatchers.IO){
        val data = hashMapOf("tkn" to tkn)

        try {

            val result = functions
                .getHttpsCallable("vnp")
                .call(data)
                .await()

            val resultData = result.data as Map<*, *>
            val success = resultData.get("success") as? Boolean ?: false
            Log.d(TAG , "validate ::: ${success}")
            return@withContext success

        } catch (e : FirebaseFunctionsException){
            val code = e.code
            val detail = e.details

            when(code){
                FirebaseFunctionsException.Code.PERMISSION_DENIED -> {
                    Log.e(TAG , "Permission denied")
                    return@withContext false
                }
                FirebaseFunctionsException.Code.UNAUTHENTICATED -> {
                    return@withContext false
                }
                else -> {
                    return@withContext false
                }
            }
        } catch (e : Exception) {
            Log.e(TAG , "Error ::: ${e.message}")
            return@withContext false
        }
    }

}