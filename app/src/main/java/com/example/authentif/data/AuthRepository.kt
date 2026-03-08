package com.example.authentif.data

interface AuthRepository {
    suspend fun loginAndGetRole(email: String, password: String): Result<String>

    suspend fun signupAndCreateProfile(
        name: String,
        email: String,
        password: String,
        role: String
    ): Result<String>

    fun isUserLoggedIn(): Boolean

    fun getCurrentUserId(): String?

    fun logout()
}