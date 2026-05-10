package com.example.smartcommunicationandnoticemanagement.data.model

import androidx.annotation.Keep

@Keep
data class Notice(
    val noticeId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "",               // CT / Mid / Final / Event / Emergency / Vacation
    val targetSemester: String = "all",  // "all" or "1"–"8"
    val postedBy: String = "",
    val postedByName: String = "",
    val postedByRole: String = "",       // "teacher" or "cr"
    val imageUrl: String = "",
    val postedAt: Long = 0L,
    val isPinned: Boolean = false,
    val seenBy: Map<String, Long> = emptyMap()   // uid -> seenAt timestamp
)
