package com.example.authentif.data

import com.example.authentif.data.models.RecruiterCandidateItem

interface RecruiterCandidatesRepository {
    fun getCurrentUserId(): String?
    suspend fun getApplicants(recruiterId: String): Result<List<RecruiterCandidateItem>>
    suspend fun updateApplicationStatus(appId: String, newStatus: String): Result<Unit>
}