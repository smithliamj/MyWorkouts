package com.example.myworkouts.ui.screens.completedworkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myworkouts.domain.model.ExerciseType
import com.example.myworkouts.ui.components.ConfirmationDialog
import com.example.myworkouts.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedWorkoutScreen(
    workoutId: Long,
    navController: NavController,
    viewModel: CompletedWorkoutViewModel = viewModel()
) {
    LaunchedEffect(workoutId) {
        viewModel.loadWorkout(workoutId)
    }

    val workoutDetailsState by viewModel.workoutDetails.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Summary") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        val details = workoutDetailsState
        if (details == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val workout = details.workout
            val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(workout.date))
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val startStr = timeFormat.format(Date(workout.startTime))
            val endStr = workout.endTime?.let { timeFormat.format(Date(it)) } ?: "—"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                // Info Banner
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("$dateStr — $startStr", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Completed at $endStr", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Button(
                        onClick = {
                            navController.navigate("${Screen.ActiveWorkout.route}/$workoutId")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.copyWorkout { newId ->
                                navController.navigate("${Screen.ActiveWorkout.route}/$newId")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Copy Workout")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Exercises List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(details.exercises) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(item.exerciseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(item.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))

                                item.sets.forEach { set ->
                                    val w = set.weight ?: 0.0
                                    val r = set.reps ?: 0
                                    val dur = set.duration
                                    val dist = set.distance

                                    val line = when {
                                        item.exerciseType == ExerciseType.WEIGHT_REPS || item.exerciseType == ExerciseType.BODYWEIGHT_REPS -> "Set ${set.setNumber}: $w kg × $r reps"
                                        item.exerciseType == ExerciseType.DURATION -> "Set ${set.setNumber}: ${dur ?: 0}m"
                                        item.exerciseType == ExerciseType.DISTANCE_DURATION -> "Set ${set.setNumber}: ${dist ?: 0.0} km in ${dur ?: 0}m"
                                        else -> "Set ${set.setNumber}: $r reps"
                                    }
                                    Text(line, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteConfirm) {
            ConfirmationDialog(
                title = "Delete Workout?",
                message = "This will permanently delete this workout and all recorded sets. This action cannot be undone.",
                onConfirm = {
                    showDeleteConfirm = false
                    val targetWorkoutId = workoutId
                    val popped = navController.popBackStack()
                    if (!popped) {
                        navController.navigate(Screen.Today.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    viewModel.deleteWorkoutById(targetWorkoutId)
                },
                onDismiss = { showDeleteConfirm = false }
            )
        }
    }
}