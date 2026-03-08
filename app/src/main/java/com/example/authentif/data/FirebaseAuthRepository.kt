package com.example.authentif.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override suspend fun loginAndGetRole(email: String, password: String): Result<String> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            val user = auth.currentUser ?: return Result.failure(Exception("Utilisateur introuvable"))
            val doc = db.collection("users").document(user.uid).get().await()
            if (!doc.exists()) return Result.failure(Exception("Profil utilisateur introuvable"))
            Result.success(doc.getString("role") ?: "candidate")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signupAndCreateProfile(
        name: String,
        email: String,
        password: String,
        role: String
    ): Result<String> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            val user = auth.currentUser ?: return Result.failure(Exception("Utilisateur introuvable"))
            val uid = user.uid

            val userData = mapOf(
                "name" to name,
                "email" to email,
                "role" to role,
                "createdAt" to System.currentTimeMillis()
            )

            db.collection("users").document(uid).set(userData).await()
            Result.success(role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    override fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override fun logout() {
        auth.signOut()
    }
}