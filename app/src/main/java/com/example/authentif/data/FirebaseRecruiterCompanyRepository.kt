package com.example.authentif.data

import com.example.authentif.data.models.RecruiterCompanyProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseRecruiterCompanyRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : RecruiterCompanyRepository {

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun logout() {
        auth.signOut()
    }

    override suspend fun loadRecruiterProfile(uid: String): Result<RecruiterCompanyProfile> {
        return try {
            val doc = db.collection("recruiters").document(uid).get().await()

            if (!doc.exists()) {
                Result.success(RecruiterCompanyProfile())
            } else {
                Result.success(
                    RecruiterCompanyProfile(
                        industry = doc.getString("industry").orEmpty(),
                        size = doc.getString("size").orEmpty(),
                        location = doc.getString("location").orEmpty(),
                        website = doc.getString("website").orEmpty(),
                        about = doc.getString("about").orEmpty()
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveRecruiterProfile(
        uid: String,
        profile: RecruiterCompanyProfile
    ): Result<Unit> {
        return try {
            val data = hashMapOf(
                "industry" to profile.industry,
                "size" to profile.size,
                "location" to profile.location,
                "website" to profile.website,
                "about" to profile.about,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection("recruiters").document(uid)
                .set(data, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}