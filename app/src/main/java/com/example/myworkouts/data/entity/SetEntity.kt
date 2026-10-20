package com.example.myworkouts.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sets")
data class SetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val workoutExerciseId: Long,
    val setNumber: Int,
    val weight: Double? = null,
    val reps: Int? = null,
    val duration: Long? = null, // in seconds
    val distance: Double? = null,
    val completed: Boolean = false
)