package com.example.myworkouts.data.dao

import androidx.room.*
import com.example.myworkouts.data.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY date DESC, startTime DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE isCompleted = 1 ORDER BY date DESC, startTime DESC")
    fun getCompletedWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE isCompleted = 0 ORDER BY startTime DESC LIMIT 1")
    fun getActiveWorkout(): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: Long): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE date >= :startDate AND date <= :endDate ORDER BY startTime ASC")
    fun getWorkoutsBetween(startDate: Long, endDate: Long): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workout: WorkoutEntity): Long

    @Update
    suspend fun update(workout: WorkoutEntity)

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun delete(workoutId: Long)

    @Query("DELETE FROM workouts")
    suspend fun deleteAll()
}