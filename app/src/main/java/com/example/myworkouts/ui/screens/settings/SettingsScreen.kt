package com.example.myworkouts.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myworkouts.ui.components.ConfirmationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val userSettings by viewModel.settings.collectAsState()
    var showClearDataConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Preferences Section
            item {
                Text("Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Weight Unit", style = MaterialTheme.typography.bodyLarge)
                            Row {
                                FilterChip(
                                    selected = userSettings.weightUnit == "kg",
                                    onClick = { viewModel.updateWeightUnit("kg") },
                                    label = { Text("kg") }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                FilterChip(
                                    selected = userSettings.weightUnit == "lbs",
                                    onClick = { viewModel.updateWeightUnit("lbs") },
                                    label = { Text("lbs") }
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Text("Default Weight Increment", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0.25, 0.5, 1.0, 2.5).forEach { inc ->
                                FilterChip(
                                    selected = userSettings.weightIncrement == inc,
                                    onClick = { viewModel.updateWeightIncrement(inc) },
                                    label = { Text("+$inc ${userSettings.weightUnit}") }
                                )
                            }
                        }
                    }
                }
            }

            // Data & Safety Section
            item {
                Text("Data & Safety", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Confirm before deleting data", style = MaterialTheme.typography.bodyLarge)
                            Switch(
                                checked = userSettings.confirmBeforeDelete,
                                onCheckedChange = { viewModel.updateConfirmBeforeDelete(it) }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Button(
                            onClick = { showClearDataConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clear All Application Data")
                        }
                    }
                }
            }
        }

        if (showClearDataConfirm) {
            ConfirmationDialog(
                title = "Clear All Data?",
                message = "This will permanently delete all recorded workouts, custom exercises, and sets. This action cannot be undone.",
                onConfirm = {
                    viewModel.clearAllData {
                        showClearDataConfirm = false
                    }
                },
                onDismiss = { showClearDataConfirm = false }
            )
        }
    }
}