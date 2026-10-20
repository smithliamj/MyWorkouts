package com.example.myworkouts.ui.screens.exercises

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSelectionScreen(
    navController: NavController,
    workoutId: Long,
    viewModel: ExerciseSelectionViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedExerciseIds by viewModel.selectedExerciseIds.collectAsState()
    val filteredExercises by viewModel.filteredExercises.collectAsState()

    var showCustomDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Abs", "Back", "Biceps", "Cardio", "Chest", "Legs", "Shoulders", "Triceps")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Exercises") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { showCustomDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Custom")
                        Text("Custom")
                    }
                }
            )
        },
        bottomBar = {
            if (selectedExerciseIds.isNotEmpty()) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = {
                                viewModel.addSelectedToWorkout(workoutId) {
                                    navController.popBackStack()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Add Selected (${selectedExerciseIds.size})", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search exercises...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Horizontal Category Chips Row (Smooth Horizontal Scroll)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = (category == "All" && selectedCategory == null) || category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.setSelectedCategory(if (category == "All") null else category)
                        },
                        label = { Text(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Vertical Exercise List (Smooth Vertical Scroll)
            if (filteredExercises.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No exercises found.", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = if (selectedExerciseIds.isNotEmpty()) 80.dp else 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredExercises) { exercise ->
                        val isChecked = selectedExerciseIds.contains(exercise.id)
                        Card(
                            onClick = {
                                viewModel.toggleExerciseSelection(exercise.id)
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        exercise.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        exercise.category,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        viewModel.toggleExerciseSelection(exercise.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showCustomDialog) {
            CustomExerciseDialog(
                initialCategory = selectedCategory ?: "Chest",
                onConfirm = { name, cat, type, desc ->
                    viewModel.createCustomExercise(name, cat, type, desc)
                },
                onDismiss = { showCustomDialog = false }
            )
        }
    }
}