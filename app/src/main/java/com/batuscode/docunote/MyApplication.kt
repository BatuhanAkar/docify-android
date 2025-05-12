package com.batuscode.docunote

import android.app.Application
import com.batuscode.docunote.SignupActivity
import com.batuscode.docunote.integrity.IntegrityHelper
import com.batuscode.docunote.utils.AiUtil
import com.batuscode.docunote.utils.Auth
import com.batuscode.docunote.utils.FunctionsUtil
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
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
        Companion.packageName = packageName
        FirebaseApp.initializeApp(this)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
        Auth.auth = Firebase.auth
        Auth.db = Firebase.firestore
        FunctionsUtil.functions = Firebase.functions


        CoroutineScope(Dispatchers.IO).launch {
            IntegrityHelper.prepareIntegrityTokenProvider(this@MyApplication)
        }
        CoroutineScope(Dispatchers.IO).launch {
            AiUtil.initGenerativeModel()
        }
    }
}