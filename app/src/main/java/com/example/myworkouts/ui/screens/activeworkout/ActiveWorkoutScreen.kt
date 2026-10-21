package com.example.myworkouts.ui.screens.activeworkout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myworkouts.data.entity.SetEntity
import com.example.myworkouts.domain.model.ExerciseType
import com.example.myworkouts.domain.model.WorkoutExerciseWithDetails
import com.example.myworkouts.ui.components.ConfirmationDialog
import com.example.myworkouts.ui.components.NumberStepper
import com.example.myworkouts.ui.components.ValueInputDialog
import com.example.myworkouts.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    navController: NavController,
    workoutId: Long,
    viewModel: ActiveWorkoutViewModel = viewModel()
) {
    LaunchedEffect(workoutId) {
        viewModel.loadWorkout(workoutId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()

    var exerciseToRemove by remember { mutableStateOf<WorkoutExerciseWithDetails?>(null) }
    var setEditTarget by remember { mutableStateOf<Triple<SetEntity, String, (String) -> Unit>?>(null) }

    val workoutDetails = uiState.workoutDetails
    val workout = workoutDetails?.workout

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Workout Session",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            viewModel.finishWorkout {
                                navController.navigate("${Screen.CompletedWorkout.route}/$workoutId") {
                                    popUpTo(Screen.Today.route) { inclusive = false }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Finish")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Finish & Save Workout", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            if (workoutDetails == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = padding.calculateBottomPadding() + 80.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(workoutDetails.exercises) { item ->
                        ExerciseCard(
                            item = item,
                            weightIncrement = userSettings.weightIncrement,
                            unit = userSettings.weightUnit,
                            onUpdateSetValues = { set, w, r, dur, dist -> viewModel.updateSetValues(set, w, r, dur, dist) },
                            onAddSet = { viewModel.addSet(item.workoutExercise.id) },
                            onDeleteSet = { set -> viewModel.deleteSet(set.id, item.workoutExercise.id) },
                            onRemoveExercise = { exerciseToRemove = item },
                            onOpenValueEdit = { set, label, onSave -> setEditTarget = Triple(set, label, onSave) }
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                navController.navigate("${Screen.ExerciseSelection.route}/$workoutId")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Exercise")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Exercise")
                        }
                    }
                }
            }
        }

        // Dialogs
        exerciseToRemove?.let { item ->
            ConfirmationDialog(
                title = "Remove Exercise?",
                message = "Remove ${item.exerciseName} from this workout?",
                onConfirm = {
                    viewModel.removeExercise(item.workoutExercise.id)
                    exerciseToRemove = null
                },
                onDismiss = { exerciseToRemove = null }
            )
        }

        setEditTarget?.let { (set, label, onSave) ->
            val initial = when {
                label.contains("Weight") -> (set.weight ?: 0.0).toString()
                label.contains("Reps") -> (set.reps ?: 0).toString()
                label.contains("Duration") || label.contains("Time") -> (set.duration ?: 0).toString()
                else -> (set.distance ?: 0.0).toString()
            }
            ValueInputDialog(
                title = "Edit $label",
                initialValue = initial,
                onConfirm = { textVal ->
                    onSave(textVal)
                },
                onDismiss = { setEditTarget = null }
            )
        }
    }
}

