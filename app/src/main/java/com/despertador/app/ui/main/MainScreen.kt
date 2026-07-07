package com.despertador.app.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.despertador.app.ui.alarm.AlarmListScreen
import com.despertador.app.ui.navigation.Routes
import com.despertador.app.ui.stopwatch.StopwatchScreen
import com.despertador.app.ui.timer.TimerScreen
import com.despertador.app.ui.worldclock.WorldClockScreen

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val items = listOf(
        BottomNavItem(Routes.Alarm.route, Routes.Alarm.title, Icons.Default.Notifications),
        BottomNavItem(Routes.WorldClock.route, Routes.WorldClock.title, Icons.Default.Home),
        BottomNavItem(Routes.Stopwatch.route, Routes.Stopwatch.title, Icons.Default.PlayArrow),
        BottomNavItem(Routes.Timer.route, Routes.Timer.title, Icons.Default.Info)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontSize = 12.sp) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Alarm.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.Alarm.route) {
                AlarmListScreen()
            }
            composable(Routes.WorldClock.route) {
                WorldClockScreen()
            }
            composable(Routes.Stopwatch.route) {
                StopwatchScreen()
            }
            composable(Routes.Timer.route) {
                TimerScreen()
            }
        }
    }
}
