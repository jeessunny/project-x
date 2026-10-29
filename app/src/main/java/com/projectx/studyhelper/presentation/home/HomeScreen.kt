package com.projectx.studyhelper.presentation.home

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectx.studyhelper.domain.manager.TimerStatus
import com.projectx.studyhelper.domain.model.StudyPreset
import com.projectx.studyhelper.domain.model.UpdateState
import com.projectx.studyhelper.presentation.theme.StreakFlameColor
import com.projectx.studyhelper.presentation.updater.UpdateDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSelectPreset: (StudyPreset) -> Unit,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onOpenUpdateDialog: () -> Unit,
    onDismissUpdateDialog: () -> Unit,
    onConfirmUpdateDownload: (com.projectx.studyhelper.domain.model.UpdateInfo) -> Unit,
    onTriggerInstall: (android.content.Context) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Project X",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onCheckForUpdates) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Check for updates"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Current dashboard */ },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Study") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* Navigation placeholder */ },
                    icon = { Icon(Icons.Outlined.Timer, contentDescription = "Analytics") },
                    label = { Text("Sessions") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* Navigation placeholder */ },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Optional Update Available Banner
            AnimatedVisibility(
                visible = uiState.updateState is UpdateState.Available || uiState.updateState is UpdateState.ReadyToInstall,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                UpdateAvailableBanner(
                    updateState = uiState.updateState,
                    onOpenDialog = onOpenUpdateDialog
                )
            }

            // Streak Badge & Stats Card
            StreakSummaryCard(
                currentStreak = uiState.streakRecord.currentStreak,
                longestStreak = uiState.streakRecord.longestStreak,
                freezeTokens = uiState.streakRecord.freezeTokensAvailable,
                isCompletedToday = uiState.streakRecord.isStreakActiveToday
            )

            // Preset Selector (15 min vs 30 min)
            PresetSelectorSection(
                selectedPreset = uiState.selectedPreset,
                onSelectPreset = onSelectPreset,
                isTimerActive = uiState.timerState.status == TimerStatus.Running || uiState.timerState.status == TimerStatus.Paused
            )

            // Study Timer Engine Card
            StudyTimerDisplayCard(
                timerState = uiState.timerState,
                onStart = onStartTimer,
                onPause = onPauseTimer,
                onResume = onResumeTimer,
                onStop = onStopTimer
            )

            // Daily Target Progress Section
            DailyProgressCard(
                accumulatedMinutesToday = uiState.accumulatedMinutesToday,
                targetMinutes = uiState.selectedPreset.minutes,
                progress = uiState.dailyProgress,
                isGoalReached = uiState.isDailyGoalReached
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Modal Update Dialog for progress and install actions
    if (uiState.showUpdateDialog || uiState.updateState is UpdateState.Downloading || uiState.updateState is UpdateState.ReadyToInstall) {
        UpdateDialog(
            updateState = uiState.updateState,
            onDismiss = onDismissUpdateDialog,
            onConfirmUpdate = onConfirmUpdateDownload,
            onInstallNow = { onTriggerInstall(context) }
        )
    }
}

@Composable
fun UpdateAvailableBanner(
    updateState: UpdateState,
    onOpenDialog: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (updateState is UpdateState.ReadyToInstall) "Update Ready to Install" else "Update Available",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Tap to view release details & install",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Button(
                onClick = onOpenDialog,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(if (updateState is UpdateState.ReadyToInstall) "Install" else "Update")
            }
        }
    }
}

@Composable
fun StreakSummaryCard(
    currentStreak: Int,
    longestStreak: Int,
    freezeTokens: Int,
    isCompletedToday: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(StreakFlameColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak Flame",
                            tint = StreakFlameColor,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "$currentStreak Day Streak",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isCompletedToday) "Completed for today!" else "Keep the flame burning today",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCompletedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-metrics (Longest streak & Freeze tokens)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Longest Streak",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Best: $longestStreak days",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AcUnit,
                        contentDescription = "Freeze Tokens",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Freezes: $freezeTokens available",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetSelectorSection(
    selectedPreset: StudyPreset,
    onSelectPreset: (StudyPreset) -> Unit,
    isTimerActive: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Study Goal Preset",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            StudyPreset.entries.forEachIndexed { index, preset ->
                SegmentedButton(
                    selected = selectedPreset == preset,
                    onClick = { onSelectPreset(preset) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = StudyPreset.entries.size),
                    enabled = !isTimerActive
                ) {
                    Text(text = preset.label)
                }
            }
        }
    }
}

@Composable
fun StudyTimerDisplayCard(
    timerState: com.projectx.studyhelper.domain.manager.TimerState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                // Background Track
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(200.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeWidth = 12.dp
                )

                // Active Progress
                CircularProgressIndicator(
                    progress = { timerState.progress },
                    modifier = Modifier.size(200.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 12.dp
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timerState.formattedRemainingTime,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (timerState.status) {
                            TimerStatus.Running -> "Focusing..."
                            TimerStatus.Paused -> "Paused"
                            TimerStatus.Completed -> "Goal Met!"
                            TimerStatus.Idle -> "Ready"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (timerState.status) {
                    TimerStatus.Idle, TimerStatus.Completed -> {
                        Button(
                            onClick = onStart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Study Session", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    TimerStatus.Running -> {
                        FilledTonalButton(
                            onClick = onPause,
                            modifier = Modifier.weight(1f).height(52.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pause")
                        }

                        Button(
                            onClick = onStop,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.weight(1f).height(52.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stop")
                        }
                    }

                    TimerStatus.Paused -> {
                        Button(
                            onClick = onResume,
                            modifier = Modifier.weight(1f).height(52.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Resume")
                        }

                        FilledTonalButton(
                            onClick = onStop,
                            modifier = Modifier.weight(1f).height(52.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("End")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyProgressCard(
    accumulatedMinutesToday: Int,
    targetMinutes: Int,
    progress: Float,
    isGoalReached: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
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
                Text(
                    text = "Daily Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (isGoalReached) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Goal Achieved",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Target Achieved",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(MaterialTheme.shapes.small)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$accumulatedMinutesToday / $targetMinutes minutes completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
