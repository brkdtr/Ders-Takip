package com.derstakip.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.derstakip.app.ui.components.AppBottomNavBar
import com.derstakip.app.ui.screens.playlist.PlaylistImportScreen
import com.derstakip.app.ui.screens.progress.ProgressScreen
import com.derstakip.app.ui.screens.schedule.ScheduleSettingsScreen
import com.derstakip.app.ui.screens.today.TodayScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        bottomBar = {
            AppBottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Today.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Today.route) {
                TodayScreen(
                    onNavigateToPlaylist = {
                        navController.navigate(Screen.Playlist.route)
                    },
                    onNavigateToSchedule = {
                        navController.navigate(Screen.Schedule.route)
                    }
                )
            }

            composable(Screen.Schedule.route) {
                ScheduleSettingsScreen(
                    onNavigateToPlaylist = {
                        navController.navigate(Screen.Playlist.route)
                    }
                )
            }

            composable(Screen.Progress.route) {
                ProgressScreen(
                    onNavigateToPlaylist = {
                        navController.navigate(Screen.Playlist.route)
                    }
                )
            }

            composable(Screen.Playlist.route) {
                PlaylistImportScreen(
                    onNavigateToSchedule = {
                        navController.navigate(Screen.Schedule.route)
                    }
                )
            }
        }
    }
}
