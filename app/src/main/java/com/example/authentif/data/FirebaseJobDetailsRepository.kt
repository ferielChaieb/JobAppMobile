package com.example.authentif.data

import com.example.authentif.data.models.JobDetailsUi
import com.example.authentif.data.models.JobPublic
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseJobDetailsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : JobDetailsRepository {

    override suspend fun getJobDetails(jobId: String): Result<JobDetailsUi> {
        return try {
            val doc = db.collection("jobs").document(jobId).get().await()
            if (!doc.exists()) return Result.failure(Exception("Job introuvable"))

            val recruiterId = doc.getString("recruiterId").orEmpty()
            val companyName = doc.getString("companyName")
                ?: doc.getString("company")
                ?: "Company"

            val job = JobPublic(
                id = doc.id,
                recruiterId = recruiterId,
                title = doc.getString("title").orEmpty(),
                type = doc.getString("type").orEmpty(),
                location = doc.getString("location").orEmpty(),
                description = doc.getString("description").orEmpty(),
                minSalary = doc.getString("minSalary") ?: "—",
                maxSalary = doc.getString("maxSalary") ?: "—",
                createdAt = (doc.getLong("createdAt") ?: 0L),
                status = doc.getString("status") ?: "active"
            )

            Result.success(JobDetailsUi(job = job, companyName = companyName))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun applyToJob(
        jobId: String,
        recruiterId: String,
        companyName: String,
        jobTitle: String,
        candidateId: String
    ): Result<Unit> {
        return try {
            // 1) éviter doublon
            val exists = db.collection("applications")
                .whereEqualTo("candidateId", candidateId)
                .whereEqualTo("jobId", jobId)
                .get().await()

            if (!exists.isEmpty) {
                return Result.failure(Exception("Déjà postulé ✅"))
            }

            // 2) lire profil candidat
            val userDoc = db.collection("users").document(candidateId).get().await()
            if (!userDoc.exists()) {
                return Result.failure(Exception("Profil introuvable"))
            }

            val candidateName = userDoc.getString("name") ?: "Candidate"
            val candidateEmail = userDoc.getString("email") ?: ""

            val pro = userDoc.get("proInfo") as? Map<*, *>
            val candidatePosition = (pro?.get("position") as? String).orEmpty()
            val candidateLocation = (pro?.get("location") as? String).orEmpty()
            val candidateExperienceYears = (pro?.get("experienceYears") as? String).orEmpty()

            val data = hashMapOf(
                "candidateId" to candidateId,
                "recruiterId" to recruiterId,
                "jobId" to jobId,
                "jobTitle" to jobTitle,
                "companyName" to companyName,
                "status" to "pending",
                "createdAt" to System.currentTimeMillis(),
                "candidateName" to candidateName,
                "candidateEmail" to candidateEmail,
                "candidatePosition" to candidatePosition,
                "candidateLocation" to candidateLocation,
                "candidateExperienceYears" to candidateExperienceYears
            )

            db.collection("applications").add(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}