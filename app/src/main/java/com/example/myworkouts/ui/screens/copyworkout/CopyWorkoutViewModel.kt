package com.example.myworkouts.ui.screens.copyworkout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworkouts.data.AppDatabase
import com.example.myworkouts.data.entity.WorkoutEntity
import com.example.myworkouts.data.repository.WorkoutRepositoryImpl
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CopyWorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val workoutRepository = WorkoutRepositoryImpl(
        database.workoutDao(),
        database.workoutExerciseDao(),
        database.setDao(),
        database.exerciseDao()
    )

    val previousWorkouts: StateFlow<List<WorkoutEntity>> = workoutRepository.completedWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun copyWorkout(workoutId: Long, onCopied: (Long) -> Unit) {
        viewModelScope.launch {
            val newWorkoutId = workoutRepository.copyWorkout(workoutId)
            onCopied(newWorkoutId)
        }
    }
}