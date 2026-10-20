package com.example.myworkouts.ui.screens.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworkouts.data.AppDatabase
import com.example.myworkouts.data.entity.WorkoutEntity
import com.example.myworkouts.data.repository.WorkoutRepositoryImpl
import kotlinx.coroutines.flow.*
import java.util.Calendar

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val workoutRepository = WorkoutRepositoryImpl(
        database.workoutDao(),
        database.workoutExerciseDao(),
        database.setDao(),
        database.exerciseDao()
    )

    private val _selectedCalendar = MutableStateFlow(Calendar.getInstance())
    val selectedCalendar: StateFlow<Calendar> = _selectedCalendar

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate

    val allCompletedWorkouts: StateFlow<List<WorkoutEntity>> = workoutRepository.completedWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutsForSelectedDate: StateFlow<List<WorkoutEntity>> = combine(
        allCompletedWorkouts,
        _selectedDate
    ) { workouts, date ->
        val cal = Calendar.getInstance()
        cal.timeInMillis = date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endOfDay = cal.timeInMillis

        workouts.filter { it.date in startOfDay..endOfDay }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun previousMonth() {
        val cal = _selectedCalendar.value.clone() as Calendar
        cal.add(Calendar.MONTH, -1)
        _selectedCalendar.value = cal
    }

    fun nextMonth() {
        val cal = _selectedCalendar.value.clone() as Calendar
        cal.add(Calendar.MONTH, 1)
        _selectedCalendar.value = cal
    }

    fun selectToday() {
        val today = Calendar.getInstance()
        _selectedCalendar.value = today
        _selectedDate.value = today.timeInMillis
    }

    fun selectDate(timeInMillis: Long) {
        _selectedDate.value = timeInMillis
    }
}