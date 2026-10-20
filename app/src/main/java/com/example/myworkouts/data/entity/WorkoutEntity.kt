package com.example.myworkouts.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val date: Long, // Epoch millis for the calendar date
    val startTime: Long,
    val endTime: Long? = null,
    val duration: Long = 0L,
    val isCompleted: Boolean = false
)