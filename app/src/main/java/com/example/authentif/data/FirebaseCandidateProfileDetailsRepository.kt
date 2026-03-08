package com.example.authentif.data

import com.example.authentif.data.models.CandidateProfile
import com.example.authentif.data.models.EducationItemModel
import com.example.authentif.data.models.ProInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseCandidateProfileDetailsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CandidateProfileDetailsRepository {

    override suspend fun getCandidateProfile(candidateId: String): Result<CandidateProfile> {
        return try {
            val doc = db.collection("users").document(candidateId).get().await()

            if (!doc.exists()) {
                Result.failure(Exception("Profil introuvable"))
            } else {
                val pro = doc.get("proInfo") as? Map<*, *>
                val skills = (doc.get("skills") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val experiences = (doc.get("experiences") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val eduRaw = (doc.get("education") as? List<*>) ?: emptyList<Any>()

                val education = eduRaw.mapNotNull { item ->
                    val m = item as? Map<*, *> ?: return@mapNotNull null
                    val degree = (m["degree"] as? String).orEmpty()
                    val school = (m["school"] as? String).orEmpty()
                    val years = (m["years"] as? String).orEmpty()

                    if (degree.isBlank() && school.isBlank() && years.isBlank()) null
                    else EducationItemModel(
                        degree = degree,
                        school = school,
                        years = years
                    )
                }

                Result.success(
                    CandidateProfile(
                        name = doc.getString("name") ?: "Candidate",
                        email = doc.getString("email") ?: "—",
                        proInfo = ProInfo(
                            position = (pro?.get("position") as? String).orEmpty(),
                            experienceYears = (pro?.get("experienceYears") as? String).orEmpty(),
                            location = (pro?.get("location") as? String).orEmpty()
                        ),
                        skills = skills,
                        experiences = experiences,
                        education = education
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}