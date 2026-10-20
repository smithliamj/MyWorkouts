package com.example.myworkouts.domain.model

enum class ExerciseType(val label: String) {
    WEIGHT_REPS("Weight + Reps"),
    BODYWEIGHT_REPS("Bodyweight + Reps"),
    DURATION("Duration"),
    DISTANCE_DURATION("Distance + Duration"),
    REPS_ONLY("Reps Only");

    companion object {
        fun fromString(value: String?): ExerciseType {
            return try {
                if (value == null) WEIGHT_REPS else valueOf(value)
            } catch (e: Exception) {
                WEIGHT_REPS
            }
        }
    }
}