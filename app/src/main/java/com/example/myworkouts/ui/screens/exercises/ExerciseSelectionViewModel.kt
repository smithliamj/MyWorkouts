package com.example.myworkouts.ui.screens.exercises

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworkouts.data.AppDatabase
import com.example.myworkouts.data.entity.ExerciseEntity
import com.example.myworkouts.data.repository.ExerciseRepositoryImpl
import com.example.myworkouts.data.repository.WorkoutRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseSelectionViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val exerciseRepository = ExerciseRepositoryImpl(database.exerciseDao())
    private val workoutRepository = WorkoutRepositoryImpl(
        database.workoutDao(),
        database.workoutExerciseDao(),
        database.setDao(),
        database.exerciseDao()
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _selectedExerciseIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedExerciseIds: StateFlow<Set<Long>> = _selectedExerciseIds

    val filteredExercises: StateFlow<List<ExerciseEntity>> = combine(
        exerciseRepository.allExercises,
        _searchQuery,
        _selectedCategory
    ) { allExercises, query, category ->
        allExercises.filter { exercise ->
            val matchesCategory = category == null || exercise.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() || exercise.name.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun toggleExerciseSelection(exerciseId: Long) {
        val current = _selectedExerciseIds.value
        _selectedExerciseIds.value = if (current.contains(exerciseId)) {
            current - exerciseId
        } else {
            current + exerciseId
        }
    }

    fun createCustomExercise(name: String, category: String, exerciseType: String, description: String?) {
        viewModelScope.launch {
            val custom = ExerciseEntity(
                name = name,
                category = category,
                exerciseType = exerciseType,
                description = description,
                isCustom = true
            )
            val id = exerciseRepository.insertExercise(custom)
            _selectedExerciseIds.value = _selectedExerciseIds.value + id
        }
    }

    fun updateExercise(exercise: ExerciseEntity) {
        viewModelScope.launch {
            exerciseRepository.updateExercise(exercise)
        }
    }

    fun deleteExercise(exerciseId: Long) {
        viewModelScope.launch {
            exerciseRepository.deleteExercise(exerciseId)
        }
    }

    fun addSelectedToWorkout(workoutId: Long, onAdded: () -> Unit) {
        viewModelScope.launch {
            val selectedIds = _selectedExerciseIds.value
            for (id in selectedIds) {
                workoutRepository.addExerciseToWorkout(workoutId, id)
            }
            onAdded()
        }
    }
}