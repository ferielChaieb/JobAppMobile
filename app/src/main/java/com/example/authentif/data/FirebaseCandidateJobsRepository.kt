package com.example.authentif.data

import com.example.authentif.data.models.JobPublic
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseCandidateJobsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CandidateJobsRepository {

    override suspend fun getActiveJobs(): Result<List<JobPublic>> {
        return try {
            val snap = db.collection("jobs")
                .whereEqualTo("status", "active")
                .get()
                .await()

            val list = snap.documents.map { doc ->
                JobPublic(
                    id = doc.id,
                    recruiterId = doc.getString("recruiterId").orEmpty(),
                    companyName = doc.getString("companyName")
                        ?: doc.getString("company")
                        ?: doc.getString("recruiterCompanyName")
                        ?: "Company",
                    title = doc.getString("title").orEmpty(),
                    type = doc.getString("type").orEmpty(),
                    location = doc.getString("location").orEmpty(),
                    description = doc.getString("description").orEmpty(),
                    minSalary = doc.getString("minSalary") ?: "—",
                    maxSalary = doc.getString("maxSalary") ?: "—",
                    status = doc.getString("status") ?: "active",
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }.sortedByDescending { it.createdAt }

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}