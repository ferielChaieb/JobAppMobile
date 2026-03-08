package com.example.authentif.data

import com.example.authentif.data.models.JobPublic

data class DashboardStats(
    val appliedCount: Int = 0,
    val acceptedCount: Int = 0
)

interface DashboardRepository {
    suspend fun getStats(candidateId: String): Result<DashboardStats>
    suspend fun getLatestJobsActive(limit: Int = 3): Result<List<JobPublic>>
}