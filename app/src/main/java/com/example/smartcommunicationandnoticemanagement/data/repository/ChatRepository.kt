package com.example.smartcommunicationandnoticemanagement.data.repository

import com.example.smartcommunicationandnoticemanagement.data.model.Message
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val database: FirebaseDatabase
) {
    fun getMessages(semester: Int): Flow<List<Message>> = callbackFlow {
        val ref = database.getReference("chats/semester_$semester")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                trySend(list.sortedBy { it.timestamp })
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun sendMessage(semester: Int, message: Message): Result<Unit> {
        return try {
            val ref = database.getReference("chats/semester_$semester").push()
            val msg = message.copy(messageId = ref.key ?: "")
            ref.setValue(msg).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
