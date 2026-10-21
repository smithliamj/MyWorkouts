package com.example.myworkouts.data.repository

import com.example.myworkouts.data.dao.ExerciseDao
import com.example.myworkouts.data.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

class ExerciseRepositoryImpl(private val exerciseDao: ExerciseDao) {
    val allExercises: Flow<List<ExerciseEntity>> = exerciseDao.getAllExercises()

    fun getExercisesByCategory(category: String): Flow<List<ExerciseEntity>> {
        return exerciseDao.getExercisesByCategory(category)
    }

    fun searchExercises(query: String): Flow<List<ExerciseEntity>> {
        return exerciseDao.searchExercises(query)
    }

    suspend fun getExerciseById(exerciseId: Long): ExerciseEntity? {
        return exerciseDao.getExerciseById(exerciseId)
    }

    suspend fun insertExercise(exercise: ExerciseEntity): Long {
        return exerciseDao.insert(exercise)
    }

    suspend fun updateExercise(exercise: ExerciseEntity) {
        exerciseDao.update(exercise)
    }

    suspend fun deleteExercise(exerciseId: Long) {
        exerciseDao.delete(exerciseId)
    }
}