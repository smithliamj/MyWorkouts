package com.example.myworkouts.ui.screens.activeworkout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworkouts.data.AppDatabase
import com.example.myworkouts.data.entity.SetEntity
import com.example.myworkouts.data.repository.SettingsRepository
import com.example.myworkouts.data.repository.WorkoutRepositoryImpl
import com.example.myworkouts.domain.model.WorkoutWithDetails
import com.example.myworkouts.domain.usecase.DetectPersonalRecordUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ActiveWorkoutUiState(
    val workoutDetails: WorkoutWithDetails? = null,
    val prAlertMessage: String? = null
)

class ActiveWorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val workoutRepository = WorkoutRepositoryImpl(
        database.workoutDao(),
        database.workoutExerciseDao(),
        database.setDao(),
        database.exerciseDao()
    )
    private val settingsRepository = SettingsRepository(application)
    private val detectPR = DetectPersonalRecordUseCase()

    private val _workoutId = MutableStateFlow<Long?>(null)
    private val _prAlertMessage = MutableStateFlow<String?>(null)

    val userSettings = settingsRepository.settings

    val uiState: StateFlow<ActiveWorkoutUiState> = _workoutId.flatMapLatest { id ->
        if (id == null) {
            flowOf(ActiveWorkoutUiState())
        } else {
            workoutRepository.getWorkoutWithDetails(id).map { details ->
                ActiveWorkoutUiState(
                    workoutDetails = details,
                    prAlertMessage = _prAlertMessage.value
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ActiveWorkoutUiState())

    fun loadWorkout(workoutId: Long) {
        _workoutId.value = workoutId
    }

    fun dismissPrAlert() {
        _prAlertMessage.value = null
    }

    fun updateWorkoutName(newName: String) {
        val id = _workoutId.value ?: return
        viewModelScope.launch {
            workoutRepository.updateWorkoutName(id, newName)
        }
    }

    fun toggleSetCompleted(set: SetEntity, exerciseName: String) {
        val newCompleted = !set.completed
        val updatedSet = set.copy(completed = newCompleted)

        viewModelScope.launch {
            workoutRepository.updateSet(updatedSet)
        }
    }

    fun updateSetValues(
        set: SetEntity,
        newWeight: Double? = set.weight,
        newReps: Int? = set.reps,
        newDuration: Long? = set.duration,
        newDistance: Double? = set.distance
    ) {
        viewModelScope.launch {
            workoutRepository.updateSet(
                set.copy(
                    weight = newWeight,
                    reps = newReps,
                    duration = newDuration,
                    distance = newDistance
                )
            )
        }
    }

    fun addSet(workoutExerciseId: Long) {
        viewModelScope.launch {
            workoutRepository.addSetToExercise(workoutExerciseId)
        }
    }

    fun deleteSet(setId: Long, workoutExerciseId: Long) {
        viewModelScope.launch {
            workoutRepository.deleteSet(setId, workoutExerciseId)
        }
    }

    fun removeExercise(workoutExerciseId: Long) {
        viewModelScope.launch {
            workoutRepository.removeExerciseFromWorkout(workoutExerciseId)
        }
    }

    fun finishWorkout(onFinished: () -> Unit) {
        val id = _workoutId.value ?: return
        viewModelScope.launch {
            workoutRepository.finishWorkout(id)
            onFinished()
        }
    }
}