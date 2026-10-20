package com.example.myworkouts.data.repository

import com.example.myworkouts.data.dao.ExerciseDao
import com.example.myworkouts.data.dao.SetDao
import com.example.myworkouts.data.dao.WorkoutDao
import com.example.myworkouts.data.dao.WorkoutExerciseDao
import com.example.myworkouts.data.entity.SetEntity
import com.example.myworkouts.data.entity.WorkoutEntity
import com.example.myworkouts.data.entity.WorkoutExerciseEntity
import com.example.myworkouts.domain.model.ExerciseType
import com.example.myworkouts.domain.model.WorkoutExerciseWithDetails
import com.example.myworkouts.domain.model.WorkoutWithDetails
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutRepositoryImpl(
    private val workoutDao: WorkoutDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val setDao: SetDao,
    private val exerciseDao: ExerciseDao
) {
    val allWorkouts: Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()
    val completedWorkouts: Flow<List<WorkoutEntity>> = workoutDao.getCompletedWorkouts()
    val activeWorkout: Flow<WorkoutEntity?> = workoutDao.getActiveWorkout()

    suspend fun getWorkoutById(workoutId: Long): WorkoutEntity? {
        return workoutDao.getWorkoutById(workoutId)
    }

    fun getWorkoutsBetween(startDate: Long, endDate: Long): Flow<List<WorkoutEntity>> {
        return workoutDao.getWorkoutsBetween(startDate, endDate)
    }

    fun getWorkoutWithDetails(workoutId: Long): Flow<WorkoutWithDetails?> {
        return workoutDao.getAllWorkouts().flatMapLatest { workouts ->
            val workout = workouts.find { it.id == workoutId }
            if (workout == null) {
                flowOf(null)
            } else {
                workoutExerciseDao.getExercisesForWorkout(workoutId).flatMapLatest { exercises ->
                    if (exercises.isEmpty()) {
                        flowOf(WorkoutWithDetails(workout = workout, exercises = emptyList()))
                    } else {
                        val exerciseFlows = exercises.map { we ->
                            val setsFlow = setDao.getSetsForWorkoutExercise(we.id)
                            val exerciseFlow = exerciseDao.getAllExercises()
                            combine(setsFlow, exerciseFlow) { sets, allExercisesList ->
                                val exerciseEntity = allExercisesList.find { it.id == we.exerciseId }
                                WorkoutExerciseWithDetails(
                                    workoutExercise = we,
                                    exerciseName = exerciseEntity?.name ?: "Unknown Exercise",
                                    category = exerciseEntity?.category ?: "General",
                                    exerciseType = ExerciseType.fromString(exerciseEntity?.exerciseType),
                                    sets = sets
                                )
                            }
                        }
                        combine(exerciseFlows) { detailsArray ->
                            WorkoutWithDetails(workout = workout, exercises = detailsArray.toList())
                        }
                    }
                }
            }
        }
    }

    suspend fun startNewWorkout(name: String, date: Long = System.currentTimeMillis()): Long {
        val newWorkout = WorkoutEntity(
            name = name.ifBlank { "Workout" },
            date = date,
            startTime = System.currentTimeMillis(),
            isCompleted = false
        )
        return workoutDao.insert(newWorkout)
    }

    suspend fun copyWorkout(workoutIdToCopy: Long, targetDate: Long = System.currentTimeMillis()): Long {
        val sourceWorkout = workoutDao.getWorkoutById(workoutIdToCopy) ?: return 0L
        val newWorkout = WorkoutEntity(
            name = sourceWorkout.name,
            date = targetDate,
            startTime = System.currentTimeMillis(),
            isCompleted = false
        )
        val newWorkoutId = workoutDao.insert(newWorkout)

        val sourceExercises = workoutExerciseDao.getExercisesForWorkoutSync(workoutIdToCopy)
        for (we in sourceExercises) {
            val newWeId = workoutExerciseDao.insert(
                WorkoutExerciseEntity(
                    workoutId = newWorkoutId,
                    exerciseId = we.exerciseId,
                    displayOrder = we.displayOrder
                )
            )
            val sourceSets = setDao.getSetsForWorkoutExerciseSync(we.id)
            val newSets = sourceSets.map { set ->
                SetEntity(
                    workoutExerciseId = newWeId,
                    setNumber = set.setNumber,
                    weight = set.weight,
                    reps = set.reps,
                    duration = set.duration,
                    distance = set.distance,
                    completed = false
                )
            }
            if (newSets.isNotEmpty()) {
                setDao.insertAll(newSets)
            }
        }
        return newWorkoutId
    }

    suspend fun addExerciseToWorkout(workoutId: Long, exerciseId: Long): Long {
        val existingExercises = workoutExerciseDao.getExercisesForWorkoutSync(workoutId)
        val nextOrder = (existingExercises.maxOfOrNull { it.displayOrder } ?: -1) + 1
        val newWeId = workoutExerciseDao.insert(
            WorkoutExerciseEntity(
                workoutId = workoutId,
                exerciseId = exerciseId,
                displayOrder = nextOrder
            )
        )

        // Pre-populate first set if previous performance exists
        val lastSets = getPreviousPerformanceForExercise(exerciseId, System.currentTimeMillis())
        val firstSet = if (lastSets.isNotEmpty()) {
            val template = lastSets.first()
            SetEntity(
                workoutExerciseId = newWeId,
                setNumber = 1,
                weight = template.weight ?: 20.0,
                reps = template.reps ?: 10,
                duration = template.duration,
                distance = template.distance,
                completed = true
            )
        } else {
            SetEntity(
                workoutExerciseId = newWeId,
                setNumber = 1,
                weight = 20.0,
                reps = 10,
                completed = true
            )
        }
        setDao.insert(firstSet)
        return newWeId
    }

    suspend fun removeExerciseFromWorkout(workoutExerciseId: Long) {
        setDao.deleteForWorkoutExercise(workoutExerciseId)
        workoutExerciseDao.delete(workoutExerciseId)
    }

    suspend fun addSetToExercise(workoutExerciseId: Long): Long {
        val currentSets = setDao.getSetsForWorkoutExerciseSync(workoutExerciseId)
        val lastSet = currentSets.lastOrNull()
        val nextSetNumber = currentSets.size + 1
        val newSet = if (lastSet != null) {
            SetEntity(
                workoutExerciseId = workoutExerciseId,
                setNumber = nextSetNumber,
                weight = lastSet.weight,
                reps = lastSet.reps,
                duration = lastSet.duration,
                distance = lastSet.distance,
                completed = true
            )
        } else {
            SetEntity(
                workoutExerciseId = workoutExerciseId,
                setNumber = nextSetNumber,
                weight = 20.0,
                reps = 10,
                completed = true
            )
        }
        return setDao.insert(newSet)
    }

    suspend fun updateSet(set: SetEntity) {
        setDao.update(set)
    }

    suspend fun deleteSet(setId: Long, workoutExerciseId: Long) {
        setDao.delete(setId)
        val remainingSets = setDao.getSetsForWorkoutExerciseSync(workoutExerciseId)
        remainingSets.forEachIndexed { index, s ->
            if (s.setNumber != index + 1) {
                setDao.update(s.copy(setNumber = index + 1))
            }
        }
    }

    suspend fun finishWorkout(workoutId: Long) {
        val workout = workoutDao.getWorkoutById(workoutId) ?: return
        val endTime = System.currentTimeMillis()
        val durationSeconds = (endTime - workout.startTime) / 1000
        workoutDao.update(
            workout.copy(
                endTime = endTime,
                duration = durationSeconds,
                isCompleted = true
            )
        )
    }

    suspend fun updateWorkoutName(workoutId: Long, newName: String) {
        val workout = workoutDao.getWorkoutById(workoutId) ?: return
        workoutDao.update(workout.copy(name = newName.ifBlank { "Workout" }))
    }

    suspend fun deleteWorkout(workoutId: Long) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.NonCancellable) {
        val exercises = workoutExerciseDao.getExercisesForWorkoutSync(workoutId)
        for (we in exercises) {
            setDao.deleteForWorkoutExercise(we.id)
        }
        workoutExerciseDao.deleteForWorkout(workoutId)
        workoutDao.delete(workoutId)
    }

    suspend fun deleteAllData() {
        workoutDao.deleteAll()
        exerciseDao.deleteAll()
    }

    suspend fun getPreviousPerformanceForExercise(exerciseId: Long, beforeWorkoutDate: Long): List<SetEntity> {
        return setDao.getPreviousSetsForExerciseSync(exerciseId, beforeWorkoutDate)
    }
}