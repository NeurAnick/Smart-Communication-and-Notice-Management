package com.example.smartcommunicationandnoticemanagement.data.model

import androidx.annotation.Keep

@Keep
data class Routine(
    val routineId: String = "",
    val semester: Int = 0,
    val day: String = "",       // Saturday/Sunday/Monday/Tuesday/Wednesday/Thursday
    val subject: String = "",
    val teacher: String = "",
    val room: String = "",
    val startTime: String = "",
    val endTime: String = ""
)
