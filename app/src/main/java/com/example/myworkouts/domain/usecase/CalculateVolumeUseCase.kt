package com.example.myworkouts.domain.usecase

import com.example.myworkouts.domain.model.WorkoutSet

class CalculateVolumeUseCase {
    operator fun invoke(sets: List<WorkoutSet>): Double {
        return sets.filter { it.completed }.sumOf { (it.weight ?: 0.0) * (it.reps ?: 0) }
    }
}