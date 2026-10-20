package com.example.myworkouts.domain.repository

import com.example.myworkouts.domain.model.Workout
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun getAllWorkouts(): Flow<List<Workout>>
    suspend fun getWorkoutById(workoutId: Long): Workout?
    fun getWorkoutsBetween(startDate: Long, endDate: Long): Flow<List<Workout>>
    suspend fun insertWorkout(workout: Workout): Long
    suspend fun deleteWorkout(workoutId: Long)
}