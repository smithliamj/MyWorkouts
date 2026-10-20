package com.example.myworkouts.ui.screens.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = viewModel()
) {
    val overview by viewModel.overview.collectAsState()
    val personalRecords by viewModel.personalRecords.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Statistics & Progress") })
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
            // Overview Section
            item {
                Text("Training Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatBox(
                            title = "Total Workouts",
                            value = "${overview.totalWorkouts}",
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            title = "This Week",
                            value = "${overview.workoutsThisWeek}",
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            title = "This Month",
                            value = "${overview.workoutsThisMonth}",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatBox(
                            title = "Total Volume",
                            value = String.format(Locale.getDefault(), "%.0f kg", overview.totalVolume),
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            title = "Total Sets",
                            value = "${overview.totalSets}",
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            title = "Streak",
                            value = "${overview.workoutStreakWeeks} wks",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Personal Records Section
            item {
                Text("Personal Records", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (personalRecords.isEmpty()) {
                item {
                    Text(
                        "No personal records detected yet. Complete sets in your workouts to see your PRs!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                items(personalRecords) { pr ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(pr.exerciseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(pr.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Column {
                                Text("Max: ${pr.heaviestWeight} kg", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("1RM: ${String.format(Locale.getDefault(), "%.1f kg", pr.best1RM)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}