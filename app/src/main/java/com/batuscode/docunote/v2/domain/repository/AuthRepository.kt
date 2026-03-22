package com.batuscode.docunote.v2.domain.repository

interface AuthRepository {
    suspend fun signInAnonymously(): Result<Unit>
}