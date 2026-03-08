package com.example.authentif.data

import com.example.authentif.data.models.CandidateProfile
import com.example.authentif.data.models.EducationItemModel
import com.example.authentif.data.models.ProInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseCandidateProfileRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CandidateProfileRepository {

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun getCurrentUserEmail(): String? = auth.currentUser?.email

    override fun logout() {
        auth.signOut()
    }

    override suspend fun loadProfile(uid: String): Result<CandidateProfile> {
        return try {
            val doc = db.collection("users").document(uid).get().await()

            if (!doc.exists()) {
                Result.success(
                    CandidateProfile(
                        name = "Candidate",
                        email = auth.currentUser?.email ?: "",
                        proInfo = ProInfo(),
                        skills = emptyList(),
                        experiences = emptyList(),
                        education = emptyList()
                    )
                )
            } else {
                val pro = doc.get("proInfo") as? Map<*, *>

                val skills = (doc.get("skills") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val exp = (doc.get("experiences") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val eduRaw = (doc.get("education") as? List<*>) ?: emptyList<Any>()

                val education = eduRaw.mapNotNull { item ->
                    val m = item as? Map<*, *> ?: return@mapNotNull null
                    val degree = (m["degree"] as? String).orEmpty()
                    val school = (m["school"] as? String).orEmpty()
                    val years = (m["years"] as? String).orEmpty()

                    if (degree.isBlank() || school.isBlank() || years.isBlank()) null
                    else EducationItemModel(degree, school, years)
                }

                Result.success(
                    CandidateProfile(
                        name = doc.getString("name") ?: "Candidate",
                        email = doc.getString("email") ?: (auth.currentUser?.email ?: ""),
                        proInfo = ProInfo(
                            position = (pro?.get("position") as? String).orEmpty(),
                            experienceYears = (pro?.get("experienceYears") as? String).orEmpty(),
                            location = (pro?.get("location") as? String).orEmpty()
                        ),
                        skills = skills,
                        experiences = exp,
                        education = education
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveProfile(uid: String, profile: CandidateProfile): Result<Unit> {
        return try {
            val data = hashMapOf(
                "name" to profile.name,
                "email" to profile.email,
                "proInfo" to hashMapOf(
                    "position" to profile.proInfo.position,
                    "experienceYears" to profile.proInfo.experienceYears,
                    "location" to profile.proInfo.location
                ),
                "skills" to profile.skills,
                "experiences" to profile.experiences,
                "education" to profile.education.map {
                    hashMapOf(
                        "degree" to it.degree,
                        "school" to it.school,
                        "years" to it.years
                    )
                }
            )

            db.collection("users").document(uid)
                .set(data, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}