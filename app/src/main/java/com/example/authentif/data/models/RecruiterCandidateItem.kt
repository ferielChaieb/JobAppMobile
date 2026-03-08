package com.example.authentif.data.models

data class RecruiterCandidateItem(
    val id: String = "",
    val candidateId: String = "",
    val candidateName: String = "",
    val jobTitle: String = "",
    val status: String = "pending",
    val appliedAt: Long = 0L
)