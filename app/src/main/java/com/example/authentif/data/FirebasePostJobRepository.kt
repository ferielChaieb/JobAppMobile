package com.example.authentif.data

import com.example.authentif.data.models.JobPublic
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebasePostJobRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : PostJobRepository {

    override suspend fun getRecruiterCompanyName(recruiterId: String): Result<String> {
        return try {
            val recDoc = db.collection("recruiters").document(recruiterId).get().await()

            val companyName = recDoc.getString("companyName")
                ?: recDoc.getString("name")
                ?: "Recruiter"

            Result.success(companyName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createJob(
        recruiterId: String,
        companyName: String,
        job: JobPublic,
        experience: String,
        skills: String
    ): Result<Unit> {
        return try {
            val data = hashMapOf(
                "recruiterId" to recruiterId,
                "recruiterCompanyName" to companyName,
                "title" to job.title,
                "type" to job.type.ifBlank { "—" },
                "location" to job.location.ifBlank { "—" },
                "minSalary" to job.minSalary.ifBlank { "—" },
                "maxSalary" to job.maxSalary.ifBlank { "—" },
                "experience" to experience.ifBlank { "—" },
                "description" to job.description,
                "skills" to skills.ifBlank { "—" },
                "status" to job.status,
                "createdAt" to job.createdAt
            )

            db.collection("jobs").add(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}