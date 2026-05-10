package com.example.smartcommunicationandnoticemanagement.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseChecker {
    suspend fun verifyConnection(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): FirebaseStatus {
        return try {
            // Test write
            val testRef = firestore.collection("_connection_test")
                .document("test")
            testRef.set(mapOf(
                "timestamp" to FieldValue.serverTimestamp(),
                "uid" to (auth.currentUser?.uid ?: "anonymous")
            )).await()
            // Test read
            testRef.get().await()
            // Clean up
            testRef.delete().await()
            FirebaseStatus.Connected
        } catch (e: Exception) {
            FirebaseStatus.Error(e.message ?: "Connection failed")
        }
    }
}

sealed class FirebaseStatus {
    object Connected : FirebaseStatus()
    data class Error(val message: String) : FirebaseStatus()
}
