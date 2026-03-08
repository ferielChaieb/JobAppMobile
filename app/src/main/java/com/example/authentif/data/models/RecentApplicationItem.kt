package com.example.authentif.data.models

data class RecentApplicationItem(
    val candidateId: String,
    val candidateName: String,
    val jobTitle: String,
    val status: String,
    val appliedAt: Long
)