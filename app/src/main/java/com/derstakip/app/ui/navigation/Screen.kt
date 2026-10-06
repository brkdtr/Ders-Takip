package com.derstakip.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Today : Screen("today", "Bugün", Icons.Default.CalendarToday)
    object Playlist : Screen("playlist", "Oynatma Listesi", Icons.Default.VideoLibrary)
    object Schedule : Screen("schedule", "Program", Icons.Default.DateRange)
    object Progress : Screen("progress", "İlerleme", Icons.Default.Insights)

    companion object {
        val bottomNavItems = listOf(Today, Schedule, Progress, Playlist)
    }
}
