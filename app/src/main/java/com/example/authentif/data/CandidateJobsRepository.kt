package com.example.authentif.data

import com.example.authentif.data.models.JobPublic

interface CandidateJobsRepository {
    suspend fun getActiveJobs(): Result<List<JobPublic>>
}