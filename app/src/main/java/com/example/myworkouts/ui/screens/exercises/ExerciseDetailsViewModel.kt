package com.example.myworkouts.ui.screens.exercises

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworkouts.data.AppDatabase
import com.example.myworkouts.data.entity.ExerciseEntity
import com.example.myworkouts.data.entity.SetEntity
import com.example.myworkouts.data.repository.ExerciseRepositoryImpl
import com.example.myworkouts.data.repository.WorkoutRepositoryImpl
import com.example.myworkouts.domain.model.ExerciseType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class ExerciseHistorySession(
    val workoutDate: Long,
    val workoutName: String,
    val sets: List<SetEntity>
)

data class ExerciseStats(
    val statLabel1: String = "Best Metric",
    val statValue1: String = "—",
    val statLabel2: String = "Total Sets",
    val statValue2: String = "0",
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

    val stats: StateFlow<ExerciseStats> = combine(exercise, historySessions) { currentExercise, sessions ->
        var maxWeight = 0.0
        var maxDistance = 0.0
        var maxTime = 0L
        var totalSets = 0

        sessions.forEach { session ->
            totalSets += session.sets.size
            session.sets.forEach { set ->
                val w = set.weight ?: 0.0
                val dist = set.distance ?: 0.0
                val dur = set.duration ?: 0L

                if (w > maxWeight) maxWeight = w
                if (dist > maxDistance) maxDistance = dist
                if (dur > maxTime) maxTime = dur
            }
        }

        val type = ExerciseType.fromString(currentExercise?.exerciseType)
        when (type) {
            ExerciseType.DISTANCE_DURATION -> ExerciseStats(
                statLabel1 = "Best Distance",
                statValue1 = "$maxDistance km",
                statLabel2 = "Best Time",
                statValue2 = "${maxTime}m",
                totalSets = totalSets,
                timesPerformed = sessions.size
            )
            ExerciseType.DURATION -> ExerciseStats(
                statLabel1 = "Best Time",
                statValue1 = "${maxTime}m",
                statLabel2 = "Total Sets",
                statValue2 = "$totalSets",
                totalSets = totalSets,
                timesPerformed = sessions.size
            )
            ExerciseType.REPS_ONLY -> ExerciseStats(
                statLabel1 = "Total Sets",
                statValue1 = "$totalSets",
                statLabel2 = "Times Performed",
                statValue2 = "${sessions.size}",
                totalSets = totalSets,
                timesPerformed = sessions.size
            )
            else -> ExerciseStats(
                statLabel1 = "Best Weight",
                statValue1 = "$maxWeight kg",
                statLabel2 = "Total Sets",
                statValue2 = "$totalSets",
                totalSets = totalSets,
                timesPerformed = sessions.size
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExerciseStats())

    fun loadExercise(id: Long) {
        _exerciseId.value = id
    }
}