package com.example.myworkouts.ui.screens.exercises

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.example.myworkouts.data.entity.ExerciseEntity
import com.example.myworkouts.domain.model.ExerciseType
import com.example.myworkouts.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExerciseCategoryScreen(
    categoryName: String,
    navController: NavController,
    viewModel: ExerciseCategoryViewModel = viewModel()
) {
    LaunchedEffect(categoryName) {
        viewModel.setCategory(categoryName)
    }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val exercises by viewModel.exercises.collectAsState()

    var showCustomDialog by remember { mutableStateOf(false) }
    var actionTargetExercise by remember { mutableStateOf<ExerciseEntity?>(null) }
    var editTargetExercise by remember { mutableStateOf<ExerciseEntity?>(null) }
    var deleteTargetExercise by remember { mutableStateOf<ExerciseEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCustomDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Custom")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search $categoryName exercises...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            if (exercises.isEmpty()) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(exercises) { exercise ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        navController.navigate("${Screen.ExerciseDetails.route}/${exercise.id}")
                                    },
                                    onLongClick = {
                                        actionTargetExercise = exercise
                                    }
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        exercise.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    exercise.description?.let { desc ->
                                        Text(
                                            desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "View Details")
                            }
                        }
                    }
                }
            }
        }

        if (showCustomDialog) {
            CustomExerciseDialog(
                initialCategory = categoryName,
                onConfirm = { name, cat, type, desc ->
                    viewModel.createCustomExercise(name, cat, type, desc)
                },
                onDismiss = { showCustomDialog = false }
            )
        }

        actionTargetExercise?.let { exercise ->
            ExerciseActionDialog(
                exercise = exercise,
                onEditSelected = {
                    editTargetExercise = exercise
                },
                onDeleteSelected = {
                    deleteTargetExercise = exercise
                },
                onDismiss = {
                    actionTargetExercise = null
                }
            )
        }

        editTargetExercise?.let { exercise ->
            CustomExerciseDialog(
                title = "Edit Exercise",
                initialName = exercise.name,
                initialCategory = exercise.category,
                initialType = ExerciseType.fromString(exercise.exerciseType),
                initialDescription = exercise.description ?: "",
                onConfirm = { name, cat, type, desc ->
                    viewModel.updateExercise(
                        exercise.copy(
                            name = name,
                            category = cat,
                            exerciseType = type,
                            description = desc
                        )
                    )
                },
                onDismiss = { editTargetExercise = null }
            )
        }

        deleteTargetExercise?.let { exercise ->
            DeleteExercisePrompt(
                exerciseName = exercise.name,
                onConfirm = {
                    viewModel.deleteExercise(exercise.id)
                },
                onDismiss = { deleteTargetExercise = null }
            )
        }
    }
}