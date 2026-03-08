package com.example.authentif.data

import com.example.authentif.data.models.RecentApplicationItem

data class RecruiterDashboardData(
    val recruiterName: String = "Recruiter",
    val activeJobsCount: Int = 0,
    val applicantsCount: Int = 0,
    val hiredCount: Int = 0,
    val recentApplications: List<RecentApplicationItem> = emptyList()
)

interface RecruiterDashboardRepository {
    fun getCurrentUserId(): String?
    fun isUserLoggedIn(): Boolean
    fun logout()

    suspend fun getRecruiterName(recruiterId: String): Result<String>
    suspend fun getDashboardData(recruiterId: String): Result<RecruiterDashboardData>
}