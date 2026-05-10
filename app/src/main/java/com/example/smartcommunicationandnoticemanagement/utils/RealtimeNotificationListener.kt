package com.example.smartcommunicationandnoticemanagement.utils

import android.content.Context
import android.util.Log
import com.example.smartcommunicationandnoticemanagement.data.model.Message
import com.example.smartcommunicationandnoticemanagement.data.model.Notice
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeNotificationListener @Inject constructor(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val database: FirebaseDatabase
) {
    private var lastNoticeId: String? = null
    private var lastMessageId: String? = null
    private var isFirstNoticeLoad = true
    private var isFirstMessageLoad = true

    fun startListening(userSemester: Int?) {
        listenForNotices()
        if (userSemester != null) {
            listenForMessages(userSemester)
        }
    }

    private fun listenForNotices() {
        firestore.collection("notices")
            .orderBy("postedAt", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val notice = change.document.toObject(Notice::class.java)
                        
                        // Ignore the very first load to avoid notifying for old notices
                        if (!isFirstNoticeLoad) {
                            if (notice.postedBy != FirebaseAuth.getInstance().currentUser?.uid) {
                                NotificationHelper.showNoticeNotification(
                                    context, 
                                    notice.title, 
                                    notice.body, 
                                    change.document.id
                                )
                            }
                        }
                        lastNoticeId = change.document.id
                    }
                }
                isFirstNoticeLoad = false
            }
    }

    private fun listenForMessages(semester: Int) {
        val ref = database.getReference("chats/semester_$semester")
        ref.limitToLast(1).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java) ?: return
                
                if (!isFirstMessageLoad) {
                    if (message.senderId != FirebaseAuth.getInstance().currentUser?.uid) {
                        NotificationHelper.showMessageNotification(
                            context,
                            message.senderName,
                            message.text,
                            semester.toString()
                        )
                    }
                }
                lastMessageId = snapshot.key
                isFirstMessageLoad = false
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
