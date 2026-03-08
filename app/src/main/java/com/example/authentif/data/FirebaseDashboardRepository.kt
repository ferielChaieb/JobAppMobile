package com.example.authentif.data

import com.example.authentif.data.models.JobPublic
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseDashboardRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : DashboardRepository {

    override suspend fun getStats(candidateId: String): Result<DashboardStats> {
        return try {
            val appliedSnap = db.collection("applications")
                .whereEqualTo("candidateId", candidateId)
                .get().await()

            val acceptedSnap = db.collection("applications")
                .whereEqualTo("candidateId", candidateId)
                .whereEqualTo("status", "accepted")
                .get().await()

            Result.success(
                DashboardStats(
                    appliedCount = appliedSnap.size(),
                    acceptedCount = acceptedSnap.size()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLatestJobsActive(limit: Int): Result<List<JobPublic>> {
        return try {
            val snap = db.collection("jobs")
                .whereEqualTo("status", "active")
                .get().await()

            if (snap.isEmpty) return Result.success(emptyList())

            val docs = snap.documents
                .sortedByDescending { readCreatedAtMillis(it) }
                .take(limit)

            val jobs = docs.map { doc ->
                JobPublic(
                    id = doc.id,
                    recruiterId = doc.getString("recruiterId") ?: "",
                    title = doc.getString("title").orEmpty(),
                    type = doc.getString("type") ?: "—",
                    location = doc.getString("location") ?: "—",
                    description = doc.getString("description") ?: "",
                    minSalary = doc.getString("minSalary") ?: "—",
                    maxSalary = doc.getString("maxSalary") ?: "—",
                    createdAt = readCreatedAtMillis(doc),
                    status = doc.getString("status") ?: "active"
                )
            }

            Result.success(jobs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun readCreatedAtMillis(doc: DocumentSnapshot): Long {
        val v = doc.get("createdAt") ?: return 0L
        return when (v) {
            is Long -> v
            is Int -> v.toLong()
            is Double -> v.toLong()
            is Float -> v.toLong()
            is Timestamp -> v.toDate().time
            is String -> v.toLongOrNull() ?: 0L
            else -> 0L
        }
    }
}