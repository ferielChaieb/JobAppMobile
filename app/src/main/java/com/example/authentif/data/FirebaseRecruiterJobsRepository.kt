package com.example.authentif.data

import com.example.authentif.data.models.JobR
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRecruiterJobsRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : RecruiterJobsRepository {

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override suspend fun getRecruiterJobsByStatus(
        recruiterId: String,
        status: String
    ): Result<List<JobR>> {
        return try {
            val snap = db.collection("jobs")
                .whereEqualTo("recruiterId", recruiterId)
                .whereEqualTo("status", status)
                .get()
                .await()

            val list = snap.documents.map { doc ->
                JobR(
                    id = doc.id,
                    recruiterId = doc.getString("recruiterId").orEmpty(),
                    title = doc.getString("title").orEmpty(),
                    type = doc.getString("type").orEmpty(),
                    location = doc.getString("location").orEmpty(),
                    description = doc.getString("description").orEmpty(),
                    status = doc.getString("status").orEmpty().ifBlank { status },
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }.sortedByDescending { it.createdAt }

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}