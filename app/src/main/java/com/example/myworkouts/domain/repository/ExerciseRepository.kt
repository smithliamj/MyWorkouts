package com.example.myworkouts.domain.repository

import com.example.myworkouts.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getAllExercises(): Flow<List<Exercise>>
    fun getExercisesByCategory(category: String): Flow<List<Exercise>>
    suspend fun insertExercise(exercise: Exercise): Long
    suspend fun deleteExercise(exerciseId: Long)
}