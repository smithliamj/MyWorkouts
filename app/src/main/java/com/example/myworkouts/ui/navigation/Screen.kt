package com.example.myworkouts.ui.navigation

sealed class Screen(val route: String) {
    object Today : Screen("today")
    object Calendar : Screen("calendar")
    object Exercises : Screen("exercises")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
    object ActiveWorkout : Screen("active_workout")
    object CopyWorkout : Screen("copy_workout")
    object ExerciseCategory : Screen("exercise_category")
    object ExerciseDetails : Screen("exercise_details")
    object ExerciseSelection : Screen("exercise_selection")
    object CompletedWorkout : Screen("completed_workout")
}