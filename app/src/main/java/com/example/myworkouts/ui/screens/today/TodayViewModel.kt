package com.example.myworkouts.ui.screens.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworkouts.data.AppDatabase
import com.example.myworkouts.data.entity.WorkoutEntity
import com.example.myworkouts.data.repository.WorkoutRepositoryImpl
import com.example.myworkouts.domain.model.WorkoutWithDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class TodayViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val workoutRepository = WorkoutRepositoryImpl(
        database.workoutDao(),
        database.workoutExerciseDao(),
        database.setDao(),
        database.exerciseDao()
    )

    val activeWorkout: StateFlow<WorkoutEntity?> = workoutRepository.activeWorkout
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayWorkouts: StateFlow<List<WorkoutEntity>> = workoutRepository.allWorkouts
        .combine(activeWorkout) { allWorkouts, active ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endOfDay = calendar.timeInMillis

            allWorkouts.filter { it.date in startOfDay..endOfDay }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startNewWorkout(name: String, onWorkoutCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val workoutId = workoutRepository.startNewWorkout(name)
            onWorkoutCreated(workoutId)
        }
    }
}