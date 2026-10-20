package com.example.myworkouts.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myworkouts.data.entity.WorkoutEntity
import com.example.myworkouts.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Calendar, 1: History List

    val selectedCalendar by viewModel.selectedCalendar.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val allCompletedWorkouts by viewModel.allCompletedWorkouts.collectAsState()
    val workoutsForSelectedDate by viewModel.workoutsForSelectedDate.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Calendar & History") },
                    actions = {
                        TextButton(onClick = { viewModel.selectToday() }) {
                            Text("Today")
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Calendar View", modifier = Modifier.padding(12.dp))
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Workout History", modifier = Modifier.padding(12.dp))
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (selectedTab == 0) {
                // Calendar View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Month Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.previousMonth() }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev Month")
                        }
                        val monthYearStr = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedCalendar.time)
                        Text(monthYearStr, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { viewModel.nextMonth() }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Grid
                    CalendarGrid(
                        selectedCalendar = selectedCalendar,
                        selectedDate = selectedDate,
                        workoutDates = allCompletedWorkouts.map { it.date },
                        onDateSelected = { timeInMillis ->
                            viewModel.selectDate(timeInMillis)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    val selectedDateStr = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()).format(Date(selectedDate))
                    Text(
                        "Workouts on $selectedDateStr",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (workoutsForSelectedDate.isEmpty()) {
                        Text("No workouts completed on this date.", color = MaterialTheme.colorScheme.outline)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(workoutsForSelectedDate) { workout ->
                                WorkoutCard(workout = workout, onClick = {
                                    navController.navigate("${Screen.CompletedWorkout.route}/${workout.id}")
                                })
                            }
                        }
                    }
                }
            } else {
                // History List View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (allCompletedWorkouts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No completed workouts yet.", color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(allCompletedWorkouts) { workout ->
                                WorkoutCard(workout = workout, onClick = {
                                    navController.navigate("${Screen.CompletedWorkout.route}/${workout.id}")
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    selectedCalendar: Calendar,
    selectedDate: Long,
    workoutDates: List<Long>,
    onDateSelected: (Long) -> Unit
) {
    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    // Calculate Grid Cells for month
    val cal = selectedCalendar.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Day Names Row
        Row(modifier = Modifier.fillMaxWidth()) {
            dayNames.forEach { dayName ->
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Days Grid
        var currentDay = 1
        for (week in 0..5) {
            if (currentDay > daysInMonth) break
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayOfWeek in 0..6) {
                    if ((week == 0 && dayOfWeek < firstDayOfWeek) || currentDay > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val dayCal = cal.clone() as Calendar
                        dayCal.set(Calendar.DAY_OF_MONTH, currentDay)
                        val dayMillis = dayCal.timeInMillis

                        // Check if day matches selected date
                        val isSelected = isSameDay(dayMillis, selectedDate)
                        // Check if day has completed workouts
                        val hasWorkout = workoutDates.any { isSameDay(it, dayMillis) }

                        val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .clickable { onDateSelected(dayMillis) }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$currentDay",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                if (hasWorkout) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }
                        currentDay++
                    }
                }
            }
        }
    }
}

fun isSameDay(millis1: Long, millis2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun WorkoutCard(
    workout: WorkoutEntity,
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(workout.date))
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val startStr = timeFormat.format(Date(workout.startTime))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Workout — $startStr", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}