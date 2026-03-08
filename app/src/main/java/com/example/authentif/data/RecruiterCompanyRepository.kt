package com.example.authentif.data

import com.example.authentif.data.models.RecruiterCompanyProfile

interface RecruiterCompanyRepository {
    fun getCurrentUserId(): String?
    fun logout()

    suspend fun loadRecruiterProfile(uid: String): Result<RecruiterCompanyProfile>
    suspend fun saveRecruiterProfile(uid: String, profile: RecruiterCompanyProfile): Result<Unit>
}