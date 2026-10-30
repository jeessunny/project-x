package com.projectx.studyhelper.presentation.study

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import com.projectx.studyhelper.domain.manager.TimerState
import com.projectx.studyhelper.domain.manager.TimerStatus
import com.projectx.studyhelper.domain.model.StudyPreset
import com.projectx.studyhelper.presentation.theme.HealthRuleBlue
import com.projectx.studyhelper.presentation.theme.StreakFlameColor
import com.projectx.studyhelper.presentation.theme.TimerAnimationStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    uiState: StudyUiState,
    timerAnimation: TimerAnimationStyle = TimerAnimationStyle.DEFAULT,
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

        // Presets: 15m, 20m, 45m & Custom
        StudyPresetChipsSection(
            selectedPreset = uiState.selectedPreset,
            onSelectPreset = onSelectPreset,
            onOpenCustomPicker = onOpenCustomPicker,
            isTimerActive = isTimerActive
        )

        // Focus Timer Display (Strict Mode: No Pause)
        StrictTimerDisplayCard(
            timerState = uiState.timerState,
            timerAnimation = timerAnimation,
            onStart = onStartTimer,
            onStop = onPromptAbort,
            onResetCompleted = onResetCompleted
        )

        // Daily Target Progress Section (Configurable Goal: Default 45m)
        DailyProgressCard(
            accumulatedMinutesToday = uiState.accumulatedMinutesToday,
            dailyGoalMinutes = uiState.dailyGoalMinutes,
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismissAbort,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "Keep Focusing",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    OutlinedButton(
                        onClick = onConfirmAbort,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "Abandon Session",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            dismissButton = null
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(350, easing = FastOutSlowInEasing))
    ) {
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
            // Strictly 15m, 20m, 45m presets
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

        // Long Session Fatigue Reminder (Strictly 46m and longer)
        AnimatedVisibility(
            visible = selectedPreset.isLongSession,
            enter = fadeIn(animationSpec = tween(300)) + expandVertically(
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ),
            exit = fadeOut(animationSpec = tween(250)) + shrinkVertically(
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            )
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = "Fatigue reminder",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Pacing Advice: Extended Block",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Long continuous study blocks can diminish knowledge retention. Consider shorter intervals (15m–20m) and rest yourself between sessions for maximum focus.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Contextual 20-20-20 Health Rule Helper Pill (for 20m)
        AnimatedVisibility(
            visible = selectedPreset.isTwentyMinuteRule,
            enter = fadeIn(animationSpec = tween(300)) + expandVertically(
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ),
            exit = fadeOut(animationSpec = tween(250)) + shrinkVertically(
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            )
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
    timerAnimation: TimerAnimationStyle = TimerAnimationStyle.DEFAULT,
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
                // Configurable Animation Dial: Classic, Wiggly Wave, Segmented, or Pulsing Halo
                TimerProgressDial(
                    progress = timerState.progress,
                    status = timerState.status,
                    animationStyle = timerAnimation,
                    modifier = Modifier.size(210.dp)
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

            // Motivational quote appears ONLY when timer is running
            AnimatedVisibility(
                visible = timerState.status == TimerStatus.Running && timerState.currentQuote.isNotBlank(),
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(18.dp))
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
                }
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

/**
 * Renders the chosen Timer Animation Style:
 * 1. DEFAULT: Classic circular progress ring (original).
 * 2. WIGGLY: Sinusoidal oscillating wave progress running smoothly along the arc.
 * 3. SEGMENTED: 60 precision radial tick marks with major 5-minute ticks and pulsing leading edge.
 * 4. GLOW: Radiant single ring with glowing neon effect.
 */
@Composable
fun TimerProgressDial(
    progress: Float,
    status: TimerStatus,
    animationStyle: TimerAnimationStyle,
    modifier: Modifier = Modifier.size(210.dp)
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val isRunning = status == TimerStatus.Running

    when (animationStyle) {
        TimerAnimationStyle.DEFAULT -> {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = modifier,
                color = trackColor,
                strokeWidth = 12.dp
            )
            CircularProgressIndicator(
                progress = { progress },
                modifier = modifier,
                color = primaryColor,
                strokeWidth = 12.dp
            )
        }

        TimerAnimationStyle.WIGGLY -> {
            val infiniteTransition = rememberInfiniteTransition(label = "WigglyTransition")
            val wavePhase by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = (2 * PI).toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1900, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "WavePhase"
            )

            Canvas(modifier = modifier) {
                val strokePx = 10.dp.toPx()
                val radius = (size.minDimension / 2f) - (strokePx / 2f) - 6.dp.toPx()
                val centerOffset = Offset(size.width / 2f, size.height / 2f)

                // NO BACKGROUND CIRCLE DRAWN (as requested)

                val isIdle = status == TimerStatus.Idle && progress <= 0.005f
                val sweepAngle = if (isIdle) 360f else (progress * 360f).coerceIn(0f, 360f)

                if (sweepAngle > 0.5f) {
                    val steps = (sweepAngle * 2.5f).toInt().coerceAtLeast(24)
                    val waveFreq = 14f // Natural wave frequency around circle
                    val amplitude = if (isRunning) 6.dp.toPx() else 3.5.dp.toPx()
                    val path = Path()

                    for (i in 0..steps) {
                        val fraction = i.toFloat() / steps
                        val angleDeg = -90f + fraction * sweepAngle
                        val angleRad = Math.toRadians(angleDeg.toDouble())

                        val wave = sin((fraction * (sweepAngle / 360f) * waveFreq * 2 * PI) + if (isRunning) wavePhase else 0f).toFloat()
                        val r = radius + wave * amplitude
                        val x = centerOffset.x + (r * cos(angleRad)).toFloat()
                        val y = centerOffset.y + (r * sin(angleRad)).toFloat()

                        if (i == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    val waveColor = if (isIdle) primaryColor.copy(alpha = 0.35f) else primaryColor
                    drawPath(
                        path = path,
                        color = waveColor,
                        style = Stroke(
                            width = strokePx,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }

        TimerAnimationStyle.SEGMENTED -> {
            val infiniteTransition = rememberInfiniteTransition(label = "SegmentedTransition")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.45f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(750, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "TickPulse"
            )

            Canvas(modifier = modifier) {
                val centerOffset = Offset(size.width / 2f, size.height / 2f)
                val totalTicks = 60
                val outerRadius = (size.minDimension / 2f) - 4.dp.toPx()
                val tickLength = 12.dp.toPx()
                val activeTickIndex = if (isRunning) {
                    (progress * totalTicks).toInt().coerceIn(0, totalTicks - 1)
                } else {
                    0
                }
                val isCompletedSession = status == TimerStatus.Completed

                for (i in 0 until totalTicks) {
                    val fraction = i.toFloat() / totalTicks
                    val isMajor = i % 5 == 0
                    val currentTickLength = if (isMajor) tickLength + 4.dp.toPx() else tickLength
                    val innerRadius = outerRadius - currentTickLength

                    val angleDeg = -90f + fraction * 360f
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    val cosVal = cos(angleRad).toFloat()
                    val sinVal = sin(angleRad).toFloat()

                    val start = Offset(
                        x = centerOffset.x + innerRadius * cosVal,
                        y = centerOffset.y + innerRadius * sinVal
                    )
                    val end = Offset(
                        x = centerOffset.x + outerRadius * cosVal,
                        y = centerOffset.y + outerRadius * sinVal
                    )

                    val isLit = isCompletedSession || (isRunning && i < activeTickIndex)
                    val isLeading = !isCompletedSession && (i == activeTickIndex) && (isRunning || status == TimerStatus.Idle)

                    val color = when {
                        isLeading -> primaryColor.copy(alpha = pulseAlpha)
                        isLit -> primaryColor
                        else -> trackColor.copy(alpha = 0.65f)
                    }
                    val strokeWidth = when {
                        isLeading -> 4.5.dp.toPx()
                        isLit -> if (isMajor) 4.dp.toPx() else 3.dp.toPx()
                        else -> if (isMajor) 2.5.dp.toPx() else 1.8.dp.toPx()
                    }

                    drawLine(
                        color = color,
                        start = start,
                        end = end,
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        TimerAnimationStyle.GLOW -> {
            val infiniteTransition = rememberInfiniteTransition(label = "GlowTransition")
            val glowPhase by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "GlowPhase"
            )

            Canvas(modifier = modifier) {
                val centerOffset = Offset(size.width / 2f, size.height / 2f)
                val strokePx = 11.dp.toPx()
                val baseRadius = (size.minDimension / 2f) - (strokePx / 2f) - 10.dp.toPx()
                val sweepAngle = (progress * 360f).coerceIn(0f, 360f)

                // 1. Single clean background track at baseRadius
                drawCircle(
                    color = trackColor,
                    radius = baseRadius,
                    center = centerOffset,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // 2. Single radiant glowing arc (NO triple rings: exact same baseRadius)
                if (sweepAngle > 0.5f) {
                    val arcTopLeft = Offset(centerOffset.x - baseRadius, centerOffset.y - baseRadius)
                    val arcSize = androidx.compose.ui.geometry.Size(baseRadius * 2f, baseRadius * 2f)

                    // Soft radiant glow diffusion around the single stroke line
                    drawArc(
                        color = primaryColor.copy(alpha = 0.20f + 0.15f * glowPhase),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(
                            width = strokePx + 14.dp.toPx() * (0.8f + 0.3f * glowPhase),
                            cap = StrokeCap.Round
                        )
                    )

                    drawArc(
                        color = primaryColor.copy(alpha = 0.38f + 0.18f * glowPhase),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(
                            width = strokePx + 6.dp.toPx() * (0.85f + 0.25f * glowPhase),
                            cap = StrokeCap.Round
                        )
                    )

                    // Core crisp single progress arc at the exact same baseRadius
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )

                    // Glowing neon head tip
                    if (isRunning) {
                        val headAngleDeg = -90f + sweepAngle
                        val headAngleRad = Math.toRadians(headAngleDeg.toDouble())
                        val headX = centerOffset.x + (baseRadius * cos(headAngleRad)).toFloat()
                        val headY = centerOffset.y + (baseRadius * sin(headAngleRad)).toFloat()
                        val headOffset = Offset(headX, headY)

                        drawCircle(
                            color = primaryColor.copy(alpha = 0.45f * (0.6f + 0.4f * glowPhase)),
                            radius = 9.dp.toPx() + 3.dp.toPx() * glowPhase,
                            center = headOffset
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.5.dp.toPx(),
                            center = headOffset
                        )
                    }
                } else if (isRunning || status == TimerStatus.Idle) {
                    // Soft resting glow beacon at 12 o'clock
                    val headOffset = Offset(centerOffset.x, centerOffset.y - baseRadius)
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.3f * (0.5f + 0.5f * glowPhase)),
                        radius = 8.dp.toPx(),
                        center = headOffset
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = headOffset
                    )
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
    var selectedMinutes by remember { mutableIntStateOf(initialMinutes.coerceIn(5, 120)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Custom Focus Duration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$selectedMinutes minutes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Exact digit quick-adjust buttons (-5m, -1m, +1m, +5m)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = { selectedMinutes = (selectedMinutes - 5).coerceIn(5, 120) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("-5m", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    FilledTonalButton(
                        onClick = { selectedMinutes = (selectedMinutes - 1).coerceIn(5, 120) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("-1m", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    FilledTonalButton(
                        onClick = { selectedMinutes = (selectedMinutes + 1).coerceIn(5, 120) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("+1m", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    FilledTonalButton(
                        onClick = { selectedMinutes = (selectedMinutes + 5).coerceIn(5, 120) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("+5m", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Smooth 1-minute slider with accurate bounds
                Slider(
                    value = selectedMinutes.toFloat(),
                    onValueChange = { selectedMinutes = it.roundToInt().coerceIn(5, 120) },
                    valueRange = 5f..120f,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Min: 5 min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Max: 120 min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedMinutes) }) {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isCompletedToday) "Completed for today!" else "Keep the flame burning today",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCompletedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isCompletedToday) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = StreakFlameColor,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
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
    dailyGoalMinutes: Int,
    progress: Float,
    isGoalReached: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Progress Limit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                if (isGoalReached) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
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
                    text = "$accumulatedMinutesToday / $dailyGoalMinutes min completed",
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
