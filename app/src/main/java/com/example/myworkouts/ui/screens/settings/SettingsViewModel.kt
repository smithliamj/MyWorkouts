package com.example.myworkouts.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworkouts.data.AppDatabase
import com.example.myworkouts.data.repository.SettingsRepository
import com.example.myworkouts.data.repository.UserSettings
import com.example.myworkouts.data.repository.WorkoutRepositoryImpl
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository(application)
    private val database = AppDatabase.getDatabase(application)
    private val workoutRepository = WorkoutRepositoryImpl(
        database.workoutDao(),
        database.workoutExerciseDao(),
        database.setDao(),
        database.exerciseDao()
    )

    val settings: StateFlow<UserSettings> = settingsRepository.settings

    fun updateWeightUnit(unit: String) {
        settingsRepository.updateWeightUnit(unit)
    }

    fun updateWeightIncrement(increment: Double) {
        settingsRepository.updateWeightIncrement(increment)
    }

    fun updateConfirmBeforeDelete(enabled: Boolean) {
        settingsRepository.updateConfirmBeforeDelete(enabled)
    }

    fun clearAllData(onCleared: () -> Unit) {
        viewModelScope.launch {
            workoutRepository.deleteAllData()
            onCleared()
        }
    }
}