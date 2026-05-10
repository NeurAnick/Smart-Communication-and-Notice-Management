package com.example.smartcommunicationandnoticemanagement.service

import com.example.smartcommunicationandnoticemanagement.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        saveTokenToFirestore(token)
    }

    private fun saveTokenToFirestore(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .update("fcmToken", token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val title = data["title"] ?: message.notification?.title ?: "New Notification"
        val body = data["body"] ?: message.notification?.body ?: ""
        val priorityType = data["priority"] ?: "NORMAL"
        val noticeId = data["noticeId"]

        val messageType = data["type"] ?: "notice"

        when (messageType) {
            "message" -> {
                val senderName = data["senderName"] ?: "Unknown"
                NotificationHelper.showMessageNotification(this, senderName, title, data["roomId"])
            }
            "notice" -> {
                if (priorityType == "URGENT") {
                    NotificationHelper.showUrgentNotification(this, title, body, noticeId)
                } else {
                    NotificationHelper.showNoticeNotification(this, title, body, noticeId)
                }
            }
            "approval" -> {
                NotificationHelper.showSystemNotification(this, title, body)
            }
            "login_alert" -> {
                NotificationHelper.showSystemNotification(this, title, body)
            }
        }
    }
}
