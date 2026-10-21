package com.example.myworkouts.ui.screens.exercises

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailsScreen(
    exerciseId: Long,
    navController: NavController,
    viewModel: ExerciseDetailsViewModel = viewModel()
) {
    LaunchedEffect(exerciseId) {
        viewModel.loadExercise(exerciseId)
    }

    val exercise by viewModel.exercise.collectAsState()
    val historySessions by viewModel.historySessions.collectAsState()
    val stats by viewModel.stats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise?.name ?: "Exercise Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val currentExercise = exercise
        if (currentExercise == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Header Details
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                currentExercise.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Category: ${currentExercise.category}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            currentExercise.description?.let { desc ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(desc, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Stats Grid
                item {
                    Text("Personal Records & Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StatCard(
                                title = stats.statLabel1,
                                value = stats.statValue1,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = stats.statLabel2,
                                value = stats.statValue2,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StatCard(
                                title = "Total Sets",
                                value = "${stats.totalSets}",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Times Performed",
                                value = "${stats.timesPerformed}",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // History Timeline
                item {
                    Text("Performance History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                if (historySessions.isEmpty()) {
                    item {
                        Text(
                            "No history available for this exercise yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    items(historySessions) { session ->
                        val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(session.workoutDate))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(dateStr, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                session.sets.forEach { set ->
                                    val w = set.weight ?: 0.0
                                    val r = set.reps ?: 0
                                    val dur = set.duration
                                    val dist = set.distance

                                    val lineText = when {
                                        w > 0 && r > 0 -> "Set ${set.setNumber}: $w kg × $r reps"
                                        r > 0 -> "Set ${set.setNumber}: $r reps"
                                        dist != null && dur != null -> "Set ${set.setNumber}: $dist km in ${dur}m"
                                        dur != null -> "Set ${set.setNumber}: ${dur}m"
                                        dist != null -> "Set ${set.setNumber}: $dist km"
                                        else -> "Set ${set.setNumber}: Completed"
                                    }
                                    Text(
                                        lineText,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}