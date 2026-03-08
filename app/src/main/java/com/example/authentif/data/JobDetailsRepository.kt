package com.example.authentif.data

import com.example.authentif.data.models.JobDetailsUi

interface JobDetailsRepository {
    suspend fun getJobDetails(jobId: String): Result<JobDetailsUi>

    suspend fun applyToJob(
        jobId: String,
        recruiterId: String,
        companyName: String,
        jobTitle: String,
        candidateId: String
    ): Result<Unit>
}