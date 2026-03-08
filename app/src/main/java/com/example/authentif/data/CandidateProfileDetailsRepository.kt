package com.example.authentif.data

import com.example.authentif.data.models.CandidateProfile

interface CandidateProfileDetailsRepository {
    suspend fun getCandidateProfile(candidateId: String): Result<CandidateProfile>
}