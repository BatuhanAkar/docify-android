package com.batuscode.docunote.v2.data.repository

import com.batuscode.docunote.v2.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val functions: FirebaseFunctions
) : AuthRepository {

    override suspend fun signInAnonymously(): Result<Unit> = try {
        auth.signInAnonymously().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // Backend-Driven: Google ile bağlandıktan sonra Claims yükseltme işlemini tetikler
    suspend fun upgradeAccountOnBackend(): Result<String> = try {
        val result = functions.getHttpsCallable("upgradeUserTier").call().await()
        val data = result.data as Map<*, *>
        Result.success(data["message"] as String)
    } catch (e: Exception) { Result.failure(e) }
}