package com.example.myworkouts.domain.usecase

import com.example.myworkouts.domain.model.WorkoutSet

class DetectPersonalRecordUseCase(
    private val calculateOneRepMax: CalculateOneRepMaxUseCase = CalculateOneRepMaxUseCase()
) {
    operator fun invoke(historicalSets: List<WorkoutSet>, currentSet: WorkoutSet): Boolean {
        if (!currentSet.completed) return false
        val currentWeight = currentSet.weight ?: 0.0
        val currentReps = currentSet.reps ?: 0
        val current1RM = calculateOneRepMax(currentWeight, currentReps)

        val bestWeightBefore = historicalSets.maxOfOrNull { it.weight ?: 0.0 } ?: 0.0
        val best1RMBefore = historicalSets.maxOfOrNull { calculateOneRepMax(it.weight ?: 0.0, it.reps ?: 0) } ?: 0.0

        return currentWeight > bestWeightBefore || current1RM > best1RMBefore
    }
}