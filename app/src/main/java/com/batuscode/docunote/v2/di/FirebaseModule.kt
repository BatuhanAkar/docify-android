package com.batuscode.docunote.v2.di

import com.batuscode.docunote.v2.data.repository.AIServiceImpl
import com.batuscode.docunote.v2.data.repository.AuthRepositoryImpl
import com.batuscode.docunote.v2.data.repository.StorageServiceImpl
import com.batuscode.docunote.v2.domain.repository.AIService
import com.batuscode.docunote.v2.domain.repository.AuthRepository
import com.batuscode.docunote.v2.domain.repository.StorageService
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = Firebase.storage

    @Provides @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig = Firebase.remoteConfig

    @Provides @Singleton
    fun provideFirebaseFunctions(): FirebaseFunctions = Firebase.functions("europe-west1") // Bölgeye dikkat!

    @Provides @Singleton
    fun provideFirebaseAnalytics(): FirebaseAnalytics = Firebase.analytics
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindAIService(impl: AIServiceImpl): AIService

    @Binds
    @Singleton
    abstract fun bindStorageService(impl: StorageServiceImpl): StorageService

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}