package com.example.myworkouts.ui.screens.completedworkout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworkouts.data.AppDatabase
import com.example.myworkouts.data.repository.WorkoutRepositoryImpl
import com.example.myworkouts.domain.model.WorkoutWithDetails
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CompletedWorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val workoutRepository = WorkoutRepositoryImpl(
        database.workoutDao(),
        database.workoutExerciseDao(),
        database.setDao(),
        database.exerciseDao()
    )

    private val _workoutId = MutableStateFlow<Long?>(null)

    val workoutDetails: StateFlow<WorkoutWithDetails?> = _workoutId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else workoutRepository.getWorkoutWithDetails(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun loadWorkout(id: Long) {
        _workoutId.value = id
    }

    fun copyWorkout(onCopied: (Long) -> Unit) {
        val id = _workoutId.value ?: return
        viewModelScope.launch {
            val newId = workoutRepository.copyWorkout(id)
            onCopied(newId)
        }
    }

    fun deleteWorkoutById(id: Long) {
        viewModelScope.launch {
            workoutRepository.deleteWorkout(id)
        }
    }

    fun deleteWorkout(onDeleted: () -> Unit) {
        val id = _workoutId.value ?: return
        viewModelScope.launch {
            workoutRepository.deleteWorkout(id)
            onDeleted()
        }
    }
}