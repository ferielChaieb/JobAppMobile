package com.example.authentif.data.models

data class ProInfo(
    val position: String = "",
    val experienceYears: String = "",
    val location: String = ""
)

data class EducationItemModel(
    val degree: String = "",
    val school: String = "",
    val years: String = ""
)

data class CandidateProfile(
    val name: String = "Candidate",
    val email: String = "",
    val proInfo: ProInfo = ProInfo(),
    val skills: List<String> = emptyList(),
    val experiences: List<String> = emptyList(),
    val education: List<EducationItemModel> = emptyList()
)