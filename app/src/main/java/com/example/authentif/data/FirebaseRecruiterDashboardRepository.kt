package com.example.authentif.data

import com.example.authentif.data.models.RecentApplicationItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRecruiterDashboardRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : RecruiterDashboardRepository {

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun isUserLoggedIn(): Boolean = auth.currentUser != null

    override fun logout() {
        auth.signOut()
    }

    override suspend fun getRecruiterName(recruiterId: String): Result<String> {
        return try {
            val doc = db.collection("recruiters").document(recruiterId).get().await()
            val name = doc.getString("name")
                ?: auth.currentUser?.displayName
                ?: "Recruiter"
            Result.success(name)
        } catch (e: Exception) {
            Result.success(auth.currentUser?.displayName ?: "Recruiter")
        }
    }

    override suspend fun getDashboardData(recruiterId: String): Result<RecruiterDashboardData> {
        return try {
            val jobsSnap = db.collection("jobs")
                .whereEqualTo("recruiterId", recruiterId)
                .get()
                .await()

            val activeJobs = jobsSnap.documents.count {
                (it.getString("status") ?: "").lowercase() == "active"
            }

            val appsSnap = db.collection("applications")
                .whereEqualTo("recruiterId", recruiterId)
                .get()
                .await()

            val docs = appsSnap.documents
            val applicantsTotal = docs.size
            val hired = docs.count {
                (it.getString("status") ?: "").lowercase() == "accepted"
            }

            val recent = docs.map { doc ->
                RecentApplicationItem(
                    candidateId = doc.getString("candidateId").orEmpty(),
                    candidateName = doc.getString("candidateName") ?: "Candidate",
                    jobTitle = doc.getString("jobTitle") ?: "—",
                    status = doc.getString("status") ?: "pending",
                    appliedAt = doc.getLong("createdAt") ?: 0L
                )
            }.sortedByDescending { it.appliedAt }
                .take(3)

            Result.success(
                RecruiterDashboardData(
                    activeJobsCount = activeJobs,
                    applicantsCount = applicantsTotal,
                    hiredCount = hired,
                    recentApplications = recent
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}