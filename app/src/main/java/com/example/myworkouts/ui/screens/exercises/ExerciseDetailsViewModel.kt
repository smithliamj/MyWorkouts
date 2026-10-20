package com.example.myworkouts.ui.screens.exercises

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworkouts.data.AppDatabase
import com.example.myworkouts.data.entity.ExerciseEntity
import com.example.myworkouts.data.entity.SetEntity
import com.example.myworkouts.data.repository.ExerciseRepositoryImpl
import com.example.myworkouts.data.repository.WorkoutRepositoryImpl
import com.example.myworkouts.domain.usecase.CalculateOneRepMaxUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class ExerciseHistorySession(
    val workoutDate: Long,
    val workoutName: String,
    val sets: List<SetEntity>
)

data class ExerciseStats(
    val maxWeight: Double = 0.0,
    val maxReps: Int = 0,
    val maxEstimated1RM: Double = 0.0,
    val totalVolume: Double = 0.0,
    val totalSets: Int = 0,
    val timesPerformed: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val exerciseRepository = ExerciseRepositoryImpl(database.exerciseDao())
    private val workoutRepository = WorkoutRepositoryImpl(
        database.workoutDao(),
        database.workoutExerciseDao(),
        database.setDao(),
        database.exerciseDao()
    )
    private val calculate1RM = CalculateOneRepMaxUseCase()

    private val _exerciseId = MutableStateFlow<Long?>(null)

    val exercise: StateFlow<ExerciseEntity?> = _exerciseId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else flowOf(id).map { exerciseRepository.getExerciseById(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val historySessions: StateFlow<List<ExerciseHistorySession>> = _exerciseId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else database.setDao().getAllSetsForExercise(id).flatMapLatest { sets ->
            workoutRepository.completedWorkouts.map { workouts ->
                val sessions = mutableListOf<ExerciseHistorySession>()
                workouts.forEach { w ->
                    val workoutSets = sets.filter { it.completed }
                    if (workoutSets.isNotEmpty()) {
                        sessions.add(
                            ExerciseHistorySession(
                                workoutDate = w.date,
                                workoutName = w.name,
                                sets = workoutSets
                            )
                        )
                    }
                }
                sessions.distinctBy { it.workoutDate }.sortedByDescending { it.workoutDate }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<ExerciseStats> = historySessions.map { sessions ->
        var maxWeight = 0.0
        var maxReps = 0
        var max1RM = 0.0
        var totalVolume = 0.0
        var totalSets = 0

        sessions.forEach { session ->
            val sessionVolume = session.sets.sumOf { (it.weight ?: 0.0) * (it.reps ?: 0) }
            totalVolume += sessionVolume
            totalSets += session.sets.size

            session.sets.forEach { set ->
                val w = set.weight ?: 0.0
                val r = set.reps ?: 0
                if (w > maxWeight) maxWeight = w
                if (r > maxReps) maxReps = r

                val e1rm = calculate1RM(w, r)
                if (e1rm > max1RM) max1RM = e1rm
            }
        }

        ExerciseStats(
            maxWeight = maxWeight,
            maxReps = maxReps,
            maxEstimated1RM = max1RM,
            totalVolume = totalVolume,
            totalSets = totalSets,
            timesPerformed = sessions.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExerciseStats())

    fun loadExercise(id: Long) {
        _exerciseId.value = id
    }
}