package com.example.smartcommunicationandnoticemanagement.data.repository

import com.example.smartcommunicationandnoticemanagement.data.model.Routine
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getRoutinesBySemester(semester: Int): Flow<List<Routine>> = callbackFlow {
        val listener = firestore.collection("routines")
            .whereEqualTo("semester", semester)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toObject(Routine::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addRoutine(routine: Routine): Result<Unit> {
        return try {
            val ref = firestore.collection("routines").document()
            val newRoutine = routine.copy(routineId = ref.id)
            ref.set(newRoutine).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRoutine(routine: Routine): Result<Unit> {
        return try {
            firestore.collection("routines").document(routine.routineId).set(routine).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRoutine(routineId: String): Result<Unit> {
        return try {
            firestore.collection("routines").document(routineId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
