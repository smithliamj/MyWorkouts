package com.example.myworkouts.domain.model

import com.example.myworkouts.data.entity.SetEntity
import com.example.myworkouts.data.entity.WorkoutEntity
import com.example.myworkouts.data.entity.WorkoutExerciseEntity

data class WorkoutWithDetails(
    val workout: WorkoutEntity,
    val exercises: List<WorkoutExerciseWithDetails>
)

data class WorkoutExerciseWithDetails(
    val workoutExercise: WorkoutExerciseEntity,
    val exerciseName: String,
    val category: String,
    val exerciseType: ExerciseType,
    val sets: List<SetEntity>,
    val previousPerformance: List<SetEntity> = emptyList()
)