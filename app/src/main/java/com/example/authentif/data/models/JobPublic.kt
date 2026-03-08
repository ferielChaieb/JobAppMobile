package com.example.authentif.data.models

data class JobPublic(
    val id: String = "",
    val recruiterId: String = "",
    val companyName: String = "Company",
    val title: String = "",
    val type: String = "",
    val location: String = "",
    val description: String = "",
    val minSalary: String = "—",
    val maxSalary: String = "—",
    val createdAt: Long = 0L,
    val status: String = "active"
)