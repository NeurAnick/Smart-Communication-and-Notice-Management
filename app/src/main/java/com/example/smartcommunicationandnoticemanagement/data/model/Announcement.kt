package com.example.smartcommunicationandnoticemanagement.data.model

import com.google.firebase.Timestamp

data class Announcement(
    val announcementId: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "", // ct_schedule / class_cancel / result / routine / event
    val courseCode: String = "",
    val courseName: String = "",
    val postedBy: String = "",
    val date: Timestamp? = null,
    val venue: String = "",
    val createdAt: Timestamp? = null,
    val department: String = "",
    val section: String = "",
    val semester: Int = 0
)
