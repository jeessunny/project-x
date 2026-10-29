package com.projectx.studyhelper.presentation.study

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectx.studyhelper.domain.manager.TimerState
import com.projectx.studyhelper.domain.manager.TimerStatus
import com.projectx.studyhelper.domain.model.StudyPreset
import com.projectx.studyhelper.presentation.theme.HealthRuleBlue
import com.projectx.studyhelper.presentation.theme.StreakFlameColor
import com.projectx.studyhelper.presentation.updater.UpdateDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    uiState: StudyUiState,
    onSelectPreset: (StudyPreset) -> Unit,
    onOpenCustomPicker: () -> Unit,
    onCloseCustomPicker: () -> Unit,
    onApplyCustomMinutes: (Int) -> Unit,
    onStartTimer: () -> Unit,
    onPromptAbort: () -> Unit,
    onConfirmAbort: () -> Unit,
    onDismissAbort: () -> Unit,
    onResetCompleted: () -> Unit
) {
    val isTimerActive = uiState.timerState.status == TimerStatus.Running

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Streak Summary Card
        StreakSummaryCard(
            currentStreak = uiState.streakRecord.currentStreak,
            longestStreak = uiState.streakRecord.longestStreak,
            freezeTokens = uiState.streakRecord.freezeTokensAvailable,
            isCompletedToday = uiState.streakRecord.isStreakActiveToday
        )

        // Expanded Presets with 20-20-20 eye rest rule
        StudyPresetChipsSection(
            selectedPreset = uiState.selectedPreset,
            onSelectPreset = onSelectPreset,
            onOpenCustomPicker = onOpenCustomPicker,
            isTimerActive = isTimerActive
        )

        // Focus Timer Display (Strict Mode: No Pause)
        StrictTimerDisplayCard(
            timerState = uiState.timerState,
            onStart = onStartTimer,
            onStop = onPromptAbort,
            onResetCompleted = onResetCompleted
        )

        // Daily Target Progress Section
        DailyProgressCard(
            accumulatedMinutesToday = uiState.accumulatedMinutesToday,
            targetMinutes = uiState.selectedPreset.minutes,
            progress = uiState.dailyProgress,
            isGoalReached = uiState.isDailyGoalReached
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Strict Abort Confirmation Dialog
    if (uiState.isAbortDialogOpen) {
        AlertDialog(
            onDismissRequest = onDismissAbort,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Abandon Session?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Strict Focus Mode is active. Abandoning this session will reset your timer to 0:00 and this time will NOT count toward your daily streak.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmAbort,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Abandon Session")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissAbort) {
                    Text("Keep Focusing")
                }
            }
        )
    }

    // Custom Duration Picker Dialog
    if (uiState.isCustomPickerOpen) {
        CustomDurationDialog(
            initialMinutes = uiState.selectedPreset.minutes,
            onDismiss = onCloseCustomPicker,
            onConfirm = onApplyCustomMinutes
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPresetChipsSection(
    selectedPreset: StudyPreset,
    onSelectPreset: (StudyPreset) -> Unit,
    onOpenCustomPicker: () -> Unit,
    isTimerActive: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Focus Preset",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudyPreset.standardPresets.forEach { preset ->
                FilterChip(
                    selected = selectedPreset.minutes == preset.minutes && selectedPreset !is StudyPreset.Custom,
                    onClick = { onSelectPreset(preset) },
                    label = { Text(preset.label) },
                    enabled = !isTimerActive
                )
            }

            // Custom Preset Chip
            val isCustomActive = selectedPreset is StudyPreset.Custom
            FilterChip(
                selected = isCustomActive,
                onClick = onOpenCustomPicker,
                label = {
                    Text(if (isCustomActive) "Custom (${selectedPreset.minutes}m)" else "Custom...")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Custom duration",
                        modifier = Modifier.size(16.dp)
                    )
                },
                enabled = !isTimerActive
            )
        }

        // Contextual 20-20-20 Health Rule Helper Pill
        AnimatedVisibility(
            visible = selectedPreset.isTwentyMinuteRule,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = HealthRuleBlue.copy(alpha = 0.12f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveRedEye,
                        contentDescription = "20-20-20 rule",
                        tint = HealthRuleBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "20-20-20 Focus Rule: Rest your eyes by looking 20 feet away for 20 seconds after 20 minutes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun StrictTimerDisplayCard(
    timerState: TimerState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onResetCompleted: () -> Unit
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
                modifier = Modifier.size(210.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(210.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeWidth = 12.dp
                )

                CircularProgressIndicator(
                    progress = { timerState.progress },
                    modifier = Modifier.size(210.dp),
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
                            TimerStatus.Running -> "Focusing (Strict)"
                            TimerStatus.Completed -> "Goal Met!"
                            TimerStatus.Idle -> "Ready"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dynamic Motivational Prompt (Smooth Animated Quote)
            AnimatedContent(
                targetState = timerState.currentQuote,
                transitionSpec = {
                    (fadeIn() + slideInVertically { it / 2 }) togetherWith (fadeOut() + slideOutVertically { -it / 2 })
                },
                label = "MotivationalQuote"
            ) { quoteText ->
                Text(
                    text = "“$quoteText”",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Strict Accountability Controls (NO PAUSE BUTTON)
            when (timerState.status) {
                TimerStatus.Idle -> {
                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Strict Focus Session", style = MaterialTheme.typography.labelLarge)
                    }
                }

                TimerStatus.Running -> {
                    Button(
                        onClick = onStop,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stop Session (Abandon)", style = MaterialTheme.typography.labelLarge)
                    }
                }

                TimerStatus.Completed -> {
                    Button(
                        onClick = onResetCompleted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Session Complete! Return to Ready", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomDurationDialog(
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedMinutes by remember { mutableFloatStateOf(initialMinutes.toFloat().coerceIn(5f, 120f)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Focus Duration") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${selectedMinutes.toInt()} minutes",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = selectedMinutes,
                    onValueChange = { selectedMinutes = it },
                    valueRange = 5f..120f,
                    steps = 22, // 5 min increments
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("5 min", style = MaterialTheme.typography.labelSmall)
                    Text("60 min", style = MaterialTheme.typography.labelSmall)
                    Text("120 min", style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedMinutes.toInt()) }) {
                Text("Set Preset")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
                            text = if (isCompletedToday) "Completed for today! 🔥" else "Keep the flame burning today",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCompletedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
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
                        contentDescription = null,
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
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Target Met",
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
