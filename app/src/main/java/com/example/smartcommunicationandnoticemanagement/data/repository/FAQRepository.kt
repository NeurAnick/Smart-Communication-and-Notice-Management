package com.example.smartcommunicationandnoticemanagement.data.repository

import com.example.smartcommunicationandnoticemanagement.data.model.FAQ
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FAQRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getFAQs(): Flow<List<FAQ>> = callbackFlow {
        val listener = firestore.collection("faqs")
            .whereEqualTo("isActive", true)
            .orderBy("category")
            .orderBy("order")
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val faqs = snap?.documents
                    ?.mapNotNull { it.toObject(FAQ::class.java) }
                    ?: emptyList()
                trySend(faqs)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addFAQ(faq: FAQ): Result<Unit> {
        return try {
            val id = if (faq.faqId.isEmpty()) firestore.collection("faqs").document().id else faq.faqId
            firestore.collection("faqs").document(id)
                .set(faq.copy(faqId = id))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFAQ(faq: FAQ): Result<Unit> {
        return try {
            firestore.collection("faqs").document(faq.faqId)
                .set(faq).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFAQ(faqId: String): Result<Unit> {
        return try {
            firestore.collection("faqs").document(faqId)
                .update("isActive", false).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
