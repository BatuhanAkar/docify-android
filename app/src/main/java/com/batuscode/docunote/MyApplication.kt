package com.batuscode.docunote

import android.app.Application
import android.content.Context
import com.batuscode.docunote.utils.AiUtil
import com.batuscode.docunote.utils.Auth
import com.batuscode.docunote.utils.FunctionsUtil
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.functions
import com.google.firebase.functions.ktx.functions
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val context : Context = this

        FirebaseApp.initializeApp(this)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
        Auth.auth = Firebase.auth
        Auth.InitializeAuthStateListener(context)
        Auth.auth.addAuthStateListener(Auth.authStateListener)
        Auth.db = Firebase.firestore
        FunctionsUtil.functions = Firebase.functions


        CoroutineScope(Dispatchers.IO).launch {
            AiUtil.initGenerativeModel()
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        Auth.auth.removeAuthStateListener(Auth.authStateListener)
    }
}