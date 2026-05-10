package com.example.smartcommunicationandnoticemanagement.data.model

import com.google.firebase.Timestamp

data class FAQ(
    val faqId: String = "",
    val question: String = "",
    val answer: String = "",
    val category: String = "General",
    val order: Int = 0,
    val isActive: Boolean = true,
    val createdBy: String = "",
    val createdAt: Timestamp? = null
)
