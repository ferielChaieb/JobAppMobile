package com.example.authentif.data

import com.example.authentif.data.models.JobR

interface RecruiterJobsRepository {
    fun getCurrentUserId(): String?
    suspend fun getRecruiterJobsByStatus(recruiterId: String, status: String): Result<List<JobR>>
}