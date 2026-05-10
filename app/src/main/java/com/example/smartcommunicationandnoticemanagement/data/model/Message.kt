package com.example.smartcommunicationandnoticemanagement.data.model

import androidx.annotation.Keep

@Keep
data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)
