package com.example.myworkouts.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myworkouts.ui.navigation.Screen

@Composable
fun AppBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainRoutes = setOf(
        Screen.Today.route,
        Screen.Calendar.route,
        Screen.Exercises.route,
        Screen.Statistics.route,
        Screen.Settings.route
    )

    if (currentRoute in mainRoutes) {
        val items = listOf(
            Triple(Screen.Today, "Today", Icons.Default.Home),
            Triple(Screen.Calendar, "Calendar", Icons.Default.DateRange),
            Triple(Screen.Exercises, "Exercises", Icons.Default.List),
            Triple(Screen.Statistics, "Stats", Icons.Default.Star),
            Triple(Screen.Settings, "Settings", Icons.Default.Settings)
        )

        NavigationBar {
            items.forEach { (screen, label, icon) ->
                NavigationBarItem(
                    icon = { Icon(icon, contentDescription = label) },
                    label = { Text(label) },
                    selected = currentRoute == screen.route,
                    onClick = {
                        if (currentRoute != screen.route) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}