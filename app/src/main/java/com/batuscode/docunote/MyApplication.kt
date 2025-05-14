package com.batuscode.docunote

import android.app.Application
import android.content.Context
import com.batuscode.docunote.integrity.IntegrityHelper
import com.batuscode.docunote.utils.AiUtil
import com.batuscode.docunote.utils.Auth
import com.batuscode.docunote.utils.FunctionsUtil
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@HiltAndroidApp
class MyApplication : Application() {
    companion object {
        lateinit var packageName : String
    }
    override fun onCreate() {
        super.onCreate()
        val context : Context = this
        Companion.packageName = packageName
        FirebaseApp.initializeApp(this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
        Auth.auth = Firebase.auth
        Auth.InitializeAuthStateListener(context)
        Auth.auth.addAuthStateListener(Auth.authStateListener)
        Auth.db = Firebase.firestore
        FunctionsUtil.functions = Firebase.functions


        CoroutineScope(Dispatchers.IO).launch {
            IntegrityHelper.prepareIntegrityTokenProvider(this@MyApplication)
        }
        CoroutineScope(Dispatchers.IO).launch {
            AiUtil.initGenerativeModel()
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        Auth.auth.removeAuthStateListener(Auth.authStateListener)
    }
}