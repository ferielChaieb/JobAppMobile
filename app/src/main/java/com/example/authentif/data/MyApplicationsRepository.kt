package com.example.authentif.data

import com.example.authentif.data.models.ApplicationItem

interface MyApplicationsRepository {
    fun getCurrentUserId(): String?
    suspend fun getMyApplications(candidateId: String): Result<List<ApplicationItem>>
}