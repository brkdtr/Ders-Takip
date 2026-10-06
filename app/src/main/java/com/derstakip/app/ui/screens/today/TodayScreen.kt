package com.derstakip.app.ui.screens.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.derstakip.app.ui.components.VideoItemCard
import com.derstakip.app.ui.theme.EmeraldSuccess
import com.derstakip.app.ui.theme.IndigoPrimary
import com.derstakip.app.ui.theme.Slate600
import com.derstakip.app.util.IsoDurationParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onNavigateToPlaylist: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Bugünkü Çalışma",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        uiState.activePlaylist?.let {
                            Text(
                                text = it.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600,
                                maxLines = 1
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = IndigoPrimary
                    )
                }

                uiState.todayPlan == null -> {
                    EmptyTodayState(
                        activePlaylistTitle = uiState.activePlaylist?.title,
                        onNavigateToPlaylist = onNavigateToPlaylist,
                        onNavigateToSchedule = onNavigateToSchedule
                    )
                }

                else -> {
                    val plan = uiState.todayPlan!!
                    val completedVideos = plan.completedVideos
                    val totalVideos = plan.totalVideos
                    val progressFraction = plan.completionPercentage

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Summary Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${plan.dayOfWeekName}, ${plan.date}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = IndigoPrimary
                                            )
                                            Text(
                                                text = "Günlük Hedef",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Slate600
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (plan.isAllCompleted) EmeraldSuccess.copy(alpha = 0.15f) else IndigoPrimary.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = "%${(progressFraction * 100).toInt()}",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = if (plan.isAllCompleted) EmeraldSuccess else IndigoPrimary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    LinearProgressIndicator(
                                        progress = { progressFraction },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp)),
                                        color = if (plan.isAllCompleted) EmeraldSuccess else IndigoPrimary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        strokeCap = StrokeCap.Round
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "$totalVideos videodan $completedVideos'si tamamlandı",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Slate600
                                        )
                                        Text(
                                            text = "Toplam ${IsoDurationParser.formatDuration(plan.totalDurationSeconds)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Celebratory Banner if 100% completed
                        if (plan.isAllCompleted) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = EmeraldSuccess.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Celebration,
                                            contentDescription = "Tebrikler",
                                            tint = EmeraldSuccess,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Tebrikler! 🎉",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldSuccess
                                            )
                                            Text(
                                                text = "Bugünün tüm çalışma hedeflerini tamamladınız. Harika bir ilerleme!",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Slate600
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Section Header
                        item {
                            Text(
                                text = "Atanmış Video Dersler (${plan.videos.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            )
                        }

                        // Videos list
                        items(plan.videos, key = { it.id }) { video ->
                            VideoItemCard(
                                video = video,
                                onToggleCompletion = { isCompleted ->
                                    viewModel.toggleVideoCompletion(video.id, isCompleted)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTodayState(
    activePlaylistTitle: String?,
    onNavigateToPlaylist: () -> Unit,
    onNavigateToSchedule: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = IndigoPrimary.copy(alpha = 0.1f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = IndigoPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Bugün İçin Program Bulunmuyor",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (activePlaylistTitle.isNullOrBlank()) {
                "Ders takibine başlamak için lütfen önce bir YouTube oynatma listesi ekleyin."
            } else {
                "\"$activePlaylistTitle\" için bugüne atanmış video yok veya bugün dinlenme gününüz. Akıllı program motoru ile yeni bir çalışma takvimi oluşturabilirsiniz."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Slate600,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (activePlaylistTitle.isNullOrBlank()) {
            Button(
                onClick = onNavigateToPlaylist,
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.VideoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Oynatma Listesi Ekle")
            }
        } else {
            Button(
                onClick = onNavigateToSchedule,
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Akıllı Program Oluştur")
            }
        }
    }
}
