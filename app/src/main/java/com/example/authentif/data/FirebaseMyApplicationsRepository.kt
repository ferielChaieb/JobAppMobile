package com.example.authentif.data

import com.example.authentif.data.models.ApplicationItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseMyApplicationsRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : MyApplicationsRepository {

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override suspend fun getMyApplications(candidateId: String): Result<List<ApplicationItem>> {
        return try {
            val snap = db.collection("applications")
                .whereEqualTo("candidateId", candidateId)
                .get()
                .await()

            val list = snap.documents.map { doc ->
                val createdAt = doc.getLong("createdAt") ?: 0L

                ApplicationItem(
                    id = doc.id,
                    jobTitle = doc.getString("jobTitle") ?: "—",
                    company = doc.getString("companyName") ?: "Company",
                    appliedDate = formatDate(createdAt),
                    status = (doc.getString("status") ?: "pending").lowercase()
                )
            }.sortedByDescending { it.appliedDate }

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatDate(ms: Long): String {
        if (ms <= 0L) return "—"
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(ms))
    }
}