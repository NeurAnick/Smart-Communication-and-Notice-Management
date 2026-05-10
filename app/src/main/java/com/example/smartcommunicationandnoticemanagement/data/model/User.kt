package com.example.smartcommunicationandnoticemanagement.data.model

import androidx.annotation.Keep

@Keep
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user",
    val adminType: String = "",      // "teacher" or "cr" (only if role=="admin")
    val department: String = "",
    val semester: Int = 0,
    val studentId: String = "",
    val profilePhotoUrl: String = "",
    val fcmToken: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val phone: String = "",
    val bio: String = ""
)
