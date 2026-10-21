package com.example.myworkouts.ui.screens.exercises

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworkouts.data.AppDatabase
import com.example.myworkouts.data.entity.ExerciseEntity
import com.example.myworkouts.data.repository.ExerciseRepositoryImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExerciseCategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val exerciseRepository = ExerciseRepositoryImpl(database.exerciseDao())

    private val _category = MutableStateFlow("")
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val exercises: StateFlow<List<ExerciseEntity>> = combine(
        _category,
        _searchQuery,
        exerciseRepository.allExercises
    ) { cat, query, all ->
        all.filter {
            it.category.equals(cat, ignoreCase = true) &&
                    (query.isBlank() || it.name.contains(query, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCategory(categoryName: String) {
        _category.value = categoryName
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
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
            exerciseRepository.insertExercise(custom)
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
}