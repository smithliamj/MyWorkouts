package com.example.myworkouts.ui.screens.exercises

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myworkouts.data.AppDatabase
import com.example.myworkouts.data.entity.ExerciseEntity
import com.example.myworkouts.data.repository.ExerciseRepositoryImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CategoryWithCount(
    val categoryName: String,
    val count: Int
)

class ExercisesViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val exerciseRepository = ExerciseRepositoryImpl(database.exerciseDao())

    private val categories = listOf("Abs", "Back", "Biceps", "Cardio", "Chest", "Legs", "Shoulders", "Triceps")

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val categoriesWithCounts: StateFlow<List<CategoryWithCount>> = exerciseRepository.allExercises
        .map { allExercises ->
            categories.map { cat ->
                val count = allExercises.count { it.category.equals(cat, ignoreCase = true) }
                CategoryWithCount(cat, count)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchResults: StateFlow<List<ExerciseEntity>> = combine(
        exerciseRepository.allExercises,
        _searchQuery
    ) { all, query ->
        if (query.isBlank()) emptyList()
        else all.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
            exerciseRepository.insertExercise(exercise)
        }
    }

    fun deleteExercise(exerciseId: Long) {
        viewModelScope.launch {
            exerciseRepository.deleteExercise(exerciseId)
        }
    }
}