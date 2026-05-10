package com.example.smartcommunicationandnoticemanagement.data.repository

import com.example.smartcommunicationandnoticemanagement.data.model.Notice
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticeRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getNoticesRealtime(): Flow<List<Notice>> = callbackFlow {
        val listener = firestore.collection("notices")
            .orderBy("postedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val notices = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Notice::class.java)?.copy(noticeId = doc.id)
                } ?: emptyList()
                trySend(notices)
            }
        awaitClose { listener.remove() }
    }

    fun getNoticesByType(type: String): Flow<List<Notice>> = callbackFlow {
        val listener = firestore.collection("notices")
            .whereEqualTo("type", type)
            .orderBy("postedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val notices = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Notice::class.java)?.copy(noticeId = doc.id)
                } ?: emptyList()
                trySend(notices)
            }
        awaitClose { listener.remove() }
    }

    suspend fun postNotice(notice: Notice): Result<Unit> {
        return try {
            val ref = firestore.collection("notices").document()
            val newNotice = notice.copy(noticeId = ref.id)
            ref.set(newNotice).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNotice(notice: Notice): Result<Unit> {
        return try {
            firestore.collection("notices").document(notice.noticeId)
                .set(notice) // use set() NOT update() to avoid partial write issues
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNotice(noticeId: String): Result<Unit> {
        return try {
            firestore.collection("notices").document(noticeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markNoticeSeen(noticeId: String, userId: String): Result<Unit> {
        return try {
            firestore.collection("notices")
                .document(noticeId)
                .update("seenBy.$userId", System.currentTimeMillis())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSeenByList(noticeId: String): Flow<Map<String, Long>> = callbackFlow {
        val listener = firestore.collection("notices")
            .document(noticeId)
            .addSnapshotListener { snapshot, _ ->
                @Suppress("UNCHECKED_CAST")
                val seenBy = snapshot?.get("seenBy") as? Map<String, Long> ?: emptyMap()
                trySend(seenBy)
            }
        awaitClose { listener.remove() }
    }

    suspend fun toggleSaveNotice(userId: String, noticeId: String, save: Boolean): Result<Unit> {
        return try {
            val ref = firestore.collection("saved").document(userId).collection("notices").document(noticeId)
            if (save) {
                ref.set(mapOf("saved" to true)).await()
            } else {
                ref.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSavedNoticeIds(userId: String): Flow<List<String>> = callbackFlow {
        val listener = firestore.collection("saved").document(userId).collection("notices")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val ids = snapshot?.documents?.map { it.id } ?: emptyList()
                trySend(ids)
            }
        awaitClose { listener.remove() }
    }
}