@Composable
fun ExerciseCard(
    item: WorkoutExerciseWithDetails,
    weightIncrement: Double,
    unit: String,
    onUpdateSetValues: (SetEntity, Double?, Int?, Long?, Double?) -> Unit,
    onAddSet: () -> Unit,
    onDeleteSet: (SetEntity) -> Unit,
    onRemoveExercise: () -> Unit,
    onOpenValueEdit: (SetEntity, String, (String) -> Unit) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.exerciseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(item.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onRemoveExercise, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove Exercise", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Set", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.7f))
                if (item.exerciseType == ExerciseType.WEIGHT_REPS || item.exerciseType == ExerciseType.BODYWEIGHT_REPS) {
                    Text("Weight ($unit)", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(2.5f))
                    Text("Reps", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(2.5f))
                } else if (item.exerciseType == ExerciseType.DURATION) {
                    Text("Duration (m)", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(5f))
                } else if (item.exerciseType == ExerciseType.DISTANCE_DURATION) {
                    Text("Dist (km)", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(2.5f))
                    Text("Time (m)", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(2.5f))
                } else {
                    Text("Reps", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(5f))
                }
                Box(modifier = Modifier.weight(0.7f), contentAlignment = Alignment.CenterEnd) {
                    Text("", style = MaterialTheme.typography.labelSmall)
                }
            }

            HorizontalDivider()

            // Sets List
            item.sets.forEach { set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${set.setNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.7f)
                    )

                    if (item.exerciseType == ExerciseType.WEIGHT_REPS || item.exerciseType == ExerciseType.BODYWEIGHT_REPS) {
                        val currentWeight = set.weight ?: 0.0
                        val currentReps = set.reps ?: 0

                        Box(modifier = Modifier.weight(2.5f), contentAlignment = Alignment.Center) {
                            NumberStepper(
                                label = "",
                                value = "$currentWeight",
                                onDecrement = {
                                    val next = (currentWeight - weightIncrement).coerceAtLeast(0.0)
                                    onUpdateSetValues(set, next, set.reps, set.duration, set.distance)
                                },
                                onIncrement = {
                                    val next = currentWeight + weightIncrement
                                    onUpdateSetValues(set, next, set.reps, set.duration, set.distance)
                                },
                                onValueClick = {
                                    onOpenValueEdit(set, "Weight ($unit)") { input ->
                                        input.toDoubleOrNull()?.let { w ->
                                            onUpdateSetValues(set, w, set.reps, set.duration, set.distance)
                                        }
                                    }
                                }
                            )
                        }

                        Box(modifier = Modifier.weight(2.5f), contentAlignment = Alignment.Center) {
                            NumberStepper(
                                label = "",
                                value = "$currentReps",
                                onDecrement = {
                                    val next = (currentReps - 1).coerceAtLeast(0)
                                    onUpdateSetValues(set, set.weight, next, set.duration, set.distance)
                                },
                                onIncrement = {
                                    val next = currentReps + 1
                                    onUpdateSetValues(set, set.weight, next, set.duration, set.distance)
                                },
                                onValueClick = {
                                    onOpenValueEdit(set, "Reps") { input ->
                                        input.toIntOrNull()?.let { r ->
                                            onUpdateSetValues(set, set.weight, r, set.duration, set.distance)
                                        }
                                    }
                                }
                            )
                        }
                    } else if (item.exerciseType == ExerciseType.DURATION) {
                        val currentDur = set.duration ?: 0L
                        Box(modifier = Modifier.weight(5f), contentAlignment = Alignment.Center) {
                            NumberStepper(
                                label = "",
                                value = "${currentDur}m",
                                onDecrement = {
                                    val next = (currentDur - 1).coerceAtLeast(0)
                                    onUpdateSetValues(set, set.weight, set.reps, next, set.distance)
                                },
                                onIncrement = {
                                    val next = currentDur + 1
                                    onUpdateSetValues(set, set.weight, set.reps, next, set.distance)
                                },
                                onValueClick = {
                                    onOpenValueEdit(set, "Duration (m)") { input ->
                                        input.toLongOrNull()?.let { d ->
                                            onUpdateSetValues(set, set.weight, set.reps, d, set.distance)
                                        }
                                    }
                                }
                            )
                        }
                    } else if (item.exerciseType == ExerciseType.DISTANCE_DURATION) {
                        val currentDist = set.distance ?: 0.0
                        val currentDur = set.duration ?: 0L
                        Box(modifier = Modifier.weight(2.5f), contentAlignment = Alignment.Center) {
                            NumberStepper(
                                label = "",
                                value = "${currentDist} km",
                                onDecrement = {
                                    val next = (currentDist - 0.5).coerceAtLeast(0.0)
                                    onUpdateSetValues(set, set.weight, set.reps, set.duration, next)
                                },
                                onIncrement = {
                                    val next = currentDist + 0.5
                                    onUpdateSetValues(set, set.weight, set.reps, set.duration, next)
                                },
                                onValueClick = {
                                    onOpenValueEdit(set, "Distance (km)") { input ->
                                        input.toDoubleOrNull()?.let { dist ->
                                            onUpdateSetValues(set, set.weight, set.reps, set.duration, dist)
                                        }
                                    }
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(2.5f), contentAlignment = Alignment.Center) {
                            NumberStepper(
                                label = "",
                                value = "${currentDur}m",
                                onDecrement = {
                                    val next = (currentDur - 1).coerceAtLeast(0)
                                    onUpdateSetValues(set, set.weight, set.reps, next, set.distance)
                                },
                                onIncrement = {
                                    val next = currentDur + 1
                                    onUpdateSetValues(set, set.weight, set.reps, next, set.distance)
                                },
                                onValueClick = {
                                    onOpenValueEdit(set, "Time (m)") { input ->
                                        input.toLongOrNull()?.let { d ->
                                            onUpdateSetValues(set, set.weight, set.reps, d, set.distance)
                                        }
                                    }
                                }
                            )
                        }
                    } else {
                        val currentReps = set.reps ?: 0
                        Box(modifier = Modifier.weight(5f), contentAlignment = Alignment.Center) {
                            NumberStepper(
                                label = "",
                                value = "$currentReps",
                                onDecrement = {
                                    val next = (currentReps - 1).coerceAtLeast(0)
                                    onUpdateSetValues(set, set.weight, next, set.duration, set.distance)
                                },
                                onIncrement = {
                                    val next = currentReps + 1
                                    onUpdateSetValues(set, set.weight, next, set.duration, set.distance)
                                },
                                onValueClick = {
                                    onOpenValueEdit(set, "Reps") { input ->
                                        input.toIntOrNull()?.let { r ->
                                            onUpdateSetValues(set, set.weight, r, set.duration, set.distance)
                                        }
                                    }
                                }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.weight(0.7f),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        IconButton(onClick = { onDeleteSet(set) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Delete Set", tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            TextButton(
                onClick = onAddSet,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("+ Add Set")
            }
        }
    }
}