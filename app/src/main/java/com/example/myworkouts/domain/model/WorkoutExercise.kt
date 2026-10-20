package com.example.myworkouts.domain.model

data class WorkoutExercise(
    val id: Long,
    val workoutId: Long,
    val exerciseId: Long,
    val displayOrder: Int,
    val exerciseName: String = "",
    val category: String = "",
    val exerciseType: ExerciseType = ExerciseType.WEIGHT_REPS
)