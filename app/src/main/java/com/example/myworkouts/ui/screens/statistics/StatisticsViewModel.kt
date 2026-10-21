package com.example.myworkouts.ui.screens.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworkouts.data.AppDatabase
import com.example.myworkouts.data.entity.WorkoutEntity
import com.example.myworkouts.data.repository.WorkoutRepositoryImpl
import com.example.myworkouts.domain.model.ExerciseType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.Calendar

data class PersonalRecordItem(
    val exerciseName: String,
    val category: String,
    val primaryMetric: String,
    val secondaryMetric: String? = null
)

data class StatisticsOverview(
    val totalWorkouts: Int = 0,
    val workoutsThisWeek: Int = 0,
    val workoutsThisMonth: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val workoutRepository = WorkoutRepositoryImpl(
        database.workoutDao(),
        database.workoutExerciseDao(),
        database.setDao(),
        database.exerciseDao()
    )

    val completedWorkouts: StateFlow<List<WorkoutEntity>> = workoutRepository.completedWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overview: StateFlow<StatisticsOverview> = completedWorkouts.map { workouts ->
        val now = Calendar.getInstance()

        val startOfWeek = now.clone() as Calendar
        startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek)
        startOfWeek.set(Calendar.HOUR_OF_DAY, 0)
        startOfWeek.set(Calendar.MINUTE, 0)
        startOfWeek.set(Calendar.SECOND, 0)

        val startOfMonth = now.clone() as Calendar
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1)
        startOfMonth.set(Calendar.HOUR_OF_DAY, 0)
        startOfMonth.set(Calendar.MINUTE, 0)
        startOfMonth.set(Calendar.SECOND, 0)

        val thisWeek = workouts.count { it.date >= startOfWeek.timeInMillis }
        val thisMonth = workouts.count { it.date >= startOfMonth.timeInMillis }

        StatisticsOverview(
            totalWorkouts = workouts.size,
            workoutsThisWeek = thisWeek,
            workoutsThisMonth = thisMonth
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsOverview())

    val personalRecords: StateFlow<List<PersonalRecordItem>> = database.exerciseDao().getAllExercises().flatMapLatest { exercises ->
        completedWorkouts.map { _ ->
            val prList = mutableListOf<PersonalRecordItem>()
            exercises.forEach { exercise ->
                val sets = database.setDao().getPreviousSetsForExerciseSync(exercise.id, System.currentTimeMillis())
                if (sets.isNotEmpty()) {
                    val maxW = sets.maxOfOrNull { it.weight ?: 0.0 } ?: 0.0
                    val maxDist = sets.maxOfOrNull { it.distance ?: 0.0 } ?: 0.0
                    val maxDur = sets.maxOfOrNull { it.duration ?: 0L } ?: 0L

                    val type = ExerciseType.fromString(exercise.exerciseType)
                    when (type) {
                        ExerciseType.DISTANCE_DURATION -> {
                            if (maxDur > 0 || maxDist > 0) {
                                prList.add(
                                    PersonalRecordItem(
                                        exerciseName = exercise.name,
                                        category = exercise.category,
                                        primaryMetric = "Max Time: ${maxDur}m",
                                        secondaryMetric = if (maxDist > 0) "Max Distance: $maxDist km" else null
                                    )
                                )
                            }
                        }
                        ExerciseType.DURATION -> {
                            if (maxDur > 0) {
                                prList.add(
                                    PersonalRecordItem(
                                        exerciseName = exercise.name,
                                        category = exercise.category,
                                        primaryMetric = "Max Time: ${maxDur}m"
                                    )
                                )
                            }
                        }
                        ExerciseType.REPS_ONLY -> {
                            prList.add(
                                PersonalRecordItem(
                                    exerciseName = exercise.name,
                                    category = exercise.category,
                                    primaryMetric = "Total Sets: ${sets.size}"
                                )
                            )
                        }
                        else -> {
                            if (maxW > 0) {
                                prList.add(
                                    PersonalRecordItem(
                                        exerciseName = exercise.name,
                                        category = exercise.category,
                                        primaryMetric = "Max Weight: $maxW kg"
                                    )
                                )
                            }
                        }
                    }
                }
            }
            prList.sortedBy { it.exerciseName }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}