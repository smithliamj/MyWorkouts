package com.example.myworkouts.domain.model

data class Workout(
    val id: Long,
    val name: String,
    val date: Long, // Epoch millis
    val startTime: Long,
    val endTime: Long? = null,
    val duration: Long = 0L
)