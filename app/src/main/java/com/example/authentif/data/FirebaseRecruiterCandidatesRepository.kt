package com.example.authentif.data

import com.example.authentif.data.models.RecruiterCandidateItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRecruiterCandidatesRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : RecruiterCandidatesRepository {

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override suspend fun getApplicants(recruiterId: String): Result<List<RecruiterCandidateItem>> {
        return try {
            val snap = db.collection("applications")
                .whereEqualTo("recruiterId", recruiterId)
                .get()
                .await()

            val list = snap.documents.map { doc ->
                RecruiterCandidateItem(
                    id = doc.id,
                    candidateId = doc.getString("candidateId").orEmpty(),
                    candidateName = doc.getString("candidateName") ?: "Candidate",
                    jobTitle = doc.getString("jobTitle") ?: "—",
                    status = doc.getString("status") ?: "pending",
                    appliedAt = doc.getLong("createdAt") ?: 0L
                )
            }.sortedByDescending { it.appliedAt }

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateApplicationStatus(appId: String, newStatus: String): Result<Unit> {
        return try {
            db.collection("applications").document(appId)
                .update("status", newStatus)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}