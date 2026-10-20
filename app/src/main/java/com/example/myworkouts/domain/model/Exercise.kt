package com.example.myworkouts.domain.model

data class Exercise(
    val id: Long,
    val name: String,
    val category: String,
    val exerciseType: ExerciseType,
    val description: String? = null,
    val isCustom: Boolean = true
)