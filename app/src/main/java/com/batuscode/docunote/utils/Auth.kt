package com.batuscode.docunote.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.batuscode.docunote.R
import com.batuscode.docunote.SignupActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import okhttp3.internal.wait

object Auth {
    const val TAG = "AuthObject"
    lateinit var auth: FirebaseAuth

    suspend fun handleSignIn(credential: Credential) : String = withContext(Dispatchers.Default){
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            return@withContext googleIdTokenCredential.idToken
        } else {
            Log.w(TAG, "Credential is not of type Google ID!")
            return@withContext ""
        }
    }

    suspend fun getCredential(context: Context) : Credential? = withContext(Dispatchers.Default){


        val credentialManager = CredentialManager.create(context)

        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                // Your server's client ID, not your Android client ID.
                .setServerClientId(context.getString(R.string.default_web_client_id))
                // Only show accounts previously used to sign in.
                .setFilterByAuthorizedAccounts(true)
                .setAutoSelectEnabled(true)
                .build()
            // Create the Credential Manager request
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val response = credentialManager.getCredential(context,request)
            return@withContext response.credential
        } catch (e: NoCredentialException){
            Log.getStackTraceString(e)

            try {

                val googleIdOption = GetGoogleIdOption.Builder()
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .build()
                // Create the Credential Manager request
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val response = credentialManager.getCredential(context,request)


                return@withContext response.credential
            } catch (e: GetCredentialCancellationException){
                Log.getStackTraceString(e)
                return@withContext null
            }
        }
    }
    suspend fun firebaseAuthWithGoogle(context: Context){

        val credential = getCredential(context)

        if (credential != null){
            val idToken = handleSignIn(credential!!)

            val gCredential = GoogleAuthProvider.getCredential(idToken,null)

            auth.signInWithCredential(gCredential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        Log.d(TAG, "signInWithCredential:success")
                       // val intent = Intent(context , AiActivity::class.java)
                       // context.startActivity(intent)
                       // (context as? Activity)?.finish()
                    } else {
                        // If sign in fails, display a message to the user
                        Log.w(TAG, "signInWithCredential:failure", task.exception)

                    }
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    suspend fun signOut(context: Context){
        Firebase.auth.signOut()

        try {
            val clearRequest = ClearCredentialStateRequest()
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(clearRequest)
            val intent = Intent(context, SignupActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ( context as? Activity)?.finish()
            Log.e(TAG, "clean credential state")
        }   catch (e: ClearCredentialException) {
            Log.e(TAG, "Couldn't clear user credentials: ${e.localizedMessage}")
        }

    }
}