package com.example.myworkouts.ui.screens.today

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myworkouts.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    navController: NavController,
    viewModel: TodayViewModel = viewModel()
) {
    val activeWorkout by viewModel.activeWorkout.collectAsState()
    val todayWorkouts by viewModel.todayWorkouts.collectAsState()

    val dateFormatted = remember {
        SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()).format(Date())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Today", style = MaterialTheme.typography.titleLarge)
                        Text(dateFormatted, style = MaterialTheme.typography.labelMedium)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Active Workout Banner if in progress
            activeWorkout?.let { active ->
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val startStr = timeFormat.format(Date(active.startTime))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable {
                            navController.navigate("${Screen.ActiveWorkout.route}/${active.id}")
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Workout in Progress",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Started at $startStr",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Button(
                            onClick = {
                                navController.navigate("${Screen.ActiveWorkout.route}/${active.id}")
                            }
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resume")
                        }
                    }
                }
            }

            // Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.startNewWorkout("Workout") { workoutId ->
                            navController.navigate("${Screen.ActiveWorkout.route}/$workoutId")
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start Workout")
                }
                OutlinedButton(
                    onClick = {
                        navController.navigate(Screen.CopyWorkout.route)
                    },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Copy Workout")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Today's Workouts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))

            if (todayWorkouts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No workouts recorded today.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap 'Start Workout' above to begin!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(todayWorkouts) { workout ->
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val startTimeStr = timeFormat.format(Date(workout.startTime))
                        val endTimeStr = workout.endTime?.let { timeFormat.format(Date(it)) } ?: "In Progress"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (workout.isCompleted) {
                                        navController.navigate("${Screen.CompletedWorkout.route}/${workout.id}")
                                    } else {
                                        navController.navigate("${Screen.ActiveWorkout.route}/${workout.id}")
                                    }
                                }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Workout — $startTimeStr",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Badge(
                                        containerColor = if (workout.isCompleted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer
                                    ) {
                                        Text(
                                            if (workout.isCompleted) "Completed" else "Active",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$startTimeStr — $endTimeStr",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}