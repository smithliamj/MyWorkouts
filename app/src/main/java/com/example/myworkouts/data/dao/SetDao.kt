package com.example.myworkouts.data.dao

import androidx.room.*
import com.example.myworkouts.data.entity.SetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SetDao {
    @Query("SELECT * FROM sets WHERE workoutExerciseId = :workoutExerciseId ORDER BY setNumber ASC")
    fun getSetsForWorkoutExercise(workoutExerciseId: Long): Flow<List<SetEntity>>

    @Query("SELECT * FROM sets WHERE workoutExerciseId = :workoutExerciseId ORDER BY setNumber ASC")
    suspend fun getSetsForWorkoutExerciseSync(workoutExerciseId: Long): List<SetEntity>

    @Query("SELECT * FROM sets WHERE workoutExerciseId IN (:workoutExerciseIds) ORDER BY setNumber ASC")
    suspend fun getSetsForWorkoutExercisesSync(workoutExerciseIds: List<Long>): List<SetEntity>

    @Query("""
        SELECT sets.* FROM sets
        INNER JOIN workout_exercises ON sets.workoutExerciseId = workout_exercises.id
        WHERE workout_exercises.exerciseId = :exerciseId
        ORDER BY sets.id ASC
    """)
    fun getAllSetsForExercise(exerciseId: Long): Flow<List<SetEntity>>

    @Query("""
        SELECT sets.* FROM sets
        INNER JOIN workout_exercises ON sets.workoutExerciseId = workout_exercises.id
        INNER JOIN workouts ON workout_exercises.workoutId = workouts.id
        WHERE workout_exercises.exerciseId = :exerciseId AND workouts.isCompleted = 1 AND workouts.date < :beforeDate
        ORDER BY workouts.date DESC, sets.setNumber ASC
    """)
    suspend fun getPreviousSetsForExerciseSync(exerciseId: Long, beforeDate: Long): List<SetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(set: SetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sets: List<SetEntity>)

    @Update
    suspend fun update(set: SetEntity)

    @Query("DELETE FROM sets WHERE id = :setId")
    suspend fun delete(setId: Long)

    @Query("DELETE FROM sets WHERE workoutExerciseId = :workoutExerciseId")
    suspend fun deleteForWorkoutExercise(workoutExerciseId: Long)
}