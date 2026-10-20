package com.example.myworkouts.data.dao

import androidx.room.*
import com.example.myworkouts.data.entity.WorkoutExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutExerciseDao {
    @Query("SELECT * FROM workout_exercises WHERE workoutId = :workoutId ORDER BY displayOrder ASC")
    fun getExercisesForWorkout(workoutId: Long): Flow<List<WorkoutExerciseEntity>>

    @Query("SELECT * FROM workout_exercises WHERE workoutId = :workoutId ORDER BY displayOrder ASC")
    suspend fun getExercisesForWorkoutSync(workoutId: Long): List<WorkoutExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workoutExercise: WorkoutExerciseEntity): Long

    @Update
    suspend fun update(workoutExercise: WorkoutExerciseEntity)

    @Query("DELETE FROM workout_exercises WHERE id = :workoutExerciseId")
    suspend fun delete(workoutExerciseId: Long)

    @Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun deleteForWorkout(workoutId: Long)
}