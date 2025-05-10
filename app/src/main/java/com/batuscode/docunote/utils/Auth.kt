package com.batuscode.docunote.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.viewModelScope
import com.batuscode.docunote.AiActivity
import com.batuscode.docunote.R
import com.batuscode.docunote.SignupActivity
import com.batuscode.docunote.model.InAppMSG
import com.batuscode.docunote.model.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.internal.wait

object Auth {
    const val TAG = "AuthObject"
    lateinit var auth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var registiration : ListenerRegistration

    private val _user = mutableStateOf(User())
    val user : State<User> = _user

    fun delivery(){
        Log.d(TAG, "listening...")

        val docRef = db.collection("users").document(auth.currentUser?.uid!!)
        registiration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen failed.", error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: ${snapshot.data}")
                val data = snapshot.data
                val metadata = data?.get("metadata") as? Map<*, *>

                CoroutineScope(Dispatchers.IO).launch {
                    val result = auth.currentUser?.getIdToken(true)?.await()
                    val validate = FunctionsUtil.vnp(result?.token!!)
                    updateClaim(validate)
                }
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }

    fun detachDelivery(){
        registiration.remove()
    }

    suspend fun checkClaims() = withContext(Dispatchers.IO){
        val result = Auth.auth.currentUser?.getIdToken(true)?.await()
        val validate = FunctionsUtil.vnp(result?.token!!)
        Log.d(TAG , "validate value is ::: $validate")
        updateClaim(validate)
        Log.d(TAG , "user validate value is ::: ${Auth.user.value.pro}")

    }
    fun updateClaim(newValue : Boolean){
        _user.value = _user.value.copy(pro = newValue)
    }

    private val _inappMessage = mutableStateOf(InAppMSG())
    val inappmessage : State<InAppMSG> = _inappMessage

    fun update_inappmessage(newValue : InAppMSG){
        _inappMessage.value = newValue
    }

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
        } catch (e : GetCredentialException){
            when (e) {
                is GetCredentialCancellationException -> {
                    Log.d(TAG, "Kullanıcı işlemi iptal etti.")
                    return@withContext null
                }
                else -> {
                    Log.e(TAG, "Beklenmedik hata: ${e.localizedMessage}")
                    return@withContext null
                }
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

                        val intent = Intent(context , AiActivity::class.java)
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
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

            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(context, SignupActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                ( context as? Activity)?.finish()
                Log.e(TAG, "clean credential state")
            }, 200)

        }   catch (e: ClearCredentialException) {
            Log.e(TAG, "Couldn't clear user credentials: ${e.localizedMessage}")
        }

    }

}