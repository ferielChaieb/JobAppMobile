package com.example.authentif.data

import com.example.authentif.data.models.CandidateProfile

interface CandidateProfileRepository {
    fun getCurrentUserId(): String?
    fun getCurrentUserEmail(): String?
    fun logout()

    suspend fun loadProfile(uid: String): Result<CandidateProfile>
    suspend fun saveProfile(uid: String, profile: CandidateProfile): Result<Unit>
}