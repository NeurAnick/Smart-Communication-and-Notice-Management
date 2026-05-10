package com.example.smartcommunicationandnoticemanagement.data.repository

import com.example.smartcommunicationandnoticemanagement.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

enum class UserRole {
    STUDENT, TEACHER, CR
}

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun register(
        email: String,
        pass: String,
        fullName: String,
        department: String,
        semester: Int,
        studentId: String
    ): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Registration failed"))

            val userMap = mapOf(
                "uid" to firebaseUser.uid,
                "name" to fullName,
                "email" to email,
                "studentId" to studentId,
                "department" to department,
                "semester" to semester,
                "role" to "user",
                "adminType" to "",
                "profilePhotoUrl" to "",
                "fcmToken" to "",
                "isActive" to true,
                "createdAt" to System.currentTimeMillis(),
                "phone" to "",
                "bio" to ""
            )

            firestore.collection("users").document(firebaseUser.uid).set(userMap).await()
            
            val savedUser = User(
                uid = firebaseUser.uid,
                name = fullName,
                email = email,
                role = "user",
                adminType = "",
                department = department,
                semester = semester,
                studentId = studentId,
                isActive = true,
                createdAt = System.currentTimeMillis()
            )
            
            // Do NOT call auth.signOut() here. User should remain logged in.
            Result.success(savedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, pass: String): Result<UserRole> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("Login failed"))

            val doc = firestore.collection("users").document(uid).get().await()
            if (!doc.exists()) return Result.failure(Exception("User profile not found"))

            val role = doc.getString("role") ?: "user"
            val adminType = doc.getString("adminType") ?: ""

            val userRole = when {
                role == "admin" && adminType == "teacher" -> UserRole.TEACHER
                role == "admin" && adminType == "cr" -> UserRole.CR
                else -> UserRole.STUDENT
            }

            Result.success(userRole)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        try {
            FirebaseMessaging.getInstance().deleteToken().await()
        } catch (e: Exception) {
            // Ignore if token delete fails
        }
        auth.signOut()
    }
}
