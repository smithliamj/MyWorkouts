package com.example.myworkouts.domain.model

data class WorkoutSet(
    val id: Long,
    val workoutExerciseId: Long,
    val setNumber: Int,
    val weight: Double? = null,
    val reps: Int? = null,
    val duration: Long? = null, // in seconds
    val distance: Double? = null,
    val completed: Boolean = false
)