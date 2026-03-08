package com.example.authentif.data.models

data class ApplicationItem(
    val id: String,
    val jobTitle: String,
    val company: String,
    val appliedDate: String,
    val status: String // pending | accepted | rejected
)