package com.example.smartcommunicationandnoticemanagement.data.repository

import com.example.smartcommunicationandnoticemanagement.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveUser(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(uid: String): User? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                // Return object and ensure uid is set from document ID if missing
                doc.toObject(User::class.java)?.copy(uid = doc.id)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getUserFlow(uid: String): Flow<User?> = callbackFlow {
        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snap, _ ->
                if (snap != null && snap.exists()) {
                    trySend(snap.toObject(User::class.java)?.copy(uid = snap.id))
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val users = snapshot?.documents?.mapNotNull { it.toObject(User::class.java)?.copy(uid = it.id) } ?: emptyList()
            trySend(users)
        }
        awaitClose { listener.remove() }
    }

    fun getUsersBySemester(semester: Int): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("users")
            .whereEqualTo("semester", semester)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { it.toObject(User::class.java)?.copy(uid = it.id) } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }
}
