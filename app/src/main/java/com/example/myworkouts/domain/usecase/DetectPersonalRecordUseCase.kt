package com.example.myworkouts.domain.usecase

import com.example.myworkouts.domain.model.WorkoutSet

class DetectPersonalRecordUseCase {
    operator fun invoke(historicalSets: List<WorkoutSet>, currentSet: WorkoutSet): Boolean {
        if (!currentSet.completed) return false
        val currentWeight = currentSet.weight ?: 0.0
        val currentDuration = currentSet.duration ?: 0L

        val bestWeightBefore = historicalSets.maxOfOrNull { it.weight ?: 0.0 } ?: 0.0
        val bestDurationBefore = historicalSets.maxOfOrNull { it.duration ?: 0L } ?: 0L

        return (currentWeight > 0 && currentWeight > bestWeightBefore) ||
                (currentDuration > 0 && currentDuration > bestDurationBefore)
    }
}