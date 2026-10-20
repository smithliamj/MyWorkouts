package com.example.myworkouts.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myworkouts.ui.components.AppBottomBar
import com.example.myworkouts.ui.navigation.Screen
import com.example.myworkouts.ui.screens.activeworkout.ActiveWorkoutScreen
import com.example.myworkouts.ui.screens.calendar.CalendarScreen
import com.example.myworkouts.ui.screens.completedworkout.CompletedWorkoutScreen
import com.example.myworkouts.ui.screens.copyworkout.CopyWorkoutScreen
import com.example.myworkouts.ui.screens.exercises.ExerciseCategoryScreen
import com.example.myworkouts.ui.screens.exercises.ExerciseDetailsScreen
import com.example.myworkouts.ui.screens.exercises.ExerciseSelectionScreen
import com.example.myworkouts.ui.screens.exercises.ExercisesScreen
import com.example.myworkouts.ui.screens.settings.SettingsScreen
import com.example.myworkouts.ui.screens.statistics.StatisticsScreen
import com.example.myworkouts.ui.screens.today.TodayScreen
import com.example.myworkouts.ui.theme.MyWorkoutsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyWorkoutsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = { AppBottomBar(navController = navController) }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Today.route,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(Screen.Today.route) { TodayScreen(navController) }
                            composable(Screen.Calendar.route) { CalendarScreen(navController) }
                            composable(Screen.Exercises.route) { ExercisesScreen(navController) }
                            composable(Screen.Statistics.route) { StatisticsScreen() }
                            composable(Screen.Settings.route) { SettingsScreen() }

                            composable(
                                "${Screen.ActiveWorkout.route}/{workoutId}",
                                arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: 0L
                                ActiveWorkoutScreen(navController = navController, workoutId = workoutId)
                            }

                            composable(Screen.CopyWorkout.route) {
                                CopyWorkoutScreen(navController = navController)
                            }

                            composable(
                                "${Screen.ExerciseCategory.route}/{categoryName}",
                                arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: "Chest"
                                ExerciseCategoryScreen(categoryName = categoryName, navController = navController)
                            }

                            composable(
                                "${Screen.ExerciseDetails.route}/{exerciseId}",
                                arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val exerciseId = backStackEntry.arguments?.getLong("exerciseId") ?: 0L
                                ExerciseDetailsScreen(exerciseId = exerciseId, navController = navController)
                            }

                            composable(
                                "${Screen.ExerciseSelection.route}/{workoutId}",
                                arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: 0L
                                ExerciseSelectionScreen(navController = navController, workoutId = workoutId)
                            }

                            composable(
                                "${Screen.CompletedWorkout.route}/{workoutId}",
                                arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
                            ) { backStackEntry ->
                                val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: 0L
                                CompletedWorkoutScreen(workoutId = workoutId, navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}