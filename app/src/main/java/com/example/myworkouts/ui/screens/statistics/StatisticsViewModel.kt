package com.example.myworkouts.ui.screens.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworkouts.data.AppDatabase
import com.example.myworkouts.data.entity.WorkoutEntity
import com.example.myworkouts.data.repository.WorkoutRepositoryImpl
import com.example.myworkouts.domain.usecase.CalculateOneRepMaxUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.Calendar

data class PersonalRecordItem(
    val exerciseName: String,
    val category: String,
    val heaviestWeight: Double,
    val maxReps: Int,
    val best1RM: Double
)

data class StatisticsOverview(
    val totalWorkouts: Int = 0,
    val workoutsThisWeek: Int = 0,
    val workoutsThisMonth: Int = 0,
    val totalSets: Int = 0,
    val totalVolume: Double = 0.0,
    val workoutStreakWeeks: Int = 0
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
    private val calculate1RM = CalculateOneRepMaxUseCase()

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

        // Calculate total sets and volume from setDao
        var totalVolume = 0.0
        var totalSets = 0

        workouts.forEach { w ->
            val weList = database.workoutExerciseDao().getExercisesForWorkoutSync(w.id)
            val weIds = weList.map { it.id }
            if (weIds.isNotEmpty()) {
                val sets = database.setDao().getSetsForWorkoutExercisesSync(weIds)
                val completedSets = sets.filter { it.completed }
                totalSets += completedSets.size
                totalVolume += completedSets.sumOf { (it.weight ?: 0.0) * (it.reps ?: 0) }
            }
        }

        StatisticsOverview(
            totalWorkouts = workouts.size,
            workoutsThisWeek = thisWeek,
            workoutsThisMonth = thisMonth,
            totalSets = totalSets,
            totalVolume = totalVolume,
            workoutStreakWeeks = calculateStreak(workouts)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsOverview())

    val personalRecords: StateFlow<List<PersonalRecordItem>> = database.exerciseDao().getAllExercises().flatMapLatest { exercises ->
        completedWorkouts.map { _ ->
            val prList = mutableListOf<PersonalRecordItem>()
            exercises.forEach { exercise ->
                val sets = database.setDao().getPreviousSetsForExerciseSync(exercise.id, System.currentTimeMillis())
                if (sets.isNotEmpty()) {
                    val maxW = sets.maxOfOrNull { it.weight ?: 0.0 } ?: 0.0
                    val maxR = sets.maxOfOrNull { it.reps ?: 0 } ?: 0
                    val max1RM = sets.maxOfOrNull { calculate1RM(it.weight ?: 0.0, it.reps ?: 0) } ?: 0.0

                    if (maxW > 0 || maxR > 0) {
                        prList.add(
                            PersonalRecordItem(
                                exerciseName = exercise.name,
                                category = exercise.category,
                                heaviestWeight = maxW,
                                maxReps = maxR,
                                best1RM = max1RM
                            )
                        )
                    }
                }
            }
            prList.sortedByDescending { it.heaviestWeight }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun calculateStreak(workouts: List<WorkoutEntity>): Int {
        if (workouts.isEmpty()) return 0
        var streak = 0
        val cal = Calendar.getInstance()
        while (true) {
            val startOfWeek = cal.clone() as Calendar
            startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek)
            startOfWeek.set(Calendar.HOUR_OF_DAY, 0)

            val endOfWeek = cal.clone() as Calendar
            endOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek + 6)
            endOfWeek.set(Calendar.HOUR_OF_DAY, 23)

            val hasWorkoutThisWeek = workouts.any { it.date in startOfWeek.timeInMillis..endOfWeek.timeInMillis }
            if (hasWorkoutThisWeek) {
                streak++
                cal.add(Calendar.WEEK_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }
}