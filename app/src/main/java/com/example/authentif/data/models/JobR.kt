package com.example.authentif.data.models

data class JobR(
    val id: String = "",
    val recruiterId: String = "",
    val title: String = "",
    val type: String = "",
    val location: String = "",
    val description: String = "",
    val status: String = "active",
    val createdAt: Long = 0L
)
