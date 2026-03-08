package com.example.authentif.data

import com.example.authentif.data.models.JobPublic

interface PostJobRepository {
    suspend fun createJob(
        recruiterId: String,
        companyName: String,
        job: JobPublic,
        experience: String,
        skills: String
    ): Result<Unit>

    suspend fun getRecruiterCompanyName(recruiterId: String): Result<String>
}