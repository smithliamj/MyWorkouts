package com.example.myworkouts.domain.usecase

class CalculateOneRepMaxUseCase {
    // Epley formula: 1RM = weight * (1 + reps / 30)
    operator fun invoke(weight: Double, reps: Int): Double {
        if (reps <= 0 || weight <= 0) return 0.0
        if (reps == 1) return weight
        return weight * (1.0 + reps / 30.0)
    }
}