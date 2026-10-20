package com.example.myworkouts.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UserSettings(
    val weightUnit: String = "kg",
    val weightIncrement: Double = 1.0,
    val autoStartWorkoutTimer: Boolean = true,
    val confirmBeforeDelete: Boolean = true
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_settings_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings

    private fun loadSettings(): UserSettings {
        return UserSettings(
            weightUnit = prefs.getString("weight_unit", "kg") ?: "kg",
            weightIncrement = prefs.getFloat("weight_increment", 1.0f).toDouble(),
            autoStartWorkoutTimer = prefs.getBoolean("auto_start_workout_timer", true),
            confirmBeforeDelete = prefs.getBoolean("confirm_before_delete", true)
        )
    }

    fun updateWeightUnit(unit: String) {
        prefs.edit().putString("weight_unit", unit).apply()
        _settings.value = loadSettings()
    }

    fun updateWeightIncrement(increment: Double) {
        prefs.edit().putFloat("weight_increment", increment.toFloat()).apply()
        _settings.value = loadSettings()
    }

    fun updateAutoStartWorkoutTimer(enabled: Boolean) {
        prefs.edit().putBoolean("auto_start_workout_timer", enabled).apply()
        _settings.value = loadSettings()
    }

    fun updateConfirmBeforeDelete(enabled: Boolean) {
        prefs.edit().putBoolean("confirm_before_delete", enabled).apply()
        _settings.value = loadSettings()
    }
}